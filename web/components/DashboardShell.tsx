"use client";

import React, { useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { 
  Sparkles, 
  LayoutDashboard, 
  FileText, 
  TrendingUp, 
  Video, 
  CreditCard, 
  Settings, 
  LogOut, 
  User, 
  Menu, 
  X,
  Plus
} from "lucide-react";
import { useApp } from "@/lib/store";

export default function DashboardShell({ children }: { children: React.ReactNode }) {
  const { user, logout } = useApp();
  const pathname = usePathname();
  const router = useRouter();
  const [mobileOpen, setMobileOpen] = useState(false);

  const menuItems = [
    {
      name: "Dashboard",
      href: "/dashboard",
      icon: <LayoutDashboard className="w-5 h-5" />
    },
    {
      name: "Script Generator",
      href: "/dashboard/scripts",
      icon: <FileText className="w-5 h-5" />
    },
    {
      name: "Hook Generator",
      href: "/dashboard/hooks",
      icon: <TrendingUp className="w-5 h-5" />
    },
    {
      name: "SEO Pack Generator",
      href: "/dashboard/seo",
      icon: <Sparkles className="w-5 h-5" />
    },
    {
      name: "Teleprompter",
      href: "/dashboard/teleprompter",
      icon: <Video className="w-5 h-5" />
    },
    {
      name: "Billing & Plans",
      href: "/dashboard/billing",
      icon: <CreditCard className="w-5 h-5" />
    },
    {
      name: "Account Settings",
      href: "/dashboard/settings",
      icon: <Settings className="w-5 h-5" />
    }
  ];

  const handleLogout = () => {
    logout();
    router.push("/");
  };

  const getPlanBadge = (plan: string) => {
    switch (plan) {
      case "founding_creator":
        return <span className="px-2.5 py-0.5 rounded-full bg-primary/10 text-primary text-[10px] font-black border border-primary/20">FOUNDING CREATOR</span>;
      case "creator_pro":
        return <span className="px-2.5 py-0.5 rounded-full bg-purple-500/10 text-purple-400 text-[10px] font-black border border-purple-500/20">CREATOR PRO</span>;
      default:
        return <span className="px-2.5 py-0.5 rounded-full bg-text-muted/10 text-text-secondary text-[10px] font-black border border-border">FREE USER</span>;
    }
  };

  return (
    <div className="min-h-screen bg-background flex text-white relative">
      {/* Mobile Header */}
      <header className="lg:hidden w-full h-16 border-b border-border bg-card/90 backdrop-blur-md fixed top-0 left-0 right-0 z-40 px-6 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded bg-primary flex items-center justify-center">
            <Sparkles className="w-4 h-4 text-black" />
          </div>
          <span className="font-extrabold text-lg tracking-tight">FocalScribe</span>
        </div>
        <button 
          onClick={() => setMobileOpen(!mobileOpen)} 
          className="p-2 rounded bg-neutral-950 border border-border text-primary"
        >
          {mobileOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
        </button>
      </header>

      {/* Sidebar Navigation */}
      <aside className={`fixed lg:sticky top-0 bottom-0 left-0 z-40 w-64 bg-card border-r border-border flex flex-col justify-between transition-transform lg:transform-none ${
        mobileOpen ? "translate-x-0 pt-16 lg:pt-0" : "-translate-x-full lg:translate-x-0"
      }`}>
        <div className="p-6 flex flex-col gap-8">
          {/* Logo */}
          <div className="hidden lg:flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center shadow-[0_0_15px_rgba(0,240,255,0.4)]">
              <Sparkles className="w-5 h-5 text-black" />
            </div>
            <span className="text-xl font-black tracking-tighter text-white">FocalScribe</span>
          </div>

          {/* Quick Create CTA */}
          <Link 
            href="/dashboard/scripts"
            onClick={() => setMobileOpen(false)}
            className="w-full py-3 px-4 rounded-xl bg-primary text-black font-extrabold text-sm flex items-center justify-center gap-2 shadow-[0_0_15px_rgba(0,240,255,0.2)] hover:shadow-[0_0_20px_rgba(0,240,255,0.4)] hover:scale-[1.01] transition-all"
          >
            <Plus className="w-4 h-4" /> New Video Script
          </Link>

          {/* Navigation Items */}
          <nav className="flex flex-col gap-1">
            {menuItems.map((item, idx) => {
              const active = pathname === item.href;
              return (
                <Link
                  key={idx}
                  href={item.href}
                  onClick={() => setMobileOpen(false)}
                  className={`flex items-center gap-3.5 py-3 px-4 rounded-xl text-sm font-bold transition-colors ${
                    active 
                      ? "bg-primary/5 text-primary border-l-2 border-primary" 
                      : "text-text-secondary hover:text-white hover:bg-neutral-950"
                  }`}
                >
                  {item.icon}
                  <span>{item.name}</span>
                </Link>
              );
            })}
          </nav>
        </div>

        {/* User Card footer */}
        <div className="p-6 border-t border-border flex flex-col gap-4 bg-background/50">
          {user && (
            <div className="flex items-center gap-3">
              <img 
                src={user.avatar_url} 
                alt="Avatar" 
                className="w-10 h-10 rounded-full border border-primary/20"
              />
              <div className="flex-1 min-w-0">
                <div className="text-sm font-bold truncate text-white">{user.full_name}</div>
                <div className="text-[10px] text-text-secondary truncate mb-1">{user.email}</div>
                {getPlanBadge(user.plan)}
              </div>
            </div>
          )}

          {/* Quota Counter */}
          {user && user.plan === "free" && (
            <div className="p-3.5 rounded-lg bg-neutral-950 border border-border text-xs">
              <div className="flex justify-between items-center text-[10px] uppercase font-bold text-text-secondary mb-1.5">
                <span>Daily Credits</span>
                <span className="text-primary">{user.generationsUsedToday} / {user.generationsLimit}</span>
              </div>
              <div className="w-full h-1.5 rounded-full bg-border overflow-hidden">
                <div 
                  className="h-full bg-primary" 
                  style={{ width: `${(user.generationsUsedToday / user.generationsLimit) * 100}%` }}
                />
              </div>
            </div>
          )}

          <button
            onClick={handleLogout}
            className="flex items-center gap-3 py-2 px-4 rounded-lg text-xs font-bold text-red-400 hover:bg-red-500/5 transition-colors mt-2"
          >
            <LogOut className="w-4 h-4" />
            <span>Sign Out</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 min-w-0 flex flex-col pt-16 lg:pt-0 pl-0 lg:pl-0 relative overflow-y-auto h-screen">
        <div className="p-6 md:p-10 max-w-6xl w-full mx-auto">
          {children}
        </div>
      </main>
    </div>
  );
}
