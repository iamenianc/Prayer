export default {
  async fetch(request, env, ctx) {
    // 1. Handle CORS preflight
    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, X-Prayer-Gateway-Secret",
        },
      });
    }

    // 2. Only allow POST requests
    if (request.method !== "POST") {
      return new Response(JSON.stringify({ error: "Method not allowed" }), {
        status: 405,
        headers: { "Content-Type": "application/json" },
      });
    }

    // 3. Validate Gateway Secret (if configured)
    if (env.APP_GATEWAY_SECRET) {
      const clientSecret = request.headers.get("X-Prayer-Gateway-Secret");
      if (clientSecret !== env.APP_GATEWAY_SECRET) {
        return new Response(JSON.stringify({ error: "Unauthorized gateway request" }), {
          status: 401,
          headers: { "Content-Type": "application/json" },
        });
      }
    }

    // 4. Assemble System Prompt from fine named modules or monolithic fallback
    const namedModules = [
      env.PROMPT_PERSONA,
      env.PROMPT_INQUIRY_FLOW,
      env.PROMPT_THEOLOGY,
      env.PROMPT_TAXONOMY_PRIVACY,
      env.PROMPT_CARD_STYLE,
      env.PROMPT_OUTPUT_SCHEMA,
    ].filter(Boolean);

    const fallbackModules = [
      env.SYSTEM_PROMPT,
      env.SYSTEM_PROMPT_1,
      env.SYSTEM_PROMPT_2,
    ].filter(Boolean);

    const promptModules = namedModules.length > 0 ? namedModules : fallbackModules;

    if (promptModules.length === 0) {
      return new Response(JSON.stringify({ error: "Configuration error: System prompt environment variables are missing (PROMPT_PERSONA, PROMPT_THEOLOGY, etc.)" }), {
        status: 500,
        headers: { "Content-Type": "application/json" },
      });
    }

    const systemPrompt = promptModules.join("\n\n");

    try {
      const body = await request.json();

      // Support structured JSON payload or legacy user_input
      const initialReflection = body.initial_reflection || body.user_input;
      if (!initialReflection || typeof initialReflection !== "string" || initialReflection.trim().length === 0) {
        return new Response(JSON.stringify({ error: "Missing or empty initial_reflection (or user_input)" }), {
          status: 400,
          headers: { "Content-Type": "application/json" },
        });
      }

      const validRoots = ["PEOPLE", "GROUPS", "GENERAL"];
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

      // 5. Forward to OpenRouter using assembled system prompt
      const openRouterResponse = await fetch("https://openrouter.ai/api/v1/chat/completions", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${env.OPENROUTER_API_KEY}`,
          "Content-Type": "application/json",
          "HTTP-Referer": "https://prayer-app.local",
          "X-Title": "Prayer Distillation Engine",
        },
        body: JSON.stringify({
          model: "nvidia/nemotron-3.5-lightning",
          temperature: 0.2,
          max_tokens: 2500,
          response_format: { type: "json_object" },
          reasoning: {
            effort: "low",
          },
          provider: {
            data_collection: "deny",
          },
          messages: [
            { role: "system", content: systemPrompt },
            { role: "user", content: promptJsonString },
          ],
        }),
      });

      if (!openRouterResponse.ok) {
        return new Response(JSON.stringify({ error: "Upstream gateway processing failure" }), {
          status: 502,
          headers: { "Content-Type": "application/json" },
        });
      }

      const openRouterData = await openRouterResponse.json();
      const content = openRouterData.choices?.[0]?.message?.content;

      if (!content) {
        const choice = openRouterData.choices?.[0];
        return new Response(JSON.stringify({
          error: "Empty or truncated model response",
          finish_reason: choice?.finish_reason,
          has_reasoning: !!(choice?.message?.reasoning || choice?.message?.reasoning_content),
          usage: openRouterData.usage
        }), {
          status: 502,
          headers: { "Content-Type": "application/json" },
        });
      }

      return new Response(content, {
        status: 200,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
        },
      });

    } catch (err) {
      return new Response(JSON.stringify({ error: "Internal server error" }), {
        status: 500,
        headers: { "Content-Type": "application/json" },
      });
    }
  },
};
