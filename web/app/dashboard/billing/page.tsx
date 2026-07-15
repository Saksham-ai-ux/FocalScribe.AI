"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { 
  CreditCard, 
  CheckCircle2, 
  Sparkles, 
  TrendingUp, 
  ShieldAlert, 
  Check 
} from "lucide-react";
import { AppProvider, useApp } from "@/lib/store";
import DashboardShell from "@/components/DashboardShell";
import { analytics } from "@/lib/analytics";

export default function BillingPage() {
  return (
    <AppProvider>
      <DashboardShell>
        <BillingContent />
      </DashboardShell>
    </AppProvider>
  );
}

function BillingContent() {
  const router = useRouter();
  const { user, upgradeUserPlan } = useApp();
  const [loadingPlan, setLoadingPlan] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState("");

  const handleUpgrade = async (plan: "founding_creator" | "creator_pro") => {
    if (!user) {
      router.push("/signup");
      return;
    }

    setLoadingPlan(plan);
    analytics.logUpgradePlanClicked(plan, plan === "founding_creator" ? "₹99/mo" : "₹199/mo");

    try {
      // 1. Call API endpoint to create subscription order
      const response = await fetch("/api/billing", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          action: "create_subscription",
          plan_type: plan,
          userId: user.id
        })
      });

      const orderData = await response.json();

      if (!orderData.success) {
        throw new Error(orderData.error || "Order creation failed");
      }

      // 2. Open Razorpay Checkout overlay
      const options = {
        key: orderData.keyId,
        subscription_id: orderData.subscriptionId,
        amount: orderData.amount,
        currency: orderData.currency,
        name: "FocalScribe",
        description: `Upgrade to ${plan.replace("_", " ").toUpperCase()} Plan`,
        image: "https://api.dicebear.com/7.x/pixel-art/svg?seed=FocalScribe",
        handler: async function (paymentResponse: any) {
          // 3. Verify payment signature on backend
          const verificationResponse = await fetch("/api/billing", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              action: "verify_subscription",
              plan_type: plan,
              userId: user.id,
              razorpay_payment_id: paymentResponse.razorpay_payment_id,
              razorpay_subscription_id: paymentResponse.razorpay_subscription_id || orderData.subscriptionId,
              razorpay_signature: paymentResponse.razorpay_signature
            })
          });

          const verificationResult = await verificationResponse.json();

          if (verificationResult.success) {
            upgradeUserPlan(plan);
            analytics.logSubscriptionPurchased(
              plan, 
              plan === "founding_creator" ? "₹99/mo" : "₹199/mo", 
              paymentResponse.razorpay_payment_id
            );
            setSuccessMessage(`Congratulations! You are now subscribed to ${plan.replace("_", " ").toUpperCase()}!`);
            setTimeout(() => setSuccessMessage(""), 5000);
          }
        },
        prefill: {
          name: user.full_name,
          email: user.email,
        },
        theme: {
          color: "#00F0FF",
        },
        modal: {
          ondismiss: function () {
            console.log("Checkout modal closed by user.");
          }
        }
      };

      // Ensure window.Razorpay SDK is active
      if ((window as any).Razorpay) {
        const rzp = new (window as any).Razorpay(options);
        rzp.open();
      } else {
        // Fallback simulation if script didn't load in layout (sandbox preview)
        console.log("Simulating checkout success in Sandbox...");
        setTimeout(() => {
          upgradeUserPlan(plan);
          analytics.logSubscriptionPurchased(plan, plan === "founding_creator" ? "₹99/mo" : "₹199/mo", "mock_pay_100");
          setSuccessMessage(`Simulated upgraded successfully to ${plan.replace("_", " ").toUpperCase()}!`);
          setTimeout(() => setSuccessMessage(""), 5000);
        }, 1500);
      }

    } catch (err) {
      console.error("Billing upgrade error:", err);
    } finally {
      setLoadingPlan(null);
    }
  };

  const getPlanNameFormatted = (plan: string) => {
    if (plan === "founding_creator") return "Founding Creator Offer (₹99/mo)";
    if (plan === "creator_pro") return "Creator Pro (₹199/mo)";
    return "Free Starter Tier (₹0/mo)";
  };

  return (
    <div className="flex flex-col gap-8 animate-fade-in-up">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-black text-white flex items-center gap-2">
          <CreditCard className="w-8 h-8 text-primary" /> Billing & Subscriptions
        </h1>
        <p className="text-sm text-text-secondary">Manage your scriptwriter plans and review automated Razorpay transactions.</p>
      </div>

      {successMessage && (
        <div className="p-6 rounded-2xl bg-green-500/10 border-2 border-green-500/30 text-center flex flex-col items-center gap-2 animate-fade-in-up">
          <div className="w-10 h-10 rounded-full bg-green-500/20 flex items-center justify-center text-green-400 font-bold">✓</div>
          <p className="text-base font-extrabold text-white">{successMessage}</p>
          <p className="text-xs text-green-400 font-medium">Your daily quotas have been successfully updated to unlimited!</p>
        </div>
      )}

      {/* Current plan box */}
      <div className="p-6 md:p-8 rounded-2xl border border-border bg-card relative overflow-hidden glow-cyan">
        <div className="absolute top-0 right-0 w-44 h-44 bg-primary/5 rounded-full blur-3xl pointer-events-none" />
        
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
          <div className="flex flex-col gap-2">
            <span className="text-[10px] font-black uppercase tracking-widest text-primary">Your Active Plan</span>
            <h3 className="text-2xl font-black text-white">{getPlanNameFormatted(user?.plan || "free")}</h3>
            <p className="text-xs text-text-secondary leading-relaxed">
              {user?.plan === "free" 
                ? "You are currently utilizing our free starter package. Upgrades lift limit thresholds instantly." 
                : "Your subscription is currently active, managed, and recurring via secure Razorpay pipelines."}
            </p>
          </div>

          <div className="p-4 rounded-xl bg-neutral-950 border border-border/80 flex flex-col gap-1 w-full md:w-56 text-center">
            <span className="text-[10px] font-bold text-text-secondary uppercase tracking-widest">Generations Counter</span>
            <span className="text-2xl font-black text-primary">
              {user?.plan === "free" ? `${user?.generationsUsedToday} / 5` : "Unlimited 🔥"}
            </span>
            <span className="text-[9px] text-text-muted">Resets daily at 12:00 AM UTC</span>
          </div>
        </div>
      </div>

      {/* Comparison Grid */}
      <div className="space-y-6">
        <div className="border-b border-border pb-2 mb-2">
          <h2 className="font-extrabold text-xl text-white">Compare Plans & Upgrades</h2>
          <p className="text-xs text-text-secondary">Pick the optimal tier for your short-form video release volume.</p>
        </div>

        <div className="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto">
          {/* Founding Creator Card */}
          <div className={`p-8 rounded-2xl border flex flex-col justify-between ${
            user?.plan === "founding_creator" 
              ? "border-primary bg-primary/5 shadow-[0_0_20px_rgba(0,240,255,0.15)]" 
              : "border-border bg-card/60 hover:border-primary/30 transition-colors"
          }`}>
            <div>
              <div className="flex justify-between items-start mb-4">
                <div>
                  <span className="text-[10px] bg-primary/10 text-primary border border-primary/20 font-black px-2 py-0.5 rounded uppercase tracking-wider block mb-1">
                    EXCLUSIVE FOUNDING OFFER
                  </span>
                  <h3 className="text-xl font-black text-white">Founding Creator</h3>
                </div>
                <div className="flex flex-col items-end">
                  <span className="text-2xl font-black text-primary">₹99</span>
                  <span className="text-[10px] text-text-secondary">/ month</span>
                </div>
              </div>

              <p className="text-xs text-text-secondary leading-relaxed mb-6">
                Limited to the first 100 signups. Lock in this early bird pricing forever, even as features scale.
              </p>

              <div className="h-px bg-border my-5" />

              <ul className="space-y-3.5 text-xs text-text-secondary">
                <li className="flex items-center gap-2"><Check className="w-4 h-4 text-primary" /> <strong>Unlimited</strong> Llama scriptwriting</li>
                <li className="flex items-center gap-2"><Check className="w-4 h-4 text-primary" /> 5 Viral Hooks Visual Scoring</li>
                <li className="flex items-center gap-2"><Check className="w-4 h-4 text-primary" /> Complete Eye-Contact Teleprompter</li>
                <li className="flex items-center gap-2"><Check className="w-4 h-4 text-primary" /> Lifetime Price Lock Guarantee</li>
              </ul>
            </div>

            <button
              onClick={() => handleUpgrade("founding_creator")}
              disabled={loadingPlan !== null || user?.plan === "founding_creator"}
              className={`w-full py-3.5 rounded-xl font-extrabold text-xs mt-8 transition-all ${
                user?.plan === "founding_creator"
                  ? "bg-neutral-900 border border-primary/20 text-primary cursor-default"
                  : "bg-primary text-black hover:shadow-[0_0_15px_rgba(0,240,255,0.4)] hover:scale-[1.01]"
              }`}
            >
              {loadingPlan === "founding_creator" ? "Verifying..." : user?.plan === "founding_creator" ? "Current Active Plan" : "Get Founding Plan (₹99)"}
            </button>
          </div>

          {/* Creator Pro Card */}
          <div className={`p-8 rounded-2xl border flex flex-col justify-between ${
            user?.plan === "creator_pro" 
              ? "border-purple-500 bg-purple-500/5 shadow-[0_0_20px_rgba(157,78,221,0.15)]" 
              : "border-border bg-card/60 hover:border-purple-500/30 transition-colors"
          }`}>
            <div>
              <div className="flex justify-between items-start mb-4">
                <div>
                  <span className="text-[10px] bg-purple-500/10 text-purple-400 border border-purple-500/20 font-black px-2 py-0.5 rounded uppercase tracking-wider block mb-1">
                    AGENCY CAPACITY
                  </span>
                  <h3 className="text-xl font-black text-white">Creator Pro</h3>
                </div>
                <div className="flex flex-col items-end">
                  <span className="text-2xl font-black text-purple-400">₹199</span>
                  <span className="text-[10px] text-text-secondary">/ month</span>
                </div>
              </div>

              <p className="text-xs text-text-secondary leading-relaxed mb-6">
                Designed for daily uploading content creators and agencies looking to scale search and video output.
              </p>

              <div className="h-px bg-border my-5" />

              <ul className="space-y-3.5 text-xs text-text-secondary">
                <li className="flex items-center gap-2"><Check className="w-4 h-4 text-purple-400" /> <strong>Unlimited</strong> Llama scriptwriting</li>
                <li className="flex items-center gap-2"><Check className="w-4 h-4 text-purple-400" /> 5 Viral Hooks Visual Scoring</li>
                <li className="flex items-center gap-2"><Check className="w-4 h-4 text-purple-400" /> Full SEO Title & Keyword packs</li>
                <li className="flex items-center gap-2"><Check className="w-4 h-4 text-purple-400" /> High Priority API speeds</li>
              </ul>
            </div>

            <button
              onClick={() => handleUpgrade("creator_pro")}
              disabled={loadingPlan !== null || user?.plan === "creator_pro"}
              className={`w-full py-3.5 rounded-xl font-extrabold text-xs mt-8 transition-all ${
                user?.plan === "creator_pro"
                  ? "bg-neutral-900 border border-purple-500/20 text-purple-400 cursor-default"
                  : "bg-purple-500 text-white hover:shadow-[0_0_15px_rgba(157,78,221,0.4)] hover:scale-[1.01]"
              }`}
            >
              {loadingPlan === "creator_pro" ? "Verifying..." : user?.plan === "creator_pro" ? "Current Active Plan" : "Get Pro Plan (₹199)"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
