/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { createClient, SupabaseClient } from "@supabase/supabase-js";
import dotenv from "dotenv";

dotenv.config();

const supabaseUrl = process.env.SUPABASE_URL || "";
const supabaseAnonKey = process.env.SUPABASE_ANON_KEY || "";
const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY || "";

let isConfigured = false;
let supabaseAdminClient: SupabaseClient | null = null;

// Validate configuration
if (
  supabaseUrl && 
  supabaseUrl.trim() !== "" && 
  supabaseUrl !== "https://your-project.supabase.co" &&
  supabaseAnonKey && 
  supabaseAnonKey.trim() !== "" &&
  supabaseAnonKey !== "your-anon-key"
) {
  try {
    // Initialize Admin/Service Client (using Service Role Key for elevated operations, fallbacks to Anon Key)
    const secretKey = supabaseServiceKey && supabaseServiceKey !== "your-service-role-key" ? supabaseServiceKey : supabaseAnonKey;
    supabaseAdminClient = createClient(supabaseUrl, secretKey, {
      auth: {
        persistSession: false,
        autoRefreshToken: false
      }
    });
    isConfigured = true;
    console.log("Supabase clients initialized successfully.");
  } catch (err) {
    console.error("Critical: Failed to initialize Supabase client SDK:", err);
  }
} else {
  console.warn("⚠️ Supabase credentials are not configured or are placeholders. The server will run in local simulation fallback mode.");
}

export function isSupabaseActive(): boolean {
  return isConfigured;
}

// Get the central service/admin client
export function getSupabaseAdmin(): SupabaseClient {
  if (!isConfigured || !supabaseAdminClient) {
    throw new Error("Supabase is not configured. Define SUPABASE_URL and keys in your environment settings.");
  }
  return supabaseAdminClient;
}

// Get a scoped client using the request's JWT token to enforce Row Level Security (RLS) policies
export function getSupabaseUserClient(authToken: string): SupabaseClient {
  if (!isConfigured) {
    throw new Error("Supabase is not configured.");
  }
  return createClient(supabaseUrl, supabaseAnonKey, {
    auth: {
      persistSession: false,
      autoRefreshToken: false
    },
    global: {
      headers: {
        Authorization: `Bearer ${authToken}`
      }
    }
  });
}

// ====================================================================
// SANDBOX FALLBACK STORAGE (Prevents application from crashing if credentials aren't set)
// ====================================================================
interface MemoryProfile {
  id: string;
  email: string;
  full_name: string;
  avatar_url: string;
  plan: "free" | "founding_creator" | "creator_pro";
  credits_left: number;
  created_at: string;
}

const localProfiles: MemoryProfile[] = [];
const localScripts: any[] = [];
const localHooks: any[] = [];
const localSeoPacks: any[] = [];
const localSessions: { [token: string]: { userId: string; expiresAt: number } } = {};

// Safe simulated auth generation helper
const generateSimulatedToken = () => {
  return "sim_token_" + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
};

// ====================================================================
// CORE DATABASE AND AUTH SERVICE LAYERS
// ====================================================================

