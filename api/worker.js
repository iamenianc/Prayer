/**
 * Prayer AI Agent API — Cloudflare Worker Serverless Proxy
 * 
 * Provides secure, anonymous inference access for the mobile prayer companion.
 * Endpoints:
 *   - POST /api/v1/guide (and POST /): Multi-turn theological distillation engine.
 *   - POST /api/v1/title: Lightweight post-commit auto-titling branch (exempt from theological validation).
 *   - GET  /health (and GET /): Edge proxy health check and route discovery.
 *   - OPTIONS: CORS preflight for all endpoints.
 */


// --- AUTHORITATIVE TWO-TIER SYSTEM PROMPTS ---

const DEFAULT_TIER1_TITLE_PROMPT = `You are a concise prayer point title writer.
Brainstorm 2 to 3 natural, meaningful title ideas (each 2 to 4 words) capturing the core burden or situation.
- Strive for clean, dignified, plain titles in Title Case (e.g., "Grandpa's Recovery & Care", "Wisdom for Knee Surgery", "Patience Amid Work Pressure").
- Strictly avoid sterile clinical codes or hospital triage labels (e.g., avoid "Grandpa Hospital Pneumonia").
- Strictly avoid overly poetic, melodramatic, or cheesy phrasing (e.g., avoid "When Breathing Falters", "Frail Breath", or greeting-card clichés).
- Never use prefixes like "Pray for", "Prayer for", or "Please pray".
Output strictly valid JSON:
{
  "candidates": ["Title One", "Title Two", "Title Three"]
}`;

const DEFAULT_TIER2_TITLE_PROMPT = `You are the strict Title Verification and Formatting Harness.
Review the prayer point text and the Tier 1 candidate titles (if provided). Select or refine the single best title:
1. HARD LIMIT: STRICTLY 2 TO 6 WORDS (target 2–4 words, never 7 or more words).
2. NO REDUNDANT PREFIXES: NEVER use prefixes like "Pray for", "Pray that", "Prayer for", "Please pray", or "Ask God to". State the point directly.
3. NATURAL & FAITHFUL: The title must faithfully reflect what the user wrote in plain, dignified words in Title Case (never ALL-CAPS). Strictly avoid both sterile clinical tags and overly poetic or cheesy phrasing.
4. OUTPUT FORMAT: Output STRICTLY valid JSON with no conversational text:
{
  "title": "Concise Title Here"
}`;

const DEFAULT_TIER1_DISTILLATION_PROMPT = `You are a thoughtful prayer distillation assistant grounded in historic Reformed Christian theology.
Your role is to reflect on the user's unstructured burden and articulate natural, sober, non-robotic questions or candidate prayer points.
- TONE: Dignified, sober, and plain. Strictly avoid artificial empathy, therapeutic clichés ("I hear how hard this is", "Bless you"), and overly poetic, dramatic, or cheesy sentimentality (no greeting-card fluff or flowery prose).
- MANDATORY INQUIRY RULE (Turn 1):
  * When user_response is null and the input is brief, vague, or lacks specific details (such as single words or short phrases like "finances", "anxious", "tired", "my boss"): you MUST formulate strictly ONE natural, concise clarifying question (6–12 words) asking plainly what is causing the situation or what is happening.
  * In this case, set skip_question: false, clarifying_question: "...", and candidate_prayer_points: [].
- CANDIDATE PRAYER POINTS (Turn 2, or when input already has clear circumstantial detail):
  * Set skip_question: true, clarifying_question: null.
  * Express the genuine circumstantial burden, heart posture, and humble trust in God's sovereign care through Jesus Christ.
  * Keep language grounded, objective, and reverent—plain and sober, avoiding both robotic jargon and flowery, cheesy, or overly poetic melodrama.
  * Draft 2 distinct candidate points exploring complementary aspects (e.g. practical wisdom/resolution, and endurance/godly conduct).
  * Keep drafts concise (titles 2–4 words in Title Case, descriptions 15–25 words).
- If root is already provided in the input, set suggested_root: null.
Output draft JSON:
{
  "skip_question": boolean,
  "clarifying_question": "string or null",
  "candidate_prayer_points": [
    {
      "title": "string",
      "description": "string",
      "suggested_root": "PEOPLE | GROUPS | GENERAL | MISSION_PARTNERS | null",
      "suggested_group": "string or null"
    }
  ]
}`;

