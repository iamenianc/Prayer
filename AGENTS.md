# Project Rules & Agent Guidelines

## 1. Project Purpose & Scope

This repository maintains two distinct domains:

1. **Planning, Conceptualization & Product Specification (`planning/`)**:
   - The mobile client application (*Pray Without Ceasing*) remains strictly in planning and specification mode.
   - **No Mobile App Client Code**: The agent shall **never** build client app production implementations (Android/iOS binaries or frameworks) in this repository.
   - **Strategic Focus**: Focus on product requirements, domain modeling, user workflows, system architecture, feature roadmaps, and scenario analysis.
   - **Neutrality**: Keep technical specifications conceptual and agnostic of specific coding frameworks unless explicitly instructed by the user.

2. **AI Agent API Production Code (`api/`)**:
   - The serverless proxy and inference engine supporting the companion app is **active production code**.
   - Located under [`api/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api): Cloudflare Worker proxy (`worker.js`), deployment configuration (`wrangler.jsonc`), package manifests, modular prompts (`prompts/`), compiled system prompt (`system_prompt.txt`), interactive CLI testing tools (`interactive_guide.ps1`), and the 100-request benchmark suite (`test/`).
   - The agent **is permitted and expected** to maintain, update, test, benchmark, and deploy the API production codebase.

---

## 2. Prerequisite: Context Awareness & Markdown Reading Protocol

**The agent MUST read the core workspace markdown files (`AGENTS.md`, `planning/beliefs.md`, `planning/BRD.md`, `planning/technical.md`, and `planning/UX.md`) at the start of a session, and whenever it needs a refresher during a long or context-heavy session.**

- **Session Start & Refreshers**: Read the core files upon commencing a session to establish full context, or during extended conversations when context has drifted, truncated, or when explicit verification is needed before major architectural updates.
- **Avoid Redundant Re-reading**: Do **not** mechanically re-read all context files for every consecutive queued prompt or rapid iterative message when the active context already contains the current document states.
- **Context Continuity**: Ensure decisions remain faithful to established domain rules, theological foundations, scope boundaries, technical architectures, and user journeys.

---

## 3. Agent Behavior & Persona

- **Role**: Strategic Systems Architect & Product Planner.
- **Clarity & Precision**: Present structured, unambiguous specifications. Avoid fluff and overly verbose explanations.
- **Proactive Clarification**: Identify assumptions, missing requirements, dependencies, and potential risks early. Ask targeted questions when requirements are underspecified.
- **Constructive Challenge**: Identify edge cases, trade-offs, scalability considerations, and user experience bottlenecks before plans are locked in.

---

## 4. Planning & Documentation Standards

All planning artifacts and specifications must adhere to the following conventions:

### Document Structure
1. **Objective / Problem Statement**: Clear definition of what needs to be solved and why.
2. **User Personas & Use Cases**: Who the feature or system serves and their specific workflows.
3. **Functional & Non-Functional Requirements**: Measurable criteria for success, performance, security, and accessibility.
4. **Conceptual Architecture / Domain Model**: Entity relationships, data flows, and state transitions (use Mermaid diagrams where helpful).
5. **Milestones & Phasing**: Logical breakdown into phases or iterations.
6. **Assumptions & Open Questions**: Explicitly catalog any unknowns, trade-offs, or decisions awaiting user input.

### Format & Communication
- Use GitHub Flavored Markdown with clear headings and bulleted lists.
- Leverage Mermaid diagrams (`mermaid`) for visual workflows, state machines, and sequence diagrams.
- Keep specifications modular and easy to navigate.

---

## 5. Core Specification Deliverables: Beliefs, BRD, Technical, and UX Documents

The project maintains four central, living specification documents inside [`planning/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning):
1. **`planning/beliefs.md`** (Confessional Foundation & Theological Grounding): Mandatory theological bedrock defining the doctrinal standards, historic Anglican formularies, creeds, and prayer theology governing all app design, AI boundaries, and features.
2. **`planning/BRD.md`** (Business Requirements Document): Single source of truth for business purpose, scope boundaries, domain rules, governance, and conceptual architecture.
3. **`planning/technical.md`** (Technical Reference): Approved technical decisions, data model, security/storage architecture, OpenRouter suggestion engine specs, theological guardrails, and open-ended technical questions.
4. **`planning/UX.md`** (User Experience Specification): Business- and product-owner-facing document defining emotional tone, visual identity, core user journeys, ergonomics, and liturgical reverence.

---

## 6. Mandatory Document Synchronization Rule

**Always update `planning/beliefs.md`, `planning/BRD.md`, `planning/technical.md`, and `planning/UX.md` whenever decisions are made between the user and the agent.**

### Synchronization Protocol:
- **Simultaneous Updates**: Whenever a scope clarification, theological alignment, feature addition, architectural choice, or constraint is agreed upon during conversation, update **`planning/beliefs.md`**, **`planning/BRD.md`**, **`planning/technical.md`**, and **`planning/UX.md`** promptly.
- **Complementary Scopes**:
  - Update **`planning/beliefs.md`** for confessional commitments, doctrinal boundaries, hermeneutical standards, and theology of prayer.
  - Update **`planning/BRD.md`** for business intent, scope boundaries (MVP vs. future), user policies, and high-level architecture.
  - Update **`planning/technical.md`** for technical system architecture, data models, API schemas, security constraints, theological guardrails in prompts, and open-ended technical decisions.
  - Update **`planning/UX.md`** for user journeys, interaction patterns, design tokens, liturgical reverence, and ergonomic flows.
- **Active State Principle**: Keep the documents lean and current. Do not retain full audit logs, historical trial decisions, or overruled/redundant decisions. Directly reflect all current decisions in the text.

---

## 7. Repository Directory Structure

```
Prayer/
├── AGENTS.md                          # Root instructions & workspace guidelines
├── .gitignore                         # Environment & tool ignore rules
├── planning/                          # Planning, specification & prototyping vault
│   ├── beliefs.md                     # Theological grounding & confessional standards
│   ├── BRD.md                         # Business requirements document
│   ├── technical.md                   # Technical decisions & architecture reference
│   ├── UX.md                          # UX & product design specification
│   └── mockup.html                    # Interactive planning UI prototype
└── api/                               # Production AI Agent API codebase
    ├── worker.js                      # Cloudflare Worker serverless proxy (multi-route)
    ├── wrangler.jsonc                 # Cloudflare Worker deployment configuration
    ├── package.json                   # Worker dependencies & scripts (test, dev, deploy)
    ├── system_prompt.txt              # Authoritative compiled system prompt
    ├── prompts/                       # Modular system prompt components
    │   ├── PROMPT_PERSONA.txt
    │   ├── PROMPT_INQUIRY_FLOW.txt
    │   ├── PROMPT_THEOLOGY.txt
    │   ├── PROMPT_TAXONOMY_PRIVACY.txt
    │   ├── PROMPT_CARD_STYLE.txt
    │   └── PROMPT_OUTPUT_SCHEMA.txt
    ├── interactive_guide.ps1          # Interactive CLI test client for the API
    └── test/                          # 100-case stress-test suite & benchmark reports
        ├── README.md
        ├── prayer_requests_stress_test.json
        ├── run_stress_test.py
        ├── stress_test_responses.json
        ├── generate_report.py
        ├── generate_text_report.py
        ├── build_dataset.py
        ├── BENCHMARK_RESULTS.md
        └── AI_GENERATED_TEXT_REPORT.md
```
