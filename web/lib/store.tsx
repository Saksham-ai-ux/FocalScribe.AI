"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { supabase, isSupabaseConfigured } from "./supabase";
import { analytics } from "./analytics";

export type PlanType = "free" | "founding_creator" | "creator_pro";

export interface UserProfile {
  id: string;
  email: string;
  full_name: string;
  avatar_url: string;
  plan: PlanType;
  generationsUsedToday: number;
  generationsLimit: number;
}

export interface Script {
  id: string;
  topic: string;
  platform: string;
  tone: string;
  duration: number;
  hook: string;
  body: string;
  call_to_action: string;
  full_text: string;
  created_at: string;
}

export interface HookVariant {
  id: string;
  type: string;
  hook_text: string;
  predicted_score: number;
  created_at: string;
}

export interface SEOPack {
  id: string;
  topic: string;
  titles: string[];
  captions: string[];
  tags: string[];
  created_at: string;
}

interface AppContextType {
  user: UserProfile | null;
  loading: boolean;
  scripts: Script[];
  hooks: HookVariant[];
  seoPacks: SEOPack[];
  currentScript: Script | null;
  currentHooks: HookVariant[];
  currentSeoPack: SEOPack | null;
  
  // Actions
  login: (email: string) => Promise<boolean>;
  signup: (email: string, fullName: string) => Promise<boolean>;
  logout: () => void;
  upgradeUserPlan: (plan: PlanType) => void;
  incrementUsage: () => boolean;
  
  // Creators
  saveScript: (script: Omit<Script, "id" | "created_at">) => void;
  saveHooks: (hooks: Omit<HookVariant, "id" | "created_at">[]) => void;
  saveSeoPack: (seo: Omit<SEOPack, "id" | "created_at">) => void;
  setCurrentScript: (script: Script | null) => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [scripts, setScripts] = useState<Script[]>([]);
  const [hooks, setHooks] = useState<HookVariant[]>([]);
  const [seoPacks, setSeoPacks] = useState<SEOPack[]>([]);
  const [currentScript, setCurrentScriptState] = useState<Script | null>(null);
  const [currentHooks, setCurrentHooks] = useState<HookVariant[]>([]);
  const [currentSeoPack, setCurrentSeoPack] = useState<SEOPack | null>(null);

  // Initialize and check local states
  useEffect(() => {
    const initSession = async () => {
      try {
        if (isSupabaseConfigured()) {
          const { data: { session } } = await supabase.auth.getSession();
          if (session) {
            // Retrieve profile from Supabase
            const { data: profile } = await supabase
              .from("profiles")
              .select("*")
              .eq("id", session.user.id)
              .single();

            const { data: sub } = await supabase
              .from("subscriptions")
              .select("plan_type")
              .eq("user_id", session.user.id)
              .single();

            const mockUser: UserProfile = {
              id: session.user.id,
              email: session.user.email || "",
              full_name: profile?.full_name || "Creator",
              avatar_url: profile?.avatar_url || `https://api.dicebear.com/7.x/pixel-art/svg?seed=${session.user.id}`,
              plan: (sub?.plan_type as PlanType) || "free",
              generationsUsedToday: 0,
              generationsLimit: sub?.plan_type === "free" ? 5 : 9999
            };
            setUser(mockUser);
            analytics.logAppOpen(session.user.id);
            setLoading(false);
            return;
          }
        }

        // If not configured or not authenticated, check localStorage for simulated user
        const storedUser = localStorage.getItem("focalscribe_sim_user");
        if (storedUser) {
          const parsed = JSON.parse(storedUser);
          setUser(parsed);
        } else {
          // Default landing state with anonymous access or require login
          setUser(null);
        }

        // Load content archives
        const storedScripts = localStorage.getItem("focalscribe_scripts");
        if (storedScripts) setScripts(JSON.parse(storedScripts));

        const storedHooks = localStorage.getItem("focalscribe_hooks");
        if (storedHooks) setHooks(JSON.parse(storedHooks));

        const storedSeo = localStorage.getItem("focalscribe_seo");
        if (storedSeo) setSeoPacks(JSON.parse(storedSeo));

      } catch (err) {
        console.error("Session init failed:", err);
      } finally {
        setLoading(false);
      }
    };

    initSession();
  }, []);

