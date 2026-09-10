# The Modern Folio: Visual Design Bible for an Analog-Inspired Leather & Paper Android Prayer Journal

> [!NOTE]
> **Scope & Intent**: This document serves as the authoritative **visual design bible and presentation specification** for a pure-text, devotional Android prayer journal. It is grounded in the romance, intimacy, and tactile presence of a **handcrafted leatherbound notebook with fine ruled paper** (such as a Midori Traveler's Notebook, Leuchtturm1917, or Smythson devotional journal), translated into contemporary, vector-crisp Android design. This is a **strict no-code document**: it provides platform-agnostic visual design specifications, spatial geometry, typographic hierarchies, and tactile interaction rules for designers and software craftspeople.

A distinguished prayer journal is more than an ephemeral digital notepad—it is an intimate, enduring space for personal intercession, thanksgiving, and quiet thought. The core aesthetic philosophy of this app is **Modern Leatherbound Craft**: capturing the emotional gravity, physical warmth, and timeless romance of a personal leather journal with heavyweight ruled paper, without falling into tacky, dated 2011-era skeuomorphic gimmicks, cold digital minimalism, or gamified distractions.

### Core Information Architecture & The Three Primary Pathways
The application is strictly organized around **three primary functional pathways**:
1. **Devotional Prayer (`PrayerSessionScreen`)**: Entered via "Start praying"; an immersive, chrome-suppressed devotional session traversing an anti-neglect prayer queue.
2. **Prayer Point Capture (`LogPrayerScreen`)**: Entered via "Add prayer points"; immediate landing on a direct lined writing pad (`LinedNotepad`) with automatic bullet formatting, bypassing initial titling or categorization, followed by a streamlined committal step.
3. **Journal Management (`JournalScreen`)**: Entered via "Open Journal" or direct swipe-left from Home; organizes records into four expandable/collapsible categories (People, Groups, General, Mission Partners), opening into Entity Detail Views, an in-place Prayer Point Editor, and Settings.

> [!IMPORTANT]
> **Devotional Integrity & Strict Exclusions**: All social feeds, user accounts, gamification metrics (streak flames, badges, confetti, achievement rings), and mechanical ordinal counters ("Point 1 of 5", "Item 1 of N") are strictly excluded from the design.

---

## 1. Aesthetic Metaphor: The Modern Leatherbound Folio

We explicitly reject both sterile, cold digital minimalism and dated, gaudy skeuomorphism.

- **What we reject (Gaudy Skeuomorphism & Digital Noise)**: Photorealistic bitmap leather textures, fake drawn thread stitches, simulated 3D bevels, plastic drop shadows, simulated paper tearing, 3D page-curling flip animations, fake wet ink drying transitions, shiny brass clasps, and gamified dopamine triggers.
- **What we embrace (Modern Vector Craft & Reverent Devotion)**: The authentic structural metaphor of a **leatherbound lined journal**, expressed through rich flat/tonal leather palettes, warm unbleached paper grounds, dynamic baseline-synchronized stationery rules, an iconic vector marker ribbon, and archival ink typography.

```
┌─────────────────────────────────────────────────────────────┐
│  LEATHER CASING (Warmth, Containment & Intimacy)            │
│  - Authentic leather dye tones (Saddle Tan, Cordovan, etc.) │
│  - Clean vector perimeter framing; flat, refined elevation  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  FINE RULED VELLUM CANVAS (Quiet Focus & Clarity)     │  │
│  │  - Warm cream/ivory stock (glare-free foundation)     │  │
│  │  - Baseline-locked feint horizontal rules             │  │
│  │  - 56dp left vertical margin guide line               │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │  THE INKED WORD (Archival Typographic Voice)    │  │  │
│  │  │  - Literary serif sitting firmly ON the rules   │  │  │
│  │  │  - Petitions, answered records & thanksgiving   │  │  │
│  │  │  - Unlabelled, quiet suggested intercessions    │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

The interface is structured around three sensory layers:
1. **The Leather Casing**: Rich, warm leather dyes that provide structural containment, warmth, and the feeling of holding a treasured personal prayer book.
2. **The Fine Ruled Canvas**: A warm, unbleached lined surface that eliminates glare, sets spatial cadence, and anchors handwritten and typed petitions.
3. **The Inked Word**: Expressive literary typography and marginal notes that feel permanently set into the page—carrying petitions, answered praises, and reflective intercessions.

---

## 2. The Leather Casing & Folio Architecture

The app's exterior frames the writing space in the spirit of a vegetable-tanned leather notebook.

### 2.1 Leather Finishes & Color Architecture
Surfaces use authentic leather dye tones rendered cleanly as solid, responsive vector surfaces—never muddy bitmap textures:

| Leather Finish | Frame Hex Token | Tint / Inset Hex Token | Character & Mood |
| :--- | :--- | :--- | :--- |
| **Saddle Tan** *(Primary Warm)* | `#8C532B` | `#F5EBE1` | Classic oiled saddle leather; warm, comforting, and organic. |
| **Horween Cordovan** | `#5E2A2B` | `#F4EAE9` | Deep oxblood leather; contemplative evening reflection and vigil. |
| **Hunter Forest** | `#2D483A` | `#EBF0EC` | Slate-moss dyed leather; grounded, meditative intercession. |
| **Obsidian Hide** | `#35322F` | `#EFECE8` | Archival black leather with rich charcoal undertones. |

### 2.2 Modern Vector Detailing (No Faux Stitching, No 3D Bevels)
- **Perimeter Casing Frame**: The outer leather border frames the canvas cleanly. Rather than fake stitch lines or faux bevels, it uses a whisper-thin tonal separation border (`1dp` at `15%` darker alpha) to define the edge of the leather cover.
- **Flat Typography & Clean Headers**: Header titles and dates sit crisply on the surface with high optical clarity, avoiding dated 3D debossing or inner bevel shadows.
- **Refined Spatial Elevation**: Layering (such as bottom sheets or tool palettes) uses soft, modern elevation tints and subtle ambient occlusion (`2dp` – `4dp` blur radius) rather than heavy skeuomorphic cast shadows.

### 2.3 The Silk Marker Ribbon (Interactive Bookmark Tab)
A hallmark of a classic leatherbound journal is its woven bookmark ribbon:
- **Visual Presentation**: A clean, vector-rendered ribbon tab (`18dp` width, `40dp` resting height, extending to `54dp` when active/pinned) anchored at the top margin. It features the traditional `6dp` swallow-tail notched tip.
- **Coloring**: Rendered in rich, flat jewel tones (Garnet Crimson `#8B2635`, Forest Emerald `#1E4D3B`, or Antique Gold `#B8860B`) with a subtle, flat vector tone shift rather than a glossy fabric sheen.
- **Devotional Interaction**: Tapping toggles the entity or entry's pinned status (e.g., flagging active intercession or a milestone answered prayer) with a smooth spring bounce and a crisp, light haptic tick.

### 2.4 Book Spine & Binding Gutter (Foldables & Tablets)
On foldables unfolded into book posture and tablets in landscape mode:
- **The Fold as Book Spine**: The physical device hinge acts naturally as the journal's sewn spine.
- **Clean Center Gutter**: An elegant `24dp` spatial gutter with a subtle ambient gradient (`16dp` width, tapering from `6%` black alpha to transparent) that gently evokes the depth of bound pages meeting the center spine, without heavy 3D curling illusions.

---

## 3. The Fine Ruled Paper System (The Lined Canvas)

The writing surface evokes a high-grade, 120gsm fountain-pen-friendly ruled notebook (such as Rhodia, Midori MD, or Leuchtturm1917).

### 3.1 Paper Stock Characteristics
The canvas uses warm, glare-free paper stocks rather than clinical `#FFFFFF`:

| Stock Name | Canvas Color Hex | High-Contrast Accessible | Physical Tone & Feel |
| :--- | :--- | :--- | :--- |
| **Warm Cream Vellum** *(Default)* | `#FAF7F0` | `#FFFFFF` | Unbleached cotton vellum; soft warmth for long prayer and writing sessions. |
| **Natural Ivory** | `#F5EFEB` | `#FFFFFF` | Bone-tinted stationery; high clarity paired with charcoal inks. |
| **Aged Parchment** | `#EFE8DA` | `#FFFDF8` | Deeper golden tone for answered prayers and retrospectives. |

### 3.2 The Baseline Synchronization Law
> [!IMPORTANT]
> **The Baseline Synchronization Law**: A digital lined paper canvas that draws static repeating stripes is fundamentally broken. When font sizes change, user line-heights adjust, or system non-linear font scaling engages, static lines slice awkwardly through glyphs.
>
> **The Requirement**: Ruled lines must **always** be dynamically anchored to the active typographic baseline metrics of the text engine. Every drawn rule seats the font's baseline precisely on top of the rule. In blank space below the text, lines continue downward at the standard body cadence (`28sp`) to maintain notebook continuity.
>
> **Exception Handling for Devotional Blocks**:
> - **Devotional Subject Header (*"Praying for [Name]"*) (`32sp` line height)**: Sits on the first prominent rule; spacing advances the grid dynamically before prayer points resume.
> - **Direct Writing Pad (`LinedNotepad` in `LogPrayerScreen`)**: Drafting begins directly on the feint rule. When the user presses Enter/Return, automatic bullet formatting inserts `•` with an indented guide, locked precisely to the subsequent baseline rule.
> - **Answered Prayer & Thanksgiving Blocks (`26sp` line height)**: Feint rules are suppressed (unruled canvas) within the block bounding box, framed by a `2dp` vertical Celadon accent rule, preventing baseline collisions.
> - **Unlabelled Suggested Intercessions (`24sp` line height)**: Asynchronous prompts load directly beneath prayer points on condensed `24sp` baseline rules.
> - **Footnotes & Historical Marginalia (`20sp` line height)**: Anchored below a centered `32dp` hairline separator on condensed `20sp` ruling.

### 3.3 Ruling Geometry & Optical Layout
1. **Horizontal Feint Rules**:
   - Drawn as delicate vector hairlines: `0.75dp` stroke weight.
   - Color: Muted feint slate/sepia (`#E2DDD5` at `70%` alpha).
   - Spacing: Dynamically locked to body text line height (`28sp` default).
2. **The Left Vertical Margin Guide**:
   - The classic stationery red/sepia vertical rule located exactly **`56dp`** from the left canvas edge.
   - Weight: `0.75dp` hairline (`#E5B4B4`).
   - Function: Creates a disciplined two-track layout: prayer status markers (Active/Answered indicators), timestamps, and category notation sit left of the margin line; narrative petitions flow to the right.
3. **Ruling Formats**:
   - **Narrow Feint (Default)**: Classical horizontal ruling aligned to body typography.
   - **Architect Dot Grid**: Subtle `1.5dp` circular dots spaced at `20dp` increments for hybrid drafting.
   - **Blank Vellum**: Clean, unlined paper for open contemplation.

### 3.4 Accessibility & High-Contrast Mode
- **Standard Mode**: Ambient feint rules (`#E2DDD5` on `#FAF7F0`, contrast ~1.2:1) act as quiet visual guides.
- **High-Contrast Accessible Mode**: Meets WCAG 2.1 AA non-text contrast guidelines with crisp rules (`#9C9488` on `#FFFFFF`, 3.2:1 contrast) and deep charcoal text (`#0F0E0D`, 19.5:1 contrast).

### 3.5 Rejection of the Light/Dark Dichotomy (The Singular Folio Law)
Physical paper does not invert into glowing black pixels when the sun sets. Digital note apps often force an artificial dichotomy between blinding `#FFFFFF` and battery-saver `#000000`.

This journal explicitly rejects that dichotomy in favor of **one singular, uncompromised tactile theme**:
- **Glare-Free Foundation**: Warm unbleached cotton vellum (`#FAF7F0`) naturally avoids the harsh ocular glare of cold digital white, remaining comfortable in bright natural light.
- **Ambient Lamplight Warmth**: In low light, the natural warm vellum surface paired with deep iron-gall ink (`#1C1A17`) mirrors the experience of reading a physical hardbound book by bedside lamplight, eliminating jarring color-inversion switches.

---

## 4. Typographic Hierarchy & Archival Inks

In a pure-text journal, typography is the entire interface. The written word must feel purposeful, permanent, and literary.

```
       56dp Margin Track                  Prayer Text Canvas
 ─────────────────────────────  ┼──────────────────────────────────────────
 [ ACTIVE ]                     │   • Complete recovery from surgery and
 ─────────────────────────────  ┼   renewed strength for the family...
 [ 10:45 AM ]                   │
 ─────────────────────────────  ┼     Quiet patience in the waiting period.
                                │
```

### 4.1 Dual-Engine Font Pairings
- **The Inked Narrative (Body & Petitions)**:
  - High-grade literary serif with historical warmth, generous x-height, and open counters.
  - Recommended typefaces: *Literata* (Google Fonts, optimized for long-form reading), *Newsreader*, *EB Garamond*, or *Lora*.
  - Strict proportions: `17sp` font size, `28sp` line height (ratio: `~1.65`), optical `+0.25sp` letter spacing.
- **The Ledger Metadata (Categories, Timestamps & Controls)**:
  - Clean, understated neo-grotesque or humanist sans-serif (*Plus Jakarta Sans*, *Inter*, or *Roboto Flex*).
  - Used for category headers (People, Groups, General, Mission Partners), timestamps, entity tags, and settings chrome—evoking neat notation in the margins of a traditional ledger.

### 4.2 Archival Ink Formulations
Text colors possess chromatic warmth rather than sterile pure black:
- **Iron-Gall Black** *(Default Narrative & Petitions)*: `#1C1A17`. Deep charcoal-black with warm undertones (`ink.primary`).
- **Walnut Sepia** *(Secondary Quotes & Headers)*: `#33261F`. Mellow umber recalling vintage dip-pen ink (`ink.secondary`).
- **Archival Midnight Blue**: `#1B2433`. Traditional fountain-pen blue-black (`ink.midnight`).
- **Celadon Sage** *(Answered Prayers & Thanksgiving)*: `#3D6B52`. Soft botanical green representing answered prayer records and peaceful resolution (`ink.answered` / `state.answered`).
- **Muted Ledger Ink**: `#6E675F`. Graphite-toned ink for timestamps, suggested lines, and category tags (`ink.muted`).

> [!NOTE]
> **Crisp Vector Ink (No Faux Wet Ink Physics)**: Characters render instantly with crisp vector precision. We do not apply faux "ink drying gradients" or simulated "paper tooth bleed", ensuring instantaneous, clean digital rendering.

### 4.3 Typographic Scale & Hierarchy

| Element | Typeface & Style | Size / Line Height | Color Token | Visual & Spatial Placement |
| :--- | :--- | :--- | :--- | :--- |
| **Frontispiece Header** | Ledger Sans, Medium | `12sp` / `16sp` (`+1.0sp` tracking) | `ink.muted` | Centered on Home screen frontispiece. |
| **Prayer Session Subject Header** | Literary Serif, Semi-Bold | `22sp` / `32sp` | `ink.primary` | Header: *"Praying for [Name]"*; sits on first prominent rule. |
| **Prayer Point Bullets** | Literary Serif, Regular | `17sp` / `28sp` (`+0.25sp`) | `ink.primary` | Sits strictly ON feint rules, auto-bulleted (`•`) past margin guide. |
| **Answered Thanksgiving Note**| Literary Serif, Italic | `16sp` / `26sp` | `ink.answered` (`#3D6B52`) | Indented `24dp` left & right; accented by flat `2dp` Celadon rule. |
| **Suggested Intercession Line**| Literary Serif, Italic | `15sp` / `24sp` | `ink.muted` (85% α) | Directly beneath points; strictly unlabelled (no "AI" or prompt chrome). |
| **Category Ledger Header** | Ledger Sans, Semi-Bold | `13sp` / `18sp` (`+0.8sp` tracking)| `leather.primary` | Expandable headers: *PEOPLE*, *GROUPS*, *GENERAL*, *MISSION PARTNERS*. |
| **Marginal Status Aside** | Ledger Sans, Regular | `11sp` / `16sp` | `ink.muted` | Lives in the 56dp margin track; signals Active or Answered status. |
| **Historical Footnote** | Literary Serif, Regular | `13sp` / `20sp` (`+0.1sp`) | `ink.secondary` (80% α) | Anchored at the bottom; separated by a centered `32dp` hairline on condensed 20sp rules. |

---

## 5. Color Architecture & Organic Palette

Color in this journal is calibrated for tranquility, long-form focus, and visual warmth, derived from traditional bookmaking materials: vegetable tannins, unbleached cotton paper, and natural pigments.

```
┌─────────────────────────────────────────────────────────────┐
│                      THE FOUR CHROMATIC TIERS               │
├─────────────────────────────────────────────────────────────┤
│  1. THE LEATHER CASING (Tanned Hides)                       │
│    Saddle Tan (#8C532B) · Cordovan (#5E2A2B) · Hunter Forest│
│  ┌───────────────────────────────────────────────────────┐  │
│  │  2. THE VELLUM CANVAS (Paper Stocks & Feint Lines)    │  │
│  │  Cream Vellum (#FAF7F0) · Ivory (#F5EFEB) · Parchment │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │  3. THE ARCHIVAL INKS (Text & Typography)       │  │  │
│  │  │ Iron-Gall (#1C1A17) · Celadon (#3D6B52) · Sepia │  │  │
│  │  │  ┌───────────────────────────────────────────┐  │  │  │
│  │  │  │  4. EPHEMERA & ACCENTS (Ribbon, Washes)   │  │  │  │
│  │  │  │     Garnet Ribbon (#8B2635) · Amber · Sage│  │  │  │
│  │  │  └───────────────────────────────────────────┘  │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 5.1 The Four Color Tiers

#### Tier 1: The Leather Casing (Perimeter Frame)
- **Saddle Tan** (`#8C532B`): Natural vegetable-tanned leather warmth (`leather.primary`).
- **Horween Cordovan** (`#5E2A2B`): Deep oxblood tones for evening reflection and vigil.
- **Hunter Forest** (`#2D483A`): Slate-moss dyed hide for quiet reflection and intercession.
- **Obsidian Hide** (`#35322F`): Understated archival charcoal-black leather.

#### Tier 2: The Writing Canvas (Paper & Ruling)
- **Warm Cream Vellum** (`#FAF7F0`): Unbleached high-cotton paper canvas (`paper.background`).
- **Natural Ivory** (`#F5EFEB`): Bone-white rag stationery alternative.
- **Feint Rule Lines** (`#E2DDD5` at `70%` α): Baseline horizontal rules (`paper.feintRule`).
- **Vertical Margin Guide** (`#E5B4B4`): Traditional red/sepia vertical rule (`paper.marginRule`).

#### Tier 3: Archival Inks (Narrative & Marginalia)
- **Iron-Gall Black** (`#1C1A17`): Dense carbon charcoal narrative and petition text (`ink.primary`).
- **Celadon Sage** (`#3D6B52`): Botanical soft green for answered prayers and thanksgiving (`ink.answered`).
- **Walnut Sepia** (`#33261F`): Earthy brown-toned ink for secondary text and history (`ink.secondary`).
- **Archival Midnight Blue**: `#1B2433`. Fountain-pen blue-black alternate narrative ink (`ink.midnight`).
- **Muted Ledger Ink** (`#6E675F`): Graphite-toned ink for timestamps, suggested lines, and category tags (`ink.muted`).

#### Tier 4: Ephemera & Accents (Ribbon & Status)
- **Garnet Ribbon** (`#8B2635`): Primary marker ribbon bookmark accent (`ribbon.primary`).
- **Forest Emerald Ribbon** (`#1E4D3B`): Ribbon variant for mission and church partners (`ribbon.emerald`).
- **Antique Gold Ribbon** (`#B8860B`): Ribbon variant for milestone answered prayers (`ribbon.gold`).
- **Celadon Sage** (`#3D6B52`): Clean autosave, silent auto-title confirmation, and answered state (`state.autosave` / `state.answered`).
- **Warm Amber Glaze** (`#F5DE88` at `45%` α): Text selection highlight (`state.selection`).
- **Madder Crimson** (`#8B2635` / Accessible `#A63D40`): Restrained warning and alert indicator (`state.alert`).

### 5.2 Visual Atmosphere & Ambient Lighting Comfort
Unlike standard digital software that abruptly flips between day and night palettes, this journal relies on the natural optical characteristics of analog materials:
- **Reflective Balance**: The cream vellum canvas (`#FAF7F0`) reflects a muted, warm light spectrum that prevents ocular fatigue in brightly lit spaces without requiring blinding digital white.
- **Low-Light Harmony**: In dim or bedside environments, the combination of warm vellum and organic charcoal ink (`#1C1A17`) produces zero harsh high-frequency blue spikes, sitting gently in the visual field like a physical book under an incandescent reading lamp.

### 5.3 Contrast Compliance (WCAG 2.1)

| Foreground Element | Background Surface | Contrast Ratio (Singular Folio) | WCAG Standard | Compliance Status |
| :--- | :--- | :--- | :--- | :--- |
| **Primary Ink (Iron-Gall)** | Paper Canvas | **15.6 : 1** | WCAG AAA (≥ 7.0:1) | **Exceeds AAA** |
| **Answered Ink (Celadon)** | Paper Canvas | **5.8 : 1** | WCAG AA (≥ 4.5:1) | **Passes AA** |
| **Secondary Ink (Sepia)** | Paper Canvas | **12.4 : 1** | WCAG AAA (≥ 7.0:1) | **Exceeds AAA** |
| **Muted Ink (Timestamps)** | Paper Canvas | **5.4 : 1** | WCAG AA (≥ 4.5:1) | **Passes AA** |
| **Selection Highlight** | Primary Text | **11.8 : 1** | WCAG AAA (≥ 7.0:1) | **Exceeds AAA** |
| **Feint Ruling Lines** | Paper Canvas | **1.25 : 1** *(ambient)* / **3.2 : 1** *(accessible)* | WCAG 2.1 Non-Text (≥ 3.0:1) | **Passes in Accessible Mode** |
| **Margin Dividing Line** | Paper Canvas | **1.6 : 1** *(ambient)* / **4.8 : 1** *(accessible)* | WCAG 2.1 Non-Text (≥ 3.0:1) | **Passes in Accessible Mode** |
| **Bookmark Ribbon** | Paper Canvas (Resting) | **9.9 : 1** | WCAG AA (≥ 4.5:1) | **Passes AA** |

---

## 6. Textual Marginalia & Journal Ephemera

Visual richness is achieved through **typographical ephemera, status seals, and margin notes** rather than decorative attachments.

```
       56dp Margin Track                  Prayer Text Canvas
 ─────────────────────────────  ┼──────────────────────────────────────────
 [ PEOPLE · David & Sarah ]     │   • Complete recovery from surgery and
 ─────────────────────────────  ┼   renewed strength for the family...
 [ 07:45 AM ]                   │
 ─────────────────────────────  ┼     Quiet patience in the waiting period.
                                │
```

### 6.1 Field Notations (Status, Entity Categories & Tags)
- Displayed in the `56dp` left margin track as neat, compact notation pills (e.g., `[ ACTIVE ]`, `[ PEOPLE ]`, or `[ ANSWERED ]`).
- Rendered in `11sp` Ledger Sans with a crisp, subtle outline (`0.5dp` stroke, `4dp` corner radius, using `ink.muted` at `50%` alpha). It evokes neat marginal notes without faux rubber-stamp ink blotches.
- Tapping or swiping status notation pills allows immediate in-line status toggling.

### 6.2 Paragraph Timestamps & Marginal Glosses
- Later prayer sessions or updates display their time markers (`10:45 AM` or `Late Evening`) in the left margin track aligned with the corresponding paragraph baseline.
- Writers and intercessors can attach short inline annotations or tags in the margin track without disrupting the linear flow of the narrative prayer points.

### 6.3 Typographic Fleurons & Section Dividers
- Section transitions (e.g., separating Active Petitions from Answered Prayers) use classic printer's ornaments (such as a centered ivy fleuron `❧`, a subtle 3-point asterism `*  *  *`, or a centered `32dp` hairline rule) rather than generic digital gray bars.

---

## 7. Organic Motion, Transitions & Micro-Interactions

Interactions respect the quiet dignity of a physical journal, using responsive, physics-based modern motion rather than literal skeuomorphic gimmicks. Every movement feels deliberate, weight-balanced, and calming—reinforcing the tactile illusion of fine stationery in motion.

### 7.1 Screen & Hierarchy Transitions

Screen-level navigation reflects spatial continuity and page preservation across the three primary pathways. Views do not pop or flash; they translate with purposeful physical resistance.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SPATIAL HIERARCHY TRANSITION                       │
├─────────────────────────────────────────────────────────────────────────────┤
│  JOURNAL DIRECTORY (INDEX)      CONTAINER TRANSFORM         ENTITY RECORD   │
│  ┌───────────────────────┐      ───────────────────►      ┌───────────────┐ │
│  │ ▼ People (14 Entities)│     Duration: 300ms            │ Peter & Mary  │ │
│  │ ┌───────────────────┐ │     Curve: Emphasized Decel    │ ───────────── │ │
│  │ │ Peter & Mary Card │ │     (0.05, 0.7, 0.1, 1.0)      │ Petitions...  │ │
│  │ └───────────────────┘ │                                │               │ │
│  └───────────────────────┘                                └───────────────┘ │
│                                                                             │
│  DEVOTIONAL QUEUE TRAVERSAL     SHARED-AXIS X SLIDE       PREVIOUS/NEXT     │
│  [ Current Subject ]            ◄──────────────────►      [ Next Subject ]  │
│                                 Spring: Stiff 320 / Damp 0.85               │
└─────────────────────────────────────────────────────────────────────────────┘
```

1. **Home Screen Router & Pathway Transitions (`HomeScreen`)**:
   - **Root Router**: Presents three primary actions without splash headers or instructional copy:
     - **Start praying** (enters `PrayerSessionScreen`)
     - **Open Journal** (enters `JournalScreen`)
     - **Add prayer points** (enters `LogPrayerScreen` with `LinedNotepad`)
   - **Horizontal Swipe-Left**: Swiping left anywhere on `HomeScreen` navigates directly to `JournalScreen` (`280ms`, Shared-Axis X slide).

2. **Devotional Prayer Traversal & Queue Navigation (`PrayerSessionScreen`)**:
   - **Contemplative Atmosphere**: Navigation bars, system buttons, and progress counters are completely suppressed.
   - **Queue Traversal Model**: Traverses an anti-neglect queue prioritizing entities that haven't been visited recently:
     - **Next Subject**: Swiping left (or tapping within the **Right 30% touch zone**) smoothly advances to the next entity (`Shared-Axis X`, spring stiffness `320`, damping `0.85`).
     - **Previous Subject**: Swiping right (or tapping within the **Left 30% touch zone**) returns to the preceding entity.
     - **Exit Prayer Session**: Swiping down anywhere (or pressing Back) slides downward (`250ms`, Emphasized Accelerate) to return to `HomeScreen`.
   - **Automatic Background Bookkeeping**: Engagement counts and timestamps are updated silently in the background—zero celebratory toasts or score popups.
   - **Devotional Prompts & Status Resolution**: An asynchronous batch query loads concise, read-only suggested lines directly beneath points (strictly unlabelled, with no chat or question chrome); long-pressing any prayer point opens a dialog to toggle between Active and Answered status without leaving the session.

3. **Add Flow Capture & Committal (`LogPrayerScreen`)**:
   - **Step 1 (Text Capture)**: Lands immediately on a direct lined writing pad (`LinedNotepad`). Features automatic bullet formatting on line breaks, bypassing title or category fields upfront, and strictly excluding AI prompts or autocompletion while drafting.
   - **Step 2 (Entity Selection & Committal)**: Tapping "Next" opens the categorization sheet presenting four categories (People, Groups, General, Mission Partners), an entity list, and an option to create a new entity inline.
   - **Save Confirmation & Asynchronous Auto-Titling**: Committal displays a confirmation banner with an **Undo** action that restores the draft; in the background, an asynchronous AI service generates a concise summary title for the newly saved point without interrupting the user.

4. **Directory to Entity Detail Expansion (`JournalScreen` & `LifoBackStack`)**:
   - **Journal Directory**: Organizes records into four expandable/collapsible categories (People, Groups, General, Mission Partners), with header controls to return Home or open Settings.
   - **Entity Detail View**: Selecting any entity opens their record via a Material Container Transform (`300ms`), presenting read-only AI prompts derived from past entries, separate listings for active and answered points, an action to add points directly to that entity, and options to rename or recategorize them.
   - **Prayer Point Editor & List Actions**: Tapping a saved prayer point opens an in-place editor to modify the title, body, active/answered status, and thanksgiving note; in list views, swiping a card toggles its answered state or triggers deletion, and long-pressing opens context menus.
   - **Strict LIFO Back Stack (`LifoBackStack`)**: Accessed from Settings to configure theme mode, text scale tiers, English dialect, and historic prayer inclusion; all back actions (system back button or edge-swipe right) navigate backwards through the nested screen hierarchy in strict Last-In, First-Out order.

5. **Posture Morphing (Single-Pane to Dual-Pane Book Spread)**:
   - **Foldable Unfolding**: When transitioning from folded to tabletop/book posture, the single-pane canvas smoothly slides to the right half-screen while the left-pane directory index fades and slides in from `-32dp` (`350ms`, Emphasized easing).
   - **Hinge Ambient Occlusion**: The central `24dp` spine gutter gradient fades in synchronously (`200ms` fade) as the hinge angle crosses `135°`.

---

### 7.2 Micro-Interactions & State Transitions

Micro-interactions are restrained and functional, providing immediate optical clarity without sensory overload.

1. **Silk Marker Ribbon Pinning**:
   - **Geometry Shift**: Resting state (`40dp` height) stretches to pinned active state (`54dp` height) with a swallow-tail notch drop.
   - **Spring Dynamics**: Modeled on an authentic textile spring with slight overtravel (stiffness `220`, damping ratio `0.70`). Touch compression of `-3dp` on press-down.
   - **Haptic Synchronization**: Exactly at the peak of the downward spring extension, the device fires a crisp `CLOCK_TICK` haptic impulse.

2. **Baseline Rule Layout Reflow & Auto-Bulleting**:
   - **LinedNotepad Auto-Bullets**: Pressing Enter/Return in `LinedNotepad` generates a new bullet point glyph (`•`) seated precisely on the next dynamic baseline rule (`250ms`, `FastOutSlowInEasing`).
   - **Font Size Adjustments**: Feint rules smoothly morph vertical spacing without jumping. New rules fade in from `0%` to `70%` alpha.

3. **Quiet Autosave & Inked-to-Disk Pulse**:
   - **Ambient Indicator**: Positioned discreetly in the ledger header bar (`ink.muted` dot or text token).
   - **Pulse Curve**: Upon background disk write or auto-title generation:
     1. Color transitions from `ink.muted` (`#6E675F`) to Celadon Sage (`#3D6B52`) over `150ms`.
     2. A whisper-soft ambient glow expands outward (`0dp` to `4dp` blur radius, `20%` alpha) over `200ms`.
     3. Rests for `800ms` in the confident Sage state.
     4. Mellows back to `ink.muted` over `400ms` using a gentle linear decay.
   - Features zero intrusive toast banners or blocking spinners.

4. **Text Selection & Warm Amber Glaze**:
   - **Highlight Entrance**: Text selection does not render with harsh digital blue bounding rectangles. Selected passages are washed in a Warm Amber Glaze (`state.selection`: `#F5DE88` at `45%` alpha).
   - **Fade-In**: Amber wash fades in instantly (`100ms`, `LinearEasing`) following the cursor drag envelope.
   - **Contextual Action Bar**: The floating editorial action bar (Copy, Edit, Toggle Answered) floats up from `Y = +8dp` with an opacity fade (`150ms`, `DecelerateEasing`), anchored `12dp` above the selection boundary.

5. **In-Place Status Resolution & Card Swipes**:
   - Long-pressing a prayer point in the prayer session opens an in-place modal to toggle Active/Answered with optional thanksgiving notes.
   - In list views, swiping a card right toggles answered state with a Celadon wash; swiping left prompts deletion with Madder Crimson restraint.

---

### 7.3 Motion System Specifications & Accessibility

| Interaction Role | Motion Type | Duration / Physics | Easing Curve | Haptic Feedback |
| :--- | :--- | :--- | :--- | :--- |
| **Directory → Entity Detail** | Container Transform | `300ms` | Emphasized Decelerate `(0.05, 0.7, 0.1, 1.0)` | None |
| **LIFO Predictive Back** | Gestural Scale + Snap | Progress-driven / `250ms` | Emphasized Accelerate `(0.3, 0.0, 0.8, 0.15)` | `GESTURE_END` tick |
| **Queue Traversal (Next/Prev)**| Horizontal Spring Slide | Spring: `stiffness: 320, damping: 0.85` | Physics-driven | Light `GESTURE_THRESHOLD` tick |
| **Session Exit (Swipe Down)** | Vertical Slide Down | `250ms` | Emphasized Accelerate | `GESTURE_END` tick |
| **Marker Ribbon Toggle**| Vertical Spring Extension | Spring: `stiffness: 220, damping: 0.70` | Physics-driven (1 oscillation) | `CLOCK_TICK` at apex |
| **Status Toggle (Active ↔ Answered)**| Cross-Fade & Ink Morph | `200ms` | Standard `(0.2, 0.0, 0.0, 1.0)` | Crisp confirmation pulse |
| **Autosave / Auto-Title Pulse** | Color & Glow Cycle | `150ms` in / `800ms` hold / `400ms` decay | Smooth Sine Cycle | None (silent and unobtrusive) |

#### Accessibility: Respecting Reduced Motion
- When Android system setting **"Remove animations"** or `Settings.Global.TRANSITION_ANIMATION_SCALE = 0` is detected:
  - All positional slides, container expansions, and spring overshoots are instantly replaced by direct, instantaneous cross-fades (`100ms` alpha transition) or zero-duration cuts.
  - The marker ribbon toggles directly between `40dp` and `54dp` heights without bouncing.
  - The autosave indicator executes a subtle color shift without pulsing halos or size changes.
  - Haptic ticks remain functional as non-visual orientation confirmations unless tactile feedback is separately disabled in OS settings.

### 7.4 The Zero Gamification Mandate
- The prayer journal is explicitly free from streak counters, badge popups, celebration confetti, floating hearts, or competitive rings.
- Ordinal progress indicators ("Item 1 of N", "3 prayers completed today") are strictly omitted. It remains a calm, personal retreat where prayer is personal communion, never a chore list.

---

## 8. Ergonomics

Digital journals must balance quiet contemplation with effortless physical handling. This section establishes the spatial geometry, reachability zones, spacing rhythms, and touch boundaries for all interactive affordances across handheld and large-screen postures.

### 8.1 Minimum Touch Targets & Sensory Boundaries
Even the most understated analog aesthetic must honor the physical realities of human fingers on glass:
- **Universal Touch Boundary**: Every tappable element must maintain an invisible bounding box of at least **`48 × 48dp`**, regardless of its visual surface area.
- **Slender Affordance Compensation**: The silk marker ribbon, while visually slender at `18dp` width, projects an invisible touch target spanning `48dp` horizontally and `56dp` vertically from the top edge. This guarantees effortless tapping without requiring deliberate precision.
- **Margin Notations & Status Seals**: Field notation pills (`11sp` text, `4dp` corner radius) sit within an expanded `48dp` vertical tap track, allowing instantaneous toggling or expansion without collision.

### 8.2 Thumb Reach & Spatial Zoning
Layout is architected around natural one-handed thumb arcs (the "Comfort Arc") to prevent hand strain:
- **Devotional Prayer Touch Zones**:
  - **Left 30% Width**: Previous entity touch zone and swipe trigger.
  - **Right 30% Width**: Next entity touch zone and swipe trigger.
  - **Center 40% Width**: Reading area and long-press status resolution.
- **The Primary Action Zone (Bottom Third)**:
  - On `HomeScreen`, primary actions ("Start praying", "Open Journal", "Add prayer points") reside in the comfortable lower-to-middle third.
- **The Observation Zone (Top Half)**:
  - Infrequent and reflective actions (e.g., Settings icon or category header collapse) reside in the top chrome.
- **The Margin Track (Left 56dp)**:
  - Marginal annotations, paragraph timestamps, and inline tags remain comfortably reachable along the edge, accessible via secondary thumb sweeps.

### 8.3 Spacing Cadence & Protective Gutters
To eliminate mistaps and preserve visual tranquility, interactive controls follow a disciplined spatial cadence:
- **Inter-Affordance Spacing**: Adjacent touch targets must maintain a minimum clear gutter of **`8dp`** (recommended **`12dp` to `16dp`** between high-frequency triggers) measured from edge to edge of their active touch boundaries.
- **Leather Casing Insets**: Floating action affordances and lower toolbars maintain a minimum **`16dp`** perimeter margin from the display bounds.
- **Vertical Spacing Rhythm**: In linear lists and toolbars, touch targets align to an `8dp` spatial grid, with standard `40dp` visual heights seated inside `48dp` centered touch envelopes.

### 8.4 Affordance Classifications & Visual Hierarchy
Interactive components reflect authentic stationery craftsmanship rather than generic digital widgets:
- **The Primary Ink Trigger (Floating Action)**:
  - A circular vector medallion (`56dp` diameter, resting on a `48dp` elevated plane) finished in rich `leather.primary` or `ink.primary`.
  - Used for opening `LinedNotepad` or beginning a new prayer capture.
- **Textual Inked Anchors**:
  - Borderless, typographic touch triggers set in Ledger Sans or Literary Serif (e.g., "Start praying", "Next", "Undo").
  - They rely on optical letter spacing (`+0.5sp` to `+1.0sp`) and tonal ink shifts to signal interactivity without bulky plastic container shapes.
- **Notational Outline Pills**:
  - Encapsulated within a whisper-thin hairline (`0.5dp` stroke, `ink.muted` at `50%` alpha), providing a discrete boundary for category and status display.
- **Tool Palettes & Docked Trays**:
  - Horizontal bottom palettes float `12dp` above the navigation bar with `8dp` internal padding between tool icons, rendered in paper stock tones with soft `2dp` ambient occlusion.

### 8.5 Posture-Aware Adaptation
- **One-Handed Mobile Posture**: Action triggers cluster dynamically along the dominant thumb side, with generous `24dp` bottom clearance.
- **Two-Handed Landscape & Foldable Book Spread**:
  - Primary navigation triggers split to the outer margins (left edge of left page, right edge of right page).
  - The central `24dp` spine gutter remains completely clear of touch affordances to prevent awkward reaching across the physical hinge.
- **Stylus Mode Accommodations**:
  - When an active stylus is detected within proximity, palm rejection isolates the canvas, and touch sensitivity for edge affordances expands by `4dp` to prevent inadvertent wrist triggering.

---

## 9. Form Factor Versatility: Foldables, Tablets & Stylus

Modern Android form factors bring the physical folio experience to life.

### 9.1 Adaptive Dual-Pane Book Spread
On foldables unfolded into book posture and tablets in landscape mode:
- **Two-Page Open Folio**:
  - **Left Page**: Journal Directory overview with expandable categories (People, Groups, General, Mission Partners) or the Anti-Neglect Queue.
  - **Right Page**: Full-screen devotional prayer session or ruled writing canvas (`LinedNotepad`).
  - **Clean Spine Gutter**: The physical hinge acts as the central binding gutter with a subtle `24dp` spatial inset and soft ambient gradient—clean and unburdened by heavy 3D paper curl graphics.

### 9.2 Tabletop Posture (Journal on Desk)
When half-folded on a desk:
- **Top Display**: Shows entity name, prayer points, and suggested intercessions in comfortable contemplative reading mode.
- **Bottom Display**: Ruled writing canvas positioned ergonomically above the keyboard or stylus input area for adding prayer updates.

### 9.3 Precision Stylus Support (S-Pen / USI)
For handwritten marginal annotations and petitions:
- **Vector Ink Rendering**: Renders responsive digital ink matching the active archival ink tone (`ink.primary` or `ink.secondary`).
- **Margin Anchoring**: Handwritten marginal notes and editorial glyphs in the 56dp track bind strictly to the specific prayer point block they accompany, flowing naturally with text reflow and vertical scrolling without encroaching on the main text canvas.

---

## 10. Privacy Concealment & Ambient System Feedback

Personal petitions and vulnerable prayers require absolute discretion and calm feedback.

### 10.1 Closed Folio Privacy Shield
- When the app is backgrounded or viewed in the Android Recent Apps task switcher, the active prayer screen is immediately shielded.
- **Presentation**: Rendered as a handsome **Closed Leather Folio Cover** graphic—a flat, rich vector leather surface in the active hide color, centered with an understated embossed monogram insignia (clean vector deboss tone, free of shiny brass clasps). No private prayer text is ever exposed to system screenshots or shoulder surfing.

### 10.2 Quiet Autosave Status & Silent Auto-Titling
- Autosave status is communicated via an understated, low-contrast ink stamp or quiet dot in the top app bar that pulses softly to Celadon Sage upon save, or displays discreet text: *"Inked to disk"*.
- When new prayer points are committed in `LogPrayerScreen`, background auto-titling occurs silently in the background, updating the point header without displaying blocking spinners or modal interruptions.

---

## 11. Presentation Specifications & Design Tokens

This section details the precise spatial dimensions, layer stack, and token matrices for engineering implementation.

### 11.1 Canvas Geometry & Spatial Layout Grid

| Zone / Component | Dimension / Value | Spatial Anchor | Visual Treatment |
| :--- | :--- | :--- | :--- |
| **Folio Leather Casing** | `100%` width & height | Screen boundaries | Bound in leather base color with subtle `1dp` perimeter stroke. |
| **Spine Gutter (Foldable)**| `24dp` total width (`12dp` each side) | Centered on foldable hinge | Clean spatial gutter with subtle ambient occlusion gradient (`6%` α). |
| **Vellum Sheet Max Width** | `720dp` max width | Centered horizontally | Optimal reading measure (60–75 characters). |
| **Left Margin Track** | **`56dp`** fixed width | Left edge of vellum sheet | Houses Active/Answered seals, categories, and timestamps. |
| **Vertical Margin Rule** | **`0.75dp`** hairline | `X = 56dp` from left edge | Traditional stationery red/sepia (`#E5B4B4`). |
| **Prayer Text Inset** | `56dp + 8dp = 64dp` | `8dp` right of margin rule | Ensures text never collides with vertical margin line. |
| **Narrative Right Padding** | `24dp` inset | Right edge of vellum sheet | Defines max content measure. |
| **Horizontal Feint Rules** | **`0.75dp`** hairline | Runs full sheet width | Anchored to dynamic text baselines; spaced at `28sp` intervals. |
| **Prayer Screen Left Zone**| Left `30%` screen width | Left display boundary | Tap/Swipe target for Previous Entity traversal. |
| **Prayer Screen Right Zone**| Right `30%` screen width| Right display boundary | Tap/Swipe target for Next Entity traversal. |
| **Prayer Screen Center Zone**| Center `40%` screen width| Center display boundary | Reading canvas & Long-press status resolution target. |
| **Minimum Touch Envelope** | **`48dp × 48dp`** min | Centered on all interactive elements | Universal minimum target boundary for accessible touch. |
| **Inter-Affordance Gutter** | **`8dp` min / `16dp` std**| Between adjacent touch targets | Prevents inadvertent adjacent actuation. |

### 11.2 The Silk Marker Ribbon Geometry

```
 (0,0) ┌──────────────────┐ (18dp, 0)
       │                  │
       │  MARKER RIBBON   │  (Height: 40dp resting / 54dp pinned)
       │  TAB             │
       │        ▲         │
       │       / \        │
 (0,H) └───────     ──────┘ (18dp, H)
            (9dp, H-6dp)
        (Swallow-tail Notch)
```

- **Resting Height**: `40dp`
- **Pinned / Active Height**: `54dp` (animated via smooth spring: stiffness `220`, damping ratio `0.70`)
- **Tab Width**: `18dp` (Active touch boundary expanded to `48dp` horizontal × `56dp` vertical)
- **Swallow-Tail Inset**: `6dp` triangular cutout centered at `X = 9dp`
- **Visual Surface**: Crisp flat vector jewel tone (`ribbon.primary`) with a subtle `5%` tonal shade toward the bottom edge.

### 11.3 Typographic Scale Specification

| Text Role | Font Family | Size (`sp`) | Line Height (`sp`) | Tracking (`sp`) | Baseline Anchor |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Frontispiece Header** | Ledger Sans (Medium) | `12` | `16` | `+1.0` | Locked to top margin rule |
| **Prayer Session Subject Header**| Literary Serif (Semi-Bold)| `22`| `32` | `0.0` | Sits on first prominent rule; advances grid |
| **Prayer Point Bullets** | Literary Serif (Regular) | `17` | `28` | `+0.25` | Sits directly ON feint rules |
| **Answered Thanksgiving Note**| Literary Serif (Italic) | `16` | `26` | `+0.15` | Rules suppressed in card; indented 24dp; Celadon accent |
| **Suggested Intercession Line**| Literary Serif (Italic) | `15` | `24` | `0.0` | Directly beneath points; quiet italic |
| **Category Ledger Header** | Ledger Sans (Semi-Bold) | `13` | `18` | `+0.8` | Expandable category titles |
| **Margin Timestamp & Status**| Ledger Sans (Regular) | `11` | `16` | `+0.5` | Aligned to paragraph baseline |
| **Historical Footnote** | Literary Serif (Regular) | `13` | `20` | `+0.1` | Sits on condensed 20sp feint rules below hairline |

### 11.4 Complete Design Token Matrix

| Token Name | Folio Token (Singular Theme) | High-Contrast (Accessible) | Role & Usage |
| :--- | :--- | :--- | :--- |
| `leather.primary` | `#8C532B` | `#5A2800` | Saddle Tan folio cover & accents |
| `leather.cordovan`| `#5E2A2B` | `#3D1A1B` | Horween Cordovan leather finish |
| `leather.forest`  | `#2D483A` | `#1A2E24` | Hunter Forest leather finish |
| `paper.background` | `#FAF7F0` | `#FFFFFF` | Warm cream vellum writing canvas |
| `paper.ivory`     | `#F5EFEB` | `#FFFFFF` | Bone-tinted natural ivory stock |
| `paper.feintRule` | `#E2DDD5` (70% α) | `#9C9488` (100% α) | Baseline-locked horizontal ruling lines |
| `paper.marginRule` | `#E5B4B4` | `#A63D40` | 56dp vertical margin dividing line |
| `ink.primary` | `#1C1A17` | `#0F0E0D` | Iron-Gall primary narrative and petition ink |
| `ink.answered` | `#3D6B52` | `#1E4D3B` | Celadon Sage for answered prayers & praises |
| `ink.midnight` | `#1B2433` | `#0F172A` | Fountain-pen blue-black alternate narrative ink |
| `ink.secondary` | `#33261F` | `#262422` | Walnut Sepia for quotes, history & headers |
| `ink.muted` | `#6E675F` | `#4A4642` | Muted ledger ink for timestamps, suggestions, tags |
| `ribbon.primary` | `#8B2635` | `#8B2635` | Garnet bookmark marker ribbon tab (default) |
| `ribbon.emerald` | `#1E4D3B` | `#1E4D3B` | Forest Emerald bookmark ribbon variant |
| `ribbon.gold` | `#B8860B` | `#996F00` | Antique Gold bookmark ribbon variant |
| `state.selection` | `#F5DE88` (45% α) | `#FFE066` (60% α) | Warm amber glaze text highlight |
| `state.answered` | `#3D6B52` | `#1E4D3B` | Celadon Sage answered prayer confirmation seal |
| `state.autosave` | `#3D6B52` | `#1E4D3B` | Celadon sage ink confirmation |
| `state.alert` | `#8B2635` | `#A63D40` | Madder crimson warning alert |

---

## 12. Summary: Dated Skeuomorphism vs. Modern Leatherbound Craft

| Design Dimension | Dated 2011 Skeuomorphism (Discarded) | Modern Leatherbound Craft (Current Standard) |
| :--- | :--- | :--- |
| **Leather Treatment** | Photorealistic bitmap textures, fake stitches, heavy bevels | **Crisp Vector Casing**: Rich solid leather dyes (`#8C532B`), subtle perimeter hairlines |
| **Paper Canvas** | Yellowed photo texture, artificial paper grain, torn edges | **Fine Ruled Vellum**: Smooth unbleached paper stock (`#FAF7F0`), glare-free and clean |
| **Paper Ruling** | Static repeating background images that collide with text | **Baseline-Locked Feint Rules**: Dynamically synchronized to font line-heights (`28sp`) |
| **Margin Guide** | Photorealistic school notebook red line with 3D drop shadow | **Vector Margin Hairline**: Clean `0.75dp` rule creating a disciplined `56dp` metadata track |
| **Bookmark Ribbon** | Shiny photorealistic woven ribbon with drop shadows & sheen | **Vector Swallow-Tail Tab**: Clean jewel-toned tab with responsive spring physics & haptic tick |
| **Typography & Ink** | Simulated wet ink drying animations, fake bleed & paper tooth | **Archival Vector Ink**: High-grade literary serif (*Literata*), instant crisp rendering |
| **Screen Navigation**| 3D page curl flips with sweeping artificial shadows | **Fluid Modern Navigation**: Predictive Back, shared-axis slide transitions, anti-neglect queue traversal |
| **Devotional Focus** | Gamified streak flames, daily points, pop-up confetti | **Zero Gamification Standard**: Quiet, unhurried intercession; no ordinal counters ("1 of N") |
| **Haptic Feedback** | Simulated pen nib scratch friction & paper drag vibrations | **Refined Tactile Clicks**: Subtle clock ticks for ribbon pinning & threshold activations |
| **Foldables & Tablets**| 3D curved binding shadows simulating bent paper | **Clean Bound Spine Gutter**: Natural spatial gutter with subtle ambient occlusion gradient |
| **Privacy Mask** | Photorealistic closed leather book with 3D brass clasps | **Clean Closed Folio**: Flat vector leather tone with minimalist embossed monogram seal |
