# Business Requirements Document (BRD) — Pray Without Ceasing

**Status:** Active Approved Draft  
**Application Title (Unofficial):** *Pray Without Ceasing* (1 Thessalonians 5:17)  
**Last Updated:** 2026-09-07  
**Document Owner:** Planning & Architecture Team  

---

## 1. Application Purpose

### 1.1 Vision & Value Proposition
Deliver an intimate, distraction-free, mobile-first prayer companion (*Pray Without Ceasing*) that cleanly separates the contemplative act of prayer from the administrative task of organizing petitions. The application opens to a pristine, blank canvas offering strictly three choices—**Start praying**, **Open Journal**, and **Log prayer points**—grounded in historic Reformed theology and supported by an objective, neutral suggestion engine provided free of charge.

### 1.2 Problem Statement
Many believers struggle with consistency, distraction, and cognitive overload during prayer. Traditional note-taking apps lack prayer lifecycles, while broad social or spiritual apps often introduce gamification (streaks, badges, confetti) and commercialized community feeds that compromise privacy and reverence. Furthermore, when entering prayer, users are often greeted with complex dashboards and task lists that trigger administrative fatigue rather than contemplative focus.

### 1.3 Target Audience & Scale Constraint
- **Target Scale**: Strictly personal and private distribution. Lifetime user base is explicitly constrained to **fewer than 100 users**.
- **Individual Believers**: Seekers of a quiet, private discipline for focused personal prayer.
- **Relational Prayer Intercessors**: Believers actively holding petitions for specific individuals, family members, ministry groups, and global concerns.

---

## 2. Application Scope

### 2.1 Platform Scope
- **Mobile Only**: Active production implementation focused on **Android** (Kotlin + Jetpack Compose under `android/`), optimized for Samsung Galaxy Flip devices and standard/de-googled Android environments.
- **Sideload Delivery**: Distributed via signed APK to Google Drive (`G:\My Drive\myApps\Prayer.apk`) for immediate personal sideloading.
- **Portability Mandate**: Core domain and architecture must guarantee seamless future portability to **iOS**.
- **Independent from Vendor Attestation**: Explicitly free from dependencies on Google Play Integrity API or Apple DeviceCheck.
- **Desktop / Web**: Strictly out of scope.

### 2.2 Core In-Scope Features (MVP)

1. **Pristine Home Screen & Gestural Navigation**:
   - The app launches to an uncluttered blank canvas featuring strictly three centered flat geometric tile options with sharp 90-degree right angles, zero curved edges, zero visible gaps (directly abutting tiles sharing a 1px boundary), and strictly zero explanatory, tutorial, or descriptive sub-text:
     1. **Start praying**
     2. **Open Journal**
     3. **Log prayer points**
   - Strictly zero hero headers, app wordmarks, or decorative headers on the launch screen; the three tiles partition the screen vertically edge-to-edge.
   - A **swipe-left gesture** also reveals the structural directory and Journal.

2. **The Three Foundational Roots (`People`, `Groups`, `General`)**:
   - **`People`**: Exclusively and strictly specific, distinct individual human relationships (e.g., spouse, parent, child, a single named friend/neighbor, and personal petitions under *Me*—including personal health, job trials, or sanctification situated within a workplace, school, or hospital). Plural or collective peer environments (such as coworkers or classmates as a unit) belong under `Groups`.
   - **`Groups`**: Collectives, communities, and shared peer/work environments (e.g., work colleagues, office team, church congregation, small groups, committees, ministries).
   - **`General`**: Broad topics, global matters, societal needs, and historic liturgical prayers.

