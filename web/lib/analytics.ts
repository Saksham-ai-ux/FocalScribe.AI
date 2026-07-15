/**
 * Centralized FocalScribe Web Analytics Tracker
 */
export const trackEvent = (eventName: string, params: Record<string, any> = {}) => {
  // 1. Log to console for debugging
  console.log(`[Analytics] Event: "${eventName}"`, params);

  // 2. Log to Google Analytics (gtag) if loaded
  if (typeof window !== "undefined" && (window as any).gtag) {
    (window as any).gtag("event", eventName, params);
  }

  // 3. Log to PostHog if loaded
  if (typeof window !== "undefined" && (window as any).posthog) {
    (window as any).posthog.capture(eventName, params);
  }
};

// Strongly typed event helpers matching our tracker
export const analytics = {
  logSignUp: (method: string) => {
    trackEvent("sign_up", { method });
  },
  
  logAppOpen: (userId?: string) => {
    trackEvent("app_open", { userId });
  },

  logScriptGenerated: (platform: string, tone: string, topic: string) => {
    trackEvent("script_generated", { platform, tone, topic_length: topic.length });
  },

  logHookGenerated: (type: string, score: number) => {
    trackEvent("hook_generated", { type, score });
  },

  logSEOPackGenerated: (topic: string) => {
    trackEvent("seo_pack_generated", { topic_length: topic.length });
  },

  logTeleprompterStarted: (durationSeconds: number, speed: number) => {
    trackEvent("teleprompter_started", { duration_seconds: durationSeconds, speed });
  },

  logUpgradeViewed: (source: string) => {
    trackEvent("upgrade_viewed", { source });
  },

  logUpgradePlanClicked: (planName: string, price: string) => {
    trackEvent("upgrade_plan_clicked", { plan_name: planName, price });
  },

  logSubscriptionPurchased: (planName: string, price: string, transactionId: string) => {
    trackEvent("subscription_purchased", { plan_name: planName, price, transaction_id: transactionId });
  }
};
