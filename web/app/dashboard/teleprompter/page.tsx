"use client";

import React, { useState, useEffect, useRef } from "react";
import { 
  Video, 
  Play, 
  Pause, 
  RotateCcw, 
  Type, 
  Activity, 
  Tv, 
  ChevronRight, 
  Eye 
} from "lucide-react";
import { AppProvider, useApp } from "@/lib/store";
import DashboardShell from "@/components/DashboardShell";

export default function TeleprompterPage() {
  return (
    <AppProvider>
      <DashboardShell>
        <TeleprompterContent />
      </DashboardShell>
    </AppProvider>
  );
}

function TeleprompterContent() {
  const { currentScript } = useApp();
  
  const [customText, setCustomText] = useState("");
  const [isEditing, setIsEditing] = useState(false);
  
  // Controls state
  const [isScrolling, setIsScrolling] = useState(false);
  const [speed, setSpeed] = useState(30); // in words per minute / step rate
  const [fontSize, setFontSize] = useState(24); // px
  const [isMirrored, setIsMirrored] = useState(false);

  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const animationFrameRef = useRef<number | null>(null);
  const scrollOffsetRef = useRef<number>(0);

  useEffect(() => {
    if (currentScript) {
      setCustomText(currentScript.full_text);
    } else {
      // Default placeholder text
      setCustomText(
        `🚨 STOP SCROLLING! \n\nIf you want to build a highly successful SaaS product in 2026, you're looking at the wrong playbook. \n\nWhile everyone else is arguing about frameworks, smart founders are focusing purely on distribution. \n\nHere is the 3-step engine you need: \nFirst, build a modular, high-speed single screen MVP. \nSecond, launch with automated Razorpay pricing. \nThird, write high-retention short videos using FocalScribe. \n\nHit that follow button right now for daily SaaS blueprints!`
      );
    }
  }, [currentScript]);

  // Handle auto-scroll loop
  useEffect(() => {
    if (isScrolling) {
      const scrollContainer = scrollContainerRef.current;
      if (!scrollContainer) return;

      const runScroll = () => {
        if (!scrollContainer) return;
        
        // Map WPM speed to visual scroll increment
        const increment = (speed / 100) * 0.4;
        scrollOffsetRef.current += increment;
        
        scrollContainer.scrollTop = scrollOffsetRef.current;

        // Reset if reached bottom
        if (scrollContainer.scrollTop >= scrollContainer.scrollHeight - scrollContainer.clientHeight) {
          setIsScrolling(false);
        } else {
          animationFrameRef.current = requestAnimationFrame(runScroll);
        }
      };

      animationFrameRef.current = requestAnimationFrame(runScroll);
    } else {
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current);
      }
    }

    return () => {
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current);
      }
    };
  }, [isScrolling, speed]);

  const handleReset = () => {
    setIsScrolling(false);
    scrollOffsetRef.current = 0;
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollTop = 0;
    }
  };

  return (
    <div className="flex flex-col gap-8 animate-fade-in-up h-[calc(100vh-140px)]">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-black text-white flex items-center gap-2">
            <Video className="w-8 h-8 text-primary" /> Smart Teleprompter
          </h1>
          <p className="text-sm text-text-secondary">Position your window right below your lens to record with perfect eye contact.</p>
        </div>

        <button
          onClick={() => setIsEditing(!isEditing)}
          className="px-4 py-2.5 rounded-lg bg-neutral-950 border border-border text-white text-xs font-bold hover:bg-neutral-900 transition-colors"
        >
          {isEditing ? "View Teleprompter" : "Edit / Paste Text"}
        </button>
      </div>

      {isEditing ? (
        <div className="flex-1 bg-card border border-border rounded-2xl p-6 flex flex-col gap-4">
          <span className="text-xs font-bold text-text-secondary uppercase tracking-wider">Configure Teleprompter Copy</span>
          <textarea
            value={customText}
            onChange={(e) => setCustomText(e.target.value)}
            className="flex-1 p-5 bg-neutral-950 border border-border rounded-xl text-sm font-mono focus:border-primary outline-none resize-none"
            placeholder="Paste your video script copy here..."
          />
          <button
            onClick={() => setIsEditing(false)}
            className="py-3 bg-primary text-black font-extrabold text-sm rounded-xl shadow-[0_0_15px_rgba(0,240,255,0.2)] hover:scale-[1.01] transition-all"
          >
            Load to Teleprompter View
          </button>
        </div>
      ) : (
        <div className="flex-1 grid lg:grid-cols-12 gap-8 items-stretch min-h-0">
          {/* Main Visual Teleprompter Panel */}
          <div className="lg:col-span-8 bg-card border border-border rounded-2xl flex flex-col overflow-hidden relative glow-cyan min-h-[450px]">
            {/* Focal Eye Lens Guide Bar */}
            <div className="h-10 bg-neutral-950/90 border-b border-border flex items-center justify-center gap-1.5 px-4 text-[10px] text-text-secondary font-bold tracking-widest uppercase relative z-20">
              <Eye className="w-4 h-4 text-primary animate-pulse" /> Keep eyes aligned with this bracket for perfect camera eye-contact
            </div>

            {/* Scrolling Viewport */}
            <div className="flex-1 bg-neutral-950 relative overflow-hidden flex flex-col justify-center">
              {/* Highlight Overlay Brackets */}
              <div className="absolute left-0 right-0 top-[40%] bottom-[40%] border-y border-primary/20 bg-primary/5 pointer-events-none z-10 flex items-center justify-between px-4">
                <span className="text-[10px] font-mono text-primary/50 font-bold tracking-widest">&gt;&gt;</span>
                <span className="text-[10px] font-mono text-primary/50 font-bold tracking-widest">&lt;&lt;</span>
              </div>

              {/* Shading gradient overlays */}
              <div className="absolute top-0 left-0 right-0 h-28 bg-gradient-to-b from-neutral-950 to-transparent pointer-events-none z-10" />
              <div className="absolute bottom-0 left-0 right-0 h-28 bg-gradient-to-t from-neutral-950 to-transparent pointer-events-none z-10" />

              <div
                ref={scrollContainerRef}
                className="w-full h-full overflow-y-auto py-[20vh] px-8 text-center scrollbar-none"
                style={{ scrollBehavior: "smooth" }}
              >
                <div
                  className="font-bold leading-relaxed whitespace-pre-wrap tracking-tight select-none"
                  style={{
                    fontSize: `${fontSize}px`,
                    transform: isMirrored ? "scaleX(-1)" : "none",
                    color: "white"
                  }}
                >
                  {customText}
                </div>
              </div>
            </div>
          </div>

          {/* Controls Config Sidebar */}
          <div className="lg:col-span-4 bg-card border border-border rounded-2xl p-6 flex flex-col justify-between gap-6">
            <div className="flex flex-col gap-6">
              <span className="text-xs font-black tracking-wider uppercase text-text-secondary border-b border-border pb-3 block">Prompter Controls</span>

              {/* Speed Controller */}
              <div className="flex flex-col gap-2">
                <div className="flex justify-between items-center text-xs font-bold text-text-secondary">
                  <span>Scroll Speed</span>
                  <span className="text-primary font-mono">{speed} WPM</span>
                </div>
                <input
                  type="range"
                  min="10"
                  max="100"
                  step="5"
                  value={speed}
                  onChange={(e) => setSpeed(parseInt(e.target.value))}
                  className="w-full h-1.5 rounded-full bg-border appearance-none cursor-pointer accent-primary"
                />
              </div>

              {/* Font Size Controller */}
              <div className="flex flex-col gap-2">
                <div className="flex justify-between items-center text-xs font-bold text-text-secondary">
                  <span>Font Scale</span>
                  <span className="text-primary font-mono">{fontSize} PX</span>
                </div>
                <input
                  type="range"
                  min="16"
                  max="48"
                  step="2"
                  value={fontSize}
                  onChange={(e) => setFontSize(parseInt(e.target.value))}
                  className="w-full h-1.5 rounded-full bg-border appearance-none cursor-pointer accent-primary"
                />
              </div>

              {/* Mirror Toggler */}
              <div className="flex justify-between items-center p-3.5 rounded-xl bg-neutral-950 border border-border">
                <div className="flex items-center gap-2">
                  <Tv className="w-4 h-4 text-primary" />
                  <div className="flex flex-col">
                    <span className="text-xs font-bold text-white">Glass Mirror Mode</span>
                    <span className="text-[10px] text-text-secondary">For external monitor panels</span>
                  </div>
                </div>
                <button
                  onClick={() => setIsMirrored(!isMirrored)}
                  className={`w-11 h-6 rounded-full transition-colors relative ${isMirrored ? "bg-primary" : "bg-border"}`}
                >
                  <span className={`w-4 h-4 rounded-full bg-black absolute top-1 transition-all ${isMirrored ? "left-6" : "left-1"}`} />
                </button>
              </div>
            </div>

            {/* Playback Actions Grid */}
            <div className="flex flex-col gap-3">
              <div className="grid grid-cols-2 gap-3">
                <button
                  onClick={() => setIsScrolling(!isScrolling)}
                  className={`py-3.5 rounded-xl font-extrabold text-sm flex items-center justify-center gap-1.5 shadow ${
                    isScrolling 
                      ? "bg-amber-500 text-black shadow-amber-500/10" 
                      : "bg-primary text-black shadow-primary/10"
                  }`}
                >
                  {isScrolling ? (
                    <>
                      <Pause className="w-4 h-4 fill-black" /> Pause
                    </>
                  ) : (
                    <>
                      <Play className="w-4 h-4 fill-black" /> Scroll
                    </>
                  )}
                </button>

                <button
                  onClick={handleReset}
                  className="py-3.5 rounded-xl bg-neutral-950 border border-border text-white text-sm font-bold flex items-center justify-center gap-1.5 hover:bg-neutral-900 transition-colors"
                >
                  <RotateCcw className="w-4 h-4" /> Reset
                </button>
              </div>

              <div className="p-3 bg-neutral-950/50 rounded-lg border border-border text-[10px] text-text-secondary text-center leading-relaxed">
                Tip: Position your web window closely to the top center of your display to perfectly minimize noticeable gaze adjustments.
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