// --- Authentication (SignUp) ---
export async function dbSupabaseSignUp(email: string, passwordPlain: string, fullName: string): Promise<{ success: boolean; token?: string; user?: any; error?: string }> {
  const normEmail = email.toLowerCase().trim();
  
  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      
      // Sign up via Supabase Auth
      const { data: authData, error: authError } = await supabase.auth.signUp({
        email: normEmail,
        password: passwordPlain,
        options: {
          data: {
            full_name: fullName.trim()
          }
        }
      });

      if (authError || !authData.user) {
        return { success: false, error: authError?.message || "Auth sign up failed" };
      }

      // Check if profile exists (in case Postgres trigger is delayed or not active)
      const { data: profileCheck } = await supabase
        .from("profiles")
        .select("*")
        .eq("id", authData.user.id)
        .single();

      let finalProfile = profileCheck;

      if (!profileCheck) {
        // Create profile manually for double safety
        const { data: newProfile, error: profileError } = await supabase
          .from("profiles")
          .insert({
            id: authData.user.id,
            email: normEmail,
            full_name: fullName.trim(),
            avatar_url: `https://api.dicebear.com/7.x/pixel-art/svg?seed=${encodeURIComponent(fullName.trim())}`,
            plan: "free",
            credits_left: 5
          })
          .select("*")
          .single();

        if (profileError) {
          console.error("Warning: Failed to insert profile manually:", profileError);
        } else {
          finalProfile = newProfile;
        }
      }

      const safeUser = {
        id: authData.user.id,
        email: normEmail,
        fullName: finalProfile?.full_name || fullName.trim(),
        avatarUrl: finalProfile?.avatar_url || `https://api.dicebear.com/7.x/pixel-art/svg?seed=${encodeURIComponent(fullName.trim())}`,
        plan: finalProfile?.plan || "free",
        creditsLeft: finalProfile?.credits_left !== undefined ? finalProfile.credits_left : 5,
        created_at: authData.user.created_at
      };

      const sessionToken = authData.session?.access_token || generateSimulatedToken();

      // Log session in custom audit database
      await supabase.from("sessions").insert({
        user_id: authData.user.id,
        token: sessionToken,
        expires_at: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString()
      });

      return { success: true, token: sessionToken, user: safeUser };

    } catch (err: any) {
      console.error("Supabase SignUp error:", err);
      return { success: false, error: err.message || "Internal registration error" };
    }
  }

  // --- Simulation Fallback ---
  const existing = localProfiles.find(p => p.email === normEmail);
  if (existing) {
    return { success: false, error: "Email already registered." };
  }

  const userId = `sim_usr_${Math.random().toString(36).substring(2, 10)}`;
  const newProfile: MemoryProfile = {
    id: userId,
    email: normEmail,
    full_name: fullName.trim(),
    avatar_url: `https://api.dicebear.com/7.x/pixel-art/svg?seed=${encodeURIComponent(fullName.trim())}`,
    plan: "free",
    credits_left: 5,
    created_at: new Date().toISOString()
  };
  
  localProfiles.push(newProfile);
  const token = generateSimulatedToken();
  localSessions[token] = { userId, expiresAt: Date.now() + 7 * 24 * 60 * 60 * 1000 };

  return {
    success: true,
    token,
    user: {
      id: newProfile.id,
      email: newProfile.email,
      fullName: newProfile.full_name,
      avatarUrl: newProfile.avatar_url,
      plan: newProfile.plan,
      creditsLeft: newProfile.credits_left,
      created_at: newProfile.created_at
    }
  };
}

