# Business Requirements Document (BRD) — Prayer App

**Status:** Active Approved Draft  
**Last Updated:** 2026-09-07  
**Document Owner:** Planning & Architecture Team  

---

## 1. Application Purpose

### 1.1 Vision & Value Proposition
Deliver an intimate, distraction-free, mobile-first prayer companion that cleanly separates the contemplative act of prayer from the administrative task of organizing petitions. The application opens to a pristine, blank canvas offering strictly two choices—**Start praying** and **Log prayer points**—grounded in historic Reformed theology and supported by an objective, neutral suggestion engine provided free of charge.

### 1.2 Problem Statement
Many believers struggle with consistency, distraction, and cognitive overload during prayer. Traditional note-taking apps lack prayer lifecycles, while broad social or spiritual apps often introduce gamification (streaks, badges, confetti) and commercialized community feeds that compromise privacy and reverence. Furthermore, when entering prayer, users are often greeted with complex dashboards and task lists that trigger administrative fatigue rather than contemplative focus.

### 1.3 Target Audience & Scale Constraint
- **Target Scale**: Strictly personal and private distribution. Lifetime user base is explicitly constrained to **fewer than 100 users**.
- **Individual Believers**: Seekers of a quiet, private discipline for focused personal prayer.
- **Relational Prayer Intercessors**: Believers actively holding petitions for specific individuals, family members, ministry groups, and global concerns.

---

## 2. Application Scope

### 2.1 Platform Scope
- **Mobile Only**: Initial launch focused exclusively on **Android** (compatible with standard Android and de-googled environments such as GrapheneOS).
- **Portability Mandate**: Core domain and architecture must guarantee seamless future portability to **iOS**.
- **Independent from Vendor Attestation**: Explicitly free from dependencies on Google Play Integrity API or Apple DeviceCheck.
- **Desktop / Web**: Strictly out of scope.

### 2.2 Core In-Scope Features (MVP)

1. **Pristine Home Screen & Gestural Navigation**:
   - The app launches to an uncluttered blank canvas featuring strictly two centered flat geometric tile options with sharp 90-degree right angles and no curved edges:
     1. **Start praying**
     2. **Log prayer points**
   - A **swipe-left gesture** reveals the structural directory and ledger.

2. **The Three Foundational Roots (`People`, `Groups`, `General`)**:
   - **`People`**: Exclusively and strictly specific, distinct individual human relationships (e.g., spouse, parent, child, a single named friend/neighbor, and personal petitions under *Me*—including personal health, job trials, or sanctification situated within a workplace, school, or hospital). Plural or collective peer environments (such as coworkers or classmates as a unit) belong under `Groups`.
   - **`Groups`**: Collectives, communities, and shared peer/work environments (e.g., work colleagues, office team, church congregation, small groups, committees, ministries).
   - **`General`**: Broad topics, global matters, societal needs, and historic liturgical prayers.

3. **Contemplative, Passive Prayer Flow ("Start Praying")**:
   - **Zero Friction**: Direct transition into a curated queue without requiring pre-session filtering.
   - **Card Focus with Configurable Batches (Flat Tessellated Tiles)**: Displays prayer points as completely flat rectangular tiles with sharp 90-degree edges (strictly zero curved edges or rounded corners). Displayed one card at a time by default, with user settings to show 1, 2, 3, 4, or 5 items per view tessellated in a geometric grid.
   - **Strictly Passive / Read-Only**: Zero buttons, checkmarks, editing tools, or task-completion toggles during prayer. Viewing an item silently updates its `last_prayed_at` timestamp.
   - **Self-Paced & Open-Ended**: Each screen is a separate prayer. Users decide how many prayers to pray and simply stop when they are ready.
   - **Zero-State Fallback (Historic Reformed Prayers)**: When the local database contains no user-logged prayers, the app draws from preloaded classic **Reformed, Protestant** prayers (The Lord's Prayer, classic Anglican Book of Common Prayer collects, and the Apostles' Creed). Once personal prayers exist, these reside under `General → Historic Prayers` with a toggle to blend into daily rotations.

