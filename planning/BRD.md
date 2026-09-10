# Business Requirements Document (BRD) — Pray Without Ceasing

**Status:** Active Approved Draft  
**Application Title (Unofficial):** *Pray Without Ceasing* (1 Thessalonians 5:17)  
**Last Updated:** 2026-09-10  
**Document Owner:** Planning & Architecture Team  

---

## 1. Application Purpose

12: ### 1.1 Vision & Value Proposition
13: Deliver an intimate, distraction-free, mobile-first prayer companion (*Pray Without Ceasing*) that cleanly separates the contemplative act of prayer from the administrative task of organizing prayer points. The application opens to a pristine, blank canvas offering strictly three choices—**Start praying**, **Open Journal**, and **Add prayer points**—grounded in historic Reformed theology and supported by an ambient, non-conversational background suggestion engine providing read-only prompts (strictly unlabelled in the UI) when viewing past prayer points.
14: 
15: ### 1.2 Problem Statement
16: Many believers struggle with consistency, distraction, and cognitive overload during prayer. Traditional note-taking apps lack prayer lifecycles, while broad social or spiritual apps often introduce gamification (streaks, badges, confetti) and commercialized community feeds that compromise privacy and reverence. Furthermore, when entering prayer, users are often greeted with complex dashboards and task lists that trigger administrative fatigue rather than contemplative focus.
17: 
18: ### 1.3 Target Audience & Scale Constraint
19: - **Target Scale**: Strictly personal and private distribution. Lifetime user base is explicitly constrained to **fewer than 100 users**.
20: - **Individual Believers**: Seekers of a quiet, private discipline for focused personal prayer.
21: - **Relational Prayer Intercessors**: Believers actively holding prayer points for specific individuals, family members, ministry groups, and global concerns.
22: 
23: ---
24: 
25: ## 2. Application Scope
26: 
27: ### 2.1 Platform Scope
28: - **Mobile Only**: Active production implementation focused on **Android** (Kotlin + Jetpack Compose under `android/`), optimized for Samsung Galaxy Flip devices and standard/de-googled Android environments.
29: - **Sideload Delivery**: Distributed via signed APK to Google Drive (`G:\My Drive\myApps\Prayer.apk`) for immediate personal sideloading.
30: - **Portability Mandate**: Core domain and architecture must guarantee seamless future portability to **iOS**.
31: - **Independent from Vendor Attestation**: Explicitly free from dependencies on Google Play Integrity API or Apple DeviceCheck.
32: - **Desktop / Web**: Strictly out of scope.
33: 
34: ### 2.2 Core In-Scope Features (MVP)
35: 
36: 1. **Pristine Home Screen & Gestural Navigation**:
37:    - The app launches to an uncluttered blank canvas featuring strictly three centered flat geometric tile options with sharp 90-degree right angles, zero curved edges, zero visible gaps (directly abutting tiles sharing a 1px boundary), and strictly zero explanatory, tutorial, or descriptive sub-text:
38:      1. **Start praying**
39:      2. **Open Journal**
40:      3. **Add prayer points**
41:    - Strictly zero hero headers, app wordmarks, or decorative headers on the launch screen; the three tiles partition the screen vertically edge-to-edge.
42:    - A **swipe-left gesture** (or tapping **Open Journal**) directly reveals the Journal overview featuring collapsible menus for People, Groups, General, and Mission Partners, directly surfacing the lists of people and items to pray for without an intermediary screen of buttons.
43: 
44: 2. **The Four Foundational Roots (`People`, `Groups`, `General`, `Mission Partners`)**:
45:    - **`People`**: Exclusively and strictly specific, distinct individual human relationships (e.g., spouse, parent, child, a single named friend/neighbor, and personal prayer points under *Me*—including personal health, job trials, or sanctification situated within a workplace, school, or hospital). Plural or collective peer environments (such as coworkers or classmates as a unit) belong under `Groups`.
46:    - **`Groups`**: Collectives, communities, and shared peer/work environments (e.g., work colleagues, office team, church congregation, small groups, committees, ministries).
47:    - **`General`**: Broad topics, global matters, societal needs, and historic liturgical prayers.
48:    - **`Mission Partners`**: Supported missionary families, mission agencies, missionaries, and ministry partners (e.g., a missionary family on the field, a Bible translation agency, a church planting ministry).
49: 
50: 3. **Contemplative, Passive Prayer Flow ("Start Praying" / Full-Screen Prayer Mode)**:
51:    - **Zero Friction & Full-Screen Immersion**: Direct transition into prayer mode. When in Prayer mode, the UI is completely full screen, with strictly zero buttons, zero card tiles, and zero grid lines.
52:    - **Pure Typographic Structure**: Displays solely the name of the entity being prayed for preceded by "Praying for" (e.g. *Praying for Sarah*), followed directly by the prayer points with ample typographic breathing room.
53:    - **Buttonless Navigation (Smartphone Swipe Primacy)**: Navigation between topics is driven primarily by natural smartphone swipe gestures: **Swipe Left** advances to the next topic (analogous to turning a prayer book page); **Swipe Right** returns to the previous topic; **Swipe Down** dismisses prayer mode and returns to Home. Touch tap zones (right 75% advance, left 25% return, top edge exit) and keyboard arrows operate as complementary accessibility fallbacks.
54:    - **Design Principle: Strict Prohibition of Ordinal Labels ("Point 1", "Point 2")**: The application shall never code, render, or display arbitrary sequential counters, numeric badges, or ordinal enumerations (e.g., *Point 1*, *Point 2*, *Point 1 of N*, *Item 1*) anywhere in the software. Prayer points are solemn intercessory prayers, not indexed checklist items or ticketing tasks. Each prayer point is recognized and presented exclusively by its meaningful title and description.
55:    - **Principle of Minimal Contextual Data Exposure**: Just because data is stored, calculated, or available in the backend does not mean it has value to show to the user. The interface strictly exposes only the least amount of data relevant to the immediate devotional context. Queue progression counters (`Topic 1 of 8`), administrative tallies (`5 points (3 active, 2 ans)`), database root tags (`(People)`), and sequential item numbers (`Point 1`, `Point 2`) are strictly suppressed from presentation.
56:    - **Balanced Queue Balancing & Interaction Metrics (Preventing Neglect)**: The local database retains cumulative interaction counts (`interacted_count`) and last-interacted timestamps (`last_interacted_at`) for every topic (as well as individual prayer points). The contemplation queue uses these silent metrics to balance the curated feed—prioritizing topics with the oldest interaction dates and lowest counts—so that relational persons, ministries, and general burdens do not get neglected over time.
57:    - **Expandable Answered Prayer Points**: Answered prayer points for the active topic are rendered softly beneath active prayer points without boxes or tiles, allowing believers to reflect on God's past faithfulness and offer thanksgiving without cluttering active intercession.
58:    - **Read-Only AI Prompts on Past Points (Strictly Unlabelled in UI)**: When contemplating an entity/topic with recorded past points, these points are submitted to the API to surface 3–5 concise, reverent prompts displayed strictly in **read only** format within the prayer flow. The prompts are strictly unlabelled in the user interface (never headed or badged as "Petitions" or "Prompts").
59:    - **Strictly Passive / Read-Only**: Zero buttons, checkmarks, editing tools, or task-completion toggles during prayer. Progressing past a topic silently increments its `interacted_count` and updates its `last_interacted_at` timestamp.
60:    - **Self-Paced & Open-Ended**: Each screen is a complete topic. Users advance through topics at their own pace and simply stop when they are ready.
61:    - **Zero-State Fallback (Historic Reformed Prayers)**: When the local database contains no user-added prayers, the app draws from preloaded classic **Reformed, Protestant** prayers (The Lord's Prayer, classic Anglican Book of Common Prayer collects, and the Apostles' Creed). Once personal prayers exist, these reside under `General → Historic Prayers` with a toggle to blend into daily rotations.
62: 
63: 4. **Entity-First Prayer Adding ("Add Prayer Points")**:
64:    - **Mandatory Initial Step (Select or Create Target Entity)**:
65:      - Tapping *Add prayer points* immediately presents an entity selection screen. Every prayer point must belong to a specific person, group, general topic, or mission partner.
66:      - Users select an existing entity (`People`, `Groups`, `General`, `Mission Partners`) from the local SQLite vault, or tap the contiguous **"New Person / Group"** creation tile to immediately establish a new name and root.
67:      - When adding is triggered from within an existing entity detail screen in the Journal, the target entity is already locked and this initial step is seamlessly satisfied.
68:    - **Integrated Lined Notepad (Direct Entry Replacement, Auto-Bullets & Post-Commit Auto-Titling)**:
69:      - Selecting a person or group immediately opens a dedicated **lined notepad** for writing prayer points directly, completely removing the intermediary "Direct Entry" button.
70:      - **Authentic Notepad Ruled Lines**: The notepad renders subtle, light horizontal lines across the entire writing area and a vertical left margin guide line mirroring an authentic physical notebook or legal pad, rendered upon the warm vintage white parchment canvas.
71:      - The user **shall not be able to see or add a title** when adding new points. The view provides strictly an unadorned ruled text canvas pre-bound to the selected person or group.
72:      - **Auto Bullet-Point Writing Pad**: The writing pad automatically formats input as a bulleted list, auto-prefixing with a bullet (`• `) and triggering a new bullet on line space (Enter / Return).
73:      - **Strictly No AI Assistance in Adding Mode**: The "Add prayer points" screen contains **zero AI assistance, zero suggestions pane, and zero AI interaction**. Believers write directly and unmediated from the heart onto the lined notepad canvas.
74:      - **Immediate Local Save & Contemplative Transition**: Tapping **Save to [Name]** (or the keyboard-accessible Save action in the top header) immediately commits the prayer point body to the local SQLite database and transitions directly into that entity's prayer point view in the Journal so the believer immediately sees the prayer point just saved, rather than abruptly dumping back to the main menu.
75:      - Following committal, a concise 2–6 word prayer point title is auto-generated asynchronously via a dedicated lightweight branch of the AI engine.
76:      - **Exemption from Theological Validation**: The title-generation branch performs purely a simple summarization task and does not require theological validation.
77:      - **Offline Graceful Fallback**: In offline scenarios, the prayer point is safely saved locally using an initial truncated text snippet until background connectivity generates the permanent title.
78: 
79: 5. **Free AI Suggestion Engine Provisioning & Budget Ceiling**:
80:    - The AI service is provided **free of charge** to users.
81:    - **Operational Budget Ceiling**: Hard limit of **maximum $5.00 / month** total operational expenditure.
82:    - **Infrastructure**: Hosted on Cloudflare Workers Free Tier ($0.00 / month).
83:    - **Zero Account Friction**: No login, no user registration, and no user-supplied API key required.
84:    - Orchestrated via a serverless proxy holding the master API key securely server-side with hard spending caps.
85: 
86: 6. **Granular Prayer Point Lifecycle, Entity Management & Long-Press Responsive Actions**:
87:    - Independent states per prayer point: `Active`, `Answered` (with resolution timestamp and thanksgiving note), and `Archived`.
88:    - **Click Once to Edit**: In the Journal directory and entity detail views, clicking or tapping a saved prayer point once immediately opens edit mode.
    - **Read-Only AI Prompts in Entity Detail (Strictly Unlabelled in UI)**: In the Journal entity detail view, viewing past prayer points displays an accompanying read-only card of AI prompts reflecting on those recorded points, strictly unlabelled in the UI (zero header or title).
    - **Long-Press Responsiveness & Planar Contextual Menus**:
     - *Individual Prayer Records*: Long-pressing any prayer point (in Journal entity detail, directory, or Sanctuary prayer mode) produces a distinct tactile haptic pulse and opens an austere planar contextual dialog (`FlatSquareShape`, 0dp radius, hairline border). Options include rapid status toggle (`Mark as Answered` $\leftrightarrow$ `Mark as Active`), full prayer point editing, and permanent deletion.
     - *People & Groups (Entities)*: Long-pressing any entity card in the Journal directory or Add-flow selection list produces haptic feedback and surfaces entity management actions:
       - **Edit Name & Category**: Rename the person or group and shift their classification sphere (`People` $\leftrightarrow$ `Groups`) seamlessly.
       - **Add Prayer Point Shortcut**: Jump directly into the lined notepad pre-bound to that entity.
       - **Permanent Cascading Deletion**: Stark confirmation dialog (*"Delete this person/group and all associated prayer points? This cannot be undone."*), permanently purging the entity and all of its associated prayer points from local storage.
   - **List Card Swipe Gestures**: On mobile touchscreens, prayer point cards in directory and detail views support rapid list swiping: **Swipe Right** toggles status between `Active` and `Answered` (recording thanksgiving); **Swipe Left** reveals immediate deletion controls.
   - **Editable Title & Body**: While titles are auto-generated on initial entry, users have complete authority to edit and customize the title and body during editing.
   - **Permanent Deletion**: The system explicitly supports permanent deletion (`DELETE FROM PRAYER_POINT`), completely purging the record from local storage upon user confirmation.
   - Answered prayers are displayed in the directory with a subtle strikethrough.