// --- Authentication (LogIn) ---
export async function dbSupabaseLogIn(email: string, passwordPlain: string): Promise<{ success: boolean; token?: string; user?: any; error?: string }> {
  const normEmail = email.toLowerCase().trim();

  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      
      // Log in via Supabase Auth
      const { data: authData, error: authError } = await supabase.auth.signInWithPassword({
        email: normEmail,
        password: passwordPlain
      });

      if (authError || !authData.user) {
        return { success: false, error: authError?.message || "Invalid credentials" };
      }

      // Fetch Profile
      const { data: profile, error: profileError } = await supabase
        .from("profiles")
        .select("*")
        .eq("id", authData.user.id)
        .single();

      if (profileError && profileError.code !== "PGRST116") {
        console.error("Failed to retrieve profile record during login:", profileError);
      }

      const safeUser = {
        id: authData.user.id,
        email: normEmail,
        fullName: profile?.full_name || authData.user.user_metadata?.full_name || "Creative User",
        avatarUrl: profile?.avatar_url || `https://api.dicebear.com/7.x/pixel-art/svg?seed=${encodeURIComponent(authData.user.id)}`,
        plan: profile?.plan || "free",
        creditsLeft: profile?.credits_left !== undefined ? profile.credits_left : 5,
        created_at: authData.user.created_at
      };

      const sessionToken = authData.session?.access_token || generateSimulatedToken();

      // Log session in custom audit database
      await supabase.from("sessions").insert({
        user_id: authData.user.id,
        token: sessionToken,
        expires_at: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString()
      });

      return { success: true, token: sessionToken, user: safeUser };

    } catch (err: any) {
      console.error("Supabase LogIn error:", err);
      return { success: false, error: err.message || "Internal server login failure" };
    }
  }

  // --- Simulation Fallback ---
  const profile = localProfiles.find(p => p.email === normEmail);
  if (!profile) {
    return { success: false, error: "Invalid email or password." };
  }

  const token = generateSimulatedToken();
  localSessions[token] = { userId: profile.id, expiresAt: Date.now() + 7 * 24 * 60 * 60 * 1000 };

  return {
    success: true,
    token,
    user: {
      id: profile.id,
      email: profile.email,
      fullName: profile.full_name,
      avatarUrl: profile.avatar_url,
      plan: profile.plan,
      creditsLeft: profile.credits_left,
      created_at: profile.created_at
    }
  };
}

// --- Session Verification / Verification of user ---
export async function dbSupabaseGetMe(token: string): Promise<any | null> {
  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      
      // Fetch authenticated user by token
      const { data: { user }, error: authError } = await supabase.auth.getUser(token);
      if (authError || !user) {
        return null;
      }

      // Fetch profile
      const { data: profile } = await supabase
        .from("profiles")
        .select("*")
        .eq("id", user.id)
        .single();

      return {
        id: user.id,
        email: user.email!,
        fullName: profile?.full_name || user.user_metadata?.full_name || "Creative User",
        avatarUrl: profile?.avatar_url || `https://api.dicebear.com/7.x/pixel-art/svg?seed=${encodeURIComponent(user.id)}`,
        plan: profile?.plan || "free",
        creditsLeft: profile?.credits_left !== undefined ? profile.credits_left : 5,
        created_at: user.created_at
      };
    } catch (err) {
      console.error("Supabase verify session failed:", err);
      return null;
    }
  }

  // --- Simulation Fallback ---
  const session = localSessions[token];
  if (!session || Date.now() > session.expiresAt) {
    if (session) delete localSessions[token];
    return null;
  }
  const profile = localProfiles.find(p => p.id === session.userId);
  if (!profile) return null;

  return {
    id: profile.id,
    email: profile.email,
    fullName: profile.full_name,
    avatarUrl: profile.avatar_url,
    plan: profile.plan,
    creditsLeft: profile.credits_left,
    created_at: profile.created_at
  };
}

// --- SignOut / Logout ---
export async function dbSupabaseLogOut(token: string): Promise<void> {
  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      await supabase.auth.admin.signOut(token);
      await supabase.from("sessions").delete().eq("token", token);
    } catch (err) {
      console.error("Supabase LogOut error:", err);
    }
    return;
  }

  if (localSessions[token]) {
    delete localSessions[token];
  }
}

