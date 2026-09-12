/**
 * Prayer AI Agent API — Cloudflare Worker Serverless Proxy
 * 
 * Provides secure, anonymous inference access for the mobile prayer companion.
 * Endpoints:
 *   - POST /api/v1/suggest: Ambient background prayer suggestions.
 *   - POST /api/v1/guide (and POST /): Multi-turn theological distillation engine.
 *   - GET  /health (and GET /): Edge proxy health check and route discovery.
 *   - OPTIONS: CORS preflight for all endpoints.
 */


// --- AUTHORITATIVE TWO-TIER SYSTEM PROMPTS ---

const DEFAULT_TIER1_SUGGEST_PROMPT = `You are a reverent, thoughtful prayer writer grounded in historic Reformed Christian theology.
Your role is to examine the recorded context for a prayer target and craft warm, flowing, and natural prayer intentions organized into three groups:
1. Praise God: Adoration of God's holy character, sovereignty, majesty, and steadfast love.
2. Thank God: Thanksgiving for His providential care, past answers, spiritual blessings, and specific mercies noted in context.
3. Ask God: Humble petitions for grace, wisdom, endurance, spiritual fruitfulness, and godly conduct.

RULES:
- THREE GROUPS: Output candidate points strictly categorized into "praise_god", "thank_god", and "ask_god".
- AT LEAST ONE PER GROUP: Provide at least 1 point in praise_god, at least 1 in thank_god, and at least 1 in ask_god.
- THANKSGIVING FROM DATA RULE: If there is no obvious good thing or blessing to thank God for from the data, create strictly only ONE point as a maximum in "thank_god".
- TOTAL POINTS: Strictly between 3 and 12 points total across all three groups combined.
- ALLOWABLE WORD LIMIT (RANGE OF 4 TO 15 WORDS): Each point must strictly be between 4 and 15 words long (never fewer than 4 words, never exceeding 15 words).
- AVOID WISHY-WASHY GENERAL PLATITUDES: Strictly eliminate vague, generic, or sentimental platitudes that could apply to anyone at any time (e.g., avoid vacuous phrasing like "For peace and joy", "That things get better", "For blessings upon them", "Because God is good", "A peaceful day"). Every prayer intention must be substantive, purposeful, and tethered to genuine spiritual or circumstantial reality.
- CONCRETE GROUNDING & SPECIFICITY: Anchor each point specifically and deeply in the actual recorded context, relationships, specific trials, burdens, or answered notes provided. Reflect the distinct substance of the person or topic rather than defaulting to interchangeable pious generalities. If context is sparse, draw on specific biblical virtues, doctrines, or vocational duties appropriate to the subject (e.g., endurance under pressure, spiritual discernment, bold gospel proclamation, steadfast love), but NEVER invent medical illnesses or unstated tragedies.
- COMPLETE, FINISHED THOUGHTS ONLY (NEVER CUT OFF): Every suggestion MUST be a 100% complete, fully finished grammatical thought. NEVER cut off mid-thought, leave an incomplete clause, or end abruptly on a preposition, conjunction, or article (never end on words such as 'and', 'or', 'in', 'to', 'for', 'with', 'that', 'of', 'on', 'at', 'the', 'a', 'an'). Target 6 to 12 words so that the full thought comfortably finishes within 15 words and has at least 4 words.
- WARM, NATURAL DEVOTIONAL CADENCE (NOT TERSE): Avoid clipped, staccato, or robotic shorthand (no 1 to 3 word fragments). Use graceful, melodious phrases that feel prayerful and reverent rather than cold bullet fragments.
- MANDATORY OPENING WORDS: EVERY single point MUST start with "For", "That", "A" (or "An"), or "Because".
  * Grammatical patterns (for syntactic illustration of opening words only):
    - "For [divine attribute, gift, or mercy]"
    - "That [person or situation may experience grace, wisdom, or peace]"
    - "A [reverent request for spiritual fruit or posture]"
    - "Because [theological reality or promise of God]"
- CRITICAL ANTI-OVERFITTING DIRECTIVE: NEVER copy, borrow, or mimic the words, themes, or scenarios from any examples in this prompt or past templates (e.g., do NOT mention cancer, illness, surgery, restructuring, or specific trials unless explicitly written in the user's recorded points). All suggestions MUST be uniquely and freshly derived from the user's actual target context.
- NO VERBATIM PARROTING: Do NOT merely parrot, echo, or copy-paste the user's input text verbatim. Synthesize the underlying spiritual need and reframe it with fresh, warm, biblical language.
- ABSOLUTELY NO CHAT OR QUESTIONS: Never ask questions. Never write conversational responses.
- Never use prefixes like "Pray for", "Please pray", or "Ask God to".
Output strictly valid JSON with no conversational text:
{
  "praise_god": [
    "<substantive praise point 4 to 15 words starting with For/That/A/Because>",
    "<substantive praise point 4 to 15 words starting with For/That/A/Because>"
  ],
  "thank_god": [
    "<substantive thanksgiving point 4 to 15 words starting with For/That/A/Because>"
  ],
  "ask_god": [
    "<substantive petition point 4 to 15 words starting with For/That/A/Because>",
    "<substantive petition point 4 to 15 words starting with For/That/A/Because>"
  ]
}`;

