# User Experience (UX) & Product Design Specification

**Document:** `UX.md`  
**Status:** Approved Design Principles & User Journey Map  
**Last Updated:** 2026-09-07  
**Target Audience:** Product Owner, Business Stakeholders, Product Designers  
**Platform Focus:** Mobile App (Clean, solemn, distraction-free companion)  

---

## 1. Product Vision & Emotional Tone

The Prayer app is conceived as a quiet, sacred personal vault. Most consumer apps rely on vibrant colors, badges, notifications, and social feeds designed to capture and monetize user attention. In contrast, this app is designed to **give attention back to the user's interior life, personal relationships, and communion with God**.

### 1.1 Core Experience Pillars
- **Radical Simplicity & Blank Entry**: The application opens to a serene, completely uncluttered screen with strictly two options: **Start praying** and **Log prayer points**. There are no distracting dashboards, activity feeds, or cluttered widgets on launch.
- **Strictly Passive Contemplation**: When praying, the screen is 100% read-only. Prayer is not a productivity checklist. There are no check-off buttons, editing controls, or administrative interruptions while in prayer.
- **Geometric Orthogonality & Tessellated Tiles**: Every surface, card, button, and container is rendered as a flat geometric tile with sharp 90-degree right angles. Rounded corners, curved pill shapes, drop shadows, and bubble cards are strictly prohibited. Adjacent components tessellate seamlessly along crisp hairline dividing seams.
- **Reverent & Solemn**: The interface feels like an architectural tablet or solemn liturgical folio. Content is communicated through crisp typography, ample whitespace, and subtle hairline dividing rules.
- **Zero Emojis & Zero Gamification**: Emojis, streak counters, celebration popups, badges, and animations are strictly excluded to preserve dignity and respect.
- **Uncompromised Privacy**: Zero accounts, zero login, zero public feeds, and zero telemetry. All prayer data resides exclusively on the user's physical device.

---

## 2. Visual Identity: High-Contrast Monochrome & Geometric Tessellation

The product uses a pure black-and-white visual identity that reflects dignity, simplicity, architectural order, and liturgical focus.

### 2.1 The Two Visual Modes
Users can toggle between two high-contrast modes depending on environment and preference:
1. **Quiet Night (Black & White)**: Pure black canvas with crisp white typography and subtle charcoal dividers. Tailored for evening devotions, bedside prayer, and low-light environments.
2. **Morning Light (White & Black)**: Clean white canvas with stark black lettering and soft gray card outlines. Tailored for daytime journaling and bright reading conditions.

### 2.2 Geometric, Tessellated Flat Tiles (Strictly Zero Curved Edges)
- **Strict Orthogonality**: Every card, button, input box, dialog, sheet, and container adheres to sharp 90-degree right angles (`border-radius: 0`). Curved edges, rounded corners, and pill buttons are strictly forbidden.
- **Planar Flat Tiles**: Surfaces are rendered as completely flat, zero-elevation rectangular tiles (`elevation: 0`, zero drop shadows, zero gradient bevels).
- **Tessellated Grid Architecture**: Multiple on-screen elements (e.g., batched prayer cards 1–5, candidate suggestion pairs, or directory lists) seamlessly abut and lock together along 1px hairline dividing seams, forming a unified, non-overlapping geometric mosaic.
- **Architectural & Liturgical Gravity**: The unyielding rectilinear geometry reinforces solemnity, permanence, and reverence, eschewing the casual, bubbly aesthetics of consumer social apps.

### 2.3 Styling & Typography Rules
- **No Decorative Icons**: Pure typographic hierarchy and delicate hairline rules.
- **Subdued Answered State**: In the ledger, when a prayer is answered, the text softens with a subtle strikethrough, visually signaling gratitude and completion while preserving the historical record.

---

## 3. Information Architecture & Navigation

The navigation model cleanly divorces the **act of praying** from the **act of organizing**.

