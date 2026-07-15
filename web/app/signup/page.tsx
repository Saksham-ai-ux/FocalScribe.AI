"use client";

import React, { useState, useEffect, Suspense } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Sparkles, Mail, User, ArrowRight, Chrome } from "lucide-react";
import { AppProvider, useApp } from "@/lib/store";

export default function SignupPage() {
  return (
    <AppProvider>
      <Suspense fallback={<div className="bg-background min-h-screen flex items-center justify-center text-primary font-mono">Loading Registration...</div>}>
        <SignupContent />
      </Suspense>
    </AppProvider>
  );
}

function SignupContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { signup, user } = useApp();
  
  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState("");
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const targetPlan = searchParams.get("plan") || "free";

  useEffect(() => {
    if (user) {
      router.push(`/dashboard?plan=${targetPlan}`);
    }
  }, [user, router, targetPlan]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !fullName) {
      setErrorMessage("Please fill in all details.");
      return;
    }
    setLoading(true);
    setErrorMessage("");

    try {
      const success = await signup(email, fullName);
      if (success) {
        setIsSubmitted(true);
        setTimeout(() => {
          router.push(`/dashboard?plan=${targetPlan}`);
        }, 1500);
      } else {
        setErrorMessage("Signup failed. Please try again.");
      }
    } catch (err) {
      setErrorMessage("An error occurred during registration.");
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
          <Link href="/" className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center shadow-[0_0_15px_rgba(0,240,255,0.4)] mb-2">
            <Sparkles className="w-6 h-6 text-black" />
          </Link>
          <h2 className="text-2xl font-black tracking-tight">Create your account</h2>
          <p className="text-xs text-text-secondary">Get 5 free credits daily instantly</p>
        </div>

        {isSubmitted ? (
          <div className="p-6 rounded-xl bg-primary/5 border border-primary/20 text-center flex flex-col items-center gap-3 animate-fade-in-up">
            <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold">✓</div>
            <p className="text-sm font-semibold text-white">Welcome to FocalScribe!</p>
            <p className="text-xs text-text-secondary">Setting up your creator workspace...</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="name" className="text-xs font-bold text-text-secondary uppercase tracking-wider">Full Name</label>
              <div className="relative">
                <User className="w-5 h-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-text-muted" />
                <input
                  id="name"
                  type="text"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="John Doe"
                  className="w-full pl-11 pr-4 py-3 bg-neutral-950 border border-border rounded-xl text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all"
                />
              </div>
            </div>

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
                  className="w-full pl-11 pr-4 py-3 bg-neutral-950 border border-border rounded-xl text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all"
                />
              </div>
            </div>

            {errorMessage && (
              <p className="text-xs text-red-500 font-medium">{errorMessage}</p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 rounded-xl bg-primary text-black font-extrabold text-sm flex items-center justify-center gap-2 hover:shadow-[0_0_15px_rgba(0,240,255,0.4)] disabled:opacity-50 transition-all"
            >
              {loading ? "Registering..." : "Sign Up Free"} <ArrowRight className="w-4 h-4" />
            </button>

            <div className="flex items-center gap-2 my-1">
              <div className="h-px bg-border flex-1" />
              <span className="text-[10px] uppercase font-bold text-text-muted">or continue with</span>
              <div className="h-px bg-border flex-1" />
            </div>

            <button
              type="button"
              onClick={() => {
                signup(email || "new_creator@gmail.com", fullName || "New Creator");
                setIsSubmitted(true);
                setTimeout(() => {
                  router.push(`/dashboard?plan=${targetPlan}`);
                }, 1500);
              }}
              className="w-full py-3 rounded-xl bg-neutral-950 border border-border text-white text-sm font-bold flex items-center justify-center gap-2 hover:bg-neutral-900 transition-colors"
            >
              <Chrome className="w-4 h-4 text-primary" /> Google Sign-In
            </button>
          </form>
        )}

        <div className="text-center text-xs text-text-secondary">
          Already have an account?{" "}
          <Link href={`/login?plan=${targetPlan}`} className="text-primary font-bold hover:underline">
            Log in
          </Link>
        </div>
      </div>
    </div>
  );
}