  const login = async (email: string): Promise<boolean> => {
    setLoading(true);
    try {
      if (isSupabaseConfigured()) {
        const { error } = await supabase.auth.signInWithOtp({ email });
        if (error) throw error;
        // In real OTP, the user confirms in email, but we complete simulation
        return true;
      }

      // Local mockup login flow
      const mockUser: UserProfile = {
        id: `usr_${Math.random().toString(36).substring(2, 9)}`,
        email: email,
        full_name: email.split("@")[0],
        avatar_url: `https://api.dicebear.com/7.x/pixel-art/svg?seed=${email}`,
        plan: "free",
        generationsUsedToday: 0,
        generationsLimit: 5
      };
      setUser(mockUser);
      localStorage.setItem("focalscribe_sim_user", JSON.stringify(mockUser));
      analytics.logSignUp("email_link");
      return true;
    } catch (err) {
      console.error("Login failed:", err);
      return false;
    } finally {
      setLoading(false);
    }
  };

  const signup = async (email: string, fullName: string): Promise<boolean> => {
    setLoading(true);
    try {
      // Create local mockup user
      const mockUser: UserProfile = {
        id: `usr_${Math.random().toString(36).substring(2, 9)}`,
        email: email,
        full_name: fullName,
        avatar_url: `https://api.dicebear.com/7.x/pixel-art/svg?seed=${fullName}`,
        plan: "free",
        generationsUsedToday: 0,
        generationsLimit: 5
      };
      setUser(mockUser);
      localStorage.setItem("focalscribe_sim_user", JSON.stringify(mockUser));
      analytics.logSignUp("email_signup");
      return true;
    } catch (err) {
      console.error("Signup failed:", err);
      return false;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("focalscribe_sim_user");
    if (isSupabaseConfigured()) {
      supabase.auth.signOut();
    }
  };

  const upgradeUserPlan = (plan: PlanType) => {
    if (!user) return;
    const updated = {
      ...user,
      plan,
      generationsLimit: plan === "free" ? 5 : 9999
    };
    setUser(updated);
    localStorage.setItem("focalscribe_sim_user", JSON.stringify(updated));
  };

  const incrementUsage = (): boolean => {
    if (!user) return false;
    if (user.plan === "free" && user.generationsUsedToday >= user.generationsLimit) {
      return false;
    }
    const updated = {
      ...user,
      generationsUsedToday: user.generationsUsedToday + 1
    };
    setUser(updated);
    localStorage.setItem("focalscribe_sim_user", JSON.stringify(updated));
    return true;
  };

  const saveScript = (scriptData: Omit<Script, "id" | "created_at">) => {
    const newScript: Script = {
      ...scriptData,
      id: `scr_${Math.random().toString(36).substring(2, 9)}`,
      created_at: new Date().toISOString()
    };
    const updated = [newScript, ...scripts];
    setScripts(updated);
    localStorage.setItem("focalscribe_scripts", JSON.stringify(updated));
    setCurrentScriptState(newScript);
  };

  const saveHooks = (hookList: Omit<HookVariant, "id" | "created_at">[]) => {
    const newHooks = hookList.map(h => ({
      ...h,
      id: `hok_${Math.random().toString(36).substring(2, 9)}`,
      created_at: new Date().toISOString()
    }));
    setHooks(prev => [...newHooks, ...prev]);
    setCurrentHooks(newHooks);
  };

  const saveSeoPack = (seoData: Omit<SEOPack, "id" | "created_at">) => {
    const newSeo: SEOPack = {
      ...seoData,
      id: `seo_${Math.random().toString(36).substring(2, 9)}`,
      created_at: new Date().toISOString()
    };
    setSeoPacks(prev => [newSeo, ...prev]);
    setCurrentSeoPack(newSeo);
  };

  const setCurrentScript = (script: Script | null) => {
    setCurrentScriptState(script);
  };

  return (
    <AppContext.Provider
      value={{
        user,
        loading,
        scripts,
        hooks,
        seoPacks,
        currentScript,
        currentHooks,
        currentSeoPack,
        login,
        signup,
        logout,
        upgradeUserPlan,
        incrementUsage,
        saveScript,
        saveHooks,
        saveSeoPack,
        setCurrentScript
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (context === undefined) {
    throw new Error("useApp must be used within an AppProvider");
  }
  return context;
};
