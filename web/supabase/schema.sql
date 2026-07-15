-- Create profiles table
CREATE TABLE public.profiles (
    id UUID REFERENCES auth.users ON DELETE CASCADE PRIMARY KEY,
    email TEXT NOT NULL,
    full_name TEXT,
    avatar_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Enable Row Level Security for profiles
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public read-only access to profiles" 
    ON public.profiles FOR SELECT USING (true);

CREATE POLICY "Allow users to update their own profile" 
    ON public.profiles FOR UPDATE USING (auth.uid() = id);

-- Create subscriptions table
CREATE TABLE public.subscriptions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users ON DELETE CASCADE NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('active', 'cancelled', 'inactive', 'past_due')),
    plan_type TEXT NOT NULL CHECK (plan_type IN ('free', 'founding_creator', 'creator_pro')),
    price_paid INTEGER DEFAULT 0 NOT NULL,
    razorpay_subscription_id TEXT,
    razorpay_customer_id TEXT,
    current_period_start TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    current_period_end TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW() + INTERVAL '1 month') NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    UNIQUE (user_id)
);

-- Enable RLS for subscriptions
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow users to view their own subscriptions" 
    ON public.subscriptions FOR SELECT USING (auth.uid() = user_id);

-- Create scripts table
CREATE TABLE public.scripts (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users ON DELETE CASCADE NOT NULL,
    topic TEXT NOT NULL,
    platform TEXT NOT NULL,
    tone TEXT NOT NULL,
    duration INTEGER NOT NULL, -- Estimated length in seconds
    hook TEXT NOT NULL,
    body TEXT NOT NULL,
    call_to_action TEXT NOT NULL,
    full_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Enable RLS for scripts
ALTER TABLE public.scripts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can fully manage their own scripts" 
    ON public.scripts FOR ALL USING (auth.uid() = user_id);

-- Create hook_variants table
CREATE TABLE public.hook_variants (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    script_id UUID REFERENCES public.scripts ON DELETE CASCADE,
    user_id UUID REFERENCES auth.users ON DELETE CASCADE NOT NULL,
    original_hook TEXT,
    hook_text TEXT NOT NULL,
    type TEXT NOT NULL, -- e.g., 'Contrarian', 'Question', 'Curiosity'
    predicted_score INTEGER NOT NULL DEFAULT 85,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Enable RLS for hook_variants
ALTER TABLE public.hook_variants ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can fully manage their own hook variants" 
    ON public.hook_variants FOR ALL USING (auth.uid() = user_id);

-- Create seo_packs table
CREATE TABLE public.seo_packs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    script_id UUID REFERENCES public.scripts ON DELETE CASCADE,
    user_id UUID REFERENCES auth.users ON DELETE CASCADE NOT NULL,
    topic TEXT NOT NULL,
    titles JSONB NOT NULL,    -- Array of strings
    captions JSONB NOT NULL,  -- Array of strings
    tags JSONB NOT NULL,      -- Array of strings
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Enable RLS for seo_packs
ALTER TABLE public.seo_packs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can fully manage their own seo packs" 
    ON public.seo_packs FOR ALL USING (auth.uid() = user_id);

-- Create usage_tracking table (5 generations limit per day for free users)
CREATE TABLE public.usage_tracking (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users ON DELETE CASCADE NOT NULL,
    generation_date DATE DEFAULT CURRENT_DATE NOT NULL,
    count INTEGER DEFAULT 0 NOT NULL,
    max_limit INTEGER DEFAULT 5 NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    UNIQUE (user_id, generation_date)
);

-- Enable RLS for usage_tracking
ALTER TABLE public.usage_tracking ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own usage limits" 
    ON public.usage_tracking FOR SELECT USING (auth.uid() = user_id);


-- Set up triggers to automatically create/sync profiles on signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, email, full_name, avatar_url)
  VALUES (
    new.id,
    new.email,
    COALESCE(new.raw_user_meta_data->>'full_name', ''),
    COALESCE(new.raw_user_meta_data->>'avatar_url', '')
  );

  -- Set up default free subscription plan on registration
  INSERT INTO public.subscriptions (user_id, status, plan_type, current_period_end)
  VALUES (new.id, 'active', 'free', NOW() + INTERVAL '100 years');

  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
