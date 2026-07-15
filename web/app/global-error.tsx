"use client";

import React from "react";

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <html>
      <body className="bg-neutral-950 text-white min-h-screen flex flex-col items-center justify-center text-center p-6 font-sans">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-red-500/10 rounded-full blur-[100px] pointer-events-none" />
        
        <div className="w-16 h-16 rounded-2xl bg-red-500 flex items-center justify-center shadow-[0_0_15px_rgba(239,68,68,0.4)] mb-6">
          <span className="text-2xl font-black text-black">!</span>
        </div>
        
        <h2 className="text-3xl font-black text-white mb-2 tracking-tight">Something went wrong!</h2>
        <p className="text-neutral-400 text-sm max-w-sm mb-8 leading-relaxed">
          An unexpected application error occurred. Please try reloading the workspace.
        </p>
        
        <button
          onClick={() => reset()}
          className="px-6 py-3 rounded-xl bg-red-500 text-white font-extrabold text-sm hover:shadow-[0_0_15px_rgba(239,68,68,0.5)] transition-all"
        >
          Try Again
        </button>
      </body>
    </html>
  );
}
