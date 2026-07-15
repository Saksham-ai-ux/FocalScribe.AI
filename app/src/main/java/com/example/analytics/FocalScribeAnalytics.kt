package com.example.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log

object FocalScribeAnalytics {
    private const val TAG = "FocalScribeAnalytics"

    fun logEvent(context: Context, eventName: String, params: Bundle = Bundle()) {
        Log.d(TAG, "Logging Event: $eventName, Params: ${params.keySet().map { "$it=${params.get(it)}" }}")
        
        // Conceptually, in full production setup, this will fire events to Firebase Analytics:
        // try {
        //     val firebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context)
        //     firebaseAnalytics.logEvent(eventName, params)
        // } catch (e: Exception) {
        //     Log.e(TAG, "Firebase Analytics call failed: ${e.message}")
        // }
    }

    fun logAppOpen(context: Context) {
        logEvent(context, "app_open")
    }

    fun logScriptGenerated(context: Context, platform: String, tone: String, duration: Int) {
        val params = Bundle().apply {
            putString("platform", platform)
            putString("tone", tone)
            putInt("duration_seconds", duration)
        }
        logEvent(context, "script_generated", params)
    }

    fun logHookGenerated(context: Context, framework: String, score: Int) {
        val params = Bundle().apply {
            putString("framework", framework)
            putInt("predicted_score", score)
        }
        logEvent(context, "hook_generated", params)
    }

    fun logSEOPackGenerated(context: Context, topic: String) {
        val params = Bundle().apply {
            putString("topic", topic)
        }
        logEvent(context, "seo_pack_generated", params)
    }

    fun logTeleprompterStarted(context: Context, duration: Int, speed: Float) {
        val params = Bundle().apply {
            putInt("estimated_duration", duration)
            putFloat("scroll_speed", speed)
        }
        logEvent(context, "teleprompter_started", params)
    }

    fun logUpgradeClicked(context: Context, source: String) {
        val params = Bundle().apply {
            putString("click_source", source)
        }
        logEvent(context, "upgrade_clicked", params)
    }

    fun logUpgradeViewed(context: Context, source: String) {
        val params = Bundle().apply {
            putString("view_source", source)
        }
        logEvent(context, "upgrade_viewed", params)
    }

    fun logUpgradePlanClicked(context: Context, planName: String, price: String) {
        val params = Bundle().apply {
            putString("plan_name", planName)
            putString("price", price)
        }
        logEvent(context, "upgrade_clicked_plan", params)
    }

    fun logFoundingPlanPurchased(context: Context) {
        logEvent(context, "founding_plan_purchased")
    }

    fun logProPlanPurchased(context: Context) {
        logEvent(context, "pro_plan_purchased")
    }

    fun logFreeLimitReached(context: Context) {
        logEvent(context, "free_limit_reached")
    }

    fun logTrialUserConverted(context: Context) {
        logEvent(context, "trial_user_converted")
    }

    fun logSubscriptionPurchased(context: Context, planName: String, price: String) {
        val params = Bundle().apply {
            putString("plan_name", planName)
            putString("price", price)
        }
        logEvent(context, "subscription_purchased", params)
    }
}