```mermaid
graph TD
    Home["Blank Home Screen<br/>(1. Start praying | 2. Log prayer points)"]
    
    Home -->|"Tap 'Start praying'"| PraySession["Start Praying Flow<br/>(Passive Curated Queue)"]
    Home -->|"Tap 'Log prayer points'"| LogChoice{"Log Choice"}
    Home -->|"Swipe Left Gesture"| Directory["Structural Directory & Ledger<br/>(3 Roots: People, Groups, General)"]
    
    LogChoice -->|"Record a prayer point"| DirectCapture["Direct Empty Text Pad<br/>(AI Intelligent Filing)"]
    LogChoice -->|"'Guide me'"| GuideMeFlow["Guide Me Flow<br/>(Step 1: Open Heart<br/>Step 2: Neutral Distillation<br/>Step 3: Save / Back / Cancel)"]
    
    Directory --> PeopleRoot["People Root<br/>(Individuals, Family, Friends)"]
    Directory --> GroupsRoot["Groups Root<br/>(Church, Teams, Communities)"]
    Directory --> GeneralRoot["General Root<br/>(World, Global, Historic Prayers)"]
    Directory --> Settings["App Settings<br/>(Theme, Language, Batch Size, Blending, Backup)"]
```

### 3.1 The Blank Entry Screen
Upon launch, the user meets an uncluttered canvas with strictly two centered options:
1. **Start praying**
2. **Log prayer points**

### 3.2 The Structural Directory (Swipe Left)
Swiping left from the blank home screen glides into the structured ledger and management vault, rooted in three foundational domains:
1. **`People`**: Exclusively and strictly specific, distinct individual relationships (e.g., spouse, children, parents, a single named friend/neighbor, and personal petitions under *Me*—including personal health, job trials, or spiritual sanctification situated within a workplace or hospital).
2. **`Groups`**: Collectives, communities, and shared peer/work environments (e.g., work colleagues, office team, church congregation, small group, committee, ministry).
3. **`General`**: Broad topics, global concerns, personal spiritual disciplines, and the preloaded collection of historic Reformed prayers.

---

## 4. Core User Journeys

### 4.1 Journey 1: "Start Praying" (Devotional Contemplation)

```mermaid
graph LR
    Launch["Tap 'Start praying'"] --> Queue["Curated Prayer Queue<br/>(Zero Friction)"]
    Queue --> ViewCard["Card Focus View<br/>(Configurable 1 to 5 items)"]
    ViewCard --> PrayQuietly["Read & Pray Silently<br/>(Strictly Passive / Read-Only)"]
    PrayQuietly --> Next["Swipe to Next Card<br/>(Self-Paced / No Quota)"]
    Next --> Exit["Exit Anytime Back to Home"]
```

1. **Immediate Immersion**: Tapping **Start praying** instantly displays the first prayer card. There are no configuration popups, filters, or setup steps required prior to praying.
2. **Card Focus & Configurable Batch Size (Flat Tessellated Tiles)**:
   - Each prayer point occupies the screen as a completely flat rectangular tile with sharp 90-degree right angles and strictly zero curved edges (`border-radius: 0`).
   - Users can configure their display preference in settings to show **1, 2, 3, 4, or 5 items** simultaneously. When multi-card batches (2–5) are displayed, the tiles tessellate seamlessly across the screen along hairline dividing rules without gaps or rounded corners.
3. **Strictly Passive / Read-Only Prayer Experience**:
   - The prayer screen contains **zero interactive buttons, checkboxes, or edit fields**.
   - The user cannot mark items as answered, edit text, or check off tasks during prayer.
   - Viewing cards automatically updates the internal `last_prayed_at` timestamp without demanding manual interaction.
4. **Self-Paced & Open-Ended**:
   - Each screen represents a distinct prayer.
   - There is no mandatory session quota or timer. The user determines the length of their devotion and simply exits back to the home screen whenever they choose.
5. **Zero-State Fallback (Curated Reformed Prayers)**:
   - If the user has not yet recorded any personal prayer points, *Start praying* presents preloaded prayers from a curated selection of historic **Reformed, Protestant** classics:
     - The Lord's Prayer
     - Classic Anglican Book of Common Prayer (BCP) collects
     - The Apostles' Creed
   - Once the user logs personal prayers, these historic prayers permanently reside under `General → Historic Prayers`, with a user toggle to either blend them into daily prayer rotations or keep them accessible strictly on demand.

---

### 4.2 Journey 2: "Log Prayer Points" (Capture & Articulation)

Tapping **Log prayer points** from the home screen presents two distinct pathways:
1. **Record a prayer point**
2. **"Guide me"**

