-- ====================================================================
-- FOCALSCRIBE PRODUCTION DATABASE SCHEMA (SUPABASE POSTGRES)
-- Run this in your Supabase SQL Editor to provision the required tables,
-- triggers, indexes, and Row Level Security (RLS) policies.
-- ====================================================================

-- Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ====================================================================
-- HELPER FUNCTIONS
-- ====================================================================

-- Safe urlencode helper function for the avatar seed (or fallback if empty)
CREATE OR REPLACE FUNCTION public.urlencode(text) RETURNS text AS $$
SELECT string_agg(
    CASE 
        WHEN c ~ '[a-zA-Z0-9.~_-]' THEN c
        ELSE concat('%', to_hex(ascii(c)))
    END, ''
) FROM regexp_split_to_table($1, '') c;
$$ LANGUAGE sql IMMUTABLE STRICT;

-- Automatically update updated_at timestamps
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = timezone('utc'::text, now());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================
-- 1. PROFILES TABLE
-- ====================================================================
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    full_name TEXT NOT NULL,
    avatar_url TEXT,
    plan TEXT NOT NULL DEFAULT 'free' CHECK (plan IN ('free', 'founding_creator', 'creator_pro')),
    credits_left INTEGER NOT NULL DEFAULT 5 CHECK (credits_left >= 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Idempotent Policy Creation
DROP POLICY IF EXISTS "Users can view their own profile" ON public.profiles;
CREATE POLICY "Users can view their own profile" 
    ON public.profiles FOR SELECT 
    USING (auth.uid() = id);

DROP POLICY IF EXISTS "Users can update their own profile" ON public.profiles;
CREATE POLICY "Users can update their own profile" 
    ON public.profiles FOR UPDATE 
    USING (auth.uid() = id);

-- Trigger for updated_at
DROP TRIGGER IF EXISTS set_updated_at ON public.profiles;
CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW EXECUTE PROCEDURE public.handle_updated_at();

-- ====================================================================
-- 2. SCRIPTS TABLE
-- ====================================================================
CREATE TABLE IF NOT EXISTS public.scripts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    topic TEXT NOT NULL,
    platform TEXT NOT NULL,
    tone TEXT NOT NULL,
    duration INTEGER NOT NULL,
    hook TEXT NOT NULL,
    body TEXT NOT NULL,
    call_to_action TEXT NOT NULL,
    full_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS
ALTER TABLE public.scripts ENABLE ROW LEVEL SECURITY;

-- Idempotent Policy Creation
DROP POLICY IF EXISTS "Users can manage their own scripts" ON public.scripts;
CREATE POLICY "Users can manage their own scripts" 
    ON public.scripts FOR ALL 
    USING (auth.uid() = user_id);

-- Trigger for updated_at
DROP TRIGGER IF EXISTS set_updated_at ON public.scripts;
CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON public.scripts
    FOR EACH ROW EXECUTE PROCEDURE public.handle_updated_at();

-- ====================================================================
-- 3. HOOKS TABLE
-- ====================================================================
CREATE TABLE IF NOT EXISTS public.hooks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    type TEXT NOT NULL,
    hook_text TEXT NOT NULL,
    predicted_score INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS
ALTER TABLE public.hooks ENABLE ROW LEVEL SECURITY;

-- Idempotent Policy Creation
DROP POLICY IF EXISTS "Users can manage their own hooks" ON public.hooks;
CREATE POLICY "Users can manage their own hooks" 
    ON public.hooks FOR ALL 
    USING (auth.uid() = user_id);

-- Trigger for updated_at
DROP TRIGGER IF EXISTS set_updated_at ON public.hooks;
CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON public.hooks
    FOR EACH ROW EXECUTE PROCEDURE public.handle_updated_at();

-- ====================================================================
-- 4. SEO PACKS TABLE
-- ====================================================================
CREATE TABLE IF NOT EXISTS public.seo_packs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    topic TEXT NOT NULL,
    titles JSONB NOT NULL DEFAULT '[]'::JSONB,
    captions JSONB NOT NULL DEFAULT '[]'::JSONB,
    tags JSONB NOT NULL DEFAULT '[]'::JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS
ALTER TABLE public.seo_packs ENABLE ROW LEVEL SECURITY;

-- Idempotent Policy Creation
DROP POLICY IF EXISTS "Users can manage their own SEO packs" ON public.seo_packs;
CREATE POLICY "Users can manage their own SEO packs" 
    ON public.seo_packs FOR ALL 
    USING (auth.uid() = user_id);

-- Trigger for updated_at
DROP TRIGGER IF EXISTS set_updated_at ON public.seo_packs;
CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON public.seo_packs
    FOR EACH ROW EXECUTE PROCEDURE public.handle_updated_at();

-- ====================================================================
-- 5. SUBSCRIPTIONS TABLE
-- ====================================================================
CREATE TABLE IF NOT EXISTS public.subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    plan TEXT NOT NULL,
    status TEXT NOT NULL,
    razorpay_order_id TEXT,
    razorpay_payment_id TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;

-- Idempotent Policy Creation
DROP POLICY IF EXISTS "Users can view their own subscriptions" ON public.subscriptions;
CREATE POLICY "Users can view their own subscriptions" 
    ON public.subscriptions FOR SELECT 
    USING (auth.uid() = user_id);

-- Trigger for updated_at
DROP TRIGGER IF EXISTS set_updated_at ON public.subscriptions;
CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON public.subscriptions
    FOR EACH ROW EXECUTE PROCEDURE public.handle_updated_at();

-- ====================================================================
-- 6. SESSIONS TABLE (Custom authentication auditing)
-- ====================================================================
CREATE TABLE IF NOT EXISTS public.sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    token TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS
ALTER TABLE public.sessions ENABLE ROW LEVEL SECURITY;

-- Idempotent Policy Creation
DROP POLICY IF EXISTS "Users can manage their own audit sessions" ON public.sessions;
CREATE POLICY "Users can manage their own audit sessions" 
    ON public.sessions FOR ALL 
    USING (auth.uid() = user_id);

-- Trigger for updated_at
DROP TRIGGER IF EXISTS set_updated_at ON public.sessions;
CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON public.sessions
    FOR EACH ROW EXECUTE PROCEDURE public.handle_updated_at();

-- ====================================================================
-- TRIGGERS & AUTOMATION: AUTOMATIC PROFILE CREATION ON USER SIGNUP
-- ====================================================================

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, full_name, avatar_url, plan, credits_left)
    VALUES (
        new.id,
        new.email,
        coalesce(new.raw_user_meta_data->>'full_name', 'Creative User'),
        'https://api.dicebear.com/7.x/pixel-art/svg?seed=' || public.urlencode(coalesce(new.raw_user_meta_data->>'full_name', new.email)),
        'free',
        5
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Drop trigger if exists and recreate
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

-- ====================================================================
-- INDEXES FOR MAXIMUM QUERY PERFORMANCE
-- ====================================================================
CREATE INDEX IF NOT EXISTS idx_scripts_user_id ON public.scripts(user_id);
CREATE INDEX IF NOT EXISTS idx_hooks_user_id ON public.hooks(user_id);
CREATE INDEX IF NOT EXISTS idx_seo_packs_user_id ON public.seo_packs(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON public.subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON public.sessions(user_id);