4. **Two-Pathway Logging ("Log Prayer Points")**:
   - **Record a prayer point (Direct Capture with Intelligent AI Filing)**: Immediate empty text pad for direct entry of an already-formed prayer point. Uses AI assistance to automatically infer and file the prayer point into the correct root category (`People`, `Groups`, or `General`) and group context without multi-turn questioning. Entity names must be masked on-device for privacy, so entity name suggestions from the AI are strictly not required; entity resolution and assignment occur locally on-device within the vault, with one-tap user confirmation or override.
   - **"Guide me" (Assisted Articulation & Distillation)**: An objective, structured tool to help users articulate tangled or heavy thoughts:
     - **App Auto-Conversion to JSON Payload**: The mobile client automatically formats its session state into a structured JSON payload (`initial_reflection`, optional pre-specified `root` and `group`, `clarifying_question`, `user_response`, `request_more`) sent in the prompt over the wire.
     - **Step 1 (Open Heart)**: Prompt (*"Who or what is on your heart?"*) with open text canvas. If initiated from within an existing directory folder or entity view, the `root` and `group` are pre-specified.
     - **Step 2 (Neutral Distillation)**: Asks clarifying questions **1 at a time** in **neutral, concise language** (not a therapy bot). Clarifying inquiry is default-mandatory on the initial turn. **Inquiry When Actionable Point Not Obvious**: If a clear actionable point is not obvious, the engine is strictly prohibited from generating candidate prayer points; it must set `skip_question: false` and formulate strictly one concise question (6–12 words) in plain, natural English without bureaucratic templates. It distinguishes between internal emotional states (asking plainly what is causing the feeling, e.g., *"What is making you feel anxious right now?"*) and external entities/topics (asking plainly what is happening, e.g., *"What is going on with your boss that you'd like to pray about?"*). The `skip_question` binary flag may only bypass questioning when the initial reflection is already clear and articulate, or when immediate points are explicitly requested. Questions are strictly **open-ended**, expecting the user to supply data rather than offering leading suggestions; when the user presents multiple competing crises simultaneously, the engine performs concise burden triage (*"Which of these is weighing on you most heavily right now?"*). Hard-capped at max 2 turns. **Unconditional Question Skipping**: The user can skip any question the app asks at any point; skipping a question immediately guarantees that no more questions are asked during that session, proceeding directly to Step 3 candidate prayer points.
     - **Step 3 (Commitment & Category Bypass)**: Presents strictly **2 candidate prayer points** (never 1, never 3). Grounded strictly in user-supplied facts; the engine must never fabricate or assume unstated medical illnesses, hospitalizations, or tragedies. **Pre-specified Category Bypass**: When `root` (and optionally `group`) is prespecified by the user upon entry, category suggestion is **not needed**; candidate points tailor directly to that declared context, and `suggested_root` / `suggested_group` return `null`. When `root` is not pre-specified, candidate points map to inferred root categories and optional group context. Entity names must be masked on-device for privacy, so entity name suggestions from the AI are strictly not required; target entity binding occurs locally on-device. All candidate points in a session consolidate under the identical root category. Users are permitted a strictly one-time action to request **2 additional suggestions** (hard ceiling of 4 suggestions per session). Governed by simple controls: **"Save"**, **"Suggest 2 more"** (one-time only), **"Back"**, and **"Cancel"**.

5. **Free AI Suggestion Engine Provisioning & Budget Ceiling**:
   - The AI service is provided **free of charge** to users.
   - **Operational Budget Ceiling**: Hard limit of **maximum $5.00 / month** total operational expenditure.
   - **Infrastructure**: Hosted on Cloudflare Workers Free Tier ($0.00 / month).
   - **Zero Account Friction**: No login, no user registration, and no user-supplied API key required.
   - Orchestrated via a serverless proxy holding the master API key securely server-side with hard spending caps.

6. **Granular Prayer Point Lifecycle**:
   - Independent states per prayer point: `Active`, `Answered` (with resolution timestamp and testimony note), and `Archived`.
   - Answered prayers are displayed in the directory with a subtle strikethrough.

