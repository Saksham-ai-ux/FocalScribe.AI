import { NextResponse } from "next/server";

export async function GET() {
  try {
    const groqKey = process.env.GROQ_API_KEY;
    const razorpayKey = process.env.RAZORPAY_KEY_ID;
    
    return NextResponse.json({
      success: true,
      groqConfigured: !!groqKey && groqKey !== "MY_GROQ_API_KEY",
      razorpayConfigured: !!razorpayKey && razorpayKey !== "rzp_test_placeholder_key_id"
    });
  } catch (error: any) {
    return NextResponse.json({
      success: false,
      error: error.message
    });
  }
}

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const { action, topic, platform, tone, scriptText, targetTone } = body;

    const apiKey = process.env.GROQ_API_KEY;

    // Standard high-quality templates if Groq API key is missing or calls fail
    if (!apiKey || apiKey === "MY_GROQ_API_KEY") {
      return NextResponse.json({
        success: true,
        isFallback: true,
        data: getFallbackData(action, { topic, platform, tone, scriptText, targetTone })
      });
    }

    let systemPrompt = "";
    let userPrompt = "";

    if (action === "generate_script") {
      systemPrompt = `You are a viral short-form video scriptwriter. Create a script designed for ${platform}. Tone should be ${tone}. Return ONLY a JSON object with the keys: "hook", "body", "call_to_action", and "estimated_duration_seconds". Do not write markdown wrapping, write only the raw JSON.`;
      userPrompt = `Write a high-converting short-form script about: "${topic}". Make it scroll-stopping.`;
    } else if (action === "generate_hooks") {
      systemPrompt = `You are a copywriting expert specialized in TikTok and Instagram hook creation. Return ONLY a JSON array of 5 hook variants. Each item in the array must be an object with keys "type" (e.g., 'Curiosity Gap', 'Contrarian', 'Direct Question'), "hook_text" (the actual hook line), and "predicted_score" (an integer from 80 to 99). Return ONLY the raw JSON list, no text or explanation.`;
      userPrompt = `Create 5 viral hook variations for this topic or text: "${topic || scriptText}".`;
    } else if (action === "generate_seo") {
      systemPrompt = `You are an SEO and Social Media strategist. Return ONLY a JSON object with keys "titles" (array of 3 viral titles), "captions" (array of 2 high-converting captions), and "tags" (array of 10 trending hashtags). Return ONLY raw JSON.`;
      userPrompt = `Generate a viral SEO pack for a short-form video on topic: "${topic}".`;
    } else if (action === "shift_tone") {
      systemPrompt = `You are an expert editor. Rewrite this script to have a "${targetTone}" tone while keeping the core message exactly the same. Return ONLY a JSON object with "hook", "body", "call_to_action", and "estimated_duration_seconds".`;
      userPrompt = `Original script:\n${scriptText}`;
    } else {
      return NextResponse.json({ error: "Invalid action" }, { status: 400 });
    }

    // Call Groq API
    const response = await fetch("https://api.groq.com/openai/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${apiKey}`
      },
      body: JSON.stringify({
        model: "llama-3.3-70b-versatile", // high-performance default Groq model
        messages: [
          { role: "system", content: systemPrompt },
          { role: "user", content: userPrompt }
        ],
        temperature: 0.7,
        response_format: { type: "json_object" }
      })
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("Groq API error:", errorText);
      // Fallback on error
      return NextResponse.json({
        success: true,
        isFallback: true,
        data: getFallbackData(action, { topic, platform, tone, scriptText, targetTone })
      });
    }

    const groqData = await response.json();
    console.log("Incoming Groq API raw response:", JSON.stringify(groqData));
    const resultText = groqData.choices[0]?.message?.content;
    console.log("Groq API raw response text content:", resultText);
    const parsedData = JSON.parse(resultText);
    console.log("Groq API parsed data:", parsedData);

    return NextResponse.json({
      success: true,
      isFallback: false,
      data: parsedData
    });

  } catch (error: any) {
    console.error("AI Generation Route Error:", error);
    return NextResponse.json({ error: "Internal Server Error", details: error.message }, { status: 500 });
  }
}

// Seamless mock generation logic to ensure visual flow
function getFallbackData(action: string, params: any) {
  const { topic, platform, tone, scriptText, targetTone } = params;
  const safeTopic = topic || "Short form creation hacks";
  const safeTone = tone || "High Energy";
  const safePlatform = platform || "TikTok";

  if (action === "generate_script") {
    return {
      hook: `🚨 Stop scrolling if you want to master "${safeTopic}" in under 60 seconds!`,
      body: `Here is the truth: most creators struggle because they don't optimize for ${safePlatform}. When you output content with a ${safeTone} vibe, you instantly trigger the algorithm. First, make sure you double down on hook framing. Second, cut out all dead air. Literally edit with zero-second transitions.`,
      call_to_action: `Hit that follow button right now if you want more ${safePlatform} secrets!`,
      estimated_duration_seconds: 42
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
    const formattedTopic = safeTopic.replace(/\s+/g, "");
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
        `#${formattedTopic}`,
        "#focalscribe",
        "#contentcreator",
        "#growyourbrand",
        "#viralhacks",
        "#scriptwriter",
        "#saas",
        "#videomarketing",
        "#shortformvideo",
        "#videoeditor"
      ]
    };
  }

  if (action === "shift_tone") {
    return {
      hook: `✨ Let's look at this differently. Tone shift to: ${targetTone || "Professional"}.`,
      body: `Regarding the script you provided, we have successfully modified the delivery. The central premise remains pristine, but the structural rhythm has been fine-tuned for a ${targetTone || "Professional"} style. Perfect for building credible authority.`,
      call_to_action: `Follow along to refine your workflow.`,
      estimated_duration_seconds: 35
    };
  }

  return {};
}