```mermaid
graph TD
    TapLog["Tap 'Log prayer points'"] --> PickPath{"Select Mode"}
    
    PickPath -->|"Record a prayer point"| DirectPad["Empty Text Pad"]
    DirectPad --> AIFiling["AI Suggests Filing Root<br/>(People / Groups / General)"]
    AIFiling --> QuickSave["Local Entity Selection & Confirm<br/>(One-Tap Save to Vault)"]
    
    PickPath -->|"'Guide me'"| Step1["Step 1: Open Heart<br/>'Who or what is on your heart?'"]
    Step1 --> Step2["Step 2: Neutral Distillation<br/>(1 open-ended question at a time; max 2 turns)"]
    Step2 -->|"Answer (up to 2 turns)"| Step2
    Step2 -->|"Skip any question / Finished"| Step3["Step 3: Review Candidate Points<br/>(Controls: Save | Back | Cancel)"]
    Step3 -->|"Save"| Commit["Committed to Local Database"]
```

#### Pathway A: "Record a Prayer Point" (Direct Capture with Intelligent AI Filing)
- Opens immediately to an empty text pad for users who already know their petition.
- The user types their prayer point directly.
- Upon entry, the client masks personal entity names on-device before communicating with the AI. The AI intelligently infers and suggests the root category (`People`, `Groups`, or `General`) and optional group context without multi-turn questioning.
- **Privacy Gate**: Entity name suggestions from the AI are strictly not required and omitted. The user confirms or binds the local entity directly from their private on-device vault with a single tap.
- **Offline Fallback**: If offline or if the user prefers, a direct manual folder/entity selector is available.

#### Pathway B: "Guide Me" (Objective AI-Assisted Articulation)
Designed for when thoughts are tangled, heavy, or difficult to articulate:
- **App Auto-Conversion to JSON**: The mobile app transparently converts the user's reflection, pre-specified category context, and turn progression into a structured JSON payload sent across the wire.
- **Step 1 (Open Heart & Contextual Entry)**: A clean, quiet prompt: *"Who or what is on your heart?"* with an open text area for raw thoughts, stream-of-consciousness writing, or voice dictation. If invoked from within an existing directory view (e.g. within a specific Person or Group folder), the destination `root` and `group` are pre-specified by the app.
- **Step 2 (Neutral Distillation)**:
  - The client masks personal entity names prior to sending. The suggestion engine analyzes the raw entry and asks clarifying questions **one at a time**.
  - **Tone & Style**: Strictly neutral, concise, and objective. **Not a therapy bot**—zero artificial empathy, zero psychological framing, and zero conversational filler.
  - **Open-Ended Inquiries & Actionable Clarity**: Questions expect the user to provide the substance. It must not use leading questions or assume intent unless the user explicitly asks for suggestions. If a clear actionable point is not obvious from the user's reflection, the system must never jump to speculative prayer cards; it promptly poses strictly one concise question (6–12 words) in natural, plain English without bureaucratic templates. It distinguishes between internal emotional states (asking plainly what is causing the feeling, e.g., *"What is making you feel anxious right now?"*) and external entities/topics (asking plainly what is happening, e.g., *"What is going on with your boss that you'd like to pray about?"*). When multiple competing crises are presented simultaneously, the engine performs concise burden triage (*"Which of these is weighing on you most heavily right now?"*).
  - **Skip Question Binary**: While a clarifying inquiry is default-mandatory, the engine evaluates a `skip_question` binary flag, advancing directly to Step 3 suggestions only when initial input is already exceptionally comprehensive and unambiguous.
  - **Unconditional Question Skipping**: The user can skip any question the app asks at any point. Skipping immediately guarantees that no more questions will be asked during that session; the flow bypasses all remaining inquiry and transitions directly to Step 3 (Review & Action).
  - **Bounded Interaction**: Hard ceiling of **maximum 2 question turns**. A prominent **"Skip" / "Skip to Suggestions"** action is always provided on every question.
