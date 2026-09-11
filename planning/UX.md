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

## 2. Core Information Architecture & The Three Primary Pathways

The application is strictly organized around three primary functional pathways radiating from the Frontispiece Home Screen:

```
                          ┌──────────────────────────┐
                          │     HOME FRONTISPIECE    │
                          │   (PRAY WITHOUT CEASING) │
                          └─────────────┬────────────┘
                                        │
           ┌────────────────────────────┼────────────────────────────┐
           ▼                            ▼                            ▼
  [ Start praying ]            [ Open Journal ]             [ Add prayer points ]
         │                              │                            │
         ▼                              ▼                            ▼
┌──────────────────┐           ┌──────────────────┐         ┌──────────────────┐
│  DEVOTIONAL      │           │  JOURNAL         │         │  PRAYER POINT    │
│  SANCTUARY       │◄─────────►│  DIRECTORY       │◄───────►│  CAPTURE         │
│  (Anti-Neglect   │ (Swipe L) │  (People, Groups,│ (Add to │  (Lined Notepad  │
│   Queue, 30/40/30│           │   General, Miss.)│  Entity)│   Direct Entry)  │
│   Zoning)        │           └────────┬─────────┘         └────────┬─────────┘
└──────────────────┘                    │                            │
                                        ▼                            ▼
                               ┌──────────────────┐         ┌──────────────────┐
                               │  ENTITY DETAIL & │         │  COMMITTAL &     │
                               │  POINT EDITOR    │         │  ASYNC TITLING   │
                               └──────────────────┘         └──────────────────┘
```

### 2.0 Frontispiece Home Screen & Launch Experience (`HomeScreen`)
- **Zero-Flash Cold Boot & Native Splash**: Application launches with `@style/Theme.Prayer` binding `android:windowBackground` directly to warm cream vellum (`#FAF7F0`), paired with an Android 12+ debossed monogram insignia (`ic_splash_monogram`), eliminating jarring white flashes upon launch.
- **Solemn Folio Opening Revelation**: Cold launches initiate a serene, non-blocking revelation sequence (~600ms): the closed leather cover with the embossed monogram parts/fades with graceful ease, revealing the illuminated vellum frontispiece and dropping the Silk Marker Ribbon into place. Tapping anywhere instantly skips into the frontispiece for zero-latency access.
- **Folio Casing & Vellum Architecture**: The screen is bounded in the user's active leather dye finish (`colors.leatherActive`), framing a centered 720dp vellum sheet with a delicate double-hairline stationery bookplate border (`0.75dp` stroke in `colors.borderSubtle`).
- **Typographic Frontispiece Inscription**: Features tracked capitals `"PRAY WITHOUT CEASING"` (`12sp`, tracking `2.0sp`), a centered printer's fleuron ornament (`❧`) flanked by hairline rules (`─── ❧ ───`), and the reverent epigraph from 1 Thessalonians 5:16–18.
- **Elevated Action Hierarchy (Tactile Bookplates)**:
  - **"Start praying"** (Devotional Gateway): Elevated sanctuary plate with 2dp elevation, warm ivory stock (`#FFFDF9`), 1.5dp `leatherActive` accent border, and dignified literary typography.
  - **"Open Journal"** (Spiritual Records Directory): Refined stationery card with 0.75dp hairline border (`colors.border`) and ledger navigation glyph `›`.
  - **"Add prayer points"** (Direct Lined Notepad Capture): Matching stationery card with 0.75dp hairline border and bullet/pen prompt.
  - Standardized 56dp–62dp heights and 12dp gutters for effortless thumb reach on Samsung Galaxy Flip and standard handhelds.

### 2.1 Pathway 1: Devotional Prayer (`SanctuaryPrayerScreen`)
- **Entry**: Tapping **"Start praying"** on Home.
- **Contemplative Immersion**: All top app bars, system chrome, buttons, and progress counters are suppressed.
- **Anti-Neglect Queue Traversal**: Believers cycle through past intercessions prioritizing neglected entities.
- **30% / 40% / 30% Touch Zoning**:
  - **Left 30%**: Tap or swipe right to return to previous topic.
  - **Center 40%**: Reading canvas and long-press for in-place status resolution (Active ↔ Answered).
  - **Right 30%**: Tap or swipe left to advance to next topic.
  - **Swipe Down Anywhere**: Slide downward (`250ms`, Emphasized Accelerate) to exit back to Home.
