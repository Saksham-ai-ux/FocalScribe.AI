"use client";

import React, { useState } from "react";
import Link from "next/link";
import { 
  Sparkles, 
  Video, 
  CheckCircle2, 
  ArrowRight, 
  Maximize2, 
  ChevronDown, 
  Play, 
  Smartphone, 
  Laptop, 
  Cpu, 
  TrendingUp, 
  Tv 
} from "lucide-react";
import { AppProvider } from "@/lib/store";

export default function Home() {
  return (
    <AppProvider>
      <LandingPageContent />
    </AppProvider>
  );
}

function LandingPageContent() {
  const [activeDemoTab, setActiveDemoTab] = useState<"script" | "hooks" | "teleprompter">("script");
  const [expandedFaq, setExpandedFaq] = useState<number | null>(null);

  const toggleFaq = (index: number) => {
    setExpandedFaq(expandedFaq === index ? null : index);
  };

  const benefits = [
    {
      icon: <Cpu className="w-6 h-6 text-primary" />,
      title: "Ultra-Fast Groq Llama 3 AI",
      desc: "Generate full video scripts, hooks, and SEO descriptions in under 1.5 seconds. No more writing blocks."
    },
    {
      icon: <Video className="w-6 h-6 text-primary" />,
      title: "Perfect Eye-Contact Teleprompter",
      desc: "Our scrolling teleprompter is positioned right below your camera so your viewers feel you are looking directly at them."
    },
    {
      icon: <TrendingUp className="w-6 h-6 text-primary" />,
      title: "Algorithmic Hook Scoring",
      desc: "Our AI analyzes your hook variants and gives them an audience-retention score before you record a single frame."
    }
  ];

  const faqs = [
    {
      q: "What is FocalScribe?",
      a: "FocalScribe is an all-in-one AI-powered writing and recording assistant for short-form creators. It writes high-retention scripts, designs viral hooks, and features an integrated scrolling teleprompter to record with professional camera eye contact."
    },
    {
      q: "What is the Founding Creator plan?",
      a: "It is a highly exclusive promo limited strictly to the first 100 creators. For just ₹99/month, you get full unlimited scripts and teleprompter tools with a lifetime price-lock guarantee. It will never increase for you!"
    },
    {
      q: "How does the teleprompter work?",
      a: "The teleprompter scrolls your generated script at your custom pace directly on screen. On mobile or web, it positions the text closest to the lens, ensuring natural look and delivery."
    },
    {
      q: "Is there a free trial?",
      a: "Yes! Every single signup gets 5 free credits every single day to write hooks, titles, and video scripts. No credit card is required."
    }
  ];

  return (
    <div className="bg-background min-h-screen text-white overflow-x-hidden font-sans selection:bg-primary selection:text-black">
      {/* Header */}
      <nav className="border-b border-border bg-background/80 backdrop-blur-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center shadow-[0_0_15px_rgba(0,240,255,0.4)]">
              <Sparkles className="w-5 h-5 text-black" />
            </div>
            <span className="text-xl font-black tracking-tighter">FocalScribe</span>
          </div>

          <div className="hidden md:flex items-center gap-8 text-sm text-text-secondary">
            <a href="#benefits" className="hover:text-primary transition-colors">Benefits</a>
            <a href="#demo" className="hover:text-primary transition-colors">Product Demo</a>
            <a href="#pricing" className="hover:text-primary transition-colors">Pricing</a>
            <a href="#faq" className="hover:text-primary transition-colors">FAQ</a>
          </div>

          <div className="flex items-center gap-4">
            <Link href="/login" className="text-sm text-text-secondary hover:text-white transition-colors">
              Log In
            </Link>
            <Link href="/signup" className="px-4 py-2 rounded-lg bg-primary text-black font-semibold text-sm hover:shadow-[0_0_15px_rgba(0,240,255,0.5)] hover:scale-[1.02] transition-all">
              Sign Up Free
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative pt-24 pb-20 px-6 max-w-7xl mx-auto flex flex-col items-center text-center">
        {/* Glow spots */}
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-primary/10 rounded-full blur-[120px] pointer-events-none" />
        
        <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-card border border-border text-xs text-primary mb-6 animate-pulse">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Exclusive: First 100 Founding Creator Plans open</span>
        </div>

        <h1 className="text-4xl sm:text-6xl md:text-7xl font-black tracking-tight max-w-5xl leading-[1.1] mb-6">
          Write Viral Short-Form Scripts. <br />
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary to-blue-500">
            Record with Perfect Eye Contact.
          </span>
        </h1>

        <p className="text-text-secondary text-lg sm:text-xl max-w-2xl mb-10 leading-relaxed">
          Create high-retention short-form videos in seconds. Generate Llama-powered scripts, scoring hook variations, viral captions, and record smoothly with our custom-built teleprompter.
        </p>

        <div className="flex flex-col sm:flex-row items-center gap-4">
          <Link href="/signup" className="px-8 py-4 rounded-xl bg-primary text-black font-extrabold text-base flex items-center gap-2 shadow-[0_0_25px_rgba(0,240,255,0.4)] hover:shadow-[0_0_35px_rgba(0,240,255,0.6)] hover:scale-[1.03] transition-all group">
            Start Generating Free <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
          </Link>
          <a href="#demo" className="px-6 py-4 rounded-xl bg-card border border-border font-bold text-base hover:bg-border transition-colors">
            Watch Interactive Demo
          </a>
        </div>

        {/* Home Screen App mockup */}
        <div className="mt-16 w-full max-w-5xl rounded-2xl border border-border bg-card/50 overflow-hidden shadow-2xl p-4 md:p-6 relative glow-cyan">
          <div className="flex items-center justify-between border-b border-border pb-4 mb-4">
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full bg-red-500" />
              <span className="w-3 h-3 rounded-full bg-yellow-500" />
              <span className="w-3 h-3 rounded-full bg-green-500" />
            </div>
            <div className="px-4 py-1.5 rounded-md bg-background border border-border text-xs text-text-secondary font-mono max-w-xs truncate">
              https://focalscribe.ai/dashboard
            </div>
            <div className="w-12" />
          </div>
          <img 
            src="https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=1200&q=80" 
            alt="FocalScribe Dashboard Preview" 
            className="w-full h-auto rounded-xl border border-border"
          />
        </div>
      </section>

      {/* Benefits Section */}
      <section id="benefits" className="py-24 border-t border-border bg-card/20 relative">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-3xl sm:text-4xl font-extrabold mb-4">Why Top Creators Choose FocalScribe</h2>
            <p className="text-text-secondary">Stop writing scripts by hand and staring into the void. Master the visual algorithm.</p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            {benefits.map((b, i) => (
              <div key={i} className="p-8 rounded-2xl border border-border bg-card/60 flex flex-col gap-4 hover:border-primary/50 transition-colors">
                <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center">
                  {b.icon}
                </div>
                <h3 className="text-xl font-bold">{b.title}</h3>
                <p className="text-text-secondary text-sm leading-relaxed">{b.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Interactive Product Demo */}
      <section id="demo" className="py-24 px-6 max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-3xl sm:text-4xl font-extrabold mb-4">Test Drive FocalScribe</h2>
          <p className="text-text-secondary">Click the tabs below to preview the core workflow built into our web application.</p>
        </div>

        <div className="grid lg:grid-cols-12 gap-12 items-start">
          {/* Controls side */}
          <div className="lg:col-span-5 flex flex-col gap-4">
            <button
              onClick={() => setActiveDemoTab("script")}
              className={`p-6 rounded-xl border text-left transition-all ${
                activeDemoTab === "script" 
                  ? "border-primary bg-primary/5 shadow-md" 
                  : "border-border bg-card/40 hover:bg-card"
              }`}
            >
              <div className="flex items-center gap-3 mb-2">
                <Sparkles className="w-5 h-5 text-primary" />
                <span className="font-extrabold">1. Write Llama-Powered Scripts</span>
              </div>
              <p className="text-text-secondary text-sm">Input any topic or keyword, pick a platform (e.g. YouTube Shorts), select your tone, and get a structured video script instantly.</p>
            </button>

            <button
              onClick={() => setActiveDemoTab("hooks")}
              className={`p-6 rounded-xl border text-left transition-all ${
                activeDemoTab === "hooks" 
                  ? "border-primary bg-primary/5 shadow-md" 
                  : "border-border bg-card/40 hover:bg-card"
              }`}
            >
              <div className="flex items-center gap-3 mb-2">
                <TrendingUp className="w-5 h-5 text-primary" />
                <span className="font-extrabold">2. Generate 5 Viral Hook Variants</span>
              </div>
              <p className="text-text-secondary text-sm">Every video needs a solid opening. Generate direct questions, curiosity gaps, and contrarian perspectives automatically.</p>
            </button>

            <button
              onClick={() => setActiveDemoTab("teleprompter")}
              className={`p-6 rounded-xl border text-left transition-all ${
                activeDemoTab === "teleprompter" 
                  ? "border-primary bg-primary/5 shadow-md" 
                  : "border-border bg-card/40 hover:bg-card"
              }`}
            >
              <div className="flex items-center gap-3 mb-2">
                <Video className="w-5 h-5 text-primary" />
                <span className="font-extrabold">3. Use the Smart Teleprompter</span>
              </div>
              <p className="text-text-secondary text-sm">Position your text directly underneath your webcam lens. Set custom scrolling speeds, font sizes, and mirror formats.</p>
            </button>
          </div>

          {/* Visual side */}
          <div className="lg:col-span-7 bg-card border border-border rounded-2xl overflow-hidden p-6 relative glow-cyan">
            {activeDemoTab === "script" && (
              <div className="space-y-4 animate-fade-in-up">
                <div className="flex justify-between items-center border-b border-border pb-3">
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded bg-blue-500/10 text-blue-400 text-xs font-bold">YouTube Shorts</span>
                    <span className="px-2 py-0.5 rounded bg-primary/10 text-primary text-xs font-bold">High Energy</span>
                  </div>
                  <span className="text-xs text-text-secondary">Estimated Length: 45s</span>
                </div>
                <div>
                  <div className="text-xs text-primary font-bold uppercase tracking-wider mb-1">Hook</div>
                  <div className="p-3 rounded bg-background border border-border text-sm font-semibold">
                    &quot;This 1 simple AI trick is literally printing money in 2026, and nobody is talking about it...&quot;
                  </div>
                </div>
                <div>
                  <div className="text-xs text-primary font-bold uppercase tracking-wider mb-1">Body</div>
                  <div className="p-3 rounded bg-background border border-border text-sm leading-relaxed text-text-secondary">
                    Stop building web apps the hard way. While everyone else is writing manual code for days, smart creators are spinning up production-ready platforms using FocalScribe and hosting on Vercel with integrated billing. Here is the blueprint you need...
                  </div>
                </div>
                <div>
                  <div className="text-xs text-primary font-bold uppercase tracking-wider mb-1">CTA</div>
                  <div className="p-3 rounded bg-background border border-border text-sm font-medium">
                    &quot;Subscribe right now if you want the daily tools list!&quot;
                  </div>
                </div>
              </div>
            )}

            {activeDemoTab === "hooks" && (
              <div className="space-y-3 animate-fade-in-up">
                <div className="flex justify-between items-center border-b border-border pb-3">
                  <span className="text-sm font-bold">Hook Variation Generator</span>
                  <span className="text-xs text-primary">Predicted Viral Rate</span>
                </div>
                
                <div className="flex items-center justify-between p-3.5 rounded bg-background border border-border">
                  <div className="flex flex-col">
                    <span className="text-[10px] text-primary uppercase font-bold tracking-wider">Curiosity Gap</span>
                    <span className="text-sm font-medium text-white">&quot;I built a SaaS product in 4 hours... here is how&quot;</span>
                  </div>
                  <div className="px-3 py-1 rounded bg-green-500/20 text-green-400 font-mono text-sm font-bold">98%</div>
                </div>

                <div className="flex items-center justify-between p-3.5 rounded bg-background border border-border">
                  <div className="flex flex-col">
                    <span className="text-[10px] text-blue-400 uppercase font-bold tracking-wider">Direct Question</span>
                    <span className="text-sm font-medium text-white">&quot;Are you still working a standard 9-5 in 2026?&quot;</span>
                  </div>
                  <div className="px-3 py-1 rounded bg-green-500/20 text-green-400 font-mono text-sm font-bold">93%</div>
                </div>

                <div className="flex items-center justify-between p-3.5 rounded bg-background border border-border">
                  <div className="flex flex-col">
                    <span className="text-[10px] text-purple-400 uppercase font-bold tracking-wider">Contrarian</span>
                    <span className="text-sm font-medium text-white">&quot;Stop trying to build a startup on social media.&quot;</span>
                  </div>
                  <div className="px-3 py-1 rounded bg-yellow-500/20 text-yellow-400 font-mono text-sm font-bold">88%</div>
                </div>
              </div>
            )}

            {activeDemoTab === "teleprompter" && (
              <div className="space-y-4 animate-fade-in-up">
                <div className="flex justify-between items-center border-b border-border pb-3">
                  <span className="text-sm font-bold">Eye-Contact Teleprompter Mode</span>
                  <div className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-red-600 animate-pulse" />
                    <span className="text-xs font-mono text-text-secondary">RECORDING PREVIEW</span>
                  </div>
                </div>

                {/* Simulated Lens Location */}
                <div className="flex flex-col items-center justify-center p-3 rounded-lg bg-background border border-border">
                  <div className="w-4 h-4 rounded-full bg-blue-500/30 border border-blue-400 flex items-center justify-center mb-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-blue-400" />
                  </div>
                  <span className="text-[10px] text-text-secondary">LOOK AT LENS (READ DIRECTLY BELOW)</span>
                </div>

                <div className="h-44 bg-neutral-950 rounded-xl overflow-hidden relative p-4 border border-border flex flex-col justify-center text-center">
                  <div className="absolute top-0 bottom-0 left-0 right-0 bg-gradient-to-b from-neutral-950 via-transparent to-neutral-950 pointer-events-none z-10" />
                  
                  <div className="space-y-4 animate-scroll-text text-xl font-bold tracking-tight text-primary">
                    <p>&quot;Stop scrolling right now!&quot;</p>
                    <p>If you want to grow a massive</p>
                    <p>social media brand in 2026,</p>
                    <p>you are doing it wrong.</p>
                    <p>You need scripts that convert.</p>
                    <p>That is where FocalScribe steps in.</p>
                  </div>
                </div>

                <div className="flex justify-between items-center text-xs text-text-secondary">
                  <span>Scroll Speed: 25wpm</span>
                  <span>Font Size: 24px</span>
                  <span>Mirror: OFF</span>
                </div>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="py-24 border-t border-border bg-card/10">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-3xl sm:text-4xl font-extrabold mb-4">Loved by Over 5,000+ Video Creators</h2>
            <p className="text-text-secondary">FocalScribe has generated over 150,000 short-form video scripts.</p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <div className="p-6 rounded-xl border border-border bg-card/40 flex flex-col justify-between">
              <p className="text-text-secondary text-sm italic">&quot;FocalScribe literally saved my production workflow. I went from spending 3 hours writing 5 scripts to writing and recording 10 videos in under an hour. The eye-contact teleprompter is amazing.&quot;</p>
              <div className="flex items-center gap-3 mt-6">
                <img src="https://api.dicebear.com/7.x/pixel-art/svg?seed=Raj" alt="User Avatar" className="w-10 h-10 rounded-full border border-primary/30" />
                <div>
                  <h4 className="text-sm font-bold">Rajesh Kumar</h4>
                  <span className="text-xs text-primary font-medium">Fintech Content Creator</span>
                </div>
              </div>
            </div>

            <div className="p-6 rounded-xl border border-border bg-card/40 flex flex-col justify-between">
              <p className="text-text-secondary text-sm italic">&quot;The ₹99 Founding Creator offer is the best investment I&apos;ve made this year. The hook variation feature got me my first 500k view video on Instagram Reels last week! Highly recommended.&quot;</p>
              <div className="flex items-center gap-3 mt-6">
                <img src="https://api.dicebear.com/7.x/pixel-art/svg?seed=Sarah" alt="User Avatar" className="w-10 h-10 rounded-full border border-primary/30" />
                <div>
                  <h4 className="text-sm font-bold">Sarah Jenkins</h4>
                  <span className="text-xs text-primary font-medium">SaaS Growth Advisor</span>
                </div>
              </div>
            </div>

            <div className="p-6 rounded-xl border border-border bg-card/40 flex flex-col justify-between">
              <p className="text-text-secondary text-sm italic">&quot;I used to look awkward on camera because my eyes would wander to read my notes. Now, with the screen-centered teleprompter, my presentation is flawless. Highly polished tool.&quot;</p>
              <div className="flex items-center gap-3 mt-6">
                <img src="https://api.dicebear.com/7.x/pixel-art/svg?seed=Nico" alt="User Avatar" className="w-10 h-10 rounded-full border border-primary/30" />
                <div>
                  <h4 className="text-sm font-bold">Nicolas Vance</h4>
                  <span className="text-xs text-primary font-medium">Tech Reviewer</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Pricing Matrix Section */}
      <section id="pricing" className="py-24 border-t border-border relative">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-primary/10 rounded-full blur-[100px] pointer-events-none" />

        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-3xl sm:text-4xl font-extrabold mb-4">Simple, transparent, creator-first pricing</h2>
            <p className="text-text-secondary">Get started absolutely free or lock in the special Founding Creator price forever.</p>
          </div>

          <div className="grid md:grid-cols-3 gap-8 items-stretch max-w-5xl mx-auto">
            {/* Free Plan */}
            <div className="p-8 rounded-2xl border border-border bg-card/50 flex flex-col justify-between">
              <div>
                <h3 className="text-lg font-bold text-text-secondary mb-2">Free Plan</h3>
                <div className="flex items-baseline gap-1 mb-6">
                  <span className="text-3xl font-extrabold">₹0</span>
                  <span className="text-xs text-text-secondary">/ month</span>
                </div>
                <p className="text-text-secondary text-sm mb-6">Perfect for new creators starting out with short-form videos.</p>
                <div className="h-px bg-border my-6" />
                <ul className="space-y-4 text-sm text-text-secondary">
                  <li className="flex items-center gap-2"><CheckCircle2 className="w-4 h-4 text-primary" /> 5 generations / day</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="w-4 h-4 text-primary" /> Basic Scriptwriter</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="w-4 h-4 text-primary" /> Standard Teleprompter</li>
                </ul>
              </div>
              <Link href="/signup" className="mt-8 w-full py-3 rounded-xl bg-card border border-border text-center text-sm font-bold hover:bg-border transition-colors">
                Start Free
              </Link>
            </div>

            {/* Founding Creator */}
            <div className="p-8 rounded-2xl border-2 border-primary bg-card relative flex flex-col justify-between overflow-hidden shadow-[0_0_25px_rgba(0,240,255,0.2)]">
              <div className="absolute top-4 right-4 bg-primary text-black text-[10px] font-black uppercase px-2 py-1 rounded">
                100 SPOTS ONLY
              </div>
              <div>
                <h3 className="text-lg font-bold text-primary mb-2">Founding Creator</h3>
                <div className="flex items-baseline gap-1 mb-6">
                  <span className="text-4xl font-black text-primary">₹99</span>
                  <span className="text-xs text-text-secondary">/ month</span>
                </div>
                <p className="text-text-secondary text-sm mb-6">First 100 users only. Lock in this lifetime rate before it doubles.</p>
                <div className="h-px bg-border my-6" />
                <ul className="space-y-4 text-sm text-text-secondary">
                  <li className="flex items-center gap-2 text-white"><CheckCircle2 className="w-4 h-4 text-primary" /> <strong>Unlimited</strong> generations</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="w-4 h-4 text-primary" /> Viral Hook Scoring</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="w-4 h-4 text-primary" /> Smart Teleprompter</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="w-4 h-4 text-primary" /> Lifetime Price Lock</li>
                </ul>
              </div>
              <Link href="/signup?plan=founding_creator" className="mt-8 w-full py-3 rounded-xl bg-primary text-black text-center text-sm font-black shadow-[0_0_15px_rgba(0,240,255,0.4)] hover:shadow-[0_0_25px_rgba(0,240,255,0.6)] hover:scale-[1.02] transition-all">
                Lock in ₹99 Offer Now
              </Link>
            </div>

            {/* Creator Pro */}
            <div className="p-8 rounded-2xl border border-border bg-card/50 flex flex-col justify-between">
              <div>
                <h3 className="text-lg font-bold text-text-secondary mb-2">Creator Pro</h3>
                <div className="flex items-baseline gap-1 mb-6">
                  <span className="text-3xl font-extrabold">₹199</span>
                  <span className="text-xs text-text-secondary">/ month</span>
                </div>
                <p className="text-text-secondary text-sm mb-6">For professional agencies and scaling daily content creators.</p>
                <div className="h-px bg-border my-6" />
                <ul className="space-y-4 text-sm text-text-secondary">
                  <li className="flex items-center gap-2"><CheckCircle2 className="w-4 h-4 text-primary" /> <strong>Unlimited</strong> generations</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="w-4 h-4 text-primary" /> Complete Viral Hook scoring</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="w-4 h-4 text-primary" /> Custom SEO & Captions Packs</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="w-4 h-4 text-primary" /> Priority support & high speed Llama</li>
                </ul>
              </div>
              <Link href="/signup?plan=creator_pro" className="mt-8 w-full py-3 rounded-xl bg-card border border-border text-center text-sm font-bold hover:bg-border transition-colors">
                Upgrade to Pro
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* FAQ Section */}
      <section id="faq" className="py-24 border-t border-border bg-card/10">
        <div className="max-w-4xl mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-extrabold mb-4">Frequently Asked Questions</h2>
            <p className="text-text-secondary">Have questions about FocalScribe? We have answers.</p>
          </div>

          <div className="space-y-4">
            {faqs.map((f, i) => (
              <div key={i} className="border border-border rounded-xl bg-card/40 overflow-hidden">
                <button
                  onClick={() => toggleFaq(i)}
                  className="w-full p-6 text-left flex justify-between items-center font-bold hover:text-primary transition-colors"
                >
                  <span>{f.q}</span>
                  <ChevronDown className={`w-5 h-5 text-text-secondary transition-transform ${expandedFaq === i ? "rotate-180 text-primary" : ""}`} />
                </button>
                {expandedFaq === i && (
                  <div className="px-6 pb-6 text-text-secondary text-sm leading-relaxed border-t border-border/50 pt-4 animate-fade-in-up">
                    {f.a}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 border-t border-border relative text-center px-6">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-primary/10 rounded-full blur-[120px] pointer-events-none" />

        <div className="max-w-4xl mx-auto">
          <h2 className="text-4xl sm:text-5xl font-black mb-6">Ready to stop losing the scroll?</h2>
          <p className="text-text-secondary text-lg max-w-xl mx-auto mb-10 leading-relaxed">
            Join thousands of creators using FocalScribe to save days of writing, maintain perfect eye contact, and grow their reach.
          </p>
          <Link href="/signup" className="px-8 py-4 rounded-xl bg-primary text-black font-extrabold text-base inline-flex items-center gap-2 shadow-[0_0_25px_rgba(0,240,255,0.4)] hover:shadow-[0_0_35px_rgba(0,240,255,0.6)] hover:scale-[1.03] transition-all">
            Get Started For Free <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border py-12 bg-background/90 text-sm text-text-secondary">
        <div className="max-w-7xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded bg-primary flex items-center justify-center">
              <Sparkles className="w-3.5 h-3.5 text-black" />
            </div>
            <span className="font-bold text-white">FocalScribe</span>
            <span>&copy; 2026. All rights reserved.</span>
          </div>

          <div className="flex items-center gap-8">
            <a href="#benefits" className="hover:text-white transition-colors">Privacy Policy</a>
            <a href="#demo" className="hover:text-white transition-colors">Terms of Service</a>
            <a href="#pricing" className="hover:text-white transition-colors">Contact Support</a>
          </div>
        </div>
      </footer>
    </div>
  );
}