// --- Profile Update ---
export async function dbSupabaseUpdateProfile(userId: string, fullName: string, email: string, avatarSeed: string): Promise<any | null> {
  const normEmail = email.toLowerCase().trim();

  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      
      // Update custom profile table
      const avatarUrl = avatarSeed ? `https://api.dicebear.com/7.x/pixel-art/svg?seed=${encodeURIComponent(avatarSeed.trim())}` : undefined;
      
      const updatePayload: any = {
        full_name: fullName.trim(),
        email: normEmail
      };
      if (avatarUrl) {
        updatePayload.avatar_url = avatarUrl;
      }

      const { data: updatedProfile, error } = await supabase
        .from("profiles")
        .update(updatePayload)
        .eq("id", userId)
        .select("*")
        .single();

      if (error) {
        console.error("Error updating profile inside DB:", error);
        return null;
      }

      // Also update auth user metadata
      await supabase.auth.admin.updateUserById(userId, {
        email: normEmail,
        user_metadata: { full_name: fullName.trim() }
      });

      return {
        id: updatedProfile.id,
        email: updatedProfile.email,
        fullName: updatedProfile.full_name,
        avatarUrl: updatedProfile.avatar_url,
        plan: updatedProfile.plan,
        creditsLeft: updatedProfile.credits_left,
        created_at: updatedProfile.created_at
      };

    } catch (err) {
      console.error("Profile db update error:", err);
      return null;
    }
  }

  // --- Simulation Fallback ---
  const profile = localProfiles.find(p => p.id === userId);
  if (!profile) return null;

  profile.full_name = fullName.trim();
  profile.email = normEmail;
  if (avatarSeed) {
    profile.avatar_url = `https://api.dicebear.com/7.x/pixel-art/svg?seed=${encodeURIComponent(avatarSeed.trim())}`;
  }

  return {
    id: profile.id,
    email: profile.email,
    fullName: profile.full_name,
    avatarUrl: profile.avatar_url,
    plan: profile.plan,
    creditsLeft: profile.credits_left,
    created_at: profile.created_at
  };
}

// --- Check and Decrement Credits (Quota Guard) ---
export async function dbSupabaseCheckAndDecrementCredits(userId: string): Promise<{ success: boolean; creditsLeft: number; plan: string }> {
  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      
      // Fetch current profile
      const { data: profile, error } = await supabase
        .from("profiles")
        .select("plan, credits_left")
        .eq("id", userId)
        .single();

      if (error || !profile) {
        return { success: false, creditsLeft: 0, plan: "free" };
      }

      if (profile.plan !== "free") {
        return { success: true, creditsLeft: profile.credits_left, plan: profile.plan };
      }

      if (profile.credits_left <= 0) {
        return { success: false, creditsLeft: 0, plan: "free" };
      }

      // Decrement credits
      const { data: updatedProfile, error: updateError } = await supabase
        .from("profiles")
        .update({ credits_left: profile.credits_left - 1 })
        .eq("id", userId)
        .select("credits_left")
        .single();

      if (updateError || !updatedProfile) {
        return { success: false, creditsLeft: 0, plan: "free" };
      }

      return { success: true, creditsLeft: updatedProfile.credits_left, plan: "free" };

    } catch (err) {
      console.error("Supabase credit checking and updates failed:", err);
      return { success: false, creditsLeft: 0, plan: "free" };
    }
  }

  // --- Simulation Fallback ---
  const profile = localProfiles.find(p => p.id === userId);
  if (!profile) {
    return { success: false, creditsLeft: 0, plan: "free" };
  }

  if (profile.plan !== "free") {
    return { success: true, creditsLeft: profile.credits_left, plan: profile.plan };
  }

  if (profile.credits_left <= 0) {
    return { success: false, creditsLeft: 0, plan: "free" };
  }

  profile.credits_left -= 1;
  return { success: true, creditsLeft: profile.credits_left, plan: "free" };
}

// --- Upgrade User Plan ---
export async function dbSupabaseUpgradePlan(userId: string, plan: "founding_creator" | "creator_pro", orderId: string, paymentId: string): Promise<any | null> {
  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      
      // Insert subscription record
      await supabase.from("subscriptions").insert({
        user_id: userId,
        plan,
        status: "active",
        razorpay_order_id: orderId,
        razorpay_payment_id: paymentId
      });

      // Upgrade profile
      const { data: profile, error } = await supabase
        .from("profiles")
        .update({ plan })
        .eq("id", userId)
        .select("*")
        .single();

      if (error || !profile) {
        return null;
      }

      return {
        id: profile.id,
        email: profile.email,
        fullName: profile.full_name,
        avatarUrl: profile.avatar_url,
        plan: profile.plan,
        creditsLeft: profile.credits_left,
        created_at: profile.created_at
      };

    } catch (err) {
      console.error("Supabase plan upgrade failure:", err);
      return null;
    }
  }

  // --- Simulation Fallback ---
  const profile = localProfiles.find(p => p.id === userId);
  if (!profile) return null;

  profile.plan = plan;
  return {
    id: profile.id,
    email: profile.email,
    fullName: profile.full_name,
    avatarUrl: profile.avatar_url,
    plan: profile.plan,
    creditsLeft: profile.credits_left,
    created_at: profile.created_at
  };
}

