/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import express from "express";
import path from "path";
import { createServer as createViteServer } from "vite";
import { GoogleGenAI, Type } from "@google/genai";
import crypto from "crypto";
import dotenv from "dotenv";
import {
  isSupabaseActive,
  dbSupabaseSignUp,
  dbSupabaseLogIn,
  dbSupabaseGetMe,
  dbSupabaseLogOut,
  dbSupabaseUpdateProfile,
  dbSupabaseCheckAndDecrementCredits,
  dbSupabaseUpgradePlan,
  dbSupabaseGetScripts,
  dbSupabaseSaveScript,
  dbSupabaseDeleteScript,
  dbSupabaseGetHooks,
  dbSupabaseSaveHooks,
  dbSupabaseGetSeoPacks,
  dbSupabaseSaveSeoPack
} from "./server_supabase";

dotenv.config();

// Note: __filename and __dirname are not needed here as process.cwd() is used to resolve paths.

const app = express();
const PORT = 3000;

app.use(express.json());

// Initialize Gemini Client
const apiKey = process.env.GEMINI_API_KEY;
let ai: GoogleGenAI | null = null;

if (apiKey && apiKey !== "MY_GEMINI_API_KEY" && apiKey.trim() !== "") {
  try {
    ai = new GoogleGenAI({
      apiKey: apiKey,
      httpOptions: {
        headers: {
          'User-Agent': 'aistudio-build',
        }
      }
    });
    console.log("Gemini client successfully initialized.");
  } catch (err) {
    console.error("Failed to initialize Gemini client:", err);
  }
} else {
  console.log("No valid GEMINI_API_KEY provided. Server will run in simulation fallback mode.");
}

// Helper: Authenticate middleware
const authenticate = async (req: express.Request, res: express.Response, next: express.NextFunction) => {
  const authHeader = req.headers.authorization;
  const token = authHeader && authHeader.split(" ")[1];
  if (!token) {
    return res.status(401).json({ success: false, error: "Access token required" });
  }
  
  try {
    const user = await dbSupabaseGetMe(token);
    if (!user) {
      return res.status(401).json({ success: false, error: "Invalid or expired session" });
    }
    req.body.authUser = user;
    req.body.authToken = token;
    next();
  } catch (err: any) {
    return res.status(401).json({ success: false, error: "Authentication failed", details: err.message });
  }
};

// ========================
// AUTHENTICATION ROUTES
// ========================

// Signup
app.post("/api/auth/signup", async (req, res) => {
  const { email, password, fullName } = req.body;
  if (!email || !password || !fullName) {
    return res.status(400).json({ success: false, error: "All fields are required" });
  }
  if (password.length < 6) {
    return res.status(400).json({ success: false, error: "Password must be at least 6 characters" });
  }

  try {
    const result = await dbSupabaseSignUp(email, password, fullName);
    if (!result.success) {
      return res.status(400).json({ success: false, error: result.error });
    }

    return res.json({ success: true, token: result.token, user: result.user });
  } catch (err: any) {
    return res.status(500).json({ success: false, error: err.message || "Registration failed" });
  }
});

// Login
app.post("/api/auth/login", async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ success: false, error: "Email and password are required" });
  }

  try {
    const result = await dbSupabaseLogIn(email, password);
    if (!result.success) {
      return res.status(401).json({ success: false, error: result.error });
    }

    return res.json({ success: true, token: result.token, user: result.user });
  } catch (err: any) {
    return res.status(500).json({ success: false, error: err.message || "Login failed" });
  }
});

