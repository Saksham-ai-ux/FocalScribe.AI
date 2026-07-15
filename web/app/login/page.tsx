"use client";

import React, { useState, useEffect, Suspense } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Sparkles, Mail, ArrowRight } from "lucide-react";
import { AppProvider, useApp } from "@/lib/store";
import { isSupabaseConfigured } from "@/lib/supabase";

export default function LoginPage() {
  return (
    <AppProvider>
      <Suspense fallback={<div className="bg-background min-h-screen flex items-center justify-center text-primary font-mono animate-pulse">Loading Auth...</div>}>
        <LoginContent />
      </Suspense>
    </AppProvider>
  );
}

function LoginContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { login, user } = useApp();
  
  const [email, setEmail] = useState("");
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [supabaseActive, setSupabaseActive] = useState(false);

  const targetPlan = searchParams.get("plan") || "free";

  useEffect(() => {
    setSupabaseActive(isSupabaseConfigured());
    if (user) {
      router.push("/dashboard");
    }
  }, [user, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) {
      setErrorMessage("Please enter a valid email address.");
      return;
    }
    setLoading(true);
    setErrorMessage("");

    try {
      const success = await login(email);
      if (success) {
        setIsSubmitted(true);
        if (!isSupabaseConfigured()) {
          // If Supabase is not active, immediately log in locally and redirect
          setTimeout(() => {
            router.push(`/dashboard?plan=${targetPlan}`);
          }, 1500);
        }
      } else {
        setErrorMessage("Login failed. Please check your credentials.");
      }
    } catch (err) {
      setErrorMessage("An unexpected error occurred. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-background min-h-screen flex items-center justify-center px-6 relative selection:bg-primary selection:text-black">
      {/* Background glow */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-primary/10 rounded-full blur-[100px] pointer-events-none" />

      <div className="w-full max-w-md p-8 rounded-2xl border border-border bg-card glow-cyan flex flex-col gap-6">
        <div className="flex flex-col items-center text-center gap-2">
          <Link href="/" className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center shadow-[0_0_15px_rgba(0,240,255,0.4)] mb-2 hover:scale-105 transition-transform">
            <Sparkles className="w-6 h-6 text-black" />
          </Link>
          <h2 className="text-2xl font-black tracking-tight text-white">Welcome back</h2>
          <p className="text-xs text-text-secondary">Log in to manage your short-form video scripts</p>
        </div>

        {isSubmitted ? (
          <div className="p-6 rounded-xl bg-primary/5 border border-primary/20 text-center flex flex-col items-center gap-3 animate-fade-in-up">
            <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold animate-bounce">✓</div>
            <p className="text-sm font-bold text-white">
              {supabaseActive ? "Check Your Inbox!" : "Authenticated successfully!"}
            </p>
            <p className="text-xs text-text-secondary px-2 leading-relaxed">
              {supabaseActive 
                ? "We have sent a magic login link to your email. Click the link in the email to sign in instantly." 
                : "Redirecting you to your creative dashboard..."}
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="email" className="text-xs font-bold text-text-secondary uppercase tracking-wider">Email Address</label>
              <div className="relative">
                <Mail className="w-5 h-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-text-muted" />
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@company.com"
                  className="w-full pl-11 pr-4 py-3 bg-neutral-950 border border-border rounded-xl text-sm text-white focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all placeholder:text-text-muted"
                  required
                />
              </div>
            </div>

            {errorMessage && (
              <p className="text-xs text-red-500 font-medium">{errorMessage}</p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 rounded-xl bg-primary text-black font-extrabold text-sm flex items-center justify-center gap-2 hover:shadow-[0_0_20px_rgba(0,240,255,0.5)] active:scale-98 disabled:opacity-50 transition-all mt-2"
            >
              {loading ? "Authenticating..." : "Send Magic Link"} <ArrowRight className="w-4 h-4" />
            </button>
          </form>
        )}

        <div className="text-center text-xs text-text-secondary pt-2 border-t border-border/40">
          Don&apos;t have an account?{" "}
          <Link href={`/signup?plan=${targetPlan}`} className="text-primary font-bold hover:underline hover:text-primary-hover transition-colors">
            Sign up for free
          </Link>
        </div>
      </div>
    </div>
  );
}