const DEFAULT_TIER2_DISTILLATION_HARNESS_PROMPT = `You are the strict Verification, Compression, and Compliance Harness for the Prayer Distillation Engine.
You receive the user's devotional input along with a Tier 1 draft.
Your mandatory task is to REWRITE, PURIFY, AND CONDENSE the draft into strictly compliant mobile prayer cards:

MANDATORY COMPLIANCE DIRECTIVES:
1. NEVER WRITE AN ACTUAL PRAYER:
   - Absolutely NO second-person prayer language or direct address to God (STRIP ALL 'Father', 'Lord Jesus', 'we come before You', 'we ask that You').
   - Believers pray directly to God; your output is strictly an OBJECTIVE PRAYER POINT summarizing the burden.
2. STRICT MOBILE BREVITY CEILINGS:
   - TITLE: HARD LIMIT STRICTLY 2 TO 6 WORDS (target 2–4 words, hard cap 6). Sober, clear, and plain in Title Case (never ALL-CAPS); strictly avoid cheesy or overly poetic titles.
   - DESCRIPTION: HARD LIMIT MAXIMUM 20–25 WORDS. Write in concise, telegraphic shorthand.
3. DESCRIPTION STRUCTURE (HIGH-LEVEL, EMBRACE VARIETY):
   - Keep descriptions telegraphic and scannable; join compact clauses or phrases with whatever punctuation fits the burden (semicolons, em-dashes, commas).
   - Do NOT force a fixed 2-to-3 clause template or a prescribed ordering of need/attitude/submission. Let each description grow naturally from the specific burden.
   - The two cards in a response MUST differ in structure and vocabulary.
4. LEXICAL DIVERSITY ACROSS CARDS:
   - When generating 2 cards, use distinct vocabulary across both cards. Do NOT repeat the exact same phrase or clause across both cards in the same response.
5. TAXONOMY & CARD INVARIANTS:
   - When generating cards (skip_question: true), clarifying_question MUST be null and output STRICTLY AND EXACTLY 2 candidate cards.
   - When asking a clarifying question (skip_question: false), candidate_prayer_points MUST be empty ([]).
   - If user_input.root is NOT null (already pre-specified, e.g. 'PEOPLE', 'GROUPS', 'GENERAL', or 'MISSION_PARTNERS'), you MUST set "suggested_root": null and "suggested_group": null on every card (a suggestion is not needed).
   - If user_input.root IS null, you MUST classify every generated card with a NON-NULL suggested_root drawn strictly from 'PEOPLE', 'GROUPS', 'GENERAL', or 'MISSION_PARTNERS'. NEVER output null for suggested_root in this case. 'PEOPLE' requires a single distinct individual; 'GROUPS' for a collective/community/setting; 'GENERAL' for broad societal, national, or abstract matters; 'MISSION_PARTNERS' for supported missionary families, mission agencies, or ministry partners. The four suggested_root values across the response MUST be identical (one single root for the whole response).
   - Entity names are masked locally for privacy; do not invent or suggest entity names.
6. CRITICAL INQUIRY PRESERVATION:
   - If tier1_draft.skip_question is false (Tier 1 posed a clarifying question), you MUST NOT generate candidate prayer points under any circumstances. Output skip_question: false, preserve that clarifying_question (refine wording only for brevity if needed), and candidate_prayer_points: []. NEVER convert a clarifying question into prayer cards.
   - If user_input.user_response is present and non-null (including the literal "skip"): the user has already answered or skipped, SO DO NOT ask any further questions. You MUST generate candidate prayer points with skip_question: true and clarifying_question: null.
   - If user_input.request_more is true: DO NOT ask questions. Generate strictly 2 NEW distinct candidate points with skip_question: true and clarifying_question: null.
7. THEOLOGICAL GUARDRAILS:
   - Classical Reformed Protestant theology (Solus Christus, Sola Gratia, Soli Deo Gloria).
   - Comfort grounded in Heidelberg Catechism Q&A 1 (resting in Christ's faithful preservation and sovereign care).
   - Reject prosperity decrees, bargaining, and word-faith formulas.
   - Never invent or assume unstated medical illnesses, hospitalizations, or tragedies.
8. DIALECT:
   - Default to English (Australian / UK) spelling (e.g., neighbour, honour, saviour) unless US English is requested.
9. OUTPUT FORMAT: Output STRICTLY valid JSON with no markdown fences or conversational commentary:
{
  "skip_question": boolean,
  "clarifying_question": "string or null",
  "candidate_prayer_points": [
    {
      "title": "string (2-6 words, Title Case)",
      "description": "string (telegraphic, <= 20-25 words; scannable but NOT a rigid clause template — vary structure naturally per burden, join compact clauses/phrases with semicolons, em-dashes, or commas as fits)",
      "suggested_root": "PEOPLE | GROUPS | GENERAL | MISSION_PARTNERS | null (set a real root from PEOPLE/GROUPS/GENERAL/MISSION_PARTNERS when user_input.root is null)",
      "suggested_group": "string or null"
    }
  ]
}`;

