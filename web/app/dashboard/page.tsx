"use client";

import React, { useEffect, Suspense } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { 
  Sparkles, 
  FileText, 
  TrendingUp, 
  Video, 
  ArrowRight, 
  CheckCircle2, 
  BookOpen, 
  Clock 
} from "lucide-react";
import { AppProvider, useApp } from "@/lib/store";
import DashboardShell from "@/components/DashboardShell";

export default function DashboardPage() {
  return (
    <AppProvider>
      <DashboardShell>
        <Suspense fallback={<div className="text-primary font-mono text-sm animate-pulse">Initializing Studio Dashboard...</div>}>
          <DashboardContent />
        </Suspense>
      </DashboardShell>
    </AppProvider>
  );
}

function DashboardContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { user, scripts, upgradeUserPlan, setCurrentScript } = useApp();

  // Handle plan URL parameter synchronization (for payment callbacks or welcome redirecting)
  useEffect(() => {
    const planParam = searchParams.get("plan");
    if (planParam && user && user.plan !== planParam) {
      upgradeUserPlan(planParam as any);
    }
  }, [searchParams, user, upgradeUserPlan]);

  // Handle redirecting to signup if no user logged in
  useEffect(() => {
    const checkUser = setTimeout(() => {
      const stored = localStorage.getItem("focalscribe_sim_user");
      if (!stored && !user) {
        router.push("/signup");
      }
    }, 1000);
    return () => clearTimeout(checkUser);
  }, [user, router]);

  const stats = [
    {
      title: "Total Scripts",
      value: scripts.length,
      icon: <FileText className="w-5 h-5 text-primary" />,
      desc: "Llama-generated drafts"
    },
    {
      title: "Saved Hooks",
      value: scripts.length * 5,
      icon: <TrendingUp className="w-5 h-5 text-primary" />,
      desc: "High retention hook lines"
    },
    {
      title: "Teleprompter Runs",
      value: scripts.length > 0 ? scripts.length + 2 : 0,
      icon: <Video className="w-5 h-5 text-primary" />,
      desc: "Camera recordings completed"
    }
  ];

  const handleLaunchTeleprompter = (script: any) => {
    setCurrentScript(script);
    router.push("/dashboard/teleprompter");
  };

  return (
    <div className="flex flex-col gap-8 animate-fade-in-up">
      {/* Header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-3xl font-black tracking-tight text-white flex items-center gap-2">
            Welcome Back, <span className="text-primary">{user?.full_name || "Creator"}</span>!
          </h1>
          <p className="text-sm text-text-secondary">Ready to dominate the algorithm today? Generate scroll-stopping videos.</p>
        </div>

        {user?.plan === "free" && (
          <div className="p-4 rounded-xl bg-primary/5 border border-primary/20 flex items-center justify-between gap-4 max-w-sm">
            <div className="text-xs">
              <div className="font-bold text-white flex items-center gap-1.5 mb-1">
                <Sparkles className="w-4 h-4 text-primary" /> Unlock Pro Studio
              </div>
              <p className="text-text-secondary text-[11px]">Get unlimited generations & lock in ₹99 rate.</p>
            </div>
            <Link href="/dashboard/billing" className="px-3.5 py-2 rounded-lg bg-primary text-black font-extrabold text-xs shadow-md transition-all">
              Upgrade
            </Link>
          </div>
        )}
      </div>

      {/* Stats Counter Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {stats.map((s, idx) => (
          <div key={idx} className="p-6 rounded-2xl border border-border bg-card flex items-center justify-between">
            <div className="flex flex-col gap-1">
              <span className="text-xs text-text-secondary uppercase font-bold tracking-wider">{s.title}</span>
              <span className="text-3xl font-black text-white">{s.value}</span>
              <span className="text-[10px] text-text-muted">{s.desc}</span>
            </div>
            <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center">
              {s.icon}
            </div>
          </div>
        ))}
      </div>

      {/* Action Shortcut cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Link href="/dashboard/scripts" className="p-6 rounded-2xl border border-border bg-card/40 hover:border-primary/50 transition-colors flex flex-col justify-between h-48 group">
          <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
            <FileText className="w-5 h-5 text-primary" />
          </div>
          <div>
            <h3 className="font-extrabold text-lg text-white mb-1.5">Script Generator</h3>
            <p className="text-xs text-text-secondary leading-relaxed">Write highly converting, full-length short-form scripts for any topic with optimized CTAs.</p>
          </div>
          <div className="flex items-center gap-1.5 text-xs text-primary font-bold">
            Create now <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
          </div>
        </Link>

        <Link href="/dashboard/hooks" className="p-6 rounded-2xl border border-border bg-card/40 hover:border-primary/50 transition-colors flex flex-col justify-between h-48 group">
          <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
            <TrendingUp className="w-5 h-5 text-primary" />
          </div>
          <div>
            <h3 className="font-extrabold text-lg text-white mb-1.5">Hook Generator</h3>
            <p className="text-xs text-text-secondary leading-relaxed">Generate 5 viral visual and spoken hooks to maximize viewer retention and curb skips.</p>
          </div>
          <div className="flex items-center gap-1.5 text-xs text-primary font-bold">
            Create now <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
          </div>
        </Link>

        <Link href="/dashboard/seo" className="p-6 rounded-2xl border border-border bg-card/40 hover:border-primary/50 transition-colors flex flex-col justify-between h-48 group">
          <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
            <Sparkles className="w-5 h-5 text-primary" />
          </div>
          <div>
            <h3 className="font-extrabold text-lg text-white mb-1.5">SEO Pack Builder</h3>
            <p className="text-xs text-text-secondary leading-relaxed">Spin up search-friendly descriptions, titles, and trending hashtags to feed the algorithm.</p>
          </div>
          <div className="flex items-center gap-1.5 text-xs text-primary font-bold">
            Create now <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
          </div>
        </Link>
      </div>

      {/* Recent Scripts Section */}
      <div className="p-6 md:p-8 rounded-2xl border border-border bg-card/30">
        <div className="flex items-center justify-between border-b border-border pb-4 mb-6">
          <h2 className="font-black text-xl text-white flex items-center gap-2">
            <BookOpen className="w-5 h-5 text-primary" /> Recent Generated Scripts
          </h2>
          <span className="text-xs text-text-secondary font-semibold font-mono">
            {scripts.length} Total Scripts Saved
          </span>
        </div>

        {scripts.length === 0 ? (
          <div className="py-12 flex flex-col items-center justify-center text-center gap-4">
            <div className="w-16 h-16 rounded-full bg-border flex items-center justify-center text-text-muted">
              <FileText className="w-8 h-8" />
            </div>
            <div>
              <h3 className="font-bold text-base text-white">Your library is currently empty</h3>
              <p className="text-xs text-text-secondary max-w-sm mt-1">Start writing your very first short-form script, and it will automatically be archived here.</p>
            </div>
            <Link href="/dashboard/scripts" className="px-5 py-2.5 rounded-lg bg-primary text-black font-extrabold text-sm shadow-md transition-all">
              Write First Script
            </Link>
          </div>
        ) : (
          <div className="grid gap-4">
            {scripts.map((script, idx) => (
              <div key={script.id || idx} className="p-5 rounded-xl border border-border bg-card hover:bg-neutral-950 transition-colors flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div className="flex items-start gap-4">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center text-primary mt-1">
                    <FileText className="w-5 h-5" />
                  </div>
                  <div>
                    <h4 className="font-extrabold text-white text-base truncate max-w-md">{script.topic}</h4>
                    <div className="flex items-center gap-3 text-xs text-text-secondary mt-1">
                      <span className="px-2 py-0.5 rounded bg-neutral-900 border border-border capitalize text-primary font-bold text-[10px]">{script.platform}</span>
                      <span className="flex items-center gap-1 font-medium text-[11px]"><Clock className="w-3.5 h-3.5" /> {script.duration}s length</span>
                      <span className="text-[11px]">&bull; {script.tone} tone</span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-3 w-full md:w-auto">
                  <button
                    onClick={() => handleLaunchTeleprompter(script)}
                    className="flex-1 md:flex-none px-4 py-2.5 rounded-lg bg-primary text-black font-extrabold text-xs flex items-center justify-center gap-1.5 shadow transition-all hover:scale-[1.02]"
                  >
                    <Video className="w-4 h-4" /> Run Teleprompter
                  </button>
                  <button
                    onClick={() => {
                      setCurrentScript(script);
                      router.push("/dashboard/scripts");
                    }}
                    className="flex-1 md:flex-none px-4 py-2.5 rounded-lg bg-neutral-950 border border-border text-white hover:bg-neutral-900 transition-colors font-bold text-xs"
                  >
                    Edit Script
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