// --- Scripts Operations ---
export async function dbSupabaseGetScripts(userId: string): Promise<any[]> {
  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      const { data, error } = await supabase
        .from("scripts")
        .select("*")
        .eq("user_id", userId)
        .order("created_at", { ascending: false });

      if (error) return [];
      
      return (data || []).map(s => ({
        id: s.id,
        userId: s.user_id,
        topic: s.topic,
        platform: s.platform,
        tone: s.tone,
        duration: s.duration,
        hook: s.hook,
        body: s.body,
        call_to_action: s.call_to_action,
        full_text: s.full_text,
        created_at: s.created_at
      }));
    } catch (err) {
      console.error("Supabase fetch scripts error:", err);
      return [];
    }
  }

  // --- Simulation Fallback ---
  return localScripts
    .filter(s => s.userId === userId)
    .sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime());
}

export async function dbSupabaseSaveScript(userId: string, data: any): Promise<any> {
  const newScript = {
    user_id: userId,
    topic: data.topic,
    platform: data.platform,
    tone: data.tone,
    duration: data.duration,
    hook: data.hook,
    body: data.body,
    call_to_action: data.call_to_action,
    full_text: data.full_text
  };

  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      const { data: saved, error } = await supabase
        .from("scripts")
        .insert(newScript)
        .select("*")
        .single();

      if (error) {
        console.error("Supabase save script error:", error);
        throw error;
      }

      return {
        id: saved.id,
        userId: saved.user_id,
        topic: saved.topic,
        platform: saved.platform,
        tone: saved.tone,
        duration: saved.duration,
        hook: saved.hook,
        body: saved.body,
        call_to_action: saved.call_to_action,
        full_text: saved.full_text,
        created_at: saved.created_at
      };
    } catch (err) {
      console.error("Supabase scripts saving exception:", err);
    }
  }

  // --- Simulation Fallback ---
  const item = {
    id: `scr_sim_${Math.random().toString(36).substring(2, 10)}`,
    userId,
    topic: data.topic,
    platform: data.platform,
    tone: data.tone,
    duration: data.duration,
    hook: data.hook,
    body: data.body,
    call_to_action: data.call_to_action,
    full_text: data.full_text,
    created_at: new Date().toISOString()
  };
  localScripts.unshift(item);
  return item;
}

export async function dbSupabaseDeleteScript(userId: string, scriptId: string): Promise<boolean> {
  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      const { error } = await supabase
        .from("scripts")
        .delete()
        .eq("id", scriptId)
        .eq("user_id", userId);

      return !error;
    } catch (err) {
      console.error("Supabase delete script failure:", err);
      return false;
    }
  }

  // --- Simulation Fallback ---
  const initialLength = localScripts.length;
  for (let i = 0; i < localScripts.length; i++) {
    if (localScripts[i].id === scriptId && localScripts[i].userId === userId) {
      localScripts.splice(i, 1);
      break;
    }
  }
  return localScripts.length !== initialLength;
}

// --- Hooks Operations ---
export async function dbSupabaseGetHooks(userId: string): Promise<any[]> {
  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      const { data, error } = await supabase
        .from("hooks")
        .select("*")
        .eq("user_id", userId)
        .order("created_at", { ascending: false });

      if (error) return [];

      return (data || []).map(h => ({
        id: h.id,
        userId: h.user_id,
        type: h.type,
        hook_text: h.hook_text,
        predicted_score: h.predicted_score,
        created_at: h.created_at
      }));
    } catch (err) {
      console.error("Supabase fetch hooks error:", err);
      return [];
    }
  }

  // --- Simulation Fallback ---
  return localHooks
    .filter(h => h.userId === userId)
    .sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime());
}