- **Silk Marker Ribbon**: Anchored at the top margin to flag active intercession or pinned focus.
- **Subject Header Rendering (Personal vs. Historic)**:
  - **Personal intercession targets** (People, Groups, Mission Partners, user-created General topics): rendered as *"Praying for [Name]"* in Semi-Bold Literary Serif (`22sp` / `32sp`), honoring the relational, pastoral idiom of personal intercession.
  - **Preloaded historic entities** (1662 BCP Collects, Apostles' Creed, Spurgeon pulpit prayers): the entity's `displayName` is rendered directly (e.g., *"C.H. Spurgeon: Help from on High"* or *"Collect for Peace (1662 BCP)"*) without the "Praying for" prefix, reflecting their liturgical rather than personal character.
  - **AI Prompt Suppression**: The read-only prompt section (`❧ Prompts for Prayer ❧`) is hidden entirely for preloaded historic entities — their texts are the prayers of the saints; AI augmentation is neither appropriate nor offered.
- **Silent Background Prompts (Collapsed by Default)**: Read-only suggested lines load silently beneath points without digital loading spinners or chat chrome. In fidelity to the principle of quiet contemplation, prompts are **hidden or collapsed by default** behind a reverent fleuron divider (`❧   Prompts for Prayer   ❧` with `Tap to view prompts`). Prompts only expand when explicitly tapped by the user. When expanded, prompts are organized into three reverent devotional groups—**Praise God**, **Thank God**, and **Ask God**—with group titles set in understated ledger headers (`typography.categoryLedgerHeader`, `colors.inkSecondary`). Each bullet (4–15 words, 3–12 points total across groups, starting with *For*, *That*, *A*, or *Because*, phrased warmly without clinical terseness or verbatim parroting, and strictly avoiding wishy-washy general platitudes) is set in literary italic (`typography.suggestedIntercession`) on condensed `24sp` feint rules, with zero AI labels or badges.



### 2.2 Pathway 2: Journal Management (`JournalScreen`)
- **Entry**: Tapping **"Open Journal"** or swiping left on Home (`280ms`, Shared-Axis X).
- **Four Classical Spheres**: Categorized into expandable/collapsible categories:
  1. *People* (Family, friends, individual discipleship)
  2. *Groups* (Small groups, Bible studies, committees)
  3. *General* (World burdens, nation, government, church universal)
  4. *Mission Partners* (Missionaries, church plants, global gospel workers)
- **Adding Subjects to Spheres**: An option at the top of each listing under each group (`+ Add person`, `+ Add group`, `+ Add topic`, `+ Add mission partner`) opens an in-place creation dialog that saves the new person or topic and immediately opens their Entity Detail view.
- **Entity Detail Spread**: View active and answered prayer points separated and grouped by date of entry with day and month written in full English words (e.g. *"Friday, 11 September 2026"*) in subtle unflashy typography (`typography.marginStatus` / `colors.inkMuted`), add points directly to an entity, pin with the Silk Marker Ribbon, and access an understated, **collapsed-by-default** prompts card (*"Prompts for Prayer"* with *Show/Hide* toggle) that expands on demand to display contemplative prompts grouped under **Praise God**, **Thank God**, and **Ask God** (`typography.categoryLedgerHeader`, `colors.leatherPrimary`).
- **In-Place Editor**: Modify body, status, and thanksgiving notes (titles are omitted).
- **Settings (`SettingsScreen`)**: Secluded preference controls for Leather Finish (tactile color swatches for Saddle Tan, Horween Cordovan, Hunter Forest, Obsidian Hide), Text Size tiers with live literary serif preview samples (*"Pray without ceasing"*), English dialect (`EN_AU_UK` 1662 BCP vs `EN_US`), Historic Reformed Prayers rotation toggle, High-Contrast Accessible Mode, and bottom colophon seal (`─── ❧ ───`) on a scrollable vellum canvas with 0dp planar geometry.

### 2.3 Pathway 3: Prayer Point Capture (`LogPrayerScreen`)
- **Entry**: Tapping **"Add prayer points"** on Home or "+ Add prayer point" in Entity Detail.
- **Step 1 (Direct Writing Pad)**: Lands directly on the ruled lined notepad canvas (`LinedNotepad`). Auto-bullet formatting on enter, zero distraction, zero AI autocompletion during writing, zero title field.
- **Step 2 (Sphere & Entity Selection)**: Tapping "Next" allows selecting an existing person/group or creating a new entity inline.
- **Committal & Instant Save**: One-tap save with an instant **Undo** snackbar, saving directly to the chosen entity without artificial title generation.

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
| | `paperMarginRule` | `#E5B4B4` | `#A63D40` | 56dp vertical margin dividing line |
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
- **Silk Marker Ribbon**: Slender visual width (`18dp`), projecting an expanded invisible touch target of **`48dp` horizontal × `56dp` vertical**.
- **Margin Track Clearance**: Left `56dp` metadata track for timestamps and status seals; narrative text insets at `64dp` (`56dp + 8dp` clearance) to eliminate glyph collisions.
- **Inter-Affordance Gutters**: Minimum `8dp` clear gutter between adjacent touch targets (`12dp` to `16dp` between primary triggers).

### 4.2 Samsung Galaxy Flip & Foldable Ergonomics
- **Tall Aspect Ratio (22:9 / 21.9:9)**: Primary controls cluster in the lower 60% thumb arc; sanctuary reading centers comfortably in the natural gaze zone.
- **Tabletop / Flex Mode**: Half-folded posture keeps the prayer reading canvas on the top screen while touch zones remain active on the bottom panel.
- **Dual-Pane Book Spread**: On tablets and unfolded foldables, an elegant `24dp` central spine gutter with soft ambient gradient separates the directory index (left page) from the devotional session (right page).

---

## 5. Organic Motion & Transitions

- **Spatial Continuity**: Directional Shared-Axis X slides (`280–320ms`) between Home and Journal.
- **Devotional Traversal**: Horizontal spring slide (`stiffness: 320, damping: 0.85`) when advancing topics; vertical slide down (`250ms`, Emphasized Accelerate) on exit.
- **Textile Marker Ribbon**: Realistic spring extension (`stiffness: 220, damping: 0.70`) from `40dp` resting to `54dp` pinned state with a crisp haptic impulse.
- **LIFO Navigation Guarantee**: System back and edge-swipe right (`X ≤ 25dp, ΔX ≥ 50dp`) navigate backward in strict Last-In, First-Out order.
- **Privacy Shield**: When the app is backgrounded or viewed in Recent Apps, the active prayer screen is instantly masked behind a flat vector Closed Leather Folio Cover with an embossed monogram insignia (`P · W · C`).

---

## 6. Zero Gamification Mandate

The application strictly excludes:
- Streak counters, flame icons, or daily score counters.
- Confetti, badges, achievement rings, or celebratory popups.
- Mechanical ordinal counters (*"Point 1 of 5"*, *"Item 1 of N"*).

Prayer is reverent, unhurried communion with God—never a gamified task list.