const DEFAULT_TIER2_SUGGEST_HARNESS_PROMPT = `You are the Verification, Graceful Phrasing, and Compliance Harness for Ambient Prayer Suggestions.
Review the target context and Tier 1 candidate suggestions. Output a refined list of suggestions organized into three groups: "praise_god", "thank_god", and "ask_god", complying with ALL rules:
1. THREE GROUPS:
   - "praise_god": Praising God's character, holiness, and sovereignty.
   - "thank_god": Thanksgiving for His blessings, provision, and answered prayers in context.
   - "ask_god": Humble petitions for grace, spiritual endurance, wisdom, and guidance.
2. AT LEAST ONE PER GROUP (THANKSGIVING CEILING): Output at least 1 point in praise_god, at least 1 in thank_god, and at least 1 in ask_god. If there is no obvious good thing or blessing to thank God for from the data, strictly create only 1 thanks point as a maximum.
3. TOTAL RANGE: Strictly between 3 and 12 total points across the three groups combined.
4. STRICT LENGTH & COMPLETION (4 TO 15 WORDS): Strictly between 4 and 15 words per suggestion (never fewer than 4 words, never exceeding 15 words).
5. COMPLETE, UNTRUNCATED THOUGHTS ONLY: Every suggestion must be a 100% complete, fully finished grammatical thought. NEVER truncate, chop, or leave a sentence hanging mid-thought. NEVER end on a preposition, conjunction, or article (such as 'and', 'or', 'in', 'to', 'for', 'with', 'that', 'of', 'on', 'at', 'the', 'a', 'an'). If a Tier 1 candidate is longer than 15 words, fewer than 4 words, or cut off, REWORD AND ADJUST IT into a complete, finished sentence of 4–15 words. Never blindly drop the ending of a sentence.
6. AVOID WISHY-WASHY GENERAL PLATITUDES: Purge vague, sentimental, or interchangeable platitudes (e.g. 'For peace and joy', 'That things improve', 'Because God is good', 'For general blessings'). Ensure every suggestion is substantive, concrete, purposeful, and deeply grounded in the context.
7. AVOID TERSE SOUNDING PHRASES: Suggestions must NOT sound clipped or robotic (strictly no 1–3 word fragments). Keep them warm, reverent, and melodious.
8. MANDATORY OPENING WORDS: EVERY single suggestion MUST start with "For", "That", "A" (or "An"), or "Because".
9. NO OVERFITTING TO PROMPT EXAMPLES: NEVER copy, borrow, or parrot words or themes from examples (e.g., do NOT mention cancer, remission, or specific illnesses unless explicitly present in the target context). Ground points strictly in the user's recorded context.
10. NO VERBATIM PARROTING: Suggestions must NOT merely parrot or echo the user's input words verbatim. Reframe the spiritual essence into fresh, flowing, reverent biblical expressions.
11. NO DIRECT PRAYER: NEVER write second-person prayers addressed to God (NO "Lord", "Father", "God", "we pray", "give them"). Output objective petitions/intentions only.
12. NO PREFIXES: Never begin with "Pray for", "Prayer for", "Please pray", "Ask God to", or bullet symbols.
13. STRICT NON-FABRICATION: Faithful to recorded facts only; never invent unstated medical crises or circumstances.
14. DIALECT: English (Australian / UK) spelling unless US is specified.
15. OUTPUT FORMAT: Output STRICTLY valid JSON with no conversational text:
{
  "praise_god": [
    "<refined substantive praise point 4 to 15 words starting with For/That/A/Because>",
    "<refined substantive praise point 4 to 15 words starting with For/That/A/Because>"
  ],
  "thank_god": [
    "<refined substantive thanksgiving point 4 to 15 words starting with For/That/A/Because>",
    "<refined substantive thanksgiving point 4 to 15 words starting with For/That/A/Because>"
  ],
  "ask_god": [
    "<refined substantive petition point 4 to 15 words starting with For/That/A/Because>",
    "<refined substantive petition point 4 to 15 words starting with For/That/A/Because>"
  ]
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
          suggest: "POST /api/v1/suggest",
          assistant: "POST /api/v1/assistant (or POST /api/v1/guide, POST /)",
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
    if (path === "/api/v1/suggest") {
      return handleSuggestionGeneration(request, env);
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
    const upstreamModel = env.OPENROUTER_MODEL || "openai/gpt-5.6-luna";

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

/**
 * Handle POST /api/v1/suggest — Ambient Background Prayer Suggestions
 * Accepts batch target context, runs Two-Tier drafter & compliance harness,
 * returns 3-12 points partitioned into Praise God, Thank God, and Ask God,
 * with max 10 words per line, grounded strictly in user records.
 */
async function handleSuggestionGeneration(request, env) {
  try {
    const body = await request.json();
    const targetName = body.target_name || null;
    const root = body.root || "GENERAL";
    const contextDescription = body.context_description || "";
    const recordedPoints = Array.isArray(body.recorded_points) ? body.recorded_points : [];
    const journalUpdates = Array.isArray(body.journal_updates) ? body.journal_updates : [];
    const currentDraft = body.current_draft || "";
    const locale = body.locale_dialect || "EN_AU_UK";

    const promptPayload = {
      target_name: targetName,
      root: root,
      context_description: contextDescription,
      recorded_points: recordedPoints.slice(0, 10),
      journal_updates: journalUpdates.slice(0, 5),
      current_draft: currentDraft.slice(0, 500),
      locale: locale,
    };

    const upstreamModel = env.OPENROUTER_MODEL || "openai/gpt-5.6-luna";

    // --- TIER 1: Grounded Petition Ideas Drafter (Temperature: 0.8, Top_P: 0.95) ---
    const tier1Content = await callOpenRouter(env, {
      model: upstreamModel,
      temperature: 0.8,
      top_p: 0.95,
      max_tokens: 3000,
      messages: [
        { role: "system", content: env.PROMPT_SUGGEST_TIER1 || DEFAULT_TIER1_SUGGEST_PROMPT },
        { role: "user", content: JSON.stringify(promptPayload) },
      ],
      title: "Prayer Suggestions - Tier 1",
    });

    let tier1Candidates = { praise_god: [], thank_god: [], ask_god: [] };
    if (tier1Content) {
      try {
        const parsed = JSON.parse(tier1Content);
        if (parsed) {
          if (Array.isArray(parsed.praise_god)) tier1Candidates.praise_god = parsed.praise_god;
          if (Array.isArray(parsed.thank_god)) tier1Candidates.thank_god = parsed.thank_god;
          if (Array.isArray(parsed.ask_god)) tier1Candidates.ask_god = parsed.ask_god;
          // Legacy support if model returned flat candidates array
          if (Array.isArray(parsed.candidates) && tier1Candidates.ask_god.length === 0) {
            tier1Candidates.ask_god = parsed.candidates;
          }
        }
      } catch {}
    }

    // --- TIER 2: Verification, Brevity & Compliance Harness (Temperature: 0.1) ---
    const tier2Content = await callOpenRouter(env, {
      model: upstreamModel,
      temperature: 0.1,
      max_tokens: 3000,
      messages: [
        { role: "system", content: env.PROMPT_SUGGEST_TIER2 || DEFAULT_TIER2_SUGGEST_HARNESS_PROMPT },
        { role: "user", content: JSON.stringify({ context: promptPayload, candidates: tier1Candidates }) },
      ],
      title: "Prayer Suggestions - Tier 2",
    });

    let resultGroups = { praise_god: [], thank_god: [], ask_god: [] };
    if (tier2Content) {
      try {
        const parsed = JSON.parse(tier2Content);
        if (parsed) {
          if (Array.isArray(parsed.praise_god)) resultGroups.praise_god = parsed.praise_god;
          if (Array.isArray(parsed.thank_god)) resultGroups.thank_god = parsed.thank_god;
          if (Array.isArray(parsed.ask_god)) resultGroups.ask_god = parsed.ask_god;
          // Legacy support if model returned flat suggestions
          if (Array.isArray(parsed.suggestions) && resultGroups.ask_god.length === 0) {
            resultGroups.ask_god = parsed.suggestions;
          }
        }
      } catch {}
    }

    // Defensive sanitization: ensure complete thoughts, valid starters, 4-15 words, and no dangling endings
    const DANGLING_ENDINGS = /\b(and|or|nor|but|yet|so|in|into|to|unto|for|with|within|without|that|which|who|whom|whose|of|off|on|onto|at|by|from|as|about|regarding|during|through|throughout|over|under|upon|against|among|between|the|a|an|his|her|their|our|my|its|your|this|these|those)\b$/i;
    const VALID_STARTERS = /^(for|that|a|an|because)\b/i;

    function finalizeSuggestion(rawText, maxWords = 15, minWords = 4) {
      if (!rawText || typeof rawText !== "string") return null;

      // 1. Strip leading bullets, numbers, quotes, dashes, or whitespace
      let text = rawText.replace(/^[\s•\-\*"'0-9.)]+/, "").replace(/["']$/, "").trim();
      if (!text) return null;

      // 2. Strip any trailing ellipses or cut-off dashes
      text = text.replace(/(\.{2,}|…|--|-)$/, "").trim();

      // 3. Remove trailing punctuation marks that precede trimming
      text = text.replace(/[,;:]+$/, "").trim();

      // 4. Split into words
      let words = text.split(/\s+/).filter(Boolean);
      if (words.length === 0) return null;

      // 5. If word count exceeds maxWords, intelligently find a complete clause or trim safely
      if (words.length > maxWords) {
        // Check if there is a natural clause break (comma, semicolon, dash) within [minWords, maxWords] words
        const clauseMatch = text.match(/^([^,;—–-]+)[,;—–-]/);
        if (clauseMatch) {
          const clauseWords = clauseMatch[1].trim().split(/\s+/).filter(Boolean);
          if (clauseWords.length >= minWords && clauseWords.length <= maxWords) {
            words = clauseWords;
          }
        }

        // If still > maxWords, slice to maxWords and recursively strip dangling prepositions/conjunctions
        if (words.length > maxWords) {
          let trimmedWords = words.slice(0, maxWords);
          while (trimmedWords.length > minWords && DANGLING_ENDINGS.test(trimmedWords[trimmedWords.length - 1])) {
            trimmedWords.pop();
          }
          words = trimmedWords;
        }
      }

      // 6. Ensure no dangling connectors or prepositions at the end
      while (words.length > minWords && DANGLING_ENDINGS.test(words[words.length - 1])) {
        words.pop();
      }

      // 7. Ensure valid starting word (For, That, A, An, Because)
      if (words.length > 0 && !VALID_STARTERS.test(words[0])) {
        words.unshift("For");
        if (words.length > maxWords) {
          words.pop();
          while (words.length > minWords && DANGLING_ENDINGS.test(words[words.length - 1])) {
            words.pop();
          }
        }
      }

      // 8. Enforce minimum word count of 4 words
      if (words.length < minWords) {
        return null;
      }

      let result = words.join(" ").replace(/[,;:\s]+$/, "").trim();
      return result.length > 0 ? result : null;
    }

    const sanitizeList = (list) =>
      (list || [])
        .map((s) => finalizeSuggestion(String(s), 15, 4))
        .filter(Boolean);

    let praiseGod = sanitizeList(resultGroups.praise_god);
    let thankGod = sanitizeList(resultGroups.thank_god);
    let askGod = sanitizeList(resultGroups.ask_god);

    // Ensure at least one per group by falling back if missing
    const fallbacks = fallbackGroupSuggestions(root, targetName);
    if (praiseGod.length === 0) praiseGod = [fallbacks.praise_god[0]];
    if (thankGod.length === 0) thankGod = [fallbacks.thank_god[0]];
    if (askGod.length === 0) askGod = [fallbacks.ask_god[0]];

    // Bounding total points to range 3 to 12
    let total = praiseGod.length + thankGod.length + askGod.length;
    while (total > 12) {
      if (askGod.length > 1) {
        askGod.pop();
      } else if (thankGod.length > 1) {
        thankGod.pop();
      } else if (praiseGod.length > 1) {
        praiseGod.pop();
      } else {
        break;
      }
      total = praiseGod.length + thankGod.length + askGod.length;
    }

    const allSuggestions = [...praiseGod, ...thankGod, ...askGod];

    return new Response(JSON.stringify({
      praise_god: praiseGod,
      thank_god: thankGod,
      ask_god: askGod,
      suggestions: allSuggestions,
    }), {
      status: 200,
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });
  } catch (err) {
    console.error("SUGGEST_HANDLER_ERROR", err && err.stack ? err.stack : String(err));
    const fallbacks = fallbackGroupSuggestions("GENERAL", null);
    return new Response(JSON.stringify({
      praise_god: fallbacks.praise_god,
      thank_god: fallbacks.thank_god,
      ask_god: fallbacks.ask_god,
      suggestions: [...fallbacks.praise_god, ...fallbacks.thank_god, ...fallbacks.ask_god],
    }), {
      status: 200,
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });
  }
}

function fallbackGroupSuggestions(root, targetName) {
  if (root === "MISSION_PARTNERS") {
    return {
      praise_god: [
        "For the Lord's sovereign dominion over all nations and peoples",
      ],
      thank_god: [
        "For faithful gospel proclamation and open doors for biblical truth",
        "That the scriptures are reaching unreached communities",
      ],
      ask_god: [
        "For fruitful gospel ministry and perseverance in difficult trials",
        "That ministry team workers remain united in Christ",
        "A steadfast spirit of courage in the mission field",
      ],
    };
  } else if (root === "GROUPS") {
    return {
      praise_god: [
        "For Christ the cornerstone and head of His gathered church",
      ],
      thank_god: [
        "For mutual love and faithful fellowship in the community",
        "That believers are growing together in grace",
      ],
      ask_god: [
        "For steadfast growth in Christ and grace amidst disagreement",
        "That our community reflects Christ as salt and light",
        "A spirit of humble service among one another",
      ],
    };
  } else if (root === "PEOPLE") {
    return {
      praise_god: [
        "For His steadfast love shown in Christ",
        "Because God is faithful in all His promises",
      ],
      thank_god: [
        "For His daily mercies and sustaining presence each morning",
        "That the Lord hears and answers our earnest prayers",
      ],
      ask_god: [
        "For wisdom, godly discernment, and comfort in distress",
        "That he may experience the deepened peace of Christ",
        "A steadfast heart anchored in God's holy word",
      ],
    };
  } else {
    return {
      praise_god: [
        "For Almighty God sovereign over all human history",
      ],
      thank_god: [
        "For His enduring patience and common grace toward all creation",
        "That the light of the gospel shines in darkness",
      ],
      ask_god: [
        "For righteousness, justice, and gospel peace across our land",
        "A renewed reverence for God's holy word today",
        "That the church remains faithful amidst cultural pressures",
      ],
    };
  }
}

function fallbackSuggestions(root, targetName) {
  const groups = fallbackGroupSuggestions(root, targetName);
  return [...groups.praise_god, ...groups.thank_god, ...groups.ask_god];
}