3. **Contemplative, Passive Prayer Flow ("Start Praying" / Full-Screen Prayer Mode)**:
   - **Zero Friction & Full-Screen Immersion**: Direct transition into prayer mode. When in Prayer mode, the UI is completely full screen, with strictly zero buttons, zero card tiles, and zero grid lines.
   - **Pure Typographic Structure**: Displays solely the name of the entity being prayed for preceded by "Praying for" (e.g. *Praying for Sarah*), followed directly by the prayer points with ample typographic breathing room.
   - **Buttonless Navigation (Smartphone Swipe Primacy)**: Navigation between topics is driven primarily by natural smartphone swipe gestures: **Swipe Left** advances to the next topic (analogous to turning a prayer book page); **Swipe Right** returns to the previous topic; **Swipe Down** dismisses prayer mode and returns to Home. Touch tap zones (right 75% advance, left 25% return, top edge exit) and keyboard arrows operate as complementary accessibility fallbacks.
   - **Design Principle: Strict Prohibition of Ordinal Labels ("Point 1", "Point 2")**: The application shall never code, render, or display arbitrary sequential counters, numeric badges, or ordinal enumerations (e.g., *Point 1*, *Point 2*, *Point 1 of N*, *Petition 1*, *Item 1*) anywhere in the software. Petitions are solemn intercessory prayers, not indexed checklist items or ticketing tasks. Each prayer point is recognized and presented exclusively by its meaningful petition title and description.
   - **Principle of Minimal Contextual Data Exposure**: Just because data is stored, calculated, or available in the backend does not mean it has value to show to the user. The interface strictly exposes only the least amount of data relevant to the immediate devotional context. Queue progression counters (`Topic 1 of 8`), administrative tallies (`5 points (3 active, 2 ans)`), database root tags (`(People)`), and sequential item numbers (`Point 1`, `Point 2`) are strictly suppressed from presentation.
   - **Balanced Queue Balancing & Interaction Metrics (Preventing Neglect)**: The local database retains cumulative interaction counts (`interacted_count`) and last-interacted timestamps (`last_interacted_at`) for every topic (as well as individual prayer points). The contemplation queue uses these silent metrics to balance the curated feed—prioritizing topics with the oldest interaction dates and lowest counts—so that relational persons, ministries, and general burdens do not get neglected over time.
   - **Expandable Answered Petitions**: Answered prayer points for the active topic are rendered softly beneath active petitions without boxes or tiles, allowing believers to reflect on God's past faithfulness and offer thanksgiving without cluttering active intercession.
   - **Strictly Passive / Read-Only**: Zero buttons, checkmarks, editing tools, or task-completion toggles during prayer. Progressing past a topic silently increments its `interacted_count` and updates its `last_interacted_at` timestamp.
   - **Self-Paced & Open-Ended**: Each screen is a complete topic. Users advance through topics at their own pace and simply stop when they are ready.
   - **Zero-State Fallback (Historic Reformed Prayers)**: When the local database contains no user-logged prayers, the app draws from preloaded classic **Reformed, Protestant** prayers (The Lord's Prayer, classic Anglican Book of Common Prayer collects, and the Apostles' Creed). Once personal prayers exist, these reside under `General → Historic Prayers` with a toggle to blend into daily rotations.

4. **Entity-First Prayer Logging ("Log Prayer Points")**:
   - **Mandatory Initial Step (Select or Create Target Entity)**:
     - Tapping *Log prayer points* immediately presents an entity selection screen. Every prayer point must belong to a specific person, group, or general topic.
     - Users select an existing entity (`People`, `Groups`, `General`) from the local SQLite vault, or tap the contiguous **"New Person / Group"** creation tile to immediately establish a new name and root.
     - When logging is triggered from within an existing entity detail screen in the Journal, the target entity is already locked and this initial step is seamlessly satisfied.
   - **Direct Entry (Elimination of Title Input, Auto-Bullets & Post-Commit Auto-Titling)**:
     - The user **shall not be able to see or add a title** when logging new points. The view provides strictly a clean, unadorned text pad pre-bound to the selected person or group.
     - **Auto Bullet-Point Writing Pad**: The writing pad automatically formats input as a bulleted list, auto-prefixing with a bullet (`• `) and triggering a new bullet on line space (Enter / Return).
     - Tapping **Save to [Name]** immediately commits the petition body to the local SQLite database.
     - Following committal, a concise 2–6 word petition title is auto-generated asynchronously via a dedicated lightweight branch of the AI engine.
     - **Exemption from Theological Validation**: The title-generation branch performs purely a simple summarization task and does not require theological validation.
     - **Offline Graceful Fallback**: In offline scenarios, the petition is safely saved locally using an initial truncated text snippet until background connectivity generates the permanent title.
   - **"Guide me" (Assisted Articulation & Distillation)**: An objective, structured tool to help users articulate tangled or heavy thoughts for the selected person or group:
     - **App Auto-Conversion to JSON Payload**: The client formats session state into a structured JSON payload (`initial_reflection`, pre-specified `root` and `group`, `clarifying_question`, `user_response`, `request_more`).
     - **Step 1 (Open Heart)**: Prompt (*"Who or what is on your heart?"*) with open text canvas, explicitly focused on the pre-selected person or group.
     - **Step 2 (Neutral Distillation)**: Asks clarifying questions **1 at a time** in **neutral, concise language** (not a therapy bot). Clarifying inquiry is default-mandatory on the initial turn. **Inquiry When Actionable Point Not Obvious**: If a clear actionable point is not obvious, the engine is strictly prohibited from generating candidate prayer points; it must set `skip_question: false` and formulate strictly one concise question (6–12 words) in plain, natural English without bureaucratic templates. It distinguishes between internal emotional states (asking plainly what is causing the feeling) and external entities/topics (asking plainly what is happening). Hard-capped at max 2 turns. **Unconditional Question Skipping**: The user can skip any question at any point, proceeding directly to candidate prayer points.
     - **Step 3 (Commitment & Category Bypass)**: Presents strictly **2 candidate prayer points** tailored to the selected target. Grounded strictly in user-supplied facts without inventing crises. Because the entity was chosen upfront, category suggestions are omitted (`suggested_root: null`, `suggested_group: null`), and points bind directly to the selected person or group upon saving. Users may invoke a strictly one-time request for 2 additional suggestions. Governed by simple controls: **"Save"**, **"Suggest 2 more"** (one-time only), **"Back"**, and **"Cancel"**.