7. **Language & Dialect Configuration (Default: English Australian/UK; Option: US English)**:
   - The application defaults to **English (Australian / UK)** (`en-AU` / `en-GB`) across all UI strings, system copy, and preloaded liturgical prayers (e.g., *saviour*, *honour*, *neighbour*).
   - A dedicated configuration option in settings allows selecting **US English** (`en-US`).
   - The dialect preference applies across all application copy and informs the AI suggestion engine to produce questions and candidate prayer points in the configured dialect.

### 2.3 Post-MVP / Future Milestones
- Native iOS client release.
- Encrypted local file export and restore (`.prayerbackup`).
- Local voice dictation for prayer point logging.

### 2.4 Explicitly Out of Scope
- **User Accounts & Cloud Sync**: Strictly zero remote databases, user accounts, or cloud synchronization.
- **Social Features & Public Sharing**: No community feeds, social sharing, public comments, or external profiling.
- **Gamification**: No streaks, badges, points, or celebratory animations.
- **Vendor Lock-in**: No dependency on proprietary store frameworks (Google Play Services / Apple DeviceCheck).

---

## 3. Application & Domain Rules

### 3.1 Privacy & Local-First Persistence Rules
- **100% Offline Vault**: All prayer points, relationships, updates, and answered testimonies reside exclusively on the physical device file system.
- **Embedded Database Encryption**: Persisted via embedded **SQLite (SQLCipher)** encrypted with keys managed by platform hardware keystores (Android Keystore / iOS Keychain).
- **Zero Data Mining**: Prayers are never analyzed for advertising, profiling, or foundational model training.
- **Privacy Gate & Entity Masking**: Raw personal identifiers and entity names must be masked or generalized on-device before text is processed by the AI proxy. Consequently, entity name suggestions from the AI are strictly not required and omitted from the AI contract; entity resolution and assignment occur entirely on-device within the encrypted local vault.
- **Zero Credential Exposure**: The upstream API key is never bundled into, referenced by, or retrievable from the mobile application code or binary packages. It resides exclusively in isolated, encrypted cloud secrets on the serverless proxy.