// Logout
app.post("/api/auth/logout", authenticate, async (req, res) => {
  try {
    await dbSupabaseLogOut(req.body.authToken);
    res.json({ success: true });
  } catch (err: any) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Profile Update / Edit Settings
app.post("/api/auth/profile", authenticate, async (req, res) => {
  const { fullName, email, avatarSeed } = req.body;
  const user = req.body.authUser;

  if (!fullName || !email) {
    return res.status(400).json({ success: false, error: "Name and email are required" });
  }

  try {
    const updatedUser = await dbSupabaseUpdateProfile(user.id, fullName, email, avatarSeed);
    if (updatedUser) {
      return res.json({ success: true, user: updatedUser });
    }
    return res.status(500).json({ success: false, error: "User update failed" });
  } catch (err: any) {
    return res.status(500).json({ success: false, error: err.message });
  }
});

// Get Session User & Saved Items
app.get("/api/auth/me", authenticate, async (req, res) => {
  try {
    const safeUser = req.body.authUser;
    const scripts = await dbSupabaseGetScripts(safeUser.id);
    const hooks = await dbSupabaseGetHooks(safeUser.id);
    const seoPacks = await dbSupabaseGetSeoPacks(safeUser.id);

    res.json({
      success: true,
      user: safeUser,
      scripts,
      hooks,
      seoPacks
    });
  } catch (err: any) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Delete Script
app.delete("/api/scripts/:id", authenticate, async (req, res) => {
  const scriptId = req.params.id;
  try {
    const deleted = await dbSupabaseDeleteScript(req.body.authUser.id, scriptId);
    if (deleted) {
      return res.json({ success: true });
    }
    return res.status(404).json({ success: false, error: "Script not found" });
  } catch (err: any) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// ========================
// PAYMENTS GATEWAY ROUTES
// ========================

// Simulated Checkout Orders Creator
app.post("/api/payments/razorpay", authenticate, (req, res) => {
  const { plan } = req.body;
  if (!plan || (plan !== "founding_creator" && plan !== "creator_pro")) {
    return res.status(400).json({ success: false, error: "Invalid plan type specified" });
  }
  
  const orderId = `order_${crypto.randomUUID().substring(0, 10)}`;
  res.json({
    success: true,
    order_id: orderId,
    amount: plan === "founding_creator" ? 99 : 199,
    currency: "INR"
  });
});

// Verified Callback Updater
app.post("/api/payments/verify", authenticate, async (req, res) => {
  const { plan, razorpay_order_id, razorpay_payment_id } = req.body;
  const user = req.body.authUser;

  if (!plan || !razorpay_order_id || !razorpay_payment_id) {
    return res.status(400).json({ success: false, error: "Verification parameters missing" });
  }

  try {
    const updated = await dbSupabaseUpgradePlan(user.id, plan, razorpay_order_id, razorpay_payment_id);
    if (!updated) {
      return res.status(500).json({ success: false, error: "User profile upgrade failed" });
    }

    res.json({ success: true, user: updated });
  } catch (err: any) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// API Health Check
app.get("/api/health", (req, res) => {
  res.json({
    status: "ok",
    geminiConfigured: !!ai,
    supabaseConfigured: isSupabaseActive(),
    time: new Date().toISOString()
  });
});

// ========================
// SECURED AI ENDPOINTS (WITH QUOTA PROTECTION)
// ========================
app.post("/api/ai", authenticate, async (req: express.Request, res: express.Response) => {
  try {
    const { action, topic, platform, tone, scriptText, targetTone, duration } = req.body;
    const user = req.body.authUser;

    const safeTopic = topic || "SaaS products";
    const safePlatform = platform || "TikTok";
    const safeTone = tone || "High Energy";
    const safeDuration = duration || 45;

    // Quota Limit Guard
    const creditsCheck = await dbSupabaseCheckAndDecrementCredits(user.id);
    if (!creditsCheck.success) {
      return res.status(403).json({
        success: false,
        error: "Quota Exceeded. You have utilized your 5 lifetime free generation credits.",
        code: "CREDITS_EXCEEDED"
      });
    }

    // Fallback data function
    const getFallback = (): any => {
      if (action === "generate_script") {
        return {
          hook: `🚨 Stop scrolling if you want to master "${safeTopic}" in under 60 seconds!`,
          body: `Here is the absolute truth: most creators struggle because they don't optimize for ${safePlatform}. When you deliver content with a ${safeTone} vibe, you instantly trigger the algorithm. First, make sure you double down on hook framing. Second, cut out all dead air. Literally edit with zero-second transitions.`,
          call_to_action: `Hit that follow button right now if you want more ${safePlatform} secrets!`,
          estimated_duration_seconds: 45
        };
      }
      if (action === "generate_hooks") {
        return [
          {
            type: "Curiosity Gap",
            hook_text: `This 1 simple hack changed how I view "${safeTopic}" forever...`,
            predicted_score: 97
          },
          {
            type: "Contrarian / Shock",
            hook_text: `Everything you've been told about "${safeTopic}" is a complete lie.`,
            predicted_score: 94
          },
          {
            type: "Direct Question",
            hook_text: `Are you still struggling with "${safeTopic}" in 2026? Try this.`,
            predicted_score: 91
          },
          {
            type: "Empathy / Pain Point",
            hook_text: `I wasted 3 years doing "${safeTopic}" wrong. Don't make my mistakes.`,
            predicted_score: 89
          },
          {
            type: "Value / Listicle",
            hook_text: `3 secret tools that will instantly double your success in "${safeTopic}".`,
            predicted_score: 86
          }
        ];
      }
      if (action === "generate_seo") {
        const cleanTopic = safeTopic.replace(/[^a-zA-Z0-9]/g, "");
        return {
          titles: [
            `How to Master ${safeTopic} (FAST!)`,
            `The Ultimate ${safeTopic} Blueprint for 2026`,
            `Why nobody is talking about this ${safeTopic} secret`
          ],
          captions: [
            `Stop doing this wrong! 😱 Save this video for later so you don't lose the blueprint! Here is exactly how to dominate ${safeTopic} using AI tools like FocalScribe.`,
            `If you are trying to scale your audience right now, this is your sign to start focusing on ${safeTopic}. Drop a comment with your thoughts! 👇`
          ],
          tags: [
            `#${cleanTopic}`,
            "#focalscribe",
            "#contentcreator",
            "#viralhacks",
            "#saas",
            "#shortformvideo"
          ]
        };
      }
      if (action === "shift_tone") {
        return {
          hook: `✨ Let's look at this differently. Tone shifted to: ${targetTone || "Professional"}.`,
          body: `Regarding the script you provided, we have successfully modified the delivery. The central premise remains pristine, but the structural rhythm has been fine-tuned for a ${targetTone || "Professional"} style. Perfect for building credible authority.`,
          call_to_action: `Follow along to refine your workflow.`,
          estimated_duration_seconds: 35
        };
      }
      return {};
    };

    if (!ai) {
      // If Gemini is not configured, send fallback data and save to database
      const data = getFallback();
      let dbSavedItem: any = null;

      if (action === "generate_script") {
        dbSavedItem = await dbSupabaseSaveScript(user.id, {
          topic: safeTopic,
          platform: safePlatform,
          tone: safeTone,
          duration: safeDuration,
          hook: data.hook,
          body: data.body,
          call_to_action: data.call_to_action,
          full_text: `${data.hook}\n\n${data.body}\n\n${data.call_to_action}`
        });
      } else if (action === "generate_hooks") {
        dbSavedItem = await dbSupabaseSaveHooks(user.id, data);
      } else if (action === "generate_seo") {
        dbSavedItem = await dbSupabaseSaveSeoPack(user.id, data);
      }

      const latestUser = {
        ...user,
        creditsLeft: creditsCheck.creditsLeft,
        plan: creditsCheck.plan
      };

      return res.json({
        success: true,
        isFallback: true,
        data: dbSavedItem || data,
        user: latestUser
      });
    }

    if (action === "generate_script") {
      const response = await ai.models.generateContent({
        model: "gemini-3.5-flash",
        contents: `Write an organic, highly viral, scroll-stopping video script about: "${safeTopic}".
        Social Platform: ${safePlatform}
        Tone Style: ${safeTone}
        Target Duration: ${safeDuration} seconds.
        
        CRITICAL REQUISITES:
        - NEVER write generic fluff, introductory remarks, or filler lines.
        - The script must be high-retention, ready to record, sound 100% natural and human, and have a clear dramatic arc.
        - Use curiosity gaps, storytelling techniques, or shocking statistics.
        - Integrate a concrete, real-life example or actionable scenario to back up the advice.
        - Ensure a compelling, conversion-focused Call to Action at the end.`,
        config: {
          systemInstruction: `You are a legendary viral short-form copywriter who drafts scripts for top TikTok, YouTube Shorts, and Instagram Reels creators with over 10M followers. Your task is to write a highly localized and structured script. Return a JSON object with strictly these keys: "hook" (an attention-grabbing spoken and visual hook within the first 3 seconds), "body" (the core storytelling or educational advice using specific numbers, real case scenarios, and zero generic boilerplate), "call_to_action" (a high-converting, smart, persuasive call to action), and "estimated_duration_seconds" (integer representation of estimated delivery duration). Do not wrap output in markdown formatting. Output pure JSON.`,
          responseMimeType: "application/json",
          responseSchema: {
            type: Type.OBJECT,
            properties: {
              hook: { type: Type.STRING },
              body: { type: Type.STRING },
              call_to_action: { type: Type.STRING },
              estimated_duration_seconds: { type: Type.INTEGER }
            },
            required: ["hook", "body", "call_to_action", "estimated_duration_seconds"]
          }
        }
      });

      const parsed = JSON.parse(response.text || "{}");
      
      const dbSaved = await dbSupabaseSaveScript(user.id, {
        topic: safeTopic,
        platform: safePlatform,
        tone: safeTone,
        duration: parsed.estimated_duration_seconds || safeDuration,
        hook: parsed.hook,
        body: parsed.body,
        call_to_action: parsed.call_to_action,
        full_text: `${parsed.hook}\n\n${parsed.body}\n\n${parsed.call_to_action}`
      });

      const latestUser = {
        ...user,
        creditsLeft: creditsCheck.creditsLeft,
        plan: creditsCheck.plan
      };

      return res.json({ success: true, data: dbSaved, user: latestUser });

    } else if (action === "generate_hooks") {
      const response = await ai.models.generateContent({
        model: "gemini-3.5-flash",
        contents: `Draft 5 viral, high-converting opening hooks for this concept: "${safeTopic}".
        Make them highly creative, punchy, and formatted according to retention dynamics. Include storytelling triggers or curiosity traps.`,
        config: {
          systemInstruction: `You are an expert copywriter specialized in YouTube Shorts, TikTok, and Instagram Reels retention analysis. Generate exactly 5 distinct, scroll-stopping hooks (e.g., curiosity gap, direct question, contrarian/shock, pain-point, or value listicle). Rate each hook with a predicted retention score from 80 to 99 based on surprise factor and visual timing. Return a JSON array of objects. Each object must have keys: "type" (categorization like "Curiosity Gap", "Shocking Statement", etc.), "hook_text" (the spoken text), and "predicted_score" (integer value). Output pure JSON.`,
          responseMimeType: "application/json",
          responseSchema: {
            type: Type.ARRAY,
            items: {
              type: Type.OBJECT,
              properties: {
                type: { type: Type.STRING },
                hook_text: { type: Type.STRING },
                predicted_score: { type: Type.INTEGER }
              },
              required: ["type", "hook_text", "predicted_score"]
            }
          }
        }
      });

      const parsed = JSON.parse(response.text || "[]");
      const dbSaved = await dbSupabaseSaveHooks(user.id, parsed);

      const latestUser = {
        ...user,
        creditsLeft: creditsCheck.creditsLeft,
        plan: creditsCheck.plan
      };

      return res.json({ success: true, data: dbSaved, user: latestUser });

    } else if (action === "generate_seo") {
      const response = await ai.models.generateContent({
        model: "gemini-3.5-flash",
        contents: `Build a highly optimized SEO metadata package for: "${safeTopic}". Include viral click-worthy titles, conversational description captions, and tags.`,
        config: {
          systemInstruction: `You are a social media optimization strategist. Generate exactly 3 click-worthy viral video titles, 2 optimized descriptions (captions) containing smart emojis, and 6 highly trending hashtags. Return a JSON object with keys: "titles" (array of strings), "captions" (array of strings), and "tags" (array of strings). Output pure JSON.`,
          responseMimeType: "application/json",
          responseSchema: {
            type: Type.OBJECT,
            properties: {
              titles: {
                type: Type.ARRAY,
                items: { type: Type.STRING }
              },
              captions: {
                type: Type.ARRAY,
                items: { type: Type.STRING }
              },
              tags: {
                type: Type.ARRAY,
                items: { type: Type.STRING }
              }
            },
            required: ["titles", "captions", "tags"]
          }
        }
      });

      const parsed = JSON.parse(response.text || "{}");
      const dbSaved = await dbSupabaseSaveSeoPack(user.id, parsed);

      const latestUser = {
        ...user,
        creditsLeft: creditsCheck.creditsLeft,
        plan: creditsCheck.plan
      };

      return res.json({ success: true, data: dbSaved, user: latestUser });

    } else if (action === "shift_tone") {
      const originalScript = scriptText || "No script provided.";
      const target = targetTone || "Professional";
      const response = await ai.models.generateContent({
        model: "gemini-3.5-flash",
        contents: `Rewrite this short-form script to have a "${target}" tone while maintaining the exact core message.\n\nOriginal Script:\n${originalScript}`,
        config: {
          systemInstruction: `You are an expert editor. Rewrite the script. Return a JSON object with strictly these keys: "hook", "body", "call_to_action", and "estimated_duration_seconds" (integer).`,
          responseMimeType: "application/json",
          responseSchema: {
            type: Type.OBJECT,
            properties: {
              hook: { type: Type.STRING },
              body: { type: Type.STRING },
              call_to_action: { type: Type.STRING },
              estimated_duration_seconds: { type: Type.INTEGER }
            },
            required: ["hook", "body", "call_to_action", "estimated_duration_seconds"]
          }
        }
      });

      const parsed = JSON.parse(response.text || "{}");
      
      const dbSaved = await dbSupabaseSaveScript(user.id, {
        topic: safeTopic,
        platform: safePlatform,
        tone: target,
        duration: parsed.estimated_duration_seconds || safeDuration,
        hook: parsed.hook,
        body: parsed.body,
        call_to_action: parsed.call_to_action,
        full_text: `${parsed.hook}\n\n${parsed.body}\n\n${parsed.call_to_action}`
      });

      const latestUser = {
        ...user,
        creditsLeft: creditsCheck.creditsLeft,
        plan: creditsCheck.plan
      };

      return res.json({ success: true, data: dbSaved, user: latestUser });
    } else {
      return res.status(400).json({ success: false, error: "Invalid action parameter" });
    }

  } catch (error: any) {
    console.error("AI Route Error:", error);
    return res.status(500).json({
      success: false,
      error: "Internal Server Error",
      details: error.message
    });
  }
});

// Configure Vite middleware in development, static serve in production
async function startServer() {
  if (process.env.NODE_ENV !== "production") {
    console.log("Starting server in DEVELOPMENT mode with Vite Middleware...");
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    console.log("Starting server in PRODUCTION mode...");
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`FocalScribe server listening on port ${PORT}`);
  });
}

startServer();
