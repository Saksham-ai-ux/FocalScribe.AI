"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { 
  Sparkles, 
  FileText, 
  Copy, 
  Video, 
  Volume2, 
  Sliders, 
  ChevronRight, 
  RefreshCw 
} from "lucide-react";
import { AppProvider, useApp } from "@/lib/store";
import DashboardShell from "@/components/DashboardShell";

export default function ScriptsPage() {
  return (
    <AppProvider>
      <DashboardShell>
        <ScriptContent />
      </DashboardShell>
    </AppProvider>
  );
}

function ScriptContent() {
  const router = useRouter();
  const { currentScript, saveScript, incrementUsage, user, setCurrentScript } = useApp();

  const [topic, setTopic] = useState("");
  const [platform, setPlatform] = useState("TikTok");
  const [tone, setTone] = useState("High Energy");
  const [duration, setDuration] = useState(45);
  
  const [loading, setLoading] = useState(false);
  const [isCopied, setIsCopied] = useState(false);
  const [targetShiftTone, setTargetShiftTone] = useState("Authoritative");
  const [isShifting, setIsShifting] = useState(false);

  // If there's a script currently being edited/viewed from dashboard
  useEffect(() => {
    if (currentScript) {
      setTopic(currentScript.topic);
      setPlatform(currentScript.platform);
      setTone(currentScript.tone);
      setDuration(currentScript.duration);
    }
  }, [currentScript]);

  const handleGenerate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!topic) return;

    // Check quota limits
    if (user?.plan === "free" && user.generationsUsedToday >= user.generationsLimit) {
      router.push("/dashboard/billing");
      return;
    }

    setLoading(true);

    try {
      const response = await fetch("/api/ai", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          action: "generate_script",
          topic,
          platform,
          tone,
          duration
        })
      });

      const result = await response.json();
      if (result.success && result.data) {
        const { hook, body, call_to_action, estimated_duration_seconds } = result.data;
        
        const full_text = `${hook}\n\n${body}\n\n${call_to_action}`;
        
        saveScript({
          topic,
          platform,
          tone,
          duration: estimated_duration_seconds || duration,
          hook,
          body,
          call_to_action,
          full_text
        });

        incrementUsage();
      }
    } catch (err) {
      console.error("Script generation failed:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleToneShift = async () => {
    if (!currentScript) return;
    setIsShifting(true);

    try {
      const response = await fetch("/api/ai", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          action: "shift_tone",
          scriptText: currentScript.full_text,
          targetTone: targetShiftTone
        })
      });

      const result = await response.json();
      if (result.success && result.data) {
        const { hook, body, call_to_action, estimated_duration_seconds } = result.data;
        const full_text = `${hook}\n\n${body}\n\n${call_to_action}`;
        
        saveScript({
          topic: currentScript.topic,
          platform: currentScript.platform,
          tone: targetShiftTone,
          duration: estimated_duration_seconds || currentScript.duration,
          hook,
          body,
          call_to_action,
          full_text
        });
      }
    } catch (err) {
      console.error("Tone shift failed:", err);
    } finally {
      setIsShifting(false);
    }
  };

  const copyToClipboard = () => {
    if (!currentScript) return;
    navigator.clipboard.writeText(currentScript.full_text);
    setIsCopied(true);
    setTimeout(() => setIsCopied(false), 2000);
  };

  const launchTeleprompter = () => {
    if (!currentScript) return;
    router.push("/dashboard/teleprompter");
  };

  return (
    <div className="flex flex-col gap-8 animate-fade-in-up">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-black text-white flex items-center gap-2">
          <FileText className="w-8 h-8 text-primary" /> Script Generator
        </h1>
        <p className="text-sm text-text-secondary">Draft organic viral scripts in seconds for short-form social videos.</p>
      </div>

      <div className="grid lg:grid-cols-12 gap-8 items-start">
        {/* Left side parameters form */}
        <form onSubmit={handleGenerate} className="lg:col-span-5 p-6 rounded-2xl border border-border bg-card flex flex-col gap-5">
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider">Video Topic or Concept</label>
            <textarea
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              placeholder="e.g., 3 secret AI websites that will save you 10 hours a week"
              rows={4}
              required
              className="w-full p-4 bg-neutral-950 border border-border rounded-xl text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none resize-none transition-all"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-text-secondary uppercase tracking-wider">Social Platform</label>
              <select
                value={platform}
                onChange={(e) => setPlatform(e.target.value)}
                className="w-full p-3 bg-neutral-950 border border-border rounded-xl text-sm focus:border-primary outline-none transition-all"
              >
                <option value="TikTok">TikTok</option>
                <option value="Instagram Reels">Instagram Reels</option>
                <option value="YouTube Shorts">YouTube Shorts</option>
              </select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-text-secondary uppercase tracking-wider">Tone of Voice</label>
              <select
                value={tone}
                onChange={(e) => setTone(e.target.value)}
                className="w-full p-3 bg-neutral-950 border border-border rounded-xl text-sm focus:border-primary outline-none transition-all"
              >
                <option value="High Energy">High Energy 🔥</option>
                <option value="Authoritative">Authoritative 🧠</option>
                <option value="Casual">Casual ☕</option>
                <option value="Mysterious">Mysterious 🤫</option>
                <option value="Inspiring">Inspiring ✨</option>
              </select>
            </div>
          </div>

          <div className="flex flex-col gap-1.5">
            <div className="flex justify-between items-center text-xs font-bold text-text-secondary uppercase tracking-wider">
              <span>Target Duration</span>
              <span className="text-primary font-mono">{duration} seconds</span>
            </div>
            <input
              type="range"
              min="15"
              max="60"
              step="15"
              value={duration}
              onChange={(e) => setDuration(parseInt(e.target.value))}
              className="w-full h-1.5 rounded-full bg-border appearance-none cursor-pointer accent-primary"
            />
            <div className="flex justify-between text-[10px] text-text-muted font-bold">
              <span>15s</span>
              <span>30s</span>
              <span>45s</span>
              <span>60s</span>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 rounded-xl bg-primary text-black font-extrabold text-sm flex items-center justify-center gap-2 shadow-[0_0_15px_rgba(0,240,255,0.2)] hover:shadow-[0_0_20px_rgba(0,240,255,0.4)] disabled:opacity-50 transition-all"
          >
            {loading ? (
              <span className="flex items-center gap-1">
                <RefreshCw className="w-4 h-4 animate-spin" /> Writing Script...
              </span>
            ) : (
              <span className="flex items-center gap-1">
                <Sparkles className="w-4 h-4" /> Generate Script
              </span>
            )}
          </button>
        </form>

        {/* Right side results view */}
        <div className="lg:col-span-7 flex flex-col gap-6">
          {currentScript ? (
            <div className="flex flex-col gap-6 animate-fade-in-up">
              {/* Actions Header */}
              <div className="p-4 rounded-xl border border-border bg-card/60 flex flex-wrap items-center justify-between gap-4">
                <div className="flex items-center gap-2 text-xs">
                  <span className="px-2 py-0.5 rounded bg-primary/10 text-primary font-bold">{currentScript.platform}</span>
                  <span className="px-2 py-0.5 rounded bg-blue-500/10 text-blue-400 font-bold">{currentScript.tone}</span>
                  <span className="text-text-secondary font-mono">{currentScript.duration}s</span>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={copyToClipboard}
                    className="p-2.5 rounded-lg bg-neutral-950 border border-border text-text-secondary hover:text-white transition-colors flex items-center gap-1 text-xs"
                    title="Copy to clipboard"
                  >
                    <Copy className="w-4 h-4" /> {isCopied ? "Copied!" : "Copy"}
                  </button>

                  <button
                    onClick={launchTeleprompter}
                    className="p-2.5 rounded-lg bg-primary text-black font-extrabold text-xs flex items-center gap-1.5 shadow"
                  >
                    <Video className="w-4 h-4" /> Teleprompter
                  </button>
                </div>
              </div>

              {/* Hook Card */}
              <div className="p-5 rounded-xl border border-border bg-card">
                <span className="text-[10px] font-black tracking-wider uppercase text-primary mb-2 block">1. The Hook (0 - 3 seconds)</span>
                <p className="text-white font-bold text-lg leading-snug">{currentScript.hook}</p>
              </div>

              {/* Body Card */}
              <div className="p-5 rounded-xl border border-border bg-card">
                <span className="text-[10px] font-black tracking-wider uppercase text-primary mb-2 block">2. The Body (3 - 40 seconds)</span>
                <p className="text-text-secondary text-base leading-relaxed whitespace-pre-wrap">{currentScript.body}</p>
              </div>

              {/* CTA Card */}
              <div className="p-5 rounded-xl border border-border bg-card">
                <span className="text-[10px] font-black tracking-wider uppercase text-primary mb-2 block">3. The Call To Action (40 - 45 seconds)</span>
                <p className="text-white font-semibold text-base">{currentScript.call_to_action}</p>
              </div>

              {/* Tone Shift Widget */}
              <div className="p-6 rounded-xl border border-border bg-neutral-950/40 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center text-primary">
                    <Sliders className="w-5 h-5" />
                  </div>
                  <div>
                    <h4 className="font-bold text-sm text-white">Shifter Sandbox</h4>
                    <p className="text-xs text-text-secondary">Instantly rewrite this script into another style.</p>
                  </div>
                </div>

                <div className="flex items-center gap-2 w-full md:w-auto">
                  <select
                    value={targetShiftTone}
                    onChange={(e) => setTargetShiftTone(e.target.value)}
                    className="p-2.5 bg-card border border-border rounded-lg text-xs font-semibold outline-none focus:border-primary"
                  >
                    <option value="High Energy">High Energy 🔥</option>
                    <option value="Authoritative">Authoritative 🧠</option>
                    <option value="Casual">Casual ☕</option>
                    <option value="Mysterious">Mysterious 🤫</option>
                    <option value="Inspiring">Inspiring ✨</option>
                  </select>
                  <button
                    onClick={handleToneShift}
                    disabled={isShifting}
                    className="px-4 py-2.5 rounded-lg bg-primary text-black font-extrabold text-xs flex items-center gap-1 hover:shadow-md disabled:opacity-50 transition-all"
                  >
                    {isShifting ? "Shifting..." : "Shift Tone"} <ChevronRight className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className="h-full min-h-[400px] border border-dashed border-border rounded-2xl flex flex-col items-center justify-center text-center p-6 bg-card/10">
              <div className="w-16 h-16 rounded-full bg-border flex items-center justify-center text-text-muted mb-4">
                <Sparkles className="w-8 h-8 animate-pulse" />
              </div>
              <h3 className="font-extrabold text-base text-white">Your Script is awaiting inputs</h3>
              <p className="text-xs text-text-secondary max-w-sm mt-1">Configure your topic and settings on the left panel, then tap &quot;Generate Script&quot; to begin your Llama writing workflow.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
