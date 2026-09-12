# Business Requirements Document (BRD) — Pray Without Ceasing

**Status:** Active Approved Draft  
**Application Title (Unofficial):** *Pray Without Ceasing* (1 Thessalonians 5:17)  
**Last Updated:** 2026-09-10  
**Document Owner:** Planning & Architecture Team  
**Canonical Design Bible:** [`planning/android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md) (Strictly read-only)  

---

## 1. Application Purpose

### 1.1 Vision & Value Proposition
Deliver an intimate, distraction-free, mobile-first prayer companion (*Pray Without Ceasing*) that cleanly separates the contemplative act of prayer from the administrative task of organizing prayer points. 

The application is grounded in **Modern Leatherbound Craft (The Modern Folio)**: capturing the emotional gravity, physical warmth, and timeless romance of a handcrafted leatherbound notebook with fine ruled paper (such as a Midori Traveler's Notebook, Leuchtturm1917, or Smythson devotional journal), translated into contemporary, vector-crisp Android design. It opens to a serene frontispiece offering four tactile pathways—**Start praying**, **Open Journal**, **Add prayer points**, and **Library**—grounded in historic Reformed theology and supported by an ambient, non-conversational background suggestion engine providing read-only prompts (strictly unlabelled in the UI) when viewing past prayer points.

### 1.2 Problem Statement
Many believers struggle with consistency, distraction, and cognitive overload during prayer. Traditional note-taking apps lack prayer lifecycles, while commercial spiritual apps often introduce gamification (streaks, badges, confetti) and commercialized community feeds that compromise privacy and reverence. Furthermore, when entering prayer, users are often greeted with complex dashboards, task checklists, and dark/light mode flashing that trigger administrative fatigue rather than contemplative focus.

### 1.3 Target Audience & Scale Constraint
- **Target Scale**: Strictly personal and private distribution. Lifetime user base is explicitly constrained to **fewer than 100 users**.
- **Individual Believers**: Seekers of a quiet, private discipline for focused personal prayer.
- **Relational Prayer Intercessors**: Believers actively holding prayer points for specific individuals, family members, ministry groups, and global mission concerns.

---

## 2. Application Scope

### 2.1 Platform Scope
- **Mobile Only**: Active production implementation focused on **Android** (Kotlin + Jetpack Compose under `android/`), optimized for Samsung Galaxy Flip devices and standard Android environments.
- **Sideload Delivery**: Distributed via signed release APK to Google Drive (`G:\My Drive\myApps\Prayer.apk`) for immediate personal sideloading.
- **Portability Mandate**: Core domain and architecture must guarantee seamless future portability to **iOS**.
- **Independent from Vendor Attestation**: Explicitly free from dependencies on Google Play Integrity API or Apple DeviceCheck.
- **Desktop / Web**: Strictly out of scope.

### 2.2 Core In-Scope Features (MVP)

1. **The Modern Folio Frontispiece & Zero-Flash Launch (`HomeScreen`)**:
   - Opens to a serene book frontispiece framed in the active leather casing (`leatherActive`), centering a 720dp warm cream vellum sheet with classical double-hairline bookplate framing.
   - Initial cold start features a zero-flash warm vellum native splash (`Theme.Prayer` and Android 12+ debossed monogram) and a serene, non-blocking folio opening revelation (~600ms).
   - Inscription presents tracked capitals *"PRAY WITHOUT CEASING"*, printer's fleuron `❧`, and Scripture epigraph (1 Thessalonians 5:16–18).
   - Presents four contiguous tactile bookplate actions: elevated sanctuary gateway (*Start praying*), directory records (*Open Journal*), direct capture notepad (*Add prayer points*), and the theological vault (*Library*).

2. **The Five Foundational Roots (`People`, `Groups`, `Mission Partners`, `General`, `Historic`)**:
   - **`People`**: Exclusively and strictly specific, distinct individual human relationships (e.g., spouse, parent, child, a named friend/neighbor, and personal prayer points under *Me*—including personal health, job trials, or sanctification situated within a workplace or hospital).
   - **`Groups`**: Collectives, communities, and shared peer/work environments (e.g., work colleagues, office team, church congregation, small groups, committees, ministries).
   - **`Mission Partners`**: Supported missionary families, mission agencies, missionaries, and ministry partners.
   - **`General`**: Broad topics, global matters, and societal needs.
   - **`Historic`**: Preloaded classic collects, confessions, and devotional prayers from church history. **Each preloaded historic prayer occupies its own distinct entity (topic) within Historic** — never grouped under a single aggregate entity. This individual-entity structure honors the theological weight and character of each historic prayer. Current preloaded historic topics: The Lord's Prayer, 1662 BCP Collects (Peace, Grace, Purity, General Thanksgiving), The Apostles' Creed, 8 C.H. Spurgeon Metropolitan Tabernacle pulpit prayers, and 16 early church prayers (Clement, Clementine, Polycarp, St James, Basil, Chrysostom, Ambrose, Nerses, Augustine, Leonine, Gelasian). Ordered in the journal following General, with General following Mission Partners.

3. **Contemplative, Passive Prayer Flow (`PrayerSessionScreen`)**:
   - **Zero Friction & Full-Screen Immersion**: When in Prayer mode, the UI is completely full screen, with strictly zero buttons, zero card tiles, and zero grid lines.
   - **The Baseline Synchronization Law**: Horizontal feint rules are dynamically locked to typographic baselines at `28sp` intervals.
   - **Heading & Inked Typographic Hierarchy**: Heading states *"Praying for [Name]"* in Semi-Bold Literary Serif (`22sp` / `32sp`) on the first prominent rule for personal intercession targets; **for preloaded historic entities, the entity's display name (e.g., *"Help from on High (C.H. Spurgeon)"* or *"Collect for Peace (1662 BCP)"*) is rendered directly without the "Praying for" prefix**, reflecting the liturgical rather than personal nature of these prayers. Prayer points render in Literary Serif (`17sp` / `28sp`) seated strictly ON feint rules.
   - **The 37.52dp Left Margin Track**: Displays discreet marginal status asides (`[ ACTIVE ]`, `[ ANSWERED ]`) and timestamps (`10:45 AM`) in the 37.52dp left track (reduced by 33% from 56dp) without cluttering the narrative text canvas (`45.52dp` text inset, `24dp` right padding). For preloaded historic prayers, active/answered status toggles and pills are suppressed; the 37.52dp left track serves as a clean margin gutter maintaining the red margin rule and text alignment.
   - **Buttonless Navigation & Ergonomic Zones**:
     - **Left 30% Width**: Tap or Swipe Right returns to previous topic.
     - **Right 30% Width**: Tap or Swipe Left advances to next topic (`Shared-Axis X`, spring stiffness `320`, damping `0.85`).
     - **Center 40% Width**: Reading canvas; long-pressing any personal prayer point opens an in-place status resolution dialog (`Active` $\leftrightarrow$ `Answered`). For preloaded historic prayers, status resolution is suppressed.
     - **Swipe Down (`ΔY > +60px` in upper half)**: Slides downward (`250ms`, Emphasized Accelerate) to return to Home.
   - **Anti-Neglect Devotional Queue**: Balances topics using retained interaction counts (`interacted_count`) and timestamps (`last_interacted_at`), prioritizing topics with the oldest interaction dates and lowest counts.
   - **Expandable Answered Prayer Section**: Answered points render in italic Literary Serif (`16sp` / `26sp` Celadon `#3D6B52`) on unruled vellum, framed by a `2dp` vertical Celadon rule.
   - **Read-Only AI Prompts on Past Points (Persistent Cache & Background Refresh)**: Displays 3–12 read-only prompts (4–15 words each, at least one per group) categorized under *Praise God*, *Thank God*, and *Ask God*, opening with *For*, *That*, *A* (or *An*), or *Because*, written in warm, flowing, non-terse language without parroting user input verbatim and strictly avoiding wishy-washy general platitudes. Crucially, the **default view is hidden or collapsed**; prompts are surfaced only if the user explicitly taps to view them, preserving quiet contemplative stillness. The last list of prompts returned by the API is cached persistently in SQLite (`suggestion_cache`), so viewing an entity immediately renders the last list of prompts from the previous session while an asynchronous background refresh queries the API, seamlessly updating the display and cache upon completion without user interruption. Every prompt must be a 100% complete, fully finished grammatical thought that never cuts off mid-sentence or ends on dangling connectors. Prompts are freshly derived without overfitting or copying few-shot examples. **AI prompt generation is suppressed entirely for preloaded historic entities** — these entities carry the prayers of the saints and require no AI augmentation.
   - **Design Principle: Strict Prohibition of Ordinal Labels ("Point 1", "Point 2")**: The application shall never code, render, or display arbitrary sequential counters, numeric badges, or ordinal enumerations anywhere in the UI.
   - **Principle of Minimal Contextual Data Exposure**: Internal database metrics, queue tallies, and taxonomy codes are strictly suppressed from devotional views.
   - **Zero-State Fallback (Historic Prayers)**: When no personal prayer topics exist, the contemplative queue draws from all 30 preloaded historic prayer entities (The Lord's Prayer, five 1662 BCP Collects/Creed, eight Spurgeon Tabernacle prayers, and sixteen early church prayers from Potts), each surfaced as individual topics.


4. **Direct Lined Notepad & Capture Flow (`LogPrayerScreen`)**:
   - **Direct ruled writing**: Lands directly on the lined vellum pad (`LinedNotepad`). Drafting begins immediately on feint rules without preliminary categorization hurdles.
   - **Zero Title Field**: Believers write directly and unmediated from the heart onto the ruled notepad canvas.
   - **Auto-Bullets & Newline Formatting**: Every new line automatically formats as a dotpoint (`• `) seated on the next dynamic baseline rule; pressing Enter anywhere in text or pasting multi-line text formats every line with a dotpoint, while backspacing an empty bullet cleanly clears it.
   - **Multi-Record Dotpoint Separation**: When saving, every dotpoint is separated out into its own distinct `PrayerPoint` record in SQLite (rather than a monolithic multi-line block). This ensures each individual dotpoint can independently be tracked, edited, and marked active or answered across Sanctuary and Journal views.
   - **Strictly No AI in Add Mode**: Zero AI assistance, zero suggestions pane, and zero AI interaction while drafting.
   - **Target Selection & Single-Tap Save**: Tapping **Next** transitions to the categorization sheet presenting four personal spheres (`People`, `Groups`, `Mission Partners`, `General`). Tapping an entity commits immediately and navigates to that entity's view in the Journal.
   - **Undo Toast Feedback**: Immediate Snackbar toast (*"Saved N prayer points to [Name]"* or *"Saved to [Name]"*) with an Undo action that deletes all records created in that batch.
   - **Quiet Autosave Pulse**: Discrete dot in ledger bar pulses to Celadon Sage (`#3D6B52`) over `150ms`, holds for `800ms`, and mellows back over `400ms`. Entries are saved cleanly without artificial titles.

5. **Aesthetic Architecture: The Modern Leatherbound Folio**:
   - **The Singular Folio Law**: Formally repudiates the artificial Day/Night (Light/Dark) toggle in favor of one singular, uncompromised tactile theme (warm cream vellum `#FAF7F0` + iron-gall ink `#1C1A17`) comfortable in both bright daylight and low bedside lamplight, with a High-Contrast Accessible Mode meeting WCAG 2.1 AA non-text contrast.
   - **Four Chromatic Tiers**: Leather casing frame (Saddle Tan `#8C532B`, Horween Cordovan `#5E2A2B`, Hunter Forest `#2D483A`, Obsidian Hide `#35322F`), Ruled paper canvas (Cream Vellum, Ivory, Parchment), Archival inks (Iron-Gall, Celadon Sage, Sepia, Midnight Blue, Muted Ledger), and Ephemera (Garnet Silk Marker Ribbon `#8B2635`, Warm Amber Glaze `#F5DE88` text selection, Madder Crimson alert).
   - **Interactive Silk Marker Ribbon Tab**: Vector swallow-tail notched tab (`18dp` width, `40dp` resting / `80dp` pinned, spring stiffness `220`, damping `0.70`, haptic tick, dynamic expanded `48 × 80dp` touch envelope). Pinned state drives priority in devotional queues and surfaces subjects in Pinned Focus.
   - **Universal Touch Boundary**: Minimum `48 × 48dp` touch bounding box across all interactive elements.

6. **Journal Directory & In-Place Editing (`JournalScreen`)**:
   - Pinned Focus: Dedicated quick-access header at the top of the directory displaying all pinned intercessions, with pinned sorting prioritized within each category.
   - Five collapsible accordion categories (`People`, `Groups`, `Mission Partners`, `General`, `Historic`).
   - Direct addition route: An action placed at the top of each personal listing under each group (`+ Add person`, `+ Add group`, `+ Add mission partner`, `+ Add topic`) opening an in-place creation dialog and navigating immediately to the subject's record upon save.
   - Material Container Transform (`300ms`, Emphasized Decelerate) to Entity Detail view.
   - **Date Separation**: Entries are grouped and separated by date of entry with day and month written out in full English words (e.g., *"Friday, 11 September 2026"*) in subtle, unflashy typography (`typography.marginStatus` / `colors.inkMuted`).
   - Click once to edit body, status, or thanksgiving note (titles are omitted).
   - Permanent cascading deletion with stark planar confirmation dialog.
   - Answered prayers displayed with Celadon coloring and subtle strikethrough.

7. **Form Factor Versatility, Foldables & Privacy**:
   - **Adaptive Dual-Pane Book Spread**: On foldables in book posture and tablets in landscape: Left page directory/queue; Right page devotional prayer or lined notepad; clean central `24dp` spine gutter with `6%` ambient occlusion gradient.
   - **Tabletop Posture (Flex Mode)**: Contemplative reading on top display; ruled canvas and controls on bottom display.
   - **Precision Stylus Support (S-Pen / USI)**: Digital archival ink, palm rejection, handwritten margin notes bound to prayer point blocks.
   - **Closed Folio Privacy Shield**: Masking Recent Apps / task switcher view with a handsome  vector leather cover and embossed monogram insignia.

8. **Language & Dialect Configuration**:
   - Default: **English (Australian / UK)** (`en-AU` / `en-GB`) across UI strings, liturgical prayers, and AI outputs.
   - Option: **US English** (`en-US`).

9. **Sequestered Settings & Hidden Preferences**:
   - Active interface is strictly free from settings toggles or display switches. All preferences (leather finish swatches, text scaling with live serif previews, dialect, historic Reformed prayer rotation, high-contrast accessible mode) are sequestered in a dedicated Modern Folio Settings view (`SettingsScreen.kt`) reached exclusively from the Journal.

10. **Lexicon & Devotional Terminology Standard**:
    - Translates database jargon into reverent language: *Praying for [Name]*, *Who are you praying for?*, *Save to [Name]*, *Thanksgiving note*, *Journal*, *Prompts*. Strictly prohibits *target*, *entity*, *ticket*, *commit*, *Point 1*, *Point 2*.

11. **Universal LIFO Back Stack & Edge-Swipe Back**:
    - Strict Last-In, First-Out back navigation preserving nested context across sub-screens, accessible via system back or universal left edge-swipe right ($X_{start} \le 25\text{dp}$, $\Delta X \ge 50\text{dp}$).

12. **Theological Library & Archival Volumes (`LibraryScreen`, `VolumeReaderScreen`)**:
    - Houses classical theological treatises and historic devotionals within the Modern Folio reading environment.
    - **Inaugural Volume 1**: John Calvin's *Of Prayer: A Perpetual Exercise of Faith. The Daily Benefits Derived from It* (*Institutes of the Christian Religion*, Book III, Chapter XX; trans. Henry Beveridge, 1845; Public Domain).
    - **Structure & Navigation**: 8 Principal Divisions, 52 analytical outline summaries, and 52 reflowed sections (99 paragraphs).
    - **Standalone Structured Asset**: Stored as `res/raw/library_calvin_prayer.json` with typed deserialization (`kotlinx.serialization`).
    - **Analog Reading Experience**: Cream vellum writing canvas, baseline-locked feint rules (`28sp`), Literary Serif typography, Table of Contents quick-jump navigation, and persistent Silk Ribbon reading progress tracking.

---

## 3. Application & Domain Rules

### 3.1 Privacy & Local-First Persistence Rules
- **100% Offline Vault**: All prayer points, relationships, updates, and answered testimonies reside exclusively on the physical device in SQLite (Room with SQLCipher).
- **Vault Archive & Device Portability**: Users can manually seal and export a password-protected, hardware-resistant AES-256-GCM encrypted backup archive (`.folio`) via Android's Storage Access Framework, and unseal/restore it on a new device with Merge or Replace options, guaranteeing 100% data portability with zero cloud reliance.
- **Zero Telemetry & Closed Folio Privacy Shield**: Prayers are never analyzed or transmitted to central databases. Recent Apps window is shielded with vector leather folio cover.
- **Privacy Gate & Entity Masking**: Personal identifiers are masked on-device prior to proxy transmission.

### 3.2 Tone, Language & AI Guardrails
- **Non-Conversational Ambient AI**: The AI assistant will **not** ask questions and will **not** chat. Zero chat interfaces, zero clarifying questions.
- **Objective Prompts, Never Scripted Prayers**: The suggestion engine must never compose actual prayers or address God directly.
- **Strict Terminology & Unlabelled UI Invariant**: Devotional cues are called **prompts** internally, strictly unlabelled in the UI (no section header or badge).
- **Zero AI in Add Mode**: 100% pure human writing on the lined notepad canvas.
- **Prompt Brevity Ceilings, Grounding & Collapsed-by-Default Display**: Prompts are strictly 4 to 15 words per line, opening with *For*, *That*, *A*, or *Because*, organized into three groups (*Praise God*, *Thank God*, *Ask God*) with at least one per group and 3 to 12 points in total. Crucially, the **default view is hidden or collapsed** across both devotional prayer (`SanctuaryPrayerScreen`) and journal detail (`JournalScreen`) views; prompts only expand when explicitly requested by the user. The last list of prompts returned by the API is cached persistently in SQLite (`suggestion_cache`), allowing the UI to immediately render cached prompts while refreshing asynchronously in the background. The AI shall **never make up content if existing data is limited**, must avoid terse sounding phrasing (no 1–3 word fragments), must never parrot back user input verbatim, and must strictly avoid wishy-washy general platitudes by anchoring in concrete circumstance and biblical depth. Furthermore, every prompt must be a complete, finished grammatical sentence that never cuts off mid-thought or ends on dangling connectors, and prompts must strictly avoid overfitting to few-shot prompt examples.
- **Two-Tier LLM Architecture**: Tier 1 (`0.8` temperature) drafts natural points across the three groups from batch target context; Tier 2 (`0.1` temperature) purifies and condenses the draft, enforcing zero direct prayers, opening words (*For*, *That*, *A*, *Because*), complete sentence closure without truncation, allowable word limit (4–15 words), avoidance of wishy-washy general platitudes, anti-overfitting, non-parroting, non-terse tone, and valid JSON wire format with descriptive schema placeholders.

### 3.3 Visual & Ergonomic Architecture Rules
- **Modern Leatherbound Folio Metaphor**: Clean vector leather casing frame, warm cream vellum canvas, baseline-synchronized rules, and archival ink.
- **The Singular Folio Law**: One uncompromised tactile theme eliminating day/night inversion, plus High-Contrast Accessible Mode.
- **The Baseline Synchronization Law**: Ruled lines dynamically anchored to active text baselines (`28sp`), never static repeating stripes.
- **The 37.52dp Left Margin Track**: Two-track layout with `37.52dp` vertical margin guide rule (`#E5B4B4`, reduced by 33% from 56dp), `45.52dp` narrative text inset, and `24dp` right padding.
- **Ergonomic Touch Targets**: Universal minimum `48 × 48dp` touch target envelope (silk ribbon expanded to `48 × 56dp`).
- **Devotional Prayer Zoning**: Left 30% (previous), Right 30% (next), Center 40% (reading / long-press status resolution).
- **The Zero Gamification Mandate**: Strictly zero streaks, badges, points, or celebration confetti.

---

## 4. High-Level Conceptual Architecture

```mermaid
graph TD
    subgraph Mobile Client (Offline-First Modern Folio)
        EntryUI["Blank Frontispiece (Pray | Open Journal | Add)"]
        ActivePrayerUI["PrayerSessionScreen (Chrome Suppressed, 30/40/30 Zones)"]
        AddingUI["LogPrayerScreen (Ruled Lined Notepad, Zero AI)"]
        JournalUI["JournalScreen (People | Groups | Mission Partners | General | Historic)"]
        LocalDB[("Encrypted Local SQLite Database<br/>(Room + SQLCipher + Keystore)")]
    end

    subgraph Free AI Bridge (Zero Auth / Zero Cost)
        Proxy["Cloudflare Worker Serverless Proxy<br/>(Anonymous Rate-Limiting & Master Key Vault)"]
        LLM["OpenRouter Inference Gateway<br/>(openai/gpt-5.6-luna, Two-Tier Pipeline)"]
    end

    EntryUI --> ActivePrayerUI
    EntryUI --> AddingUI
    EntryUI --> JournalUI

    ActivePrayerUI --> LocalDB
    AddingUI --> LocalDB
    JournalUI --> LocalDB

    ActivePrayerUI -.->"Batch Past Points Context"| Proxy
    JournalUI -.->"Batch Past Points Context"| Proxy
    AddingUI -.->"Async Title Generation (Post-Commit)"| Proxy
    Proxy --> LLM
```

---

## 5. Multi-Session Development Governance & Concurrency Rules

To preserve software integrity when multiple autonomous agents are dispatched concurrently:
1. **Repository Synchronization**: Agents operate in a shared codebase and must assume peer presence; files must be checked and re-read before edits to avoid race conditions.
2. **Build & Compiler Protection**: Gradle daemons and testing pipelines must be serialized; commands that kill daemons (`--stop`, `clean`) are strictly barred during parallel execution.
3. **Consolidated Releases**: Deployments to `G:\My Drive\myApps\Prayer.apk` are consolidated to prevent file lock write collisions and guarantee that released builds contain all merged, peer-validated features.