5. **Free AI Suggestion Engine Provisioning & Budget Ceiling**:
   - The AI service is provided **free of charge** to users.
   - **Operational Budget Ceiling**: Hard limit of **maximum $5.00 / month** total operational expenditure.
   - **Infrastructure**: Hosted on Cloudflare Workers Free Tier ($0.00 / month).
   - **Zero Account Friction**: No login, no user registration, and no user-supplied API key required.
   - Orchestrated via a serverless proxy holding the master API key securely server-side with hard spending caps.

6. **Granular Prayer Point Lifecycle & Editing Requirements**:
   - Independent states per prayer point: `Active`, `Answered` (with resolution timestamp and thanksgiving note), and `Archived`.
   - **Click Once to Edit**: In the Journal directory and entity detail views, clicking or tapping a saved prayer point once immediately opens edit mode.
   - **List Card Swipe Gestures**: On mobile touchscreens, petition cards in directory and detail views support rapid list swiping: **Swipe Right** toggles status between `Active` and `Answered` (recording thanksgiving); **Swipe Left** reveals immediate deletion controls.
   - **Editable Title & Body**: While titles are auto-generated on initial log, users have complete authority to edit and customize the title and body during editing.
   - **Permanent Deletion**: The system explicitly supports permanent deletion (`DELETE FROM PRAYER_POINT`), completely purging the record from local storage upon user confirmation.
   - Answered prayers are displayed in the directory with a subtle strikethrough.

7. **Language & Dialect Configuration (Default: English Australian/UK; Option: US English)**:
   - The application defaults to **English (Australian / UK)** (`en-AU` / `en-GB`) across all UI strings, system copy, and preloaded liturgical prayers (e.g., *saviour*, *honour*, *neighbour*).
   - A dedicated configuration option in settings allows selecting **US English** (`en-US`).
   - The dialect preference applies across all application copy and informs the AI suggestion engine to produce questions and candidate prayer points in the configured dialect.

8. **Sequestered Settings & Hidden Preferences**:
   - The active interface is strictly free from settings, display switches, or configuration controls.
   - All user preferences (Quiet Night vs. Morning Light, dialect selection, and historic prayer blending) are sequestered in a dedicated Settings panel reached exclusively via intentional navigation from the Journal.

9. **Lexicon & Devotional Terminology Standard**:
   - The application strictly translates raw functional, database, and engineering language into reverent, personal, and liturgically grounded app vocabulary.
   - Clinical or marketing terms such as *"Target"* or *"Target Entity"* are strictly prohibited from user-facing presentations, replaced universally by **`Praying for [Name]`**, **`Who are you praying for?`**, and **`Person or group`**.
   - Arbitrary sequential numbering (*Point 1*, *Point 2*, *Item 1*) is strictly forbidden; petitions are presented purely by substantive title and body.
   - Administrative and database operations are framed relationally: saving to disk is presented as **`Save to [Name]`**, AI inquiry is framed as a gentle **`Clarifying question`** with **`Continue`** and **`Skip to petitions`**, and answered prayers are framed around **`Thanksgiving notes`** for God's sovereign faithfulness.