7. **Language & Dialect Configuration (Default: English Australian/UK; Option: US English)**:
   - The application defaults to **English (Australian / UK)** (`en-AU` / `en-GB`) across all UI strings, system copy, and preloaded liturgical prayers (e.g., *saviour*, *honour*, *neighbour*).
   - A dedicated configuration option in settings allows selecting **US English** (`en-US`).
   - The dialect preference applies across all application copy and informs the AI suggestion engine to produce questions and candidate prayer points in the configured dialect.

8. **Sequestered Settings & Hidden Preferences**:
   - The active interface is strictly free from settings, display switches, or configuration controls.
   - All user preferences (text scaling, dialect selection, and historic prayer blending) are sequestered in a dedicated Settings panel reached exclusively via intentional navigation from the Journal.

9. **Lexicon & Devotional Terminology Standard**:
   - The application strictly translates raw functional, database, and engineering language into reverent, personal, and liturgically grounded app vocabulary.
   - Clinical or marketing terms such as *"Target"* or *"Target Entity"* are strictly prohibited from user-facing presentations, replaced universally by **`Praying for [Name]`**, **`Who are you praying for?`**, and **`Person or group`**.
   - Arbitrary sequential numbering (*Point 1*, *Point 2*, *Item 1*) is strictly forbidden; prayer points are presented purely by substantive title and body.
   - Administrative and database operations are framed relationally: saving to disk is presented as **`Save to [Name]`**, AI inquiry is framed as a gentle **`Clarifying question`** with **`Continue`** and **`Skip to prayer points`**, and answered prayers are framed around **`Thanksgiving notes`** for God's sovereign faithfulness.

