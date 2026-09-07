# User Experience (UX) & Product Design Specification

**Document:** `planning/UX.md`  
**Status:** Approved Design Principles & User Journey Map  
**Application Title (Unofficial):** *Pray Without Ceasing* (1 Thessalonians 5:17)  
**Last Updated:** 2026-09-07  
**Target Audience:** Product Owner, Business Stakeholders, Product Designers  
**Platform Focus:** Mobile App (Clean, solemn, distraction-free companion)  

---

## 1. Product Vision & Emotional Tone

The Prayer app is conceived as a quiet, sacred personal vault. Most consumer apps rely on vibrant colors, badges, notifications, and social feeds designed to capture and monetize user attention. In contrast, this app is designed to **give attention back to the user's interior life, personal relationships, and communion with God**.

### 1.1 Core Experience Pillars
- **Radical Simplicity & Blank Entry**: The application opens to a serene, completely uncluttered screen with strictly three options: **Start praying**, **Open Journal**, and **Add prayer points**. There are no distracting dashboards, activity feeds, or cluttered widgets on launch.
- **Strictly Passive Full-Screen Prayer Mode**: When in Prayer mode (*Start praying*), the UI transitions into an immersive, full-screen sanctuary: strictly zero buttons, zero card tiles, and zero grid lines. There are no check-off boxes, editing tools, or progress bars. The view presents exclusively the name of the entity being prayed for preceded by "Praying for" (e.g., *Praying for Sarah*), followed directly by the prayer points in serene typographic whitespace.
- **Zero-Gap Contiguous Geometry**: In navigation, prayer entry, and journal views, every surface, tile, card, and button is strictly adjacent to its neighbors, leaving zero visible gaps, floating margins, or padding gutters between UI elements. Tiles share 1px hairline boundary seams to form a continuous, unified architectural plane. Rounded corners, curved pill shapes, drop shadows, and bubble cards are strictly prohibited.
- **Reverent & Solemn**: The interface feels like an architectural tablet or solemn liturgical folio. Content is communicated through crisp typography, ample whitespace, and subtle hairline dividing rules.
- **Austere Simplicity (Zero Explanatory / Tutorial Text)**: As an unyielding design principle, the application shall never display explanatory, onboarding, or tutorial-like text on its UI elements. Buttons, tiles, and headers present strictly functional labels without descriptive sub-captions or instructional crutches.
- **Strict Prohibition of Ordinal / Sequential Labels ("Point 1", "Point 2")**: As an absolute design principle, the application shall never code, render, or display arbitrary sequential counters, numeric badges, or ordinal enumerations (e.g., *Point 1*, *Point 2*, *Point 1 of N*, *Item 1*) anywhere in the UI. Prayer points before Almighty God are sacred burdens of prayer, never numbered tickets or items on a bureaucratic checklist. Each prayer point is recognized and presented exclusively by its meaningful title and descriptive content.
- **Minimal Contextual Data Exposure**: Just because data is stored, calculated, or available in the backend does not mean it has value to show to the user. The interface shall strictly expose only the absolute least amount of data relevant to the immediate devotional context. Progress counters (`Topic 1 of 8`), database tallies (`7 points (5 active)`), entity taxonomy tags, and queue sequence metrics are ruthlessly suppressed from devotional and navigation views.
- **Sequestered Configuration (Zero Clutter)**: The interface is never cluttered with configuration, settings, or display options. All user preferences reside in a dedicated Settings panel navigated to only when needed from the Journal, preserving an austere, quiet devotional space.
- **Zero Emojis & Zero Gamification**: Emojis, streak counters, celebration popups, badges, and animations are strictly excluded to preserve dignity and respect.
- **Uncompromised Privacy**: Zero accounts, zero login, zero public feeds, and zero telemetry. All prayer data resides exclusively on the user's physical device.

---

## 2. Visual Identity: High-Contrast Monochrome & Contiguous Tessellation

The product uses a pure black-and-white visual identity that reflects dignity, simplicity, architectural order, and liturgical focus.

### 2.1 The Two Visual Modes
Users can toggle between two high-contrast modes depending on environment and preference, with **Morning Light** serving as the default:
1. **Morning Light (White & Black) — *(Default)***: Clean white canvas with stark black lettering, subtle soft-gray hairline borders, and subdued gray metadata. Tailored for bright reading conditions, daytime contemplation, and structured readability.
2. **Quiet Night (Black & White)**: Pure black canvas with crisp white typography and subtle charcoal dividers. Tailored for evening devotions, bedside prayer, and low-light environments.

### 2.2 Contiguous Geometric Tiles (Zero Gaps & Zero Curved Edges)
- **Strict Orthogonality**: Every card, button, input box, dialog, sheet, and container adheres to sharp 90-degree right angles (`border-radius: 0`). Curved edges, rounded corners, and pill buttons are strictly forbidden.
- **Planar Flat Tiles**: Surfaces are rendered as completely flat, zero-elevation rectangular tiles (`elevation: 0`, zero drop shadows, zero gradient bevels).
- **Zero Visible Gaps & Direct Adjacency**: All on-screen elements (topic prayer points, navigation buttons, input panes, and suggestion options) are directly adjacent to each other. The layout strictly eliminates margins, gutters, and floating card gaps. Elements share 1px hairline boundary seams to form an edge-to-edge architectural mosaic.
- **Architectural & Liturgical Gravity**: The unyielding rectilinear geometry reinforces solemnity, permanence, and reverence, eschewing the casual, bubbly aesthetics of consumer social apps.