const DEFAULT_INQUIRY_PROMPT = `You are a focused clarifying inquiry assistant for a prayer journal. The user has written only a brief, vague reflection. Your SINGLE task is to formulate exactly ONE concise, natural, open-ended clarifying question that invites the user to describe the actual situation or burden they want to bring to prayer.
- Ask strictly ONE question, ideally 6 to 12 words, never more than 15 words.
- Plain, natural English. NEVER bureaucratic or stiff phrasing (never "Which burden or circumstance regarding...").
- If the reflection is an internal feeling or emotional state (e.g. anxious, tired, sad, overwhelmed, confused): ask plainly what is causing that feeling.
- If it names a person or topic with no context (e.g. my boss, finances, David, church): ask plainly what is happening.
- If several competing concerns are presented together: perform concise burden triage and ask which weighs most heavily.
- Never guess, speculate, or invent unstated circumstances. Never propose candidate prayer points.
Output STRICTLY valid JSON with no other text:
{
  "skip_question": false,
  "clarifying_question": "<your question here>",
  "candidate_prayer_points": []
}`;

// --- TURN-1 VAGUE INPUT DETECTION (Deterministic Inquiry Gate) ---

const VAGUE_DETAIL_HINT = /(\btomorrow\b|\byesterday\b|\btoday\b|\btonight\b|\bweek\b|\bmonth\b|\bmorning\b|\bafternoon\b|\bevening\b|\bround\b|\bday\b|chemo|surgery|hospital|scan|tumou?r|diagnos|interview|meeting|exam|trial|rent|overdue|funeral|accident|passed|passing|gave|got|lost|losing|broke|argu|fight|figh|crisis|emergency|died|death|born|delivery|nausea|pain|heal|recover|results|test|promotion|redundan|dismiss|fired|layoff|boss|\d)/i;

function isVagueReflection(text) {
  const tokens = text.trim().split(/\s+/).filter(Boolean);
  if (tokens.length <= 3) return true;
  if (tokens.length > 8) return false;
  return !VAGUE_DETAIL_HINT.test(text);
}

function fallbackClarifyingQuestion(text) {
  const t = text.trim();
  if (/(anxious|anxiety|worr|afraid|scared|fear|sad|grief|griev|tired|exhaust|overwhelm|stress|depress|lone|lonely|confus|ashamed|guilt)/i.test(t)) {
    return "What is making you feel this way right now?";
  }
  if (/\b(my boss|my manager|work|job|colleague|co-worker|coworker)\b/i.test(t)) {
    return "What is happening at work right now?";
  }
  if (/\b(finance|money|financ|debt|rent|bill)\b/i.test(t)) {
    return "What is happening with your finances right now?";
  }
  return "What is the situation you would like to pray about?";
}