10. **Smartphone Gestural Standard**:
   - The application treats touchscreen smartphones as tactile instruments of devotion, actively leveraging native gestures rather than relying on legacy button-heavy web patterns.
   - Horizontal swipe inputs (left/right topic progression, card swipe actions, and edge-swipe back navigation) are elevated as primary user-input paradigms across the mobile client.

11. **Universal LIFO Back Stack & Context Preservation**:
   - The application enforces a strict Last-In, First-Out (LIFO) back navigation model across all application domains and modal hierarchies.
   - Users can step back in the stack at any time via either the native Android system back gesture/button or by swiping right from the left screen edge ($X_{start} \le 25\text{dp}$, $\Delta X \ge 50\text{dp}$, $|\Delta X| \ge 1.5 |\Delta Y|$).
   - Back navigation faithfully unwinds nested context layer by layer (e.g., adding a prayer point transitions directly into that entity's prayer point view in the Journal to review the saved points; pressing back reveals the collapsible Journal overview of people and groups; pressing back once more returns peacefully to the root Home menu).

### 2.3 Post-MVP / Future Milestones
- Native iOS client release.
- Encrypted local file export and restore (`.prayerbackup`).
- Local voice dictation for adding prayer points.

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
- **Non-Conversational & Ambient AI**: The AI assistant will **not** ask questions and will **not** be able to chat to. Conversational turn-taking, artificial empathy, clarifying questions, and chat interfaces are strictly forbidden. The AI works quietly in the background to show suggested prompts based on existing content.
- **Objective Prompts, Never Scripted Prayers**: The suggestion engine must never compose actual prayers or address God directly (e.g., never output "Father God...", "Dear Lord...", "Lord Jesus...", "Thy will be done", or second-person invocations to God). Believers pray themselves; the engine strictly summarizes the burden or thanksgiving into concise suggested prompts.
- **Strict Terminology & Unlabelled UI Invariant**: Devotional cues provided by the system are called **prompts** internally. They must **never** be referred to as "petitions". In the UI, they must **never be labelled as such anywhere** (no "Prompts", "Petitions", or section title/header). They are rendered strictly as quiet, unlabelled, read-only bullet points (`• [prompt]`).
- **Zero AI in Add Mode**: When adding new prayer points (`LogPrayerScreen`), the interface is a clean, undisturbed lined notepad with 100% pure typing and zero AI assistance, zero ambient suggestion pane, and zero API calls.
- **Read-Only Prompts When Viewing Past Points**: When viewing past prayer points in Start Praying mode (`SanctuaryPrayerScreen`) or Prayer Journal mode (`JournalScreen`), the client batches existing prayer points and requests devotional prompts from the API proxy (`POST /api/v1/suggest`), rendering the results in read-only format.
- **Prompt Output Format & Brevity Ceilings**: Prompts are displayed as read-only bullet points, with each prompt strictly between **1 and 6 words long**. Redundant prefixes (e.g., "Pray for...", "Ask God to...") are stripped.
- **Strict Non-Fabrication Invariant (Anti-Hallucination)**: The lines produced by the AI shall **never make up content if existing data is limited**. The engine is strictly bounded by the recorded facts and must never extrapolate, invent crises, or fabricate medical or emotional circumstances.
- **Batch Target Context Invariant**: All user recorded data on the target is passed to the AI **in one go, not line by line**.
- **Geometric & Contiguous Tessellated UI Architecture (Zero Gaps & Zero Curved Edges)**: The user interface is strictly composed of flat, non-skeuomorphic, planar tiles with sharp 90-degree right angles. Rounded corners, curved pill buttons, bubble cards, drop shadows, and circular frames are strictly forbidden. Crucially, the design eliminates all visible gaps, margins, and gutters between UI elements; all tiles and components are directly adjacent and contiguous to each other, sharing 1px hairline boundary seams across the entire viewport to form a unified architectural plane.
- **Material Design 3 (M3) Foundation & 8dp Spacing Rhythm**: The native Android application is engineered upon Android Material 3 (`androidx.compose.material3`), reconciling modern design systems with liturgical solemnity by enforcing `FlatSquareShape = RoundedCornerShape(0.dp)` across all M3 shape tokens. Interactive surfaces leverage M3 `Scaffold` with `SnackbarHost`, M3 `TopAppBar`, M3 `OutlinedTextField`, and M3 `Button` / `OutlinedCard` components. All paddings, margins, and clearances follow a strict 8dp spacing grid (`PrayerSpacing`: 4dp, 8dp, 16dp, 24dp, 32dp, 48dp min touch target, 56dp action height, 72dp sanctuary bottom clearance), specifically calibrated for Samsung Galaxy Flip aspect ratios (22:9 / 21.9:9).
- **Solemn Devotional Motion Mechanics**: Interactions avoid flashy consumer animations in favor of reverent, intentional motion reflecting Anglican prayer traditions: staggered home launch entrance, simulated prayer book page-turning in Sanctuary mode via directional `AnimatedContent` with `FastOutSlowInEasing` (350ms), shared-axis sub-screen slide-and-fade transitions, dynamic list item animations (`Modifier.animateItem()`), and smooth bottom pane reveal/collapse.
- **Principle of Austere UI & Zero Explanatory / Config Clutter**: As an unyielding principle, the application shall **never** display explanatory, onboarding, or tutorial-like text on its UI elements. Buttons, tiles, and headers present strictly functional labels without descriptive sub-captions or explanatory commentary. Furthermore, the devotional interface shall never be cluttered with configuration controls, display options, or settings toggles; all preferences are sequestered into a dedicated Settings view accessible exclusively via deliberate navigation from the Journal.
- **Two-Tier LLM Architecture (Thoughtful Generation + Strict Compliance Harness)**: To resolve the tension between evocative, natural phrasing and unyielding compliance with theological guardrails, brevity ceilings, and anti-fabrication invariants, the API proxy deploys a two-tier pipeline in `api/worker.js`. Tier 1 operates at `0.7` temperature (`max_tokens: 9000`) to generate thoughtful, natural, grounded suggestion lines from the full batch target context. Tier 2 operates at low temperature (`0.1`, `max_tokens: 9000`) as a strict verification and compliance harness that rewrites, purifies, and condenses the Tier 1 draft, unconditionally enforcing zero direct address to God, 1–6 words per line, zero fabrication beyond supplied target records, and valid JSON wire array format.
- **Dialect & Orthography Standards**: System copy, preloaded historic prayers, and AI outputs adhere to **English (Australian / UK)** by default, switching consistently to **US English** when configured by the user.
- **Modular Prompt Deployment**: To comply with serverless execution constraints (e.g., Cloudflare Workers 5.1 kB text binding ceiling) while optimizing attention primacy, prompt directives are decomposed into fine modules assembled in sequence: `PROMPT_PERSONA` (ambient suggestion identity, no chat/questions), `PROMPT_SUGGESTION_FLOW` (batch target ingestion, 1–6 word lines, 1–5 lines visible, scroll refresh, non-fabrication rule), `PROMPT_THEOLOGY` (doctrinal & comfort boundaries), `PROMPT_TAXONOMY_PRIVACY` (root mapping & entity masking), `PROMPT_CARD_STYLE` (concise 1–6 word line brevity), and `PROMPT_OUTPUT_SCHEMA` (JSON array of strings).
- **Benchmarked Stress-Testing Battery**: The suggestion engine is subjected to a standardized 100-request evaluation suite ([`api/test/prayer_requests_stress_test.json`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/test/prayer_requests_stress_test.json)) spanning diverse perspectives, theological boundaries, and root categories to guarantee line brevity and confessional fidelity under stress, with comprehensive evaluation recorded in [`api/test/BENCHMARK_RESULTS.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/test/BENCHMARK_RESULTS.md) and detailed linguistic analysis in [`api/test/AI_GENERATED_TEXT_REPORT.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/test/AI_GENERATED_TEXT_REPORT.md).
- **Full-Spec Automated Test & Layout Battery**: The companion specifications are fully executable and provably validated via an automated headless Playwright test suite ([`planning/tests/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/tests)) and a comprehensive native Android unit test suite ([`android/app/src/test/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test)) validating universal 0px border-radius, contiguous 0px gaps/gutters, 1px hairline planar seams, gesture discrimination ratios ($\ge 1.5$), reactive LIFO back stack navigation, title-free Direct Entry auto-bullets, bottom ambient suggestion pane toggling, anti-neglect queue balancing, theological guardrails (*Solus Christus*, rejection of prosperity/saints/manifesting, zero direct prayers to God), wire API serialization, and strict prohibition of clinical or ordinal labels.

---

## 4. High-Level Conceptual Architecture

```mermaid
graph TD
    subgraph Mobile Client (Offline-First)
        EntryUI["Blank Entry Screen (Pray | Open Journal | Add)"]
        ActivePrayerUI["Passive Prayer Flow (Card Focus)"]
        AddingUI["Add Flow (Lined Notepad)"]
        JournalUI["Journal (People | Groups | General | Mission Partners)"]
        LocalDB[("Encrypted Local SQLite Database<br/>(SQLCipher + Hardware Keystore)")]
    end

    subgraph Free AI Bridge (Zero Auth / Zero Cost)
        Proxy["Cloudflare Worker Serverless Proxy<br/>(Anonymous Rate-Limiting & Master Key Vault)"]
        LLM["OpenRouter Inference Gateway"]
    end

    EntryUI --> ActivePrayerUI
    EntryUI --> AddingUI
    EntryUI --> JournalUI

    ActivePrayerUI --> LocalDB
    AddingUI --> LocalDB
    JournalUI --> LocalDB

    ActivePrayerUI -.->"Batch Past Points Context"| Proxy
    JournalUI -.->"Batch Past Points Context"| Proxy
    Proxy --> LLM
```