### 2.3 Styling & Typography Rules
- **No Decorative Icons**: Pure typographic hierarchy and delicate hairline rules.
- **Subdued Answered State**: In the Journal, when a prayer is answered, the text softens with a subtle strikethrough, visually signaling gratitude and completion while preserving the historical record.

### 2.4 Material 3 Compliance, 8dp Spacing Rhythm & Motion Dynamics
- **Material 3 Alignment with 0dp Planar Contract**:
  - The design system leverages modern Android Material 3 foundations (`androidx.compose.material3`) while strictly preserving the solemn, non-commercial 0dp aesthetic.
  - All Material 3 shape tokens are overridden to `FlatSquareShape` (`RoundedCornerShape(0.dp)`), ensuring that M3 surfaces, dialogs, progress bars, text fields, and cards maintain crisp right angles.
  - Standard M3 component hierarchy:
    - Root layouts leverage M3 `Scaffold` paired with a global `SnackbarHost` for accessible, reverent feedback (e.g. confirming saved prayer points or updates).
    - Top headers implement M3 `TopAppBar` (`TopAppBarDefaults`) with standard `IconButton` actions and 56dp height.
    - Text inputs implement M3 `OutlinedTextField` with `shape = FlatSquareShape`, animated floating labels, active focus rings, and high-contrast monochrome border colors.
    - Card surfaces use M3 `OutlinedCard` with 0dp corners, 0.5dp hairline borders, and generous padding.
    - Buttons employ M3 `Button`, `OutlinedButton`, and `TextButton` with `FlatSquareShape` and bounded ripple effects.
- **Strict 8dp Spacing Grid & Touch Target Ergonomics (`PrayerSpacing`)**:
  - All layouts strictly adhere to the formal 8dp spacing grid tokens (`PrayerSpacing`):
    - `extraSmall = 4.dp`: inline caption gaps, subtle metadata separation.
    - `small = 8.dp`: sub-component spacing, chip padding, field labels.
    - `medium = 16.dp`: standard screen edge padding, card inner padding, list item gaps.
    - `large = 24.dp`: section separation, dialog padding, card margins.
    - `extraLarge = 32.dp`: sanctuary horizontal breathing margins, topic header clearance.
    - `huge = 48.dp`: accessibility touch target minimum constraint (`minTouchTarget = 48.dp`).
    - `primaryActionHeight = 56.dp`: primary action slabs and bottom buttons.
    - `sanctuaryBottom = 72.dp`: contemplative bottom clearance above navigation pills.
  - Window insets are strictly managed via `Modifier.safeDrawingPadding()`, guaranteeing that content naturally avoids notches, camera cutouts, and gesture pills across all fold postures on Samsung Galaxy Flip devices.
- **Solemn Devotional Motion Mechanics**:
  - **Staggered Launch Entrance**: Home screen action slabs glide gracefully into view with staggered fade-and-settle animations (0ms, 60ms, 120ms delays with `FastOutSlowInEasing`).
  - **Liturgical Page-Turn Navigation**: Advancing or retreating between prayer topics in Sanctuary mode uses horizontal slide animations (`AnimatedContent`) with soft crossfades and `FastOutSlowInEasing` (350ms duration). This mimics the deliberate, sacred physical act of turning pages in an Anglican psalter or Book of Common Prayer.
  - **Directional Sub-Screen Transitions**: Navigating deeper into Journal hierarchies slides in from the right; returning slides out to the right (`slideInHorizontally` + `fadeIn` / `slideOutHorizontally` + `fadeOut` with `tween(320, easing = FastOutSlowInEasing)`). Entering Sanctuary prayer uses a solemn crossfade paired with subtle 24dp vertical settling.
  - **List Item Animations**: Dynamic additions, updates, and reordering in Journal lists use `Modifier.animateItem()` for fluid visual continuity.
  - **Candidate Cards Materialization**: AI-generated prayer points glide into view using staggered `AnimatedVisibility` (fade-in + slide-up).
  - **Animated Guidance Progress**: AI inference uses M3 `LinearProgressIndicator` accompanied by animated status transitions ("Attuning...", "Distilling thoughts...", "Formulating prayer points...") via `AnimatedContent`.
  - **Soft Expansion**: Answered prayer sections and sub-views reveal their content smoothly via vertical expansion (`AnimatedVisibility(enter = expandVertically + fadeIn, exit = shrinkVertically + fadeOut)`).
  - **Interactive State Animations**: Status toggles (Active $\leftrightarrow$ Answered) and settings selections animate colors smoothly via `animateColorAsState`.

### 2.5 Lexicon & Devotional Terminology Standard (Translating Functional Concepts to Sacred Language)

The application maintains an intentional, uncompromising distinction between **underlying functional/database concepts** (used by developers and data schemas) and **devotional app language** (experienced by the believer). Raw engineering jargon, military or marketing terms (such as *"target"*), database primitives (*"entity"*, *"record"*, *"row"*), and bureaucratic ticketing nomenclature (*"item"*, *"status"*, *"resolution"*) are strictly translated into reverent, personal, and liturgically grounded terminology:

| Functional / Database Concept | Meaningful App Language | Theological & Liturgical Rationale |
| :--- | :--- | :--- |
| **Target / Target Entity (`entity_id`)** | **`Praying for [Name]`** (in prayer & adding) / **`Person or group`** | Human beings, congregations, and ministries are sacred souls and fellowships held before the Throne of Grace, never clinical "targets" or abstract "entities". |
| **Entity Selection / Step 0 Prompt** | **`Who are you praying for?`** | Replaces clinical selection procedures with a pastoral, relational inquiry. |
| **Create Entity Action** | **`Add a person or group`** $\rightarrow$ **`Add & continue`** | Replaces database object instantiation with natural relational entry. |
| **Picker Source Section** | **`From your journal`** (never *"Or Select Existing"*) | Acknowledges the user's ongoing relational journal rather than a generic database table. |
| **Prayer Point / Record / Row / Item** | **`Prayer Point`** or **`Prayer`** (or unadorned substantive title) | Prayer points before God are solemn intercessory burdens, never items on a checklist or tickets in a queue. |
| **Ordinal Numbers (`Point 1`, `Point 2`)** | **Strictly Prohibited** (Pure title & description) | Every prayer point is a distinct, earnest plea; numbering reduces sacred intercession to administrative accounting. |
| **Capture Modes / Pathway Choice** | **`Lined Notepad`** & **`Prayer Assistant`** | Integrated direct writing on authentic ruled notepad with pushed-down assisted distillation. |
| **AI Raw Reflection / User Input** | **`What is on your heart?`** | Welcomes honest personal reflection rather than demanding form data. |
| **Clarifying Inquiry / Distillation Turn** | **`Clarifying question`** $\rightarrow$ Action: **`Continue`** | Softens AI pipeline jargon into a gentle, focused conversational touchpoint. |
| **Skip Question Action** | **`Skip to prayer points`** (never *"Skip Question Binary"*) | Directly conveys destination without clinical process terminology. |
| **Candidate Points / Review & Commit** | **`Review prayer points`** / **`Prayer points for [Name]`** | Focuses on the prayer content rather than AI generation status. |
| **Commit to Database Action** | **`Save to [Name]`** / **`Save prayer points`** (never *"Save to Entity Vault"*) | Affirms the relational destination in the user's journal rather than disk persistence. |
| **Secondary AI Generation** | **`Suggest 2 more`** (strictly one-time action) | Restrained, functional action without gamified generation prompts. |
| **Active Prayer State** | Clean, unadorned typography | Default posture of ongoing, watchful intercession. |
| **Answered Prayer State** | **`Answered`** (with soft strikethrough in Journal) | Cultivates thanksgiving (*eucharistia*) and praise for God's faithfulness; not a "closed ticket". |
| **Answer / Resolution Note** | **`Thanksgiving note`** (or note of God's faithfulness) | Honors the theological reality that answered prayer produces gratitude to God alone. |
| **Devotional Queue / Balancing Metrics** | **`Start praying`** (queue metrics are 100% silent) | Anti-neglect balancing is a quiet pastoral servant; metrics are never exposed to the user. |
| **Journal Vault Directory** | **`Journal`** (`People`, `Groups`, `General`, `Mission Partners`) | Personal spiritual vault; avoids database administration vocabulary. |
| **Settings Panel** | **`Settings`** (`Appearance`, `Text Size`, `Language`, `Historic Prayers`) | Dignified, quiet preferences; avoids developer terminology like "Config" or "Tiers". |

---

## 3. Screen Structure & Navigation Framework

```mermaid
graph TD
    Launch([App Launch]) --> Home["Blank Home Screen<br/>(1. Start praying | 2. Open Journal | 3. Add prayer points)"]
    Home -->|"Tap 'Start praying'"| PraySession["Full-Screen Prayer Mode<br/>(No Buttons, No Tiles, 'Praying for [Name]')"]
    Home -->|"Tap 'Add prayer points'"| AddStep0["Who are you praying for?<br/>(Add or Choose Person/Group)"]
    AddStep0 -->|"Selected Focus"| AddStep1["Praying for [Name]<br/>(Lined Notepad & 'Prayer Assistant')"]
    Home -->|"Tap 'Open Journal' / Swipe Left"| Journal["Journal Directory<br/>(People | Groups | General | Mission Partners)"]
    
    Journal --> PeopleRoot["People Root<br/>(Personal & Distinct Relational Sphere)"]
    Journal --> GroupsRoot["Groups Root<br/>(Church, Teams, Communities)"]
    Journal --> GeneralRoot["General Root<br/>(World, Global, Historic Prayers)"]
    Journal --> MissionRoot["Mission Partners Root<br/>(Supported Missionaries & Agencies)"]
    Journal --> Settings["App Settings<br/>(Appearance, Text Size, Language, Historic Prayers)"]
```

### 3.1 The Blank Entry Screen
Upon launch, the user meets an uncluttered, contiguous canvas featuring strictly three centered flat action tiles with unadorned labels and zero explanatory sub-text:
1. **Start praying**
2. **Open Journal**
3. **Add prayer points**

The home screen features strictly zero hero headers, wordmarks, app logos, or arbitrary header titles. The three action slabs divide the screen vertically and contiguously edge-to-edge.

### 3.2 The Journal (Open Journal / Swipe Left)
Tapping **Open Journal** (or swiping left from the blank home screen) glides into the structured Journal and management vault, rooted in four foundational domains:
1. **`People`**: Exclusively and strictly specific, distinct individual relationships (e.g., spouse, children, parents, a single named friend/neighbor, and personal prayer points under *Me*—including personal health, job trials, or spiritual sanctification situated within a workplace or hospital).
2. **`Groups`**: Collectives, communities, and shared peer/work environments (e.g., work colleagues, office team, church congregation, small group, committee, ministry).
3. **`General`**: Broad topics, global concerns, personal spiritual disciplines, and the preloaded collection of historic Reformed prayers.
4. **`Mission Partners`**: Supported missionary families, mission agencies, missionaries, and ministry partners (e.g., a missionary family on the field, a Bible translation agency, a church planting ministry).

---

## 4. Core User Journeys

### 4.1 Journey 1: "Start Praying" (Full-Screen Prayer Mode)

```mermaid
graph LR
    Launch["Tap 'Start praying'"] --> PrayerCanvas["Full-Screen Prayer Canvas<br/>(No Buttons, No Tiles, No Grid)"]
    PrayerCanvas --> Header["Heading: 'Praying for [Entity]'"]
    Header --> Points["Pure Typographic Prayer Points"]
    Points --> Gestures["Advance Topic (Tap / Swipe)"]
    Gestures --> Exit["Exit to Home (Swipe Down / Esc)"]
```

1. **Immediate Immersion & Balanced Curation**: 
   - Tapping **Start praying** instantly immerses the user in full-screen prayer mode without configuration popups, filters, or setup steps.
   - **Curated Feed Balancing (Anti-Neglect)**: The local engine balances the queue using each topic's retained interaction count (`interacted_count`) and timestamp (`last_interacted_at`). Topics that have never been prayed for, or have the oldest last-interacted dates and lowest counts, are prioritized at the front of the queue so that no relationship, small group, or burden is forgotten.
2. **Full-Screen, Tileless, Gridless Typographic Presentation**:
   - The top navigation bar, bottom action dock, and on-screen buttons are completely suppressed (`display: none`).
   - The canvas contains **no boxy card tiles, no borders, and no grid lines**.
   - **Heading**: The screen begins with a clean, reverent heading stating the name of the entity preceded by "Praying for" (e.g., `Praying for Sarah`, `Praying for Parish Council`).
   - **Prayer Points**: Directly underneath, each prayer point is presented with its title and body separated by generous typographic whitespace.
   - **Design Principle: Strict Prohibition of Ordinal Labels ("Point 1", "Point 2")**: Contemplation views and prayer lists present strictly the prayer point title and description. Coding or rendering useless sequential labels or numeric badges (such as *Point 1*, *Point 2*, *Point 1 of N*, status tags, or ticket categories) is strictly prohibited.
   - **Expandable Answered Prayer Points**: Answered prayer points for that topic are rendered softly beneath active prayer points without boxy tiles or borders.
3. **Buttonless Gestural Navigation (Swipe-First Mobile Ergonomics)**:
   - Because on-screen buttons are completely eliminated to preserve solemn contemplation, topic progression leverages the full tactile capability of smartphones:
     - **Swipe Left (`ΔX < -40px`) / Tap right 75% / ArrowRight**: Advances to next prayer topic. Primary thumb action on mobile.
     - **Swipe Right (`ΔX > +40px`) / Tap left 25% / ArrowLeft**: Returns to previous prayer topic.
     - **Swipe Down (`ΔY > +60px`) / Tap Top Edge / Escape**: Exits prayer mode and returns immediately to the home screen.
   - Progressing past a topic automatically and silently increments the topic's `interacted_count`, updates its `last_interacted_at` timestamp, and touches `last_interacted_at` across all constituent active prayer points without demanding manual interaction.
4. **Self-Paced & Open-Ended**:
   - Each screen represents a complete topic.
   - There is no mandatory session quota or timer. The user determines the length of their devotion, advances topic by topic, and simply exits back to the home screen whenever they choose.
5. **Zero-State Fallback (Curated Reformed Prayers)**:
   - If the user has not yet recorded any personal prayer points, *Start praying* presents preloaded prayers from a curated selection of historic **Reformed, Protestant** classics:
     - The Lord's Prayer
     - Classic Anglican Book of Common Prayer (BCP) collects
     - The Apostles' Creed
   - Once the user adds personal prayers, these historic prayers permanently reside under `General → Historic Prayers`, with a user toggle to either blend them into daily prayer rotations or keep them accessible strictly on demand.

---

### 4.2 Journey 2: "Add Prayer Points" (Capture & Articulation)

Tapping **Add prayer points** from the home screen initiates a structured, person- and group-first workflow. Because every prayer point in the application belongs to a specific person, group, general concern, or mission partner (`INDIVIDUAL_ENTITY` under `People`, `Groups`, `General`, or `Mission Partners`), **the initial step strictly asks who is on the user's heart before capturing prayer points**:

```mermaid
graph TD
    TapAdd["Tap 'Add prayer points'"] --> Step0["Initial Step: Who are you praying for?<br/>(Add or Choose Person/Group)"]
    
    Step0 -->|"Choose from Journal"| FocusSelected["Praying for [Name]<br/>(e.g., Sarah / Parish Council)"]
    Step0 -->|"Add Person / Group"| CreateNew["Add Person / Group Tile<br/>(Name + People, Groups, General, or Mission Partners)"]
    CreateNew --> FocusSelected
    
    FocusSelected --> LinedNotepad["Integrated Lined Notepad Canvas<br/>(Ruled notepad lines, auto-bullets, Save)"]
    LinedNotepad --> CommitDirect["Save to [Name]"]
    
    FocusSelected -->|"Tap 'Prayer Assistant' (pushed down)"| Step1["Step 1: Open Heart<br/>'What is on your heart?'"]
    Step1 --> Step2["Step 2: Clarifying Question<br/>(1 open-ended question; max 2 turns)"]
    Step2 -->|"Answer (up to 2 turns)"| Step2
    Step2 -->|"Skip to prayer points / Finished"| Step3["Step 3: Review Prayer Points<br/>(2 concise prayer points tailored to [Name])"]
    Step3 -->|"Save to [Name]"| CommitGuided["Saved to [Name] in Journal"]
```

#### Step 0: Mandatory Initial Step — "Who are you praying for?"
Before drafting prayer points, the user specifies the person or group:
1. **"From your journal" Quick-Picker**: An edge-to-edge list of recent people and groups grouped by `People`, `Groups`, `General`, and `Mission Partners`. Tapping any name immediately binds the context (*Praying for [Name]*) and advances to capture mode.
2. **"Add a person, group, general topic, or mission partner" Tile**: A contiguous hairline creation area at the top of the list allowing immediate entry:
   - Enter name (e.g., *"David"*, *"Youth Ministry"*, *"Wycliffe Bible Translators"*).
   - Select sphere (`People`, `Groups`, `General`, or `Mission Partners`).
   - Tapping **Add & continue** creates the person/group locally and proceeds immediately into adding prayer points.

---

#### Pathway A: Integrated Lined Notepad (Direct Writing, Ruled Lines, Auto-Bullets & Post-Commit Auto-Titling)
Selecting an entity immediately reveals a dedicated, contemplative lined notepad for writing prayer points directly—removing the intermediary "Direct Entry" button:
- **Authentic Physical Notepad Ruled Lines**: The entire writing surface is rendered with subtle horizontal ruled lines spaced evenly with the font line-height (32sp / 32dp), providing the tactile reverence of pen and paper on a personal devotional pad.
- **Pushed-Down Secondary Action**: The lined notepad expands vertically to take up the vast majority of the screen (`Modifier.weight(1f)`), pushing the secondary **"Prayer Assistant"** button down to a 72dp action slab at the very bottom of the screen.
- **Zero Title Field**: The user is never prompted for or shown a title field when adding new prayer points. The view presents strictly an unadorned ruled text canvas bound to the target person or group.
- **Auto Bullet-Point Writing Pad**: The writing pad automatically formats input into bullet points:
  - Focus or initial activation auto-seeds the pad with a bullet prefix (`• `).
  - Pressing line space (<kbd>Enter</kbd> / <kbd>Return</kbd>) automatically generates a new bullet on the subsequent line (`\n• `).
  - Pressing <kbd>Enter</kbd> or <kbd>Backspace</kbd> on an empty bullet removes the bullet cleanly to allow ending lists without friction.
- **Immediate Local Save**: When prayer points are typed, a primary action button **Save to [Name]** appears immediately beneath the notepad (and a complementary "Save" action is enabled in the top header), immediately committing the text to the encrypted local SQLite database.
- **Post-Committal Branched AI Title Generation**: After local saving, an asynchronous lightweight branch of the AI engine generates a concise 2–6 word prayer point title in the background, updating the record (`UPDATE PRAYER_POINT SET title = ?`).
- **Theological Validation Exemption**: Because this branch performs exclusively the straightforward summarization of user-committed text into a brief label, theological validation is not required.
- **Offline Fallback**: When offline, the prayer point is stored with a truncated text preview as an interim label until background connectivity generates the permanent title.

---

#### Pathway B: "Prayer Assistant" (Objective AI-Assisted Articulation)
Designed for when thoughts regarding the selected person or group are tangled, heavy, or difficult to articulate:
- **App Auto-Conversion to JSON**: The mobile app transparently packages the user's reflection, the pre-selected entity context (`root` and `group`), and turn progression into a structured JSON payload sent across the wire.
- **Privacy Gate & Pre-Specified Root Bypass**: Because the entity is selected upfront, personal entity names are masked on-device prior to network transmission. The upstream model is explicitly instructed that the category context is pre-specified (`suggested_root: null`), tailoring candidate prayer points strictly to the selected person or group without redundant categorization prompts.
- **Step 2 (Neutral Distillation)**:
  - The client masks personal entity names prior to sending. The suggestion engine analyzes the raw entry and asks clarifying questions **one at a time**.
  - **Tone & Style**: Strictly neutral, concise, and objective. **Not a therapy bot**—zero artificial empathy, zero psychological framing, and zero conversational filler.
  - **Open-Ended Inquiries & Actionable Clarity**: Questions expect the user to provide the substance. It must not use leading questions or assume intent unless the user explicitly asks for suggestions. If a clear actionable point is not obvious from the user's reflection, the system must never jump to speculative prayer cards; it promptly poses strictly one concise question (6–12 words) in natural, plain English without bureaucratic templates. It distinguishes between internal emotional states (asking plainly what is causing the feeling, e.g., *"What is making you feel anxious right now?"*) and external entities/topics (asking plainly what is happening, e.g., *"What is going on with your boss that you'd like to pray about?"*). When multiple competing crises are presented simultaneously, the engine performs concise burden triage (*"Which of these is weighing on you most heavily right now?"*).
  - **Skip Question Binary**: While a clarifying inquiry is default-mandatory, the engine evaluates a `skip_question` binary flag, advancing directly to Step 3 prayer points only when initial input is already exceptionally comprehensive and unambiguous.
  - **Unconditional Question Skipping**: The user can skip any question the app asks at any point. Skipping immediately guarantees that no more questions will be asked during that session; the flow bypasses all remaining inquiry and transitions directly to Step 3 (Review & Commit).
  - **Bounded Interaction**: Hard ceiling of **maximum 2 question turns**. A prominent **"Skip to prayer points"** (or **"Skip"**) action is always provided on every question.
- **Step 3 (Review & Action — Flat Tessellated Prayer Points)**:
  - Presents strictly and exactly **2 concise candidate prayer points** (never 1, never 3), rendered as flat, geometric rectangular tiles with sharp 90-degree right angles, zero curved edges, and zero drop shadows.
  - Grounded strictly in facts provided by the user; the assistant never presumes or fabricates illnesses, cancer, or medical crises.
  - **Contextual Pre-specification Bypass**: When `root` (and optional `group`) was already prespecified upon entry, category suggestions are omitted entirely ("suggestion isn't needed"). The cards directly reflect the pre-selected context without asking the user to confirm or re-categorize. When entry was uncontextualized, suggested root categories (`People`, `Groups`, `General`, `Mission Partners`) are presented.
  - Entity name suggestions are strictly not required and omitted for privacy; assignment to the person or group is performed locally on-device.
  - **Objective Prayer Points, Never Scripted Prayers**: Candidate cards show discrete, telegraphic prayer points and burdens—never pre-written prayers addressing God directly (no "Father God...", "Lord...", or second-person prayer prose). The user does the praying; the cards organize the prayer points.
  - **Telegraphic Card Aesthetics & Strict Brevity**: Phrased in compact shorthand (using symbols like `&`, `→`, `↑`, standard abbreviations like `govt`, and omitting filler articles/auxiliary verbs; the abbreviation `w/` or `/w` is strictly excluded in favor of "with" or omission) to maximize glanceability and minimize screen clutter on mobile cards. Strictly constrained to **2–6 word titles** (targeting 2–4 words, hard ceiling of 6 words) and a hard ceiling of **maximum 20–25 words per description** (never lengthy multi-sentence paragraphs). Employs a high-level description structure direction: cards stay telegraphic and scannable, but are explicitly not confined to a fixed clause template or ordering — clause count and punctuation (semicolons, em-dashes, commas) vary naturally to express each burden distinctively rather than reading as clones of one formula.
  - **Resilience Across Wordiness & Emotional Load**: The distillation experience is stress-tested against a 100-case benchmark dataset ([`api/test/prayer_requests_stress_test.json`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/test/prayer_requests_stress_test.json)) ensuring that whether the user enters a 5-word cryptic fragment or a 250-word emotional ramble, the candidate cards consistently honor the 2–6 word title and $\le$ 25-word telegraphic description standard, with verified live responses documented in [`api/test/BENCHMARK_RESULTS.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/test/BENCHMARK_RESULTS.md) and textual quality evaluated in [`api/test/AI_GENERATED_TEXT_REPORT.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/test/AI_GENERATED_TEXT_REPORT.md).
  - **Two-Tier Model Experience**: To ensure prayer points feel personal, thoughtful, and spiritually grounded rather than mechanical or formulaic, the backend employs a Two-Tier pipeline: Tier 1 (`temperature: 1.0`, `top_p: 0.95`, `max_tokens: 9000`) generates natural, thoughtful, sober drafts without clinical triage tags or cheesy/overly poetic melodrama, while Tier 2 (`temperature: 0.1`, `max_tokens: 9000`) compresses and verifies the text into telegraphic, scannable descriptions (no rigid clause template, natural variation per burden) with 2–6 word titles, guaranteeing both heartfelt resonance and scannable mobile brevity.
  - **Graceful Quota & Offline Fallback**: If network connectivity is unavailable or the daily operational quota is exhausted, the UI quietly presents: *"The assistant is currently unavailable. You can record your prayer points directly."* Users are seamlessly transitioned to direct manual recording without disruption.

---

### 4.3 Journey 3: "Open Journal" (Structural Journal & Directory)

Tapping **Open Journal** (or swiping left from the home screen) opens the complete management journal:
1. **Hierarchy Browsing**:
   - Drill into **`People`**, **`Groups`**, **`General`**, or **`Mission Partners`** to see all associated entities and active prayer points.
2. **Editing Saved Prayer Points (Click-Once Entry & Title Editing)**:
   - **Click Once to Start Editing**: In the Entity Detail view, clicking or tapping any saved prayer point once immediately transitions into edit mode for that prayer point.
   - **Editable Title**: The prayer point title (initially auto-generated upon creation) is fully exposed and editable. Users can freely modify, refine, or rename the title to reflect evolving pastoral circumstances.
   - **Editable Body & Auto-Bullets**: The prayer point body is fully editable, maintaining the auto bullet-point behavior (line space triggers a new bullet).
   - **Status & Thanksgiving Toggle**: Within the editor, users can mark the prayer point as `Active` or `Answered` (with an optional thanksgiving note). Answered points in the list display with subdued text and an ultra-faint strikethrough line (`rgba(255, 255, 255, 0.2)` in dark mode / `rgba(0, 0, 0, 0.2)` in light mode) so the text remains cleanly legible while visually resolved.
3. **Permanent Deletion (`Delete prayer point`)**:
   - Believers can permanently remove prayer points that have ended or were added in error.
   - Tapping **Delete prayer point** reveals a stark, planar confirmation prompt (*"Delete this prayer point? This cannot be undone."*).
   - Confirming permanently purges the record from local SQLite storage (`DELETE FROM PRAYER_POINT WHERE id = ?`).
   - Editor controls: **Save changes**, **Cancel**, and **Delete prayer point**.
4. **Settings Access**:
   - Theme toggle (*Morning Light* [Default] vs. *Quiet Night*).
   - **Text Scaling**: Dedicated three-tier selector with pronounced, distinct jumps (**Large** *(Default)*, **Regular**, and **Compact**). The **Large** default features prominent, high-presence typography (28px home buttons, 22px card titles, 18px prayer points) for effortless, strain-free devotional contemplation. **Regular** offers balanced density (20px buttons, 16px card titles, 13px prayer points), and **Compact** provides high-density scannability (15px buttons, 12.5px card titles, 10.5px prayer points).
   - **Language / Dialect configuration**: Dedicated selector between **English (Australian / UK)** *(Default)* and **US English**. Switching immediately adjusts UI copy, prayer collects, and distillation orthography.
   - Historic Reformed prayers rotation toggle (blend into daily rotation vs. library-only).
   - Local encrypted database backup and export.

---

## 5. Smartphone Gestural System (Swipe & Touch Paradigms)

Smartphones are tactile, touch-first instruments. Devotional prayer frequently occurs during personal quiet time, walking, or moments where the phone is held in one hand. Relying strictly on small buttons or tap zones demands visual targeting, pulling the believer's focus away from prayer. 

The application elevates **Swipe Input** to a first-class interaction standard across all touch form factors:

### 5.1 Full-Screen Prayer Mode Gestures
In the buttonless, gridless prayer canvas, navigation between topics is governed primarily by horizontal thumb swipes:
- **Swipe Left (`ΔX < -40px`)**: Advances to the **Next topic** in the balanced anti-neglect queue. Matches the universal physical metaphor of turning forward to the next page of a prayer book.
- **Swipe Right (`ΔX > +40px`)**: Returns to the **Previous topic**.
- **Swipe Down (`ΔY > +60px` initiated in upper half of screen)**: Dismisses prayer mode and returns immediately to the Home screen.
- **Accessibility Fallbacks**: Tapping the right 75% of the screen, tapping the left 25%, tapping the top edge, and hardware keyboard arrows (`ArrowLeft`, `ArrowRight`, `Escape`, `Space`) remain operational as complementary fallbacks.

### 5.2 List & Card Swipe Actions (Journal & Entity Detail)
In directory and prayer point views, individual cards support native mobile list swipe gestures:
- **Swipe Card Right (`ΔX > +60px`)**: Instant status toggle between `ACTIVE` and `ANSWERED`. Provides immediate tactile satisfaction without forcing the user into edit mode just to mark answered prayer.
- **Swipe Card Left (`ΔX < -60px`)**: Reveals destructive management action (**Permanent Delete**), presenting an immediate planar confirmation dialog.
- **Tap / Single Click**: Opens the full Prayer Point Editor for title and body refinement.

### 5.3 Long-Press Responsiveness & Planar Contextual Actions
Across all touch-first views, long-pressing interactive devotional records or entities delivers an immediate, subtle tactile haptic vibration (10–15ms system `LongPress`) and surfaces stark, planar contextual action dialogs complying with the 0dp, zero-curved-edge architectural contract:
1. **People & Groups (Journal & Add-Flow Selection)**:
   - **Long Press Trigger**: Holding any person or group tile in the Journal directory or Add-flow quick picker (`LogStep.SELECT_ENTITY`) triggers haptic feedback and reveals a planar context menu:
     - **`+ Add prayer point`**: Immediately initiates adding a new prayer point anchored to this person or group.
     - **`Edit name & category`**: Opens an in-place editing dialog to rename the person/group or change their sphere (`People` $\leftrightarrow$ `Groups`). (Protected/hidden for preloaded historic collections).
     - **`Delete`**: Surfaces a planar confirmation dialog (*"Delete this person or group and all associated prayer points? This cannot be undone."*), permanently removing the entity and cascading deletion across all associated prayer points.
2. **Individual Prayer Records (Journal Entity Detail)**:
   - **Long Press Trigger**: Holding any saved prayer point tile in an entity detail list triggers haptic feedback and surfaces:
     - **`Mark as Answered` / `Mark as Active`**: Instant status toggle without requiring navigation into the full editor.
     - **`Edit prayer point`**: Navigates directly into the title, body, and thanksgiving editor.
     - **`Delete`**: Surfaces a planar confirmation dialog (*"Delete this prayer point? This cannot be undone."*) to permanently purge the record.
3. **Full-Screen Sanctuary Prayer Mode**:
   - **Long Press Trigger**: Holding any active or answered prayer point in full-screen sanctuary mode triggers haptic feedback and surfaces a quiet, solemn dialog:
     - **`Mark as Answered` / `Mark as Active`**: Allows the believer to record answered prayer in real time during devotion without leaving sanctuary mode.
     - **`Dismiss`**: Closes the dialog and immediately resumes contemplative focus.

### 5.4 Global Navigation, LIFO Back Stack & Edge-Swipe Back
- **Last-In, First-Out (LIFO) Back Stack Architecture**: All navigation follows a strict LIFO hierarchy preserving nested context across sub-screens:
  - Adding flows initiated from within an entity detail screen (`Journal` $\rightarrow$ `EntityDetail` $\rightarrow$ `LogPrayer`) pop back directly to that entity's detail view in the Journal, rather than abruptly collapsing to Home.
  - Multi-tier sub-stacks (e.g. `JournalView` in `JournalScreen` and `LogStep` in `LogPrayerScreen`) handle granular view navigation independently before delegating to top-level application navigation.
- **Swipe Right from Left Screen Edge (`Xstart ≤ 25dp, ΔX ≥ 50dp, |ΔX| ≥ 1.5|ΔY|`)**: Universal edge-swipe back navigation. Mimics the physical act of turning back a leaf in a devotional journal or prayer book. Seamlessly navigates back one hierarchical level from any sub-screen (`SanctuaryPrayerScreen` $\rightarrow$ `Home`, `EntityDetail` $\rightarrow$ `Journal` $\rightarrow$ `Home`, `LogStep.PrayerAssistant` $\rightarrow$ `LogStep.CaptureMethod` $\rightarrow$ `LogStep.TargetSelection` $\rightarrow$ `Home`).
- **Unified Platform & Gestural Back Traversal**: Edge-swipe right is fully harmonized with the Android system back handler (`BackHandler`), ensuring identical, predictable LIFO traversal regardless of whether the user taps the hardware/system back button, invokes system gesture navigation, or uses the in-app edge swipe.

### 5.5 Home Screen Gestural Pathway
- **Swipe Left on Home Canvas (`ΔX < -50px`)**: Directly slides into the Journal directory, providing immediate, fluid access to the spiritual records vault.

---

## 6. Mobile Ergonomics & Accessibility

- **Samsung Galaxy Flip & Foldable Ergonomics**:
  - **Tall Aspect Ratio (22:9 / 21.9:9)**: Optimized for tall vertical viewports (such as 360x740dp to 412x960dp on Galaxy Z Flip). Slabs stretch edge-to-edge; prayer sanctuary mode centers prayer points comfortably within the natural upper and middle reading zones.
  - **Single-Handed Thumb Zone**: Primary controls, bottom action slabs, and swipe triggers sit within the lower 60% thumb sweep radius, allowing complete operation without awkward finger gymnastics.
  - **Foldable Posture & Flex Mode**: When placed in half-folded flex posture on a flat surface or bedside table, the top screen maintains the reverent contemplation canvas while touch/swipe zones remain active on the bottom panel.
- **Thumb-Zone Navigation**: Primary actions (*Save*, *Cancel*, swipe gestures) sit comfortably within the natural thumb sweep radius on modern smartphone screens.
- **Frictionless Entry**: Zero account creation, zero login, and zero API key configuration. Users are able to pray or add prayer points within one second of opening the app.
- **Instant Local Performance**: All read and write operations interact directly with the embedded SQLite database on device, guaranteeing zero network lag or loading spinners.
- **Haptic Feedback**: Light system haptics (10–15ms) accompany successful swipe triggers (topic advance, answered toggle, delete reveal) to provide silent, non-distracting tactile reassurance.

---

## 7. Automated UI Layout & Gestural Contract Testing

To guarantee that the austere, non-commercial, liturgical visual identity is never degraded by accidental style drift, the user experience is continuously verified by automated UI layout and contract test suites across three mobile viewports (Standard 390x844, Compact 360x740, and Large 428x926):
1. **Zero-Radius Contract**: Automated DOM traversal asserting `borderRadius === '0px'` across all interactive components, buttons, inputs, tiles, and dialogue surfaces.
2. **Contiguity & Adjacency Contract**: Sub-pixel bounding rect measurement confirming that adjacent slabs (such as the three vertical home slabs and journal tiles) maintain 0px margins/padding gutters and directly abut along 1px hairline boundary seams (`Math.abs(rect[i+1].top - rect[i].bottom) <= 1.5px`).
3. **Monochrome Contrast & Theme Invariants**: Programmatic verification of color tokens in Morning Light (`--bg: #FFFFFF`, `--text: #111111`) and Quiet Night (`--bg: #000000`, `--text: #FFFFFF`), maintaining WCAG AAA contrast ratios.
4. **Three-Tier Typography Scale Verification**: Automated evaluation asserting exact font size tokens across Large (Default), Regular, and Compact scales.
5. **Full-Screen Buttonless Sanctuary Invariant**: Automated assertion verifying that in `screen-pray`, `#top-bar` and all buttons are suppressed (`display: none`), with zero card borders or gridlines.
6. **Tactile Gesture Discrimination Testing**: Synthetic touch event simulation testing horizontal topic advance ($\Delta X \le -40\text{px}$), return ($\Delta X \ge +40\text{px}$), swipe-down exit ($\Delta Y \ge +60\text{px}$ in upper screen), and diagonal rejection ($|\Delta X| < 1.5 |\Delta Y|$).
7. **Negative Lexicon & Anti-Ordinal Audit**: Programmatic scanning of rendered text across all views asserting zero forbidden clinical terms (`target`, `entity`, `ticket`, `commit`, `sqlite`) and zero ordinal labels (`Point 1`, `Item 1`, `Point 1 of N`).

In the production native Android client ([`android/app/src/test/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test)), these contract tests are replicated and verified natively in Kotlin (`LayoutGeometryTest.kt`, `GestureEngineTest.kt`, `LifoBackStackTest.kt`, `LexiconContractTest.kt`, `DevotionalFlowsTest.kt`, `AntiNeglectQueueTest.kt`, `TheologicalGuardrailsTest.kt`, `PrayerApiClientTest.kt`, and `DataModelsTest.kt`), totaling 64 automated tests passing with 100% compliance.