function parseInquiryContent(content) {
  try {
    const parsed = JSON.parse(content);
    const q = (typeof parsed.clarifying_question === "string") ? parsed.clarifying_question.trim() : "";
    if (parsed.skip_question === false && q.length >= 4 && q.split(/\s+/).length <= 18) {
      return { skip_question: false, clarifying_question: q, candidate_prayer_points: [] };
    }
  } catch {
    // fall through to fallback
  }
  return null;
}

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const path = url.pathname.replace(/\/+$/, "") || "/";

    // 1. Universal CORS Preflight
    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, X-Prayer-Gateway-Secret",
          "Access-Control-Max-Age": "86400",
        },
      });
    }

    // 2. Health check & route discovery
    if (request.method === "GET" && (path === "/" || path === "/health")) {
      return new Response(JSON.stringify({
        status: "online",
        service: "Prayer AI Agent API Proxy",
        version: "1.3.0",
        endpoints: {
          assistant: "POST /api/v1/assistant (or POST /api/v1/guide, POST /)",
          title: "POST /api/v1/title",
          health: "GET /health",
        },
      }, null, 2), {
        status: 200,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
        },
      });
    }

    // 3. Only allow POST requests for operational endpoints
    if (request.method !== "POST") {
      return new Response(JSON.stringify({ error: "Method not allowed" }), {
        status: 405,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
        },
      });
    }

    // 4. Validate Gateway Secret (if configured)
    if (env.APP_GATEWAY_SECRET) {
      const clientSecret = request.headers.get("X-Prayer-Gateway-Secret");
      if (clientSecret !== env.APP_GATEWAY_SECRET) {
        return new Response(JSON.stringify({ error: "Unauthorized gateway request" }), {
          status: 401,
          headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
          },
        });
      }
    }

    // 5. Route to appropriate handler
    if (path === "/api/v1/title") {
      return handleTitleGeneration(request, env);
    } else if (path === "/api/v1/assistant" || path === "/api/v1/guide" || path === "/" || path === "/guide" || path === "/assistant") {
      return handleDistillationGuide(request, env);
    } else {
      return new Response(JSON.stringify({ error: "Not found", path }), {
        status: 404,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
        },
      });
    }
  },
};

/**
 * Helper to execute an OpenRouter inference request.
 * Automatically disables hidden reasoning overhead to protect latency and token quotas.
 */