- **Step 3 (Review & Action — Flat Tessellated Candidate Tiles)**:
  - Presents strictly and exactly **2 concise candidate prayer points** (never 1, never 3), rendered as flat, geometric rectangular tiles with sharp 90-degree right angles, zero curved edges, and zero drop shadows.
  - Grounded strictly in facts provided by the user; the assistant never presumes or fabricates illnesses, cancer, or medical crises.
  - **Contextual Pre-specification Bypass**: When `root` (and optional `group`) was already prespecified upon entry, category suggestions are omitted entirely ("suggestion isn't needed"). The cards directly reflect the pre-selected context without asking the user to confirm or re-categorize. When entry was uncontextualized, suggested root categories (`People`, `Groups`, `General`) are presented.
  - Entity name suggestions are strictly not required and omitted for privacy; target entity assignment is performed locally on-device.
  - **Objective Petitions, Never Scripted Prayers**: Candidate cards show discrete, telegraphic petitions and burdens—never pre-written prayers addressing God directly (no "Father God...", "Lord...", or second-person prayer prose). The user does the praying; the cards organize the petitions.
  - **Telegraphic Card Aesthetics & Strict Brevity**: Phrased in compact shorthand (using symbols like `&`, `→`, `↑`, standard abbreviations like `govt`, and omitting filler articles/auxiliary verbs; the abbreviation `w/` or `/w` is strictly excluded in favor of "with" or omission) to maximize glanceability and minimize screen clutter on mobile cards. Strictly constrained to **2–6 word titles** (targeting 2–4 words, hard ceiling of 6 words) and a hard ceiling of **maximum 20–25 words per description** (never lengthy multi-sentence paragraphs). Employs a preferred **2-to-3 clause semicolon pattern** (Clause 1: immediate need/action; Clause 2: heart posture/spiritual fruit; Clause 3: submission to God's sovereign will/peace) to enhance visual hierarchy and scannability on mobile screens.
  - **Resilience Across Wordiness & Emotional Load**: The distillation experience is stress-tested against a 100-case benchmark dataset ([`test/prayer_requests_stress_test.json`](file:///c:/Users/ianch/sourcecode/repos/Prayer/test/prayer_requests_stress_test.json)) ensuring that whether the user enters a 5-word cryptic fragment or a 250-word emotional ramble, the candidate cards consistently honor the 2–6 word title and $\le$ 25-word telegraphic description standard, with verified live responses documented in [`test/BENCHMARK_RESULTS.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/test/BENCHMARK_RESULTS.md) and textual quality evaluated in [`test/AI_GENERATED_TEXT_REPORT.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/test/AI_GENERATED_TEXT_REPORT.md).
  - User controls are strictly limited to focused actions: **Save**, **"Suggest 2 more"** (strictly one-time action per session; disabled or hidden once invoked to prevent decision paralysis), **Back**, and **Cancel**.
- **Graceful Quota & Offline Fallback**: If network connectivity is unavailable or the daily operational quota is exhausted, the UI quietly presents: *"The assistant is currently unavailable. You can record your prayer points directly."* Users are seamlessly transitioned to direct manual recording without disruption.

---

### 4.3 Journey 3: "Swipe Left" (Structural Directory & Ledger)

Swiping left from the home screen opens the complete management ledger:
1. **Hierarchy Browsing**:
   - Drill into **`People`**, **`Groups`**, or **`General`** to see all associated entities and active prayer points.
2. **Marking Answered**:
   - Tapping an active prayer point allows marking it as `Answered`, capturing an optional resolution date and testimony note.
   - Answered points display with a soft strikethrough.
3. **Editing & Archiving**:
   - Update titles, adjust notes, or archive points no longer held in active prayer.
4. **Settings Access**:
   - Theme toggle (*Quiet Night* vs. *Morning Light*).
   - **Language / Dialect configuration**: Dedicated selector between **English (Australian / UK)** *(Default)* and **US English**. Switching immediately adjusts UI copy, prayer collects, and distillation orthography.
   - Card batch size configuration (1, 2, 3, 4, or 5 items per view).
   - Historic Reformed prayers rotation toggle (blend into daily rotation vs. library-only).
   - Local encrypted database backup and export.

---

## 5. Mobile Ergonomics & Accessibility

- **Thumb-Zone Navigation**: The primary two options on the home screen and primary actions (*Save*, *Back*, *Cancel*) sit naturally within thumb reach on mobile devices.
- **Frictionless Entry**: Zero account creation, zero login, and zero API key configuration. Users are able to pray or log within one second of opening the app.
- **Instant Local Performance**: All read and write operations interact directly with the embedded SQLite database on device, guaranteeing zero network lag or loading spinners.