export async function dbSupabaseSaveHooks(userId: string, hooksList: any[]): Promise<any[]> {
  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      const payload = hooksList.map(h => ({
        user_id: userId,
        type: h.type,
        hook_text: h.hook_text,
        predicted_score: h.predicted_score
      }));

      const { data, error } = await supabase
        .from("hooks")
        .insert(payload)
        .select("*");

      if (error) {
        console.error("Supabase save hooks error:", error);
        throw error;
      }

      return (data || []).map(h => ({
        id: h.id,
        userId: h.user_id,
        type: h.type,
        hook_text: h.hook_text,
        predicted_score: h.predicted_score,
        created_at: h.created_at
      }));
    } catch (err) {
      console.error("Supabase hooks save exception:", err);
    }
  }

  // --- Simulation Fallback ---
  const saved: any[] = [];
  hooksList.forEach(h => {
    const item = {
      id: `hok_sim_${Math.random().toString(36).substring(2, 10)}`,
      userId,
      type: h.type,
      hook_text: h.hook_text,
      predicted_score: h.predicted_score,
      created_at: new Date().toISOString()
    };
    localHooks.unshift(item);
    saved.push(item);
  });
  return saved;
}

// --- SEO Packs Operations ---
export async function dbSupabaseGetSeoPacks(userId: string): Promise<any[]> {
  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      const { data, error } = await supabase
        .from("seo_packs")
        .select("*")
        .eq("user_id", userId)
        .order("created_at", { ascending: false });

      if (error) return [];

      return (data || []).map(s => ({
        id: s.id,
        userId: s.user_id,
        topic: s.topic,
        titles: typeof s.titles === "string" ? JSON.parse(s.titles) : s.titles,
        captions: typeof s.captions === "string" ? JSON.parse(s.captions) : s.captions,
        tags: typeof s.tags === "string" ? JSON.parse(s.tags) : s.tags,
        created_at: s.created_at
      }));
    } catch (err) {
      console.error("Supabase fetch seo error:", err);
      return [];
    }
  }

  // --- Simulation Fallback ---
  return localSeoPacks
    .filter(s => s.userId === userId)
    .sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime());
}

export async function dbSupabaseSaveSeoPack(userId: string, data: any): Promise<any> {
  const payload = {
    user_id: userId,
    topic: data.topic,
    titles: data.titles,
    captions: data.captions,
    tags: data.tags
  };

  if (isConfigured) {
    try {
      const supabase = getSupabaseAdmin();
      const { data: saved, error } = await supabase
        .from("seo_packs")
        .insert(payload)
        .select("*")
        .single();

      if (error) {
        console.error("Supabase save SEO Pack error:", error);
        throw error;
      }

      return {
        id: saved.id,
        userId: saved.user_id,
        topic: saved.topic,
        titles: typeof saved.titles === "string" ? JSON.parse(saved.titles) : saved.titles,
        captions: typeof saved.captions === "string" ? JSON.parse(saved.captions) : saved.captions,
        tags: typeof saved.tags === "string" ? JSON.parse(saved.tags) : saved.tags,
        created_at: saved.created_at
      };
    } catch (err) {
      console.error("Supabase SEO saving exception:", err);
    }
  }

  // --- Simulation Fallback ---
  const item = {
    id: `seo_sim_${Math.random().toString(36).substring(2, 10)}`,
    userId,
    topic: data.topic,
    titles: data.titles,
    captions: data.captions,
    tags: data.tags,
    created_at: new Date().toISOString()
  };
  localSeoPacks.unshift(item);
  return item;
}
