"use client";

import React, { useState } from "react";
import { 
  Settings, 
  User, 
  Key, 
  Database, 
  Cpu, 
  CreditCard, 
  HelpCircle,
  CheckCircle2,
  AlertCircle
} from "lucide-react";
import { AppProvider, useApp } from "@/lib/store";
import DashboardShell from "@/components/DashboardShell";
import { isSupabaseConfigured } from "@/lib/supabase";

export default function SettingsPage() {
  return (
    <AppProvider>
      <DashboardShell>
        <SettingsContent />
      </DashboardShell>
    </AppProvider>
  );
}

function SettingsContent() {
  const { user } = useApp();
  
  // Checking configured environmental parameters
  const [profileName, setProfileName] = useState(user?.full_name || "Saksham");
  const [profileEmail, setProfileEmail] = useState(user?.email || "saksham@focalscribe.com");
  const [isSaved, setIsSaved] = useState(false);

  const keyStatus = [
    {
      name: "Supabase Authentication & DB",
      desc: "Handles persistent user sessions, profile details, and script logs.",
      configured: isSupabaseConfigured(),
      icon: <Database className="w-5 h-5 text-primary" />
    },
    {
      name: "Groq AI (Llama-3)",
      desc: "Required to compile viral hooks, video scripts, and SEO metadata.",
      configured: process.env.GROQ_API_KEY !== undefined && process.env.GROQ_API_KEY !== "MY_GROQ_API_KEY",
      icon: <Cpu className="w-5 h-5 text-primary" />
    },
    {
      name: "Razorpay Checkout Gateway",
      desc: "Manages pricing checkouts, recurring billing periods, and transaction signatures.",
      configured: process.env.RAZORPAY_KEY_ID !== undefined && process.env.RAZORPAY_KEY_ID !== "rzp_test_placeholder_key_id",
      icon: <CreditCard className="w-5 h-5 text-primary" />
    }
  ];

  const handleSaveProfile = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaved(true);
    
    // Save to localStorage
    if (user) {
      const updated = {
        ...user,
        full_name: profileName,
        email: profileEmail
      };
      localStorage.setItem("focalscribe_sim_user", JSON.stringify(updated));
    }

    setTimeout(() => setIsSaved(false), 2000);
  };

  return (
    <div className="flex flex-col gap-8 animate-fade-in-up">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-black text-white flex items-center gap-2">
          <Settings className="w-8 h-8 text-primary" /> Account Settings
        </h1>
        <p className="text-sm text-text-secondary">Update your creator profile and inspect system integrations status.</p>
      </div>

      <div className="grid lg:grid-cols-12 gap-8 items-start">
        {/* Profile Card form */}
        <form onSubmit={handleSaveProfile} className="lg:col-span-5 p-6 rounded-2xl border border-border bg-card flex flex-col gap-5">
          <div className="flex items-center gap-3 border-b border-border pb-4 mb-2">
            <User className="w-5 h-5 text-primary" />
            <span className="text-sm font-black text-white uppercase tracking-wider">Creator Details</span>
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider">Full Display Name</label>
            <input
              type="text"
              value={profileName}
              onChange={(e) => setProfileName(e.target.value)}
              required
              className="w-full px-4 py-3 bg-neutral-950 border border-border rounded-xl text-sm focus:border-primary outline-none transition-colors"
            />
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider">Registered Email</label>
            <input
              type="email"
              value={profileEmail}
              onChange={(e) => setProfileEmail(e.target.value)}
              required
              className="w-full px-4 py-3 bg-neutral-950 border border-border rounded-xl text-sm focus:border-primary outline-none transition-colors"
            />
          </div>

          {isSaved && (
            <p className="text-xs text-primary font-bold">✓ Profile changes saved to local browser storage!</p>
          )}

          <button
            type="submit"
            className="w-full py-3 rounded-xl bg-primary text-black font-extrabold text-sm shadow hover:scale-[1.01] transition-all"
          >
            Save Profile
          </button>
        </form>

        {/* Integration Status Console */}
        <div className="lg:col-span-7 p-6 rounded-2xl border border-border bg-card flex flex-col gap-5">
          <div className="flex items-center gap-3 border-b border-border pb-4 mb-2">
            <Key className="w-5 h-5 text-primary" />
            <span className="text-sm font-black text-white uppercase tracking-wider">API Configuration Diagnostic</span>
          </div>

          <div className="space-y-4">
            {keyStatus.map((status, idx) => (
              <div key={idx} className="p-4 rounded-xl border border-border bg-neutral-950/40 flex justify-between items-start gap-4">
                <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center text-primary shrink-0">
                  {status.icon}
                </div>

                <div className="flex-1">
                  <h4 className="font-extrabold text-sm text-white mb-0.5">{status.name}</h4>
                  <p className="text-[11px] text-text-secondary leading-relaxed">{status.desc}</p>
                </div>

                {status.configured ? (
                  <div className="flex items-center gap-1.5 px-2.5 py-1 rounded bg-green-500/10 text-green-400 border border-green-500/20 text-[10px] font-bold">
                    <CheckCircle2 className="w-3.5 h-3.5" /> ACTIVE
                  </div>
                ) : (
                  <div className="flex items-center gap-1.5 px-2.5 py-1 rounded bg-yellow-500/10 text-yellow-500 border border-yellow-500/20 text-[10px] font-bold">
                    <AlertCircle className="w-3.5 h-3.5" /> SIMULATOR
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Secure config tutorial card */}
          <div className="p-4 rounded-xl bg-primary/5 border border-primary/20 flex gap-3 text-xs leading-relaxed text-text-secondary">
            <HelpCircle className="w-5 h-5 text-primary shrink-0" />
            <div>
              <strong className="text-white block mb-0.5">How to update credentials:</strong>
              Do not write credentials directly into the code. Instead, register your keys securely inside the **Secrets panel in the AI Studio UI** under `GROQ_API_KEY` and `RAZORPAY_KEY_ID`. They will automatically inject on the next preview reload!
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
