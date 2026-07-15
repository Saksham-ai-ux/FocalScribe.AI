"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { 
  Sparkles, 
  TrendingUp, 
  Copy, 
  FileText, 
  RefreshCw, 
  CheckCircle2 
} from "lucide-react";
import { AppProvider, useApp } from "@/lib/store";
import DashboardShell from "@/components/DashboardShell";

export default function HooksPage() {
  return (
    <AppProvider>
      <DashboardShell>
        <HooksContent />
      </DashboardShell>
    </AppProvider>
  );
}

function HooksContent() {
  const router = useRouter();
  const { currentHooks, saveHooks, incrementUsage, user, setCurrentScript } = useApp();

  const [inputConcept, setInputConcept] = useState("");
  const [loading, setLoading] = useState(false);
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const handleGenerateHooks = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputConcept) return;

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
          action: "generate_hooks",
          topic: inputConcept
        })
      });

      const result = await response.json();
      if (result.success && Array.isArray(result.data)) {
        saveHooks(result.data);
        incrementUsage();
      }
    } catch (err) {
      console.error("Hook generation failed:", err);
    } finally {
      setLoading(false);
    }
  };

  const copyHookText = (id: string, text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const writeScriptFromHook = (hookText: string) => {
    // Inject hook text as topic for a new script
    setCurrentScript({
      id: "",
      topic: hookText,
      platform: "TikTok",
      tone: "High Energy",
      duration: 45,
      hook: hookText,
      body: "",
      call_to_action: "",
      full_text: "",
      created_at: new Date().toISOString()
    });
    router.push("/dashboard/scripts");
  };

  const getScoreColor = (score: number) => {
    if (score >= 95) return "bg-green-500/20 text-green-400 border-green-500/30";
    if (score >= 90) return "bg-blue-500/20 text-blue-400 border-blue-500/30";
    return "bg-yellow-500/20 text-yellow-400 border-yellow-500/30";
  };

  return (
    <div className="flex flex-col gap-8 animate-fade-in-up">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-black text-white flex items-center gap-2">
          <TrendingUp className="w-8 h-8 text-primary" /> Hook Variation Generator
        </h1>
        <p className="text-sm text-text-secondary">Generate 5 distinct visual opening hooks with algorithmic predicted visual retention rates.</p>
      </div>

      <div className="grid lg:grid-cols-12 gap-8 items-start">
        {/* Input box */}
        <form onSubmit={handleGenerateHooks} className="lg:col-span-5 p-6 rounded-2xl border border-border bg-card flex flex-col gap-5">
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider">Video Concept or Script Intro</label>
            <textarea
              value={inputConcept}
              onChange={(e) => setInputConcept(e.target.value)}
              placeholder="e.g., How to build a successful SaaS in India under 50k rupees"
              rows={5}
              required
              className="w-full p-4 bg-neutral-950 border border-border rounded-xl text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none resize-none transition-all"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 rounded-xl bg-primary text-black font-extrabold text-sm flex items-center justify-center gap-2 shadow-[0_0_15px_rgba(0,240,255,0.2)] hover:shadow-[0_0_20px_rgba(0,240,255,0.4)] disabled:opacity-50 transition-all"
          >
            {loading ? (
              <span className="flex items-center gap-1">
                <RefreshCw className="w-4 h-4 animate-spin" /> Analyzing & Generating...
              </span>
            ) : (
              <span className="flex items-center gap-1">
                <Sparkles className="w-4 h-4" /> Generate 5 Hooks
              </span>
            )}
          </button>
        </form>

        {/* Output list side */}
        <div className="lg:col-span-7 flex flex-col gap-6">
          {currentHooks.length > 0 ? (
            <div className="space-y-4 animate-fade-in-up">
              <div className="flex justify-between items-center border-b border-border pb-3">
                <span className="text-sm font-bold text-text-secondary">Generated Hook Variants</span>
                <span className="text-[11px] text-primary font-bold">Llama-3 Score Metrics</span>
              </div>

              {currentHooks.map((h, i) => (
                <div key={h.id || i} className="p-5 rounded-xl border border-border bg-card flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 hover:border-primary/30 transition-colors">
                  <div className="flex-1">
                    <span className="text-[9px] uppercase font-bold text-primary tracking-widest px-2 py-0.5 rounded bg-primary/10 border border-primary/20 mb-2 inline-block">
                      {h.type}
                    </span>
                    <p className="text-white font-bold text-base leading-snug">{h.hook_text}</p>
                  </div>

                  <div className="flex sm:flex-col items-center sm:items-end justify-between w-full sm:w-auto gap-3 pt-3 sm:pt-0 border-t sm:border-t-0 border-border">
                    <div className={`px-2.5 py-1 rounded-md border text-xs font-mono font-bold ${getScoreColor(h.predicted_score)}`}>
                      {h.predicted_score}% Score
                    </div>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => copyHookText(h.id || i.toString(), h.hook_text)}
                        className="p-2 rounded bg-neutral-950 border border-border text-text-secondary hover:text-white transition-colors"
                        title="Copy text"
                      >
                        <Copy className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => writeScriptFromHook(h.hook_text)}
                        className="px-3 py-2 rounded bg-primary/10 hover:bg-primary/20 text-primary border border-primary/20 hover:border-primary/40 font-bold text-xs flex items-center gap-1.5 transition-all"
                        title="Draft Script from this"
                      >
                        <FileText className="w-3.5 h-3.5" /> Script
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="h-full min-h-[350px] border border-dashed border-border rounded-2xl flex flex-col items-center justify-center text-center p-6 bg-card/10">
              <div className="w-16 h-16 rounded-full bg-border flex items-center justify-center text-text-muted mb-4">
                <TrendingUp className="w-8 h-8 animate-pulse" />
              </div>
              <h3 className="font-extrabold text-base text-white">Your Hooks are awaiting analysis</h3>
              <p className="text-xs text-text-secondary max-w-sm mt-1">Configure your concept in the left panel, and tap &quot;Generate 5 Hooks&quot; to review scores and variants.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
