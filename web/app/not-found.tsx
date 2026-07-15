"use client";

import React from "react";
import Link from "next/link";
import { Sparkles } from "lucide-react";

export default function NotFound() {
  return (
    <div className="bg-neutral-950 min-h-screen flex flex-col items-center justify-center text-center p-6 select-none text-white font-sans">
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-cyan-500/10 rounded-full blur-[100px] pointer-events-none" />
      
      <div className="w-16 h-16 rounded-2xl bg-cyan-400 flex items-center justify-center shadow-[0_0_15px_rgba(0,240,255,0.4)] mb-6 animate-pulse">
        <Sparkles className="w-10 h-10 text-black" />
      </div>
      
      <h1 className="text-4xl font-black text-white mb-2 tracking-tight">404 - Page Not Found</h1>
      <p className="text-neutral-400 text-sm max-w-sm mb-8 leading-relaxed">
        The creator workspace or page you are trying to access does not exist or has been relocated.
      </p>
      
      <Link href="/" className="px-6 py-3 rounded-xl bg-cyan-400 text-black font-extrabold text-sm hover:shadow-[0_0_15px_rgba(0,240,255,0.5)] hover:scale-[1.02] transition-all">
        Go Back Home
      </Link>
    </div>
  );
}
