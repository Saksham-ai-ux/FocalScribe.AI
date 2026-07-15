"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { 
  Sparkles, 
  Copy, 
  RefreshCw, 
  ListPlus 
} from "lucide-react";
import { AppProvider, useApp } from "@/lib/store";
import DashboardShell from "@/components/DashboardShell";

export default function SeoPage() {
  return (
    <AppProvider>
      <DashboardShell>
        <SeoContent />
      </DashboardShell>
    </AppProvider>
  );
}

function SeoContent() {
  const router = useRouter();
  const { currentSeoPack, saveSeoPack, incrementUsage, user } = useApp();

  const [topic, setTopic] = useState("");
  const [loading, setLoading] = useState(false);
  
  const [copiedTitleIdx, setCopiedTitleIdx] = useState<number | null>(null);
  const [copiedCaptionIdx, setCopiedCaptionIdx] = useState<number | null>(null);
  const [copiedTags, setCopiedTags] = useState(false);

  const handleGenerateSeo = async (e: React.FormEvent) => {
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
          action: "generate_seo",
          topic
        })
      });

      const result = await response.json();
      if (result.success && result.data) {
        saveSeoPack({
          topic,
          titles: result.data.titles,
          captions: result.data.captions,
          tags: result.data.tags
        });
        incrementUsage();
      }
    } catch (err) {
      console.error("SEO pack generation failed:", err);
    } finally {
      setLoading(false);
    }
  };

  const copyTitle = (text: string, idx: number) => {
    navigator.clipboard.writeText(text);
    setCopiedTitleIdx(idx);
    setTimeout(() => setCopiedTitleIdx(null), 2000);
  };

  const copyCaption = (text: string, idx: number) => {
    navigator.clipboard.writeText(text);
    setCopiedCaptionIdx(idx);
    setTimeout(() => setCopiedCaptionIdx(null), 2000);
  };

  const copyAllTags = (tags: string[]) => {
    const formatted = tags.join(" ");
    navigator.clipboard.writeText(formatted);
    setCopiedTags(true);
    setTimeout(() => setCopiedTags(false), 2000);
  };

  return (
    <div className="flex flex-col gap-8 animate-fade-in-up">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-black text-white flex items-center gap-2">
          <Sparkles className="w-8 h-8 text-primary" /> SEO Pack Builder
        </h1>
        <p className="text-sm text-text-secondary">Spin up optimized titles, captions, and tags to trigger the visual feed algorithm.</p>
      </div>

      <div className="grid lg:grid-cols-12 gap-8 items-start">
        {/* Form panel */}
        <form onSubmit={handleGenerateSeo} className="lg:col-span-5 p-6 rounded-2xl border border-border bg-card flex flex-col gap-5">
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider">Target Video Concept</label>
            <textarea
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              placeholder="e.g., 3 high-paying tech skills you can learn on YouTube for free"
              rows={4}
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
                <RefreshCw className="w-4 h-4 animate-spin" /> Packaging Metadata...
              </span>
            ) : (
              <span className="flex items-center gap-1">
                <Sparkles className="w-4 h-4" /> Build SEO Pack
              </span>
            )}
          </button>
        </form>

        {/* Results output */}
        <div className="lg:col-span-7 flex flex-col gap-6">
          {currentSeoPack ? (
            <div className="space-y-6 animate-fade-in-up">
              {/* Titles Block */}
              <div className="p-6 rounded-2xl border border-border bg-card">
                <div className="flex justify-between items-center mb-4 pb-2 border-b border-border/50">
                  <span className="text-xs font-black uppercase text-primary">1. Viral Optimized Titles</span>
                  <span className="text-[10px] text-text-secondary font-semibold">Copy chosen title</span>
                </div>
                <div className="space-y-3">
                  {currentSeoPack.titles.map((t, i) => (
                    <div key={i} className="flex justify-between items-center gap-3 p-3 rounded-lg bg-neutral-950 border border-border/60">
                      <p className="text-sm font-extrabold text-white">{t}</p>
                      <button
                        onClick={() => copyTitle(t, i)}
                        className="p-1.5 rounded hover:bg-neutral-900 border border-border text-xs text-text-secondary hover:text-white transition-colors"
                      >
                        {copiedTitleIdx === i ? "Copied!" : <Copy className="w-3.5 h-3.5" />}
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* Captions Block */}
              <div className="p-6 rounded-2xl border border-border bg-card">
                <div className="flex justify-between items-center mb-4 pb-2 border-b border-border/50">
                  <span className="text-xs font-black uppercase text-primary">2. Optimized Captions</span>
                  <span className="text-[10px] text-text-secondary font-semibold">Copy caption text</span>
                </div>
                <div className="space-y-4">
                  {currentSeoPack.captions.map((c, i) => (
                    <div key={i} className="p-4 rounded-lg bg-neutral-950 border border-border/60 flex flex-col gap-3">
                      <p className="text-xs leading-relaxed text-text-secondary whitespace-pre-wrap">{c}</p>
                      <button
                        onClick={() => copyCaption(c, i)}
                        className="self-end px-3 py-1.5 rounded hover:bg-neutral-900 border border-border text-xs font-bold text-primary flex items-center gap-1 transition-colors"
                      >
                        {copiedCaptionIdx === i ? "Copied!" : <span className="flex items-center gap-1"><Copy className="w-3 h-3" /> Copy Caption</span>}
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* Hashtags Block */}
              <div className="p-6 rounded-2xl border border-border bg-card">
                <div className="flex justify-between items-center mb-4 pb-2 border-b border-border/50">
                  <span className="text-xs font-black uppercase text-primary">3. Trending Hashtags</span>
                  <button
                    onClick={() => copyAllTags(currentSeoPack.tags)}
                    className="px-3 py-1.5 rounded-lg bg-neutral-950 hover:bg-neutral-900 border border-border text-xs font-bold text-white flex items-center gap-1 transition-all"
                  >
                    {copiedTags ? "Copied All!" : <span className="flex items-center gap-1"><Copy className="w-3 h-3" /> Copy All</span>}
                  </button>
                </div>
                <div className="flex flex-wrap gap-2">
                  {currentSeoPack.tags.map((tag, i) => (
                    <span key={i} className="px-2.5 py-1.5 rounded-md bg-neutral-950 border border-border text-xs font-mono font-bold text-primary">
                      {tag}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          ) : (
            <div className="h-full min-h-[350px] border border-dashed border-border rounded-2xl flex flex-col items-center justify-center text-center p-6 bg-card/10">
              <div className="w-16 h-16 rounded-full bg-border flex items-center justify-center text-text-muted mb-4">
                <Sparkles className="w-8 h-8 animate-pulse" />
              </div>
              <h3 className="font-extrabold text-base text-white">Your SEO Pack is awaiting metadata</h3>
              <p className="text-xs text-text-secondary max-w-sm mt-1">Configure your concept in the left panel, and tap &quot;Build SEO Pack&quot; to compile hashtags and titles.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