### 3.2 Tone & Language Guardrails
- **Non-Therapeutic AI**: The suggestion engine must never mimic a human pastor, counselor, or conversational companion. Artificial empathy, conversational pleasantries, and pseudo-psychological validation are strictly forbidden.
- **Objective Petitions, Never Scripted Prayers**: The suggestion engine must never compose actual prayers or address God directly (e.g., never output "Father God...", "Dear Lord...", "Lord Jesus...", "Thy will be done", or second-person invocations to God). Believers pray themselves; the engine strictly summarizes the petition, burden, or thanksgiving into an objective prayer point.
- **Strict Suggestion Count & One-Time Expansion**: The candidate generation step must always provide strictly and exactly 2 prayer point suggestions per turn (never 1, never 3). Users are permitted a strictly one-time request for 2 additional suggestions (hard ceiling of 4 suggestions total per session).
- **Linguistic Precision**: The copy must remain direct, dignified, and free from pretentious or pseudo-spiritual jargon.
- **Concise Phrasing & Zero Boilerplate**: Candidate titles and descriptions avoid redundant prefixes (e.g., "Pray for...", "Pray that...", "Ask God to..."). Since every item is inherently a prayer point, entries state the specific need, person, or thanksgiving directly.
- **Telegraphic AI Output Convention & Strict Length Ceilings**: AI suggestions employ a compact shorthand tailored for mobile prayer cards:
  - *Strict Word Limits*: Titles are strictly capped at **2–6 words** (targeting 2–4 words, hard ceiling of 6 words). Descriptions are strictly written in concise telegraphic shorthand, hard-capped at **maximum 20–25 words** (never lengthy multi-sentence paragraphs).
  - *Preferred Structure (2-to-3 Clause Semicolon Pattern)*: Uses strictly 2 to 3 compact clauses separated by semicolons (;) to divide distinct petition facets (Clause 1: immediate need/action; Clause 2: heart posture/spiritual fruit; Clause 3: submission to God's sovereign will/peace) for optimal mobile card scannability.
  - *Single Focus*: Each card focuses on a single specific burden rather than compounding multiple disparate petitions.
  - *Abbreviations & Symbols*: Common keyboard symbols (e.g., `→` for leads to/resulting in, `↑` for increase/growth, `↓` for decrease/relief, `&` for and) and standard abbreviations (e.g., `govt` for government, `eg` for example). The abbreviation `w/` or `/w` is strictly prohibited (must write out "with" or omit the preposition).
  - *Omission of Fillers*: Stripping grammatically necessary but low-information words like articles (*a*, *an*, *the*), auxiliary verbs (*is*, *are*), and loose connectives.
  - *Telegraphic Style*: Punchy phrases focused strictly on core nouns and verbs.
- **Geometric & Tessellated UI Architecture (Zero Curved Edges)**: The user interface is strictly composed of flat, non-skeuomorphic, planar tiles with sharp 90-degree right angles. Rounded corners, curved pill buttons, bubble cards, drop shadows, and circular frames are strictly forbidden. All on-screen elements (such as batched prayer cards, buttons, or suggestion tiles) tessellate seamlessly along crisp hairline dividers.
- **Dialect & Orthography Standards**: System copy, preloaded historic prayers, and AI outputs adhere to **English (Australian / UK)** by default, switching consistently to **US English** when configured by the user.
- **Modular Prompt Deployment**: To comply with serverless execution constraints (e.g., Cloudflare Workers 5.1 kB text binding ceiling) while optimizing attention primacy, prompt directives are decomposed into fine modules assembled in sequence: `PROMPT_PERSONA` (identity & First Principle), `PROMPT_INQUIRY_FLOW` (control loop & inquiry), `PROMPT_THEOLOGY` (doctrinal & comfort boundaries), `PROMPT_TAXONOMY_PRIVACY` (root mapping & entity masking), `PROMPT_CARD_STYLE` (card brevity & shorthand), and `PROMPT_OUTPUT_SCHEMA` (JSON structure).
- **Benchmarked Stress-Testing Battery**: The suggestion engine is subjected to a standardized 100-request evaluation suite ([`test/prayer_requests_stress_test.json`](file:///c:/Users/ianch/sourcecode/repos/Prayer/test/prayer_requests_stress_test.json)) spanning all wordiness tiers (5–253 words), diverse perspectives, theological boundaries, and root categories to guarantee mobile card brevity and confessional fidelity under stress, with comprehensive evaluation recorded in [`test/BENCHMARK_RESULTS.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/test/BENCHMARK_RESULTS.md) and detailed linguistic analysis in [`test/AI_GENERATED_TEXT_REPORT.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/test/AI_GENERATED_TEXT_REPORT.md).

---

## 4. High-Level Conceptual Architecture

```mermaid
graph TD
    subgraph Mobile Client (Offline-First)
        EntryUI["Blank Entry Screen (Pray | Log)"]
        ActivePrayerUI["Passive Prayer Flow (Card Focus)"]
        LoggingUI["Log Flow (Record Point | 'Guide me')"]
        DirectoryUI["Swipe-Left Directory (People | Groups | General)"]
        LocalDB[("Encrypted Local SQLite Database<br/>(SQLCipher + Hardware Keystore)")]
    end

    subgraph Free AI Bridge (Zero Auth / Zero Cost)
        Proxy["Cloudflare Worker Serverless Proxy<br/>(Anonymous Rate-Limiting & Master Key Vault)"]
        LLM["OpenRouter Inference Gateway"]
    end

    EntryUI --> ActivePrayerUI
    EntryUI --> LoggingUI
    EntryUI --> DirectoryUI

    ActivePrayerUI --> LocalDB
    LoggingUI --> LocalDB
    DirectoryUI --> LocalDB

    LoggingUI -.->"Encrypted Payload (Sanitized)"| Proxy
    Proxy --> LLM
```
