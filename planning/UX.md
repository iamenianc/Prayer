# User Experience & Product Design Specification (UX.md)

**Document:** [`planning/UX.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/UX.md)  
**Status:** Living Architectural UX Contract & Design System  
**Application Title:** *Pray Without Ceasing* (1 Thessalonians 5:17)  
**Design Reference:** Canonical Visual Design Bible in [`planning/android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md) (Strictly Read-Only)  
**Target Form Factors:** Samsung Galaxy Flip / Foldables, Standard Android Handhelds, Tablets  

---

## 1. Objective & Product Philosophy

The *Pray Without Ceasing* mobile companion is a pure-text, liturgical Android prayer journal. It deliberately departs from sterile, cold digital minimalism and rejects gaudy, dated 2011-era skeuomorphism.

The core design philosophy is **Modern Leatherbound Craft (The Modern Folio)**:
- **Structural Metaphor**: The tactile warmth, physical containment, and romance of an analog leatherbound journal with fine 120gsm ruled stationery (such as a Midori Traveler's Notebook or Leuchtturm1917).
- **Modern Vector Detailing**: Flat, responsive vector surfaces, whisper-thin tonal perimeter separation hairlines (`1dp` at 15% darker alpha), and refined ambient occlusion rather than fake stitch bitmaps or plastic drop shadows.
- **Liturgical Gravity**: A calm sanctuary for prayer and intercession, honoring quiet communion with God without commercial noise, account logins, or social feeds.

---

## 2. Core Information Architecture & The Five Primary Pathways

The application is strictly organized around five primary functional pathways radiating from the Frontispiece Home Screen:

```
                          ┌──────────────────────────┐
                          │     HOME FRONTISPIECE    │
                          │   (PRAY WITHOUT CEASING) │
                          └─────────────┬────────────┘
                                        │
           ┌────────────────────────────┼────────────────────────────┬────────────────────────────┬────────────────────────────┐
           ▼                            ▼                            ▼                            ▼                            ▼
  [ Start praying ]            [ Open Journal ]             [ Add prayer points ]        [ Library ]                  [ Notes ]
         │                              │                            │                            │                            │
         ▼                              ▼                            ▼                            ▼                            ▼
┌──────────────────┐           ┌──────────────────┐         ┌──────────────────┐         ┌──────────────────┐         ┌──────────────────┐
│  DEVOTIONAL      │           │  JOURNAL         │         │  PRAYER POINT    │         │  THEOLOGICAL     │         │  DAILY NOTES     │
│  SANCTUARY       │◄─────────►│  DIRECTORY       │◄───────►│  CAPTURE         │         │  LIBRARY         │         │  & REFLECTIONS   │
│  (Anti-Neglect   │ (Swipe L) │  (People, Groups,│ (Add to │  (Lined Notepad  │         │  (Bookshelf &    │         │  (Freeform Rule, │
│   Queue, 30/40/30│           │   General, Miss.)│  Entity)│   Direct Entry)  │         │   Catalog)       │         │   Date Titled)   │
│   Zoning)        │           └────────┬─────────┘         └────────┬─────────┘         └────────┬─────────┘         └────────┬─────────┘
└──────────────────┘                    │                            │                            │                            │
                                        ▼                            ▼                            ▼                            ▼
                               ┌──────────────────┐         ┌──────────────────┐         ┌──────────────────┐         ┌──────────────────┐
                               │  ENTITY DETAIL & │         │  COMMITTAL &     │         │  VOLUME READER   │         │  NOTE DETAIL &   │
                               │  POINT EDITOR    │         │  ASYNC TITLING   │         │  (Calvin 52 Sec, │         │  AI PROMPTS      │
                               └──────────────────┘         └──────────────────┘         │   TOC, Feint R.) │         │  (Praise/Thank/  │
                                                                                         └──────────────────┘         │   Ask God)       │
                                                                                                                      └──────────────────┘
```

### 2.0 Frontispiece Home Screen & Launch Experience (`HomeScreen`)
- **Zero-Flash Cold Boot & Native Splash**: Application launches with `@style/Theme.Prayer` binding `android:windowBackground` directly to warm cream vellum (`#FAF7F0`), paired with an Android 12+ debossed monogram insignia (`ic_splash_monogram`), eliminating jarring white flashes upon launch.
- **Modern Folio Homescreen Launcher Icon (`ic_launcher`)**: Fully compliant with the Singular Folio Law and Modern Leatherbound Craft:
  - **Background (`ic_launcher_background`)**: Solid Saddle Tan vegetable-tanned leather (`#8C532B`, canonical `leather.primary`), strictly rejecting sterile `#000000` black.
  - **Foreground (`ic_launcher_foreground`)**: Garnet Crimson (`#8B2635`) Silk Marker Ribbon tab with authentic 4dp swallow-tail notch draped from the upper margin, paired with a solemn rectilinear Latin cross centered in clean vector leather deboss (`#4A2810`) and warm amber-gold highlight rim (`#33A66B3D`), strictly rejecting stark `#FFFFFF` white.
  - **Android 13+ Themed Icons (`ic_launcher_monochrome`)**: Dedicated monochrome silhouette layer enabling seamless Material You dynamic theming on Samsung One UI and Google Pixel launchers.
- **Solemn Folio Opening Revelation**: Cold launches initiate a serene, non-blocking revelation sequence (~600ms): the closed leather cover with the embossed monogram parts/fades with graceful ease, revealing the illuminated vellum frontispiece and dropping the Silk Marker Ribbon into place. Tapping anywhere instantly skips into the frontispiece for zero-latency access.
- **Folio Casing & Vellum Architecture**: The screen is bounded in the user's active leather dye finish (`colors.leatherActive`), framing a centered 720dp vellum sheet with a delicate double-hairline stationery bookplate border (`0.75dp` stroke in `colors.borderSubtle`).
- **Typographic Frontispiece Inscription**: Features tracked capitals `"PRAY WITHOUT CEASING"` (`12sp`, tracking `2.0sp`), a centered printer's fleuron ornament (`❧`) flanked by hairline rules (`─── ❧ ───`), and the reverent epigraph from 1 Thessalonians 5:16–18.
- **Elevated Action Hierarchy (Tactile Bookplates)**:
  - **"Start praying"** (Devotional Gateway): Elevated sanctuary plate with 2dp elevation, warm ivory stock (`#FFFDF9`), 1.5dp `leatherActive` accent border, and dignified literary typography.
  - **"Open Journal"** (Spiritual Records Directory): Refined stationery card with 0.75dp hairline border (`colors.border`) and ledger navigation glyph `›`.
  - **"Add prayer points"** (Direct Lined Notepad Capture): Matching stationery card with 0.75dp hairline border and bullet/pen prompt.
  - **"Library"** (Theological Vault): Matching stationery card with 0.75dp hairline border, printer's fleuron `❧`, and subtitle `"Classical treatises & historic devotionals"`.
  - **"Notes"** (Daily Reflections & Study Vault): Matching stationery card with 0.75dp hairline border, pen glyph `✎`, and subtitle `"Daily reflections & study notes"`.
  - Ergonomically spaced with 10dp vertical gutters to ensure effortless thumb reach across all five actions on Samsung Galaxy Flip and standard handhelds.

### 2.1 Pathway 1: Devotional Prayer (`SanctuaryPrayerScreen`)
- **Entry**: Tapping **"Start praying"** on Home.
- **Contemplative Immersion**: All top app bars, system chrome, buttons, and progress counters are suppressed.
- **Anti-Neglect Queue Traversal**: Believers cycle through past intercessions prioritizing neglected entities.
- **30% / 40% / 30% Touch Zoning**:
  - **Left 30%**: Tap or swipe right to return to previous topic.
  - **Center 40%**: Reading canvas and long-press for in-place status resolution (Active ↔ Answered).
  - **Right 30%**: Tap or swipe left to advance to next topic.
  - **Swipe Down Anywhere**: Slide downward (`250ms`, Emphasized Accelerate) to exit back to Home.
- **Silk Marker Ribbon**: Anchored at the top margin to flag active intercession or pinned focus. Pinned entities immediately take priority in the Sanctuary prayer queue and are collected under the dedicated Pinned Focus directory header.
- **Subject Header Rendering (Personal vs. Historic)**:
  - **Personal intercession targets** (People, Groups, Mission Partners, user-created General topics): rendered as *"Praying for [Name]"* in Semi-Bold Literary Serif (`22sp` / `32sp`), honoring the relational, pastoral idiom of personal intercession.
  - **Preloaded historic entities** (1662 BCP Collects, Apostles' Creed, Spurgeon pulpit prayers, early church prayers): the entity's `displayName` is rendered directly with Title first, then author/source (e.g., *"Help from on High (C.H. Spurgeon)"* or *"Collect for Peace (1662 BCP)"*) without the "Praying for" prefix, reflecting their liturgical rather than personal character.
  - **AI & Answered Status Suppression**: The read-only prompt section (`❧ Prompts for Prayer ❧`) and active/answered toggle controls are suppressed entirely for preloaded historic entities — their texts are the prayers of the saints; AI augmentation and temporal active/answered toggle notations are neither appropriate nor offered. In both Sanctuary and Journal views, the 37.52dp margin track (reduced by 33% from 56dp) is preserved as an unadorned structural spacer to maintain the red margin guide line and 45.52dp narrative text inset alignment.
- **Silent Background Prompts (Persistent Cache & Background Refresh, Collapsed by Default)**: Read-only suggested lines load silently beneath points without digital loading spinners or chat chrome. The last list of prompts returned by the API is cached persistently in local SQLite (`suggestion_cache`). When the user views an entity, the UI immediately presents the last list of prompts returned the last time the app was used, while an asynchronous background refresh queries for fresh prompts, seamlessly updating the display and cache in place upon completion. In fidelity to the principle of quiet contemplation, prompts are **hidden or collapsed by default** behind a reverent fleuron divider (`❧   Prompts for Prayer   ❧` with `Tap to view prompts`). Prompts only expand when explicitly tapped by the user. When expanded, prompts are organized into three reverent devotional groups—**Praise God**, **Thank God**, and **Ask God**—with group titles set in understated ledger headers (`typography.categoryLedgerHeader`, `colors.inkSecondary`). Each bullet (4–15 words, 3–12 points total across groups, starting with *For*, *That*, *A*, or *Because*, phrased warmly without clinical terseness or verbatim parroting, and strictly avoiding wishy-washy general platitudes) is set in literary italic (`typography.suggestedIntercession`) on condensed `24sp` feint rules, with zero AI labels or badges.
- **Dynamic Ruled Lines, Pinch-to-Zoom & Mathematical Baseline Synchronization**: Feint horizontal ruled lines are dynamically locked to the active text line spacing ($H_{px} = \text{lineHeight}$) across all typography scale tiers. In addition to default typography, multi-touch 2-finger pinch-to-zoom dynamically rescales text and baseline rules between 75% and 250% (`0.75f` to `2.5f`), with persistent zoom saved in SQLite (`TABLE_CONFIG`). Lines are drawn via linear progression $y_k = Y_{anchor} + k \times H_{px}$ anchored to the first text baseline ($y_{start} = Y_{anchor} \pmod{H_{px}}$). All inter-point vertical spacing, headers, and status boxes are quantized to exact integer multiples of $H_{px}$ ($1 \times H_{px}$ blank line between points), ensuring every line of text sits directly ON a feint rule with zero baseline drift across any number of points or wrapped lines. A floating zoom indicator pill (`${percent}% • Reset`) and top bar indicator (`${percent}% ↺`) provide instant reset to 100%.

### 2.2 Pathway 2: Journal Management (`JournalScreen`)
- **Entry**: Tapping **"Open Journal"** or swiping left on Home (`280ms`, Shared-Axis X).
- **Pinch-to-Zoom Scaling**: Multi-touch 2-finger pinch-to-zoom enables flexible reading and drafting magnification (`0.75f` to `2.5f`) matching the Library reader, scaling all journal directory headers, prayer point texts, in-place editing notepads, and prompts proportionally with floating zoom pill and top bar reset triggers.
- **Five Classical Spheres**: Categorized into expandable/collapsible categories:
  1. *People* (Family, friends, individual discipleship)
  2. *Groups* (Small groups, Bible studies, committees)
  3. *Mission Partners* (Missionaries, church plants, global gospel workers)
  4. *General* (World burdens, nation, government, church universal)
  5. *Historic* (Classic collects, confessions, and historic prayers of the saints; Title-first display)
- **Adding Subjects to Spheres**: An option at the top of each personal listing under each group (`+ Add person`, `+ Add group`, `+ Add mission partner`, `+ Add topic`) opens an in-place creation dialog that saves the new person or topic and immediately opens their Entity Detail view. Historic prayers are preloaded devotional content and do not include an add action.
- **Entity Detail Spread & Inline Draft Point**: View active and answered prayer points separated and grouped by date of entry with day and month written in full English words (e.g. *"Friday, 11 September 2026"*) in subtle unflashy typography (`typography.marginStatus` / `colors.inkMuted`). When adding more points to an active record, tapping `+ Add prayer point` at the bottom of the list opens an inline editable draft point directly below existing points on the fine-ruled paper canvas—with `[ DRAFT ]` pill in the 37.52dp margin track, active autofocus, inline multiline dotpoint formatting (`splitIntoDotpoints`), and immediate Cancel / Save Point actions—without navigating away to the dedicated capture screen. Pin with the Silk Marker Ribbon, and access an understated, **collapsed-by-default** prompts card (*"Prompts for Prayer"* with *Show/Hide* toggle) positioned below the `+ Add prayer point` button that expands on demand to display contemplative prompts grouped under **Praise God**, **Thank God**, and **Ask God** (`typography.categoryLedgerHeader`, `colors.leatherPrimary`).
- **Marginal Status Notation (Active & Answered)**: Within the 37.52dp margin track (reduced by 33% from 56dp), active personal prayer points display an analog line-drawn pencil icon (`Icons.Outlined.Edit`) rather than the word "ACTIVE"; answered records display the classic `[ ANSWERED ]` notation pill in Celadon green (`#3D6B52`). Tapping either indicator immediately and optimistically toggles the petition's state with 0ms perceptible lag, instant local UI reflow, background SQLite persistence, and haptic confirmation. For preloaded historic prayers, this marginal indicator is suppressed; the 37.52dp margin track serves as a clean spacer to preserve the red margin rule and text alignment.
- **In-Place Editor (`JournalView.EDIT_PRAYER_POINT`)**: Pure-text editing on `LinedNotepad`. Upon opening, the typing cursor is instantly active with keyboard focus requested at the end of the last line of the prayer point text. Supports multi-touch 2-finger pinch-to-zoom (`0.75f` to `2.5f`), dynamic baseline-synchronized ruling rules reflow, top bar reset trigger (`${percent}% ↺`), and floating zoom indicator pill. Status toggling is excluded here because it is canonically handled in the margin track; if the prayer point is already marked answered, the thanksgiving testimony field is presented. Includes a primary "Save changes" button, a "Delete prayer point" action, and an immediate "Save" button in the TopAppBar.
- **Keyboard Inset Guarantee**: All bottom action buttons, toolbars, and menus lift dynamically above the software keyboard upon input focus via `WindowInsets.safeDrawing`, preventing keyboard occlusion across phone, foldable, and tablet form factors. Dialogs provide internal vertical scroll so action buttons remain reachable.
- **Settings (`SettingsScreen`)**: Secluded preference controls for Leather Finish (tactile color swatches for Saddle Tan, Horween Cordovan, Hunter Forest, Obsidian Hide), English dialect (`EN_AU_UK` 1662 BCP vs `EN_US`), Historic Reformed Prayers rotation toggle, High-Contrast Accessible Mode, and bottom colophon seal (`─── ❧ ───`) on a scrollable vellum canvas with 0dp planar geometry. Text sizing is omitted from the settings menu and governed flexibly across all reading surfaces via multi-touch pinch-to-zoom.

### 2.3 Pathway 3: Prayer Point Capture (`LogPrayerScreen`)
- **Entry**: Tapping **"Add prayer points"** on Home.
- **Step 1 (Direct Writing Pad)**: Lands directly on the ruled lined notepad canvas (`LinedNotepad`). Auto-bullet formatting on every new line, zero distraction, zero AI autocompletion during writing, zero title field. Upon entering the screen to add prayer points, the typing cursor is instantly active and focused at the end of the last line (immediately after the initial bullet and space `• `), ready for immediate input without tapping.
- **Step 2 (Sphere & Entity Selection)**: Tapping "Next" allows selecting an existing person/group or creating a new entity inline.
- **Multi-Record Committal & Independent Tracking**: Every dotpoint is separated out into its own distinct `PrayerPoint` record upon save. Each dotpoint sits on its own feint rule with an independent marginal indicator (line-drawn pencil for active, `[ ANSWERED ]` pill for answered) in the Journal and individual long-press resolution in Sanctuary prayer.
- **Batch Undo Feedback**: Immediate confirmation toast (*"Saved N prayer points to [Name]"* / *"Saved to [Name]"*) with an **Undo** action that deletes all records created in that batch.

### 2.4 Pathway 4: Theological Library & Reading Experience (`LibraryScreen`, `VolumeReaderScreen`)
- **Entry**: Tapping **"Library"** on Home.
- **Folio Bookshelf (`LibraryScreen`)**:
  - Frontispiece header *"THEOLOGICAL LIBRARY"* flanked by printer's fleuron `❧`.
  - Archival volume card for *Volume 1: John Calvin — Of Prayer: A Perpetual Exercise of Faith* (1845 Henry Beveridge translation; 8 Divisions, 52 Sections).
  - Displays reading progress ribbon and last read section aside.
- **Immersive Folio Reader (`VolumeReaderScreen`)**:
  - Continuous cream vellum reading canvas with dynamic feint rules anchored to `28sp` baselines.
  - Body prose in Literary Serif (`17sp` / `28sp`), Roman numeral section headers (`20sp`), and italic outline arguments (`15sp`).
  - Table of Contents bottom sheet / quick-jump drawer displaying all 8 Principal Divisions and 52 section outlines for immediate access.
  - Silk Marker Ribbon bookmarking to save and restore the reader's active section.
  - Buttonless reading gestures: Swipe left/right to navigate between sections; swipe down from top to return to bookshelf.
  - **Pinch-to-Zoom & Baseline Synchronization**:
    - Natural multi-touch 2-finger pinch dynamically rescales text between 75% and 250% (`0.75f` to `2.5f`).
    - Flowing prose reflows naturally within page margins with zero horizontal overflow; feint horizontal rules dynamically scale in exact lockstep ($1:1$ parity with paragraph line-height) upholding the **Baseline Synchronization Law**.
    - Subtle floating zoom pill (e.g., `125% • Reset`) appears during gestures and auto-dismisses after 1.5s; tap immediately resets to 100%.
    - Top ledger bar displays a discreet reset trigger (`125% ↺`) whenever viewing at a non-default zoom level.
    - Zoom scale is persisted across sections and reading sessions in local configuration.

### 2.5 Pathway 5: Daily Reflections & Study Notes (`NotesScreen`)
- **Entry**: Tapping **"Notes"** on Home.
- **The Date as Grouping Invariant**: The calendar date is not a note; it is an organizing grouping container under which the believer can file any number of notes with individual titles (though titles are optional).
- **Three-Tier Folio Navigation Architecture (`LifoBackStack`)**:
  - **Tier 1: Date Groupings Directory (`NotesView.OVERVIEW`)**:
    - Displays date groupings chronologically (`is_pinned DESC, created_at DESC`).
    - Elevated tactile card **Today** displays today's date, current filed note count, and a direct `+ Note` quick creation action.
    - Past Date Groupings display note counts (e.g. `"3 notes"`), study topic tag, and preview of note titles/snippets.
    - Pinned date groupings display the Garnet Silk Marker Ribbon (`❧`).
  - **Tier 2: Date Detail View (`NotesView.DATE_DETAIL`)**:
    - Folio header presents the full date in `typography.subjectHeader`, Silk Marker Ribbon toggle, and editable inline study topic pill (`+ Add study topic or passage`).
    - Prominent `+ Add Note` button to file another note under this date.
    - Vertical list of filed notes showing title (or italic `(Untitled Note)`), timestamp (`10:45 AM`), 2-line snippet preview, and deletion actions.
  - **Tier 3: Ruled Notepad Canvas (`NotesView.NOTE_EDITOR`)**:
    - Header shows breadcrumb date indicator and individual **Title (Optional)** field in Literary Serif (`20sp`, semi-bold).
    - Natural ruled notepad canvas (`LinedNotepad`): freeform typing with standard carriage returns (no forced bullets on Enter).
    - Feint rules dynamically lock to typographic baselines with multi-touch pinch-to-zoom (`0.75f` to `2.5f`), floating zoom pill, and top bar reset trigger (`${percent}% ↺`).
    - Autosaves quietly to SQLite on back navigation or explicit Save click.
- **Contemplative AI Prayer Prompts**:
  - Collapsed by default under a quiet reverent fleuron divider (`❧   Prompts for Prayer   ❧` with *Tap to view prompts*), honoring quiet contemplation.
  - Analyzes the reflection or study note content to generate contemplative prompts categorized under **Praise God**, **Thank God**, and **Ask God** (`typography.categoryLedgerHeader`, `colors.leatherPrimary`).
  - Cached persistently in SQLite (`suggestion_cache`) for instantaneous display on revisit with silent background refresh.
- **Queue Isolation**:
  - Notes (`RootCode.NOTES`) are strictly excluded from the Sanctuary prayer queue traversal (`getContemplativeTopics`), preserving devotional prayer integrity while keeping personal reflections safely archived.

---

## 3. Visual Design System & The Modern Folio

### 3.1 The Four Chromatic Tiers

| Tier | Component / Token | Folio Color Hex | High-Contrast AA | Role & Character |
| :--- | :--- | :--- | :--- | :--- |
| **Tier 1: Leather Casing** | `leatherPrimary` | `#8C532B` | `#5A2800` | Saddle Tan vegetable-tanned leather cover |
| | `leatherCordovan` | `#5E2A2B` | `#3D1A1B` | Horween Cordovan oxblood leather finish |
| | `leatherForest` | `#2D483A` | `#1A2E24` | Hunter Forest slate-moss leather finish |
| | `leatherObsidian` | `#35322F` | `#35322F` | Obsidian Hide archival charcoal-black |
| | `leatherPerimeterBorder` | `rgba(0,0,0,0.15)` | `rgba(0,0,0,0.25)` | Whisper-thin 1dp outer leather boundary |
| **Tier 2: Writing Canvas** | `paperBackground` | `#FAF7F0` | `#FFFFFF` | Warm unbleached cream cotton vellum canvas |
| | `paperIvory` | `#F5EFEB` | `#FFFFFF` | Bone-tinted natural ivory stock |
| | `paperFeintRule` | `#E2DDD5` (70% α) | `#9C9488` | Baseline-locked stationery horizontal feint rules |
| | `paperMarginRule` | `#E5B4B4` | `#A63D40` | 37.52dp vertical margin dividing line (reduced by 33% from 56dp) |
| **Tier 3: Archival Inks** | `inkPrimary` | `#1C1A17` | `#0F0E0D` | Iron-Gall carbon charcoal primary narrative ink |
| | `inkAnswered` | `#3D6B52` | `#1E4D3B` | Celadon Sage for answered prayers & praises |
| | `inkSecondary` | `#33261F` | `#262422` | Walnut Sepia for quotes, epigraphs & history |
| | `inkMuted` | `#6E675F` | `#4A4642` | Muted ledger ink for timestamps & suggestions |
| **Tier 4: Ephemera & Accents** | `ribbonPrimary` | `#8B2635` | `#8B2635` | Garnet Crimson woven bookmark ribbon tab |
| | `selectionAmber` | `#F5DE88` (45% α) | `#FFE066` | Warm Amber Glaze text selection wash |
| | `stateAlert` | `#8B2635` | `#A63D40` | Madder Crimson restrained warning/delete |

### 3.2 The Singular Folio Law
Physical paper does not invert into glowing black pixels at sunset. The app rejects the harsh day/night light/dark dichotomy in favor of **one uncompromised, glare-free tactile theme** (`#FAF7F0` Cream Vellum + `#1C1A17` Iron-Gall Ink) comfortable under sunlight and bedside lamplight, accompanied by a dedicated WCAG 2.1 AA High-Contrast Mode for accessibility.

### 3.3 The Baseline Synchronization Law
Ruled lines must **always** be dynamically anchored to the active typographic baselines of the text engine (`28sp` cadence for body text), never static repeating stripes.
- Feint rules are cleanly suppressed within Answered Prayer bounding boxes, framed by a `2dp` vertical Celadon accent rule.
- Grouped devotional intercessions (*Praise God*, *Thank God*, *Ask God*) load beneath points on condensed `24sp` baseline rules under understated ledger category headers, strictly unlabelled as AI.

### 3.4 Typographic Scale & Hierarchy
- **Body & Petitions**: Literary Serif, `17sp` / `28sp` line height (`+0.25sp` tracking).
- **Subject Header**: Literary Serif Semi-Bold, `22sp` / `32sp`.
- **Answered Thanksgiving**: Literary Serif Italic, `16sp` / `26sp` (`inkAnswered`).
- **Suggested Intercession**: Literary Serif Italic, `15sp` / `24sp` (`inkMuted` at 85% α).
- **Frontispiece Header**: Ledger Sans Medium, `12sp` / `16sp` (`+1.0sp` tracking).
- **Ledger Metadata & Status**: Ledger Sans Regular, `11sp` / `16sp` (`+0.5sp` tracking).

---

## 4. Mobile Ergonomics & Spatial Geometry

### 4.1 Touch Targets & Spatial Bounds
- **Universal Minimum Touch Boundary**: All interactive elements maintain an invisible touch envelope of at least **`48 × 48dp`** (e.g. status pills `[ ACTIVE ]` / `[ ANSWERED ]`, action triggers, and icon buttons).
- **Silk Marker Ribbon**: Slender visual width (`18dp`), projecting an expanded invisible touch target of **`48dp` horizontal × `80dp` vertical** (dynamic with ribbon extension). Anchored flush to the top-end margin (`Alignment.TopEnd` with `4dp` breathing margin, visual envelope `4dp` to `22dp`) with `36dp` narrative text clearance and `52dp` prompt card clearance, guaranteeing the ribbon never obscures devotional text or menu text (e.g. *Show/Hide* toggles).
- **Margin Track Clearance**: Left `37.52dp` metadata track (reduced by 33% from 56dp) for timestamps and status seals; narrative text insets at `45.52dp` (`37.52dp + 8dp` clearance) to eliminate glyph collisions.
- **Inter-Affordance Gutters**: Minimum `8dp` clear gutter between adjacent touch targets (`12dp` to `16dp` between primary triggers).

### 4.2 Samsung Galaxy Flip & Foldable Ergonomics
- **Tall Aspect Ratio (22:9 / 21.9:9)**: Primary controls cluster in the lower 60% thumb arc; sanctuary reading centers comfortably in the natural gaze zone.
- **Tabletop / Flex Mode**: Half-folded posture keeps the prayer reading canvas on the top screen while touch zones remain active on the bottom panel.
- **Dual-Pane Book Spread**: On tablets and unfolded foldables, an elegant `24dp` central spine gutter with soft ambient gradient separates the directory index (left page) from the devotional session (right page).

---

## 5. Organic Motion & Transitions

- **Spatial Continuity**: Directional Shared-Axis X slides (`280–320ms`) between Home and Journal.
- **Devotional Traversal**: Horizontal spring slide (`stiffness: 320, damping: 0.85`) when advancing topics; vertical slide down (`250ms`, Emphasized Accelerate) on exit.
- **Textile Marker Ribbon**: Realistic spring extension (`stiffness: 220, damping: 0.70`) from `40dp` resting to `80dp` pinned state with a crisp haptic impulse.
- **LIFO Navigation Guarantee**: System back and edge-swipe right (`X ≤ 25dp, ΔX ≥ 50dp`) navigate backward in strict Last-In, First-Out order.
- **Privacy Shield**: When the app is backgrounded or viewed in Recent Apps, the active prayer screen is instantly masked behind a flat vector Closed Leather Folio Cover with an embossed monogram insignia (`P · W · C`).

---

## 6. Zero Gamification Mandate

The application strictly excludes:
- Streak counters, flame icons, or daily score counters.
- Confetti, badges, achievement rings, or celebratory popups.
- Mechanical ordinal counters (*"Point 1 of 5"*, *"Item 1 of N"*).

Prayer is reverent, unhurried communion with God—never a gamified task list.

---

## 7. Multi-Session Design Cohesion & Integrity

When multiple autonomous agents contribute UI/UX changes simultaneously:
- **Design Token Purity**: All components must uniformly consume existing design tokens (`PrayerSpacing`, `PrayerColors`, `FlatSquareShape`).
- **Target Line Verification**: Agents modifying shared composable layouts (such as [`JournalScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/JournalScreen.kt) or [`SanctuaryPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt)) must re-read active code immediately before applying edits to ensure newly added actions (e.g. margin indicators, bottom buttons) are never discarded or duplicated.