async function callOpenRouter(env, { model, temperature, top_p, max_tokens, messages, title = "Prayer Distillation Engine" }) {
  try {
    const requestPayload = {
      model,
      temperature,
      max_tokens,
      response_format: { type: "json_object" },
      reasoning: { enabled: false },
      provider: { data_collection: "deny" },
      messages,
    };
    if (top_p !== undefined && top_p !== null) {
      requestPayload.top_p = top_p;
    }

    const response = await fetch("https://openrouter.ai/api/v1/chat/completions", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${env.OPENROUTER_API_KEY}`,
        "Content-Type": "application/json",
        "HTTP-Referer": "https://prayer-app.local",
        "X-Title": title,
      },
      body: JSON.stringify(requestPayload),
    });

    if (!response.ok) return null;
    const data = await response.json();
    return data.choices?.[0]?.message?.content || null;
  } catch {
    return null;
  }
}

/**
 * Handle POST /api/v1/title — Two-Tier Branched Post-Commit Auto-Titling
 * Tier 1: Thoughtful, non-sterile title brainstorming (Temperature: 1.0, Top_P: 0.95).
 * Tier 2: Low temperature (0.1) compliance harness enforcing word ceilings, prefix elimination, and JSON format.
 */
async function handleTitleGeneration(request, env) {
  try {
    const body = await request.json();
    const prayerPointBody = (body.body || body.text || body.initial_reflection || "").trim();

    if (!prayerPointBody) {
      return new Response(JSON.stringify({ error: "Missing or empty prayer point body" }), {
        status: 400,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
        },
      });
    }

    const dialect = body.dialect === "EN_US" ? "US English (e.g., Savior, Honor, Neighbor)" : "English (Australian / UK; e.g., Saviour, Honour, Neighbour)";
    const upstreamModel = env.OPENROUTER_MODEL || "nvidia/nemotron-3.5-lightning";

    // --- TIER 1: Creative & Plain Brainstorming (Temperature: 1.0, Top_P: 0.95) ---
    const tier1Content = await callOpenRouter(env, {
      model: upstreamModel,
      temperature: 1.0,
      top_p: 0.95,
      max_tokens: 9000,
      messages: [
        { role: "system", content: DEFAULT_TIER1_TITLE_PROMPT },
        { role: "user", content: prayerPointBody.slice(0, 2000) },
      ],
      title: "Prayer Title Generator - Tier 1",
    });

    let tier1Candidates = [];
    if (tier1Content) {
      try {
        const parsed = JSON.parse(tier1Content);
        if (Array.isArray(parsed.candidates)) {
          tier1Candidates = parsed.candidates;
        }
      } catch {
        // Soft fail: proceed directly to Tier 2
      }
    }

    // --- TIER 2: Verification & Compliance Harness (Low Temperature: 0.1) ---
    const tier2Prompt = `${env.PROMPT_TITLE || DEFAULT_TIER2_TITLE_PROMPT}
Dialect requirement: ${dialect}.`;

    const tier2Input = JSON.stringify({
      prayer_point_text: prayerPointBody.slice(0, 2000),
      tier1_candidates: tier1Candidates.length > 0 ? tier1Candidates : undefined,
      dialect: dialect,
    });

    const tier2Content = await callOpenRouter(env, {
      model: upstreamModel,
      temperature: 0.1,
      max_tokens: 9000,
      messages: [
        { role: "system", content: tier2Prompt },
        { role: "user", content: tier2Input },
      ],
      title: "Prayer Title Generator - Tier 2",
    });

    if (!tier2Content) {
      return new Response(JSON.stringify({ error: "Empty or truncated model response" }), {
        status: 502,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
        },
      });
    }

    return new Response(tier2Content, {
      status: 200,
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });

  } catch (err) {
    return new Response(JSON.stringify({ error: "Internal server error" }), {
      status: 500,
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });
  }
}

/**
 * Handle POST /api/v1/assistant, POST /api/v1/guide (and POST /) — "Prayer Assistant" Distillation Engine
 * Multi-turn, confessional Reformed inquiry and candidate prayer point generation.
 */
async function handleDistillationGuide(request, env) {
  try {
    const body = await request.json();

    // Support structured JSON payload or legacy user_input
    const initialReflection = body.initial_reflection || body.user_input;
    if (!initialReflection || typeof initialReflection !== "string" || initialReflection.trim().length === 0) {
      return new Response(JSON.stringify({ error: "Missing or empty initial_reflection (or user_input)" }), {
        status: 400,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
        },
      });
    }

    const validRoots = ["PEOPLE", "GROUPS", "GENERAL", "MISSION_PARTNERS"];
    const root = (body.root && validRoots.includes(String(body.root).toUpperCase()))
      ? String(body.root).toUpperCase()
      : null;

    const group = (body.group && typeof body.group === "string")
      ? body.group.trim().slice(0, 100)
      : null;

    const clarifyingQuestion = (body.clarifying_question && typeof body.clarifying_question === "string")
      ? body.clarifying_question.trim().slice(0, 500)
      : null;

    const userResponse = (body.user_response && typeof body.user_response === "string")
      ? body.user_response.trim().slice(0, 1000)
      : null;

    const requestMore = Boolean(body.request_more);

    // Package sanitized structured JSON payload for model prompt
    const promptPayload = {
      initial_reflection: initialReflection.trim().slice(0, 1500),
      root: root,
      group: group,
      clarifying_question: clarifyingQuestion,
      user_response: userResponse,
      request_more: requestMore,
    };

    const promptJsonString = JSON.stringify(promptPayload);
    const upstreamModel = env.OPENROUTER_MODEL || "nvidia/nemotron-3.5-lightning";

    // --- DETERMINISTIC TURN-1 INQUIRY GATE ---
    // When the user has not yet provided a response (fresh Turn 1), a brief or
    // context-free reflection MUST trigger a clarifying question rather than
    // speculative card generation (see planning/BRD.md, planning/UX.md).
    const isFreshTurn1 = !requestMore && (userResponse === null || userResponse.trim().length === 0);
    if (isFreshTurn1 && isVagueReflection(initialReflection.trim())) {
      const inquiryPrompt = env.PROMPT_INQUIRY || DEFAULT_INQUIRY_PROMPT;
      const inquiryContent = await callOpenRouter(env, {
        model: upstreamModel,
        temperature: 1.0,
        top_p: 0.95,
        max_tokens: 1000,
        messages: [
          { role: "system", content: inquiryPrompt },
          { role: "user", content: String(initialReflection).trim().slice(0, 1500) },
        ],
        title: "Prayer Distillation Engine - Clarifying Inquiry",
      });

      let inquiryResponse = inquiryContent ? parseInquiryContent(inquiryContent) : null;
      if (!inquiryResponse) {
        inquiryResponse = {
          skip_question: false,
          clarifying_question: fallbackClarifyingQuestion(String(initialReflection)),
          candidate_prayer_points: [],
        };
      }

      return new Response(JSON.stringify(inquiryResponse), {
        status: 200,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
        },
      });
    }

    // --- TIER 1: Thoughtful Articulation & Pastoral Insight (Temperature: 1.0, Top_P: 0.95) ---
    const tier1Content = await callOpenRouter(env, {
      model: upstreamModel,
      temperature: 1.0,
      top_p: 0.95,
      max_tokens: 9000,
      messages: [
        { role: "system", content: DEFAULT_TIER1_DISTILLATION_PROMPT },
        { role: "user", content: promptJsonString },
      ],
      title: "Prayer Distillation Engine - Tier 1",
    });

    let parsedTier1 = null;
    if (tier1Content) {
      try {
        parsedTier1 = JSON.parse(tier1Content);
      } catch {
        parsedTier1 = tier1Content; // pass raw text if unparseable
      }
    }

    // --- TIER 2: Verification, Compression & Compliance Harness (Low Temperature: 0.1) ---
    const tier2HarnessPrompt = env.SYSTEM_PROMPT || DEFAULT_TIER2_DISTILLATION_HARNESS_PROMPT;

    const tier2Input = JSON.stringify({
      user_input: promptPayload,
      tier1_draft: parsedTier1 || undefined,
    });

    const tier2Content = await callOpenRouter(env, {
      model: upstreamModel,
      temperature: 0.1,
      max_tokens: 9000,
      messages: [
        { role: "system", content: tier2HarnessPrompt },
        { role: "user", content: tier2Input },
      ],
      title: "Prayer Distillation Engine - Tier 2",
    });

    if (!tier2Content) {
      return new Response(JSON.stringify({ error: "Empty or truncated model response" }), {
        status: 502,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
        },
      });
    }

    // Defensive normalization: guarantee pre-specified root invariants on wire output
    let finalOutput = tier2Content;
    try {
      const parsedFinal = JSON.parse(tier2Content);
      if (promptPayload.root && Array.isArray(parsedFinal.candidate_prayer_points)) {
        for (const card of parsedFinal.candidate_prayer_points) {
          if (card && typeof card === "object") {
            card.suggested_root = null;
            card.suggested_group = null;
          }
        }
        finalOutput = JSON.stringify(parsedFinal);
      }
    } catch {
      // Retain raw tier2Content if unparseable
    }

    return new Response(finalOutput, {
      status: 200,
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });

  } catch (err) {
    console.error("GUIDE_HANDLER_ERROR", err && err.stack ? err.stack : String(err));
    return new Response(JSON.stringify({ error: "Internal server error" }), {
      status: 500,
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });
  }
}