10. **Smartphone Gestural Standard**:
   - The application treats touchscreen smartphones as tactile instruments of devotion, actively leveraging native gestures rather than relying on legacy button-heavy web patterns.
   - Horizontal swipe inputs (left/right topic progression, card swipe actions, and edge-swipe back navigation) are elevated as primary user-input paradigms across the mobile client.

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
- **Geometric & Contiguous Tessellated UI Architecture (Zero Gaps & Zero Curved Edges)**: The user interface is strictly composed of flat, non-skeuomorphic, planar tiles with sharp 90-degree right angles. Rounded corners, curved pill buttons, bubble cards, drop shadows, and circular frames are strictly forbidden. Crucially, the design eliminates all visible gaps, margins, and gutters between UI elements; all tiles and components are directly adjacent and contiguous to each other, sharing 1px hairline boundary seams across the entire viewport to form a unified architectural plane.
- **Principle of Austere UI & Zero Explanatory / Config Clutter**: As an unyielding principle, the application shall **never** display explanatory, onboarding, or tutorial-like text on its UI elements. Buttons, tiles, and headers present strictly functional labels without descriptive sub-captions or explanatory commentary. Furthermore, the devotional interface shall never be cluttered with configuration controls, display options, or settings toggles; all preferences are sequestered into a dedicated Settings view accessible exclusively via deliberate navigation from the Journal.
- **Dialect & Orthography Standards**: System copy, preloaded historic prayers, and AI outputs adhere to **English (Australian / UK)** by default, switching consistently to **US English** when configured by the user.
- **Modular Prompt Deployment**: To comply with serverless execution constraints (e.g., Cloudflare Workers 5.1 kB text binding ceiling) while optimizing attention primacy, prompt directives are decomposed into fine modules assembled in sequence: `PROMPT_PERSONA` (identity & First Principle), `PROMPT_INQUIRY_FLOW` (control loop & inquiry), `PROMPT_THEOLOGY` (doctrinal & comfort boundaries), `PROMPT_TAXONOMY_PRIVACY` (root mapping & entity masking), `PROMPT_CARD_STYLE` (card brevity & shorthand), and `PROMPT_OUTPUT_SCHEMA` (JSON structure).
- **Benchmarked Stress-Testing Battery**: The suggestion engine is subjected to a standardized 100-request evaluation suite ([`api/test/prayer_requests_stress_test.json`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/test/prayer_requests_stress_test.json)) spanning all wordiness tiers (5–253 words), diverse perspectives, theological boundaries, and root categories to guarantee mobile card brevity and confessional fidelity under stress, with comprehensive evaluation recorded in [`api/test/BENCHMARK_RESULTS.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/test/BENCHMARK_RESULTS.md) and detailed linguistic analysis in [`api/test/AI_GENERATED_TEXT_REPORT.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/test/AI_GENERATED_TEXT_REPORT.md).
- **Full-Spec Reference Prototype & Automated UI Layout Battery**: The mobile companion specifications are fully executable and provably validated via a high-fidelity reference prototype ([`planning/prototype.html`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/prototype.html)), an in-browser spec validator ([`planning/test_runner.html`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/test_runner.html)), and an automated headless Playwright test suite ([`planning/tests/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/tests)) validating universal 0px border-radius, contiguous 0px gaps/gutters, 1px hairline planar seams, gesture discrimination ratios ($\ge 1.5$), title-free Direct Entry auto-bullets, and strict prohibition of clinical or ordinal labels.

---

## 4. High-Level Conceptual Architecture

```mermaid
graph TD
    subgraph Mobile Client (Offline-First)
        EntryUI["Blank Entry Screen (Pray | Open Journal | Log)"]
        ActivePrayerUI["Passive Prayer Flow (Card Focus)"]
        LoggingUI["Log Flow (Record Point | 'Guide me')"]
        JournalUI["Journal (People | Groups | General)"]
        LocalDB[("Encrypted Local SQLite Database<br/>(SQLCipher + Hardware Keystore)")]
    end

    subgraph Free AI Bridge (Zero Auth / Zero Cost)
        Proxy["Cloudflare Worker Serverless Proxy<br/>(Anonymous Rate-Limiting & Master Key Vault)"]
        LLM["OpenRouter Inference Gateway"]
    end

    EntryUI --> ActivePrayerUI
    EntryUI --> LoggingUI
    EntryUI --> JournalUI

    ActivePrayerUI --> LocalDB
    LoggingUI --> LocalDB
    JournalUI --> LocalDB

    LoggingUI -.->"Encrypted Payload (Sanitized)"| Proxy
    Proxy --> LLM
```
