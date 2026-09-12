# Codebase Knowledge Graph & System Architecture Map

**Document:** [`planning/codebase_knowledge_graph.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/codebase_knowledge_graph.md)  
**Status:** Living Architectural Blueprint & Dependency Graph  
**Application Title:** *Pray Without Ceasing* (1 Thessalonians 5:17)  
**Last Updated:** 2026-09-12  
**Doctrinal Bedrock:** Classical Reformed, Historic Anglican (1662 BCP), Heidelberg Catechism Q&A 1  
**Design Bedrock:** Modern Leatherbound Craft (The Modern Folio) defined in [`planning/android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md) (Strictly read-only)  
**Platform Scope:** Mobile Client (Android Kotlin + Jetpack Compose) & Edge API Proxy (Cloudflare Worker)  

---

## 1. Overview & Architectural Topology

The *Pray Without Ceasing* repository is organized into three primary operational domains:

1. **Planning, Conceptualization & Product Specification ([`planning/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning))**:
   The doctrinal, functional, technical, and UX contract repository. It holds living doctrinal specifications (`beliefs.md`), business requirements (`BRD.md`), technical reference (`technical.md`), UX specifications (`UX.md`), unified data architecture covering prayer points, library books, and daily notes (`notes_data_architecture.md`), the immutable visual design bible ([`android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md)), and the automated browser-based layout and gesture contract test suite.
2. **AI Agent API Production Engine ([`api/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api))**:
   A zero-cost, stateless Cloudflare Worker serverless edge proxy that interfaces anonymously with upstream LLM inference providers (OpenRouter) with strict Reformed theological guardrails, budget hard ceilings ($5.00/mo), and dual execution branches (Ambient Grounded Suggestions & Post-Commit Auto-Titling).
3. **Native Android Production Client ([`android/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android))**:
   An offline-first, encrypted (Room with SQLCipher) mobile client implemented in Kotlin and Jetpack Compose adhering to the Modern Leatherbound Folio aesthetic, 0dp planar geometry, 8dp spacing rhythm, dual-engine typography, baseline-synchronized feint rules, buttonless swipe gesture mechanics, and an anti-neglect intercession queue.

```mermaid
graph TD
    subgraph Planning_and_Specification ["1. Planning & Specifications (planning/)"]
        BELIEFS["beliefs.md<br/>(Theological Bedrock)"]
        BRD["BRD.md<br/>(Business Requirements)"]
        TECH["technical.md<br/>(Technical Reference)"]
        UX["UX.md<br/>(Design & Interaction)"]
        NOTES_ARCH["notes_data_architecture.md<br/>(Unified Data Architecture:<br/>Points, Books & Notes)"]
        JOURNAL_DESIGN["android_journal_design_principles.md<br/>(Canonical Folio Design Bible - Read-Only)"]
        TEST_RUNNER["test_runner.html<br/>(DOM & Layout Contract)"]
        SPEC_TESTS["planning/tests/*.spec.js<br/>(Playwright Verification)"]
    end

    subgraph API_Edge_Proxy ["2. AI Agent API Proxy (api/)"]
        WORKER["worker.js<br/>(Cloudflare Worker Router)"]
        WRANGLER["wrangler.jsonc<br/>(Cloudflare Deployment Config)"]
        COMPILED_PROMPT["system_prompt.txt<br/>(Authoritative Compiled Prompt)"]
    end

    subgraph Android_Client ["3. Native Android Client (android/)"]
        MAIN["MainActivity.kt<br/>(Lifecycle, Privacy Shield & Routing)"]
        UI_SCREENS["ui/screens/<br/>(Home, Sanctuary, Log, Journal, Notes, Settings, Library, VolumeReader)"]
        UI_COMPONENTS["ui/components/<br/>(LinedNotepad, SilkMarkerRibbon, ClosedFolioShield, VaultBackupDialogs)"]

        UI_GESTURES["ui/gestures/<br/>(TouchGestureModifier, LIFO)"]
        DATA_LOCAL["data/local/<br/>(PrayerDatabaseHelper, Repository)"]
        DATA_SECURITY["data/security/<br/>(VaultBackupCrypto.kt)"]
        DATA_MODELS["data/models/<br/>(Models.kt, PreloadedContent.kt, LibraryModels.kt, LibraryContent.kt, VaultBackupPayload.kt)"]
        RAW_ASSETS["res/raw/<br/>(historic_prayers.json, library_calvin_prayer.json)"]
        NETWORK["network/<br/>(PrayerApiClient.kt)"]
        JVM_TESTS["android/src/test/java/<br/>(139 Replicated JVM Tests)"]
    end

    BELIEFS -.->|"Doctrinal Constraints"| COMPILED_PROMPT
    BELIEFS -.->|"Liturgical Gravity"| UX
    BRD -.->|"Functional Scope"| TECH
    JOURNAL_DESIGN -.->|"Canonical Modern Folio Design Bible"| UX
    JOURNAL_DESIGN -.->|"Visual & Ergonomic Specifications"| UI_THEME
    JOURNAL_DESIGN -.->|"Pathways & Sensory Metaphor"| UI_SCREENS
    UX -.->|"Planar 0dp Geometry & Folio Tokens"| UI_THEME
    UX -.->|"Buttonless Gestures & 30/40/30 Zones"| UI_GESTURES
    TECH -.->|"Offline SQLCipher Spec"| DATA_LOCAL
    TECH -.->|"API Proxy Contracts"| WORKER
    
    COMPILED_PROMPT --> WORKER

    WORKER <-->|"JSON over HTTPS<br/>(Masked Entities, X-Prayer-Gateway-Secret)"| NETWORK
    NETWORK --> MAIN
    MAIN --> UI_SCREENS
    UI_SCREENS --> DATA_LOCAL
    DATA_LOCAL --> DATA_MODELS
    DATA_MODELS --> RAW_HISTORIC
    
    SPEC_TESTS -.->|"Validates Layout Spec"| TEST_RUNNER
    JVM_TESTS -.->|"Verifies Implementation"| Android_Client
```

---

## 2. Ontological & Doctrinal Knowledge Graph

The application architecture enforces strict theological, ontological, and lexical invariants across all tiers.

```mermaid
erDiagram
    ROOT_CATEGORY ||--o{ INDIVIDUAL_ENTITY : "classifies"
    INDIVIDUAL_ENTITY ||--o{ PRAYER_POINT : "anchors"
    INDIVIDUAL_ENTITY ||--o| SUGGESTION_CACHE : "caches"
    PRAYER_POINT ||--o{ JOURNAL_UPDATE : "chronicles"
    APP_CONFIG ||--|| CLIENT_ENVIRONMENT : "configures"

    ROOT_CATEGORY {
        string code PK "PEOPLE | GROUPS | MISSION_PARTNERS | GENERAL | HISTORIC | NOTES"
        string displayTitle "People | Groups | Mission Partners | General | Historic | Notes"
        int sortOrder "1 | 2 | 3 | 4 | 5 | 6"
    }

    INDIVIDUAL_ENTITY {
        string id PK "UUID"
        string rootCode FK "PEOPLE | GROUPS | MISSION_PARTNERS | GENERAL | HISTORIC | NOTES"
        string displayName "Individual name or collective title"
        string contextDescription "Relational / vocational background"
        boolean isPreloadedHistoric "Flag for preloaded collects/creeds"
        int interactedCount "Cumulative contemplative reviews"
        int64 lastInteractedAt "Epoch ms of last prayer engagement"
        int64 createdAt "Epoch ms of entity creation"
    }

    SUGGESTION_CACHE {
        string entityId PK "References INDIVIDUAL_ENTITY.id"
        string praiseGod "JSON array of Praise God prompts"
        string thankGod "JSON array of Thank God prompts"
        string askGod "JSON array of Ask God prompts"
        string suggestions "JSON array of flattened suggestions"
        int64 timestamp "Epoch ms when cached"
    }

    PRAYER_POINT {
        string id PK "UUID"
        string entityId FK "References INDIVIDUAL_ENTITY.id"
        string title "Concise 2-6 word summary"
        string description "Telegraphic, scannable body ON feint rules"
        string status "ACTIVE | ANSWERED | ARCHIVED | HISTORIC"
        int interactedCount "Times reviewed in prayer mode"
        int64 createdAt "Epoch ms of entry"
        int64 lastInteractedAt "Epoch ms of last contemplation"
        int64 answeredAt "Epoch ms when marked answered"
        string answeredTestimony "Thanksgiving reflection note"
    }

    JOURNAL_UPDATE {
        string id PK "UUID"
        string prayerPointId FK "References PRAYER_POINT.id"
        string updateText "Chronological pastoral note"
        int64 timestamp "Epoch ms"
    }

    APP_CONFIG {
        string localeDialect "EN_AU_UK (default) | EN_US"
        string leatherFinish "SADDLE_TAN (default) | CORDOVAN | HUNTER_FOREST | OBSIDIAN"
        string paperStock "CREAM_VELLUM (default) | NATURAL_IVORY | AGED_PARCHMENT"
        string textScale "LARGE (default) | REGULAR | COMPACT (data model compatibility)"
        float textZoomScale "0.75f - 2.5f zoom scale persisted in TABLE_CONFIG"
        boolean blendHistoricPrayers "Interleave historic collects into rotation"
    }
```

### 2.1 Confessional Guardrails & Doctrinal Invariants

| Doctrinal Invariant | Architectural Enforcement Point | System Guarantee |
| :--- | :--- | :--- |
| **Exclusivity of Christ (*Solus Christus*)** | [`api/prompts/PROMPT_THEOLOGY.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_THEOLOGY.txt), [`TheologicalGuardrailsTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/TheologicalGuardrailsTest.kt) | Prayers directed exclusively to God in the name of Jesus Christ; zero saint, relic, Mary, or angelic intercession. |
| **Sovereignty of God (*Soli Deo Gloria*)** | [`api/prompts/PROMPT_THEOLOGY.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_THEOLOGY.txt), [`api/worker.js`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/worker.js) | Complete rejection of prosperity dogmas, word-faith decrees, positive manifestation, and bargaining. |
| **Heidelberg Catechism Q&A 1 Comfort** | [`api/prompts/PROMPT_THEOLOGY.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_THEOLOGY.txt) | Suffering and anxiety framed in Christ's ownership, Father's sovereign preservation, and Holy Spirit assurance. |
| **Believer's Agency (Ambient Assistant)** | [`api/prompts/PROMPT_PERSONA.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_PERSONA.txt), [`PROMPT_SUGGESTION_FLOW.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_SUGGESTION_FLOW.txt) | AI operates silently in the background; zero questions and zero chat; provides telegraphic suggested prayer points; never writes actual prayers addressing God ("Dear Lord..."). |
| **Strict Non-Fabrication & Anti-Overfitting** | [`api/worker.js`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/worker.js), [`api/system_prompt.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/system_prompt.txt) | AI shall never invent unstated trials, cancer, surgeries, or tragedies; strictly grounded in recorded target context and forbidden from copying or overfitting to few-shot prompt examples. |
| **Complete Grammatical Sentences (Never Cut Off)** | [`api/worker.js`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/worker.js) | Every suggested prompt must be a 100% complete, fully finished grammatical thought. Serverless proxy defensively strips dangling prepositions/conjunctions and condenses gracefully without blind truncation. |
| **Batch Target Context Ingestion** | [`api/worker.js`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/worker.js), [`PrayerApiClient.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/network/PrayerApiClient.kt) | All recorded user data on the target is passed to the AI in one go (not line by line) via `POST /api/v1/suggest`. |
| **Modern Leatherbound Folio Metaphor** | [`android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md), [`UX.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/UX.md) | Structural metaphor of leatherbound notebook with fine ruled paper, warm vector casing, fine vellum canvas, archival inks, and silk marker ribbon. |
| **The Singular Folio Law** | [`android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md), [`UX.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/UX.md) | Single glare-free tactile theme (cream vellum + iron-gall ink) comfortable in daylight and bedside lamplight, eliminating day/night color inversion. |
| **The Baseline Synchronization Law** | [`android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md), [`LinedNotepad.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/components/LinedNotepad.kt) | Ruled lines dynamically anchored to active typographic baselines (`28sp`); never static repeating background stripes. |
| **The 37.52dp Left Margin Track** | [`android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md), [`UX.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/UX.md) | Disciplined two-track layout: 37.52dp vertical margin rule (reduced by 33% from 56dp) for status notation pills and timestamps; 45.52dp narrative text inset. |
| **Universal 48dp Touch Target** | [`android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md), [`UX.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/UX.md) | Minimum `48 × 48dp` touch bounding box on all interactive elements (silk ribbon expanded to `48 × 80dp` dynamic, anchored flush to top-end with `36dp` narrative text clearance and `52dp` prompt card clearance to prevent obscuring text or menu text). |
| **Devotional Prayer Touch Zoning** | [`android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md), [`SanctuaryPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt) | Left 30% (previous), Right 30% (next), Center 40% (reading & in-place status resolution). |
| **Prohibition of Ordinal / Sequential Badges** | [`planning/UX.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/UX.md), [`LexiconContractTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/LexiconContractTest.kt) | Never code or display generic ordinal labels (*Point 1*, *Point 2*, *Item 1 of N*); prayer points are sacred burdens. |
| **Minimal Contextual Data Exposure** | [`planning/BRD.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/BRD.md), [`SanctuaryPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt) | Internal progression counters, queue tallies, and root tags are suppressed from devotional presentation. |
| **Collapsed-by-Default Prayer Prompts** | [`beliefs.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/beliefs.md), [`SanctuaryPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt), [`JournalScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/JournalScreen.kt) | AI prayer prompts are hidden and collapsed by default across sanctuary prayer and journal detail views, expanding only upon explicit user request. |
| **Persistent Prompt Cache & Background Refresh** | [`beliefs.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/beliefs.md), [`technical.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/technical.md), [`SanctuaryPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt), [`JournalScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/JournalScreen.kt), [`PrayerRepository.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/local/PrayerRepository.kt), [`PrayerDatabaseHelper.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/local/PrayerDatabaseHelper.kt) | The last list of prompts returned by the API is cached persistently in SQLite (`suggestion_cache`, DB v10); viewing an entity immediately renders the last list of prompts returned the last time the app was used, while an asynchronous background refresh queries for fresh prompts, seamlessly updating the display and cache upon completion without user interruption. |
| **Closed Folio Privacy Shield** | [`android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md), [`MainActivity.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/MainActivity.kt) | Recent Apps task switcher view is shielded by a flat vector leather cover with embossed monogram seal. |
| **Historic Prayer AI & Answered Status Suppression** | [`SanctuaryPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt), [`JournalScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/JournalScreen.kt), [`PreloadedContent.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/models/PreloadedContent.kt) | Entities with `isPreloadedHistoric = true` receive no AI prompt generation and suppress active/answered toggle buttons and status resolution across Sanctuary and Journal views; these carry the timeless prayers of the saints and require no temporal answered state. Subject header rendered as direct title (not "Praying for") in Sanctuary mode, with the 37.52dp left track serving as an unadorned structural spacer preserving the red margin rule. |
| **Multi-Record Dotpoint Separation** | [`LogPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/LogPrayerScreen.kt), [`PrayerRepository.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/local/PrayerRepository.kt) | Every new line in the lined notepad automatically formats with a dotpoint (`• `); when committed, each dotpoint is separated out via `splitIntoDotpoints` and persisted via `savePrayerPoints` as an independent `PrayerPoint` record in SQLite, enabling each dotpoint to be individually tracked and marked active or answered across Sanctuary and Journal views. |
| **Multi-Session Concurrency & Build Serialization Protocol** | [`AGENTS.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/AGENTS.md), [`technical.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/technical.md) | When multiple agent sessions run concurrently, agents strictly serialize Gradle executions, bar `.\gradlew.bat --stop` or `clean`, use `--no-daemon` for tests, re-read files immediately before editing to prevent race conditions on shared files, and consolidate release deployments under a single-deployer protocol. |

---

## 3. Subsystem Deep Dive: Planning & Contracts (`planning/`)

```mermaid
graph LR
    subgraph Documents ["Living Specification Documents"]
        BELIEFS["beliefs.md<br/>(Doctrinal Bedrock)"]
        BRD["BRD.md<br/>(Business Requirements)"]
        TECH["technical.md<br/>(Technical Reference)"]
        UX["UX.md<br/>(Design & Interaction)"]
        GRAPH["codebase_knowledge_graph.md<br/>(System Architecture Graph)"]
        JOURNAL_SPEC["android_journal_design_principles.md<br/>(Canonical Folio Design Bible - Read-Only)"]
    end

    subgraph Verification ["Specification Verification Suite"]
        RUNNER["test_runner.html<br/>(DOM Harness & Spec Contract)"]
        T_QUEUE["tests/anti_neglect_queue.spec.js"]
        T_FLOWS["tests/devotional_flows.spec.js"]
        T_GESTURE["tests/gesture_engine.spec.js"]
        T_LAYOUT["tests/layout_geometry.spec.js"]
        T_LEXICON["tests/lexicon_contract.spec.js"]
        T_HARNESS["tests/test_runner.spec.js"]
    end

    JOURNAL_SPEC -.->|"Immutable Visual & Ergonomic Bible"| UX
    BELIEFS --> BRD
    BRD --> TECH
    TECH --> UX
    UX --> RUNNER
    RUNNER --> T_QUEUE
    RUNNER --> T_FLOWS
    RUNNER --> T_GESTURE
    RUNNER --> T_LAYOUT
    RUNNER --> T_LEXICON
    RUNNER --> T_HARNESS
```

### 3.1 Document Matrix
- **[`beliefs.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/beliefs.md)**: Confessional commitments, 1662 BCP liturgical roots, Heidelberg Q1 comfort model, Modern Leatherbound Folio reverence, and prohibitions against prosperity gospel, saintly intercession, and AI prayer generation.
- **[`BRD.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/BRD.md)**: Product scope, target audience (<100 users, personal distribution), 3-choice frontispiece, passive prayer mode, direct lined notepad entry, Modern Folio design system, and $5/month operational budget ceiling.
- **[`technical.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/technical.md)**: SQLCipher database schema, zero-leakage API key security, Cloudflare Worker proxy specs, baseline-synchronized feint rules, 37.52dp margin track (reduced by 33% from 56dp), universal 48dp touch targets, closed folio privacy shield, and anti-neglect queue balancing logic.
- **[`UX.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/UX.md)**: Modern Leatherbound Folio aesthetic, The Singular Folio Law, Four Chromatic Tiers, dual-engine typography, baseline synchronization, 37.52dp margin track, silk marker ribbon tab, 30/40/30 devotional touch zones, LIFO back stack transitions, and sacred terminology lexicon.
- **[`android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md)**: Authoritative visual design bible for the analog-inspired leather and paper Android journal (**Strictly Read-Only for agents**).

---

## 4. Subsystem Deep Dive: AI Agent API (`api/`)

```mermaid
flowchart TD
    CLIENT["Android Client (PrayerApiClient.kt)"]
    
    subgraph CF_Worker ["Cloudflare Worker Proxy (worker.js)"]
        CORS["CORS Preflight (OPTIONS)"]
        HEALTH["GET /health, GET /<br/>Route Discovery"]
        AUTH["Validate X-Prayer-Gateway-Secret"]
        ROUTER{Route Path}
        
        subgraph Suggest_Branch ["Batch Suggestion Branch (/api/v1/suggest)"]
            PARSE_SUGGEST["Parse SuggestRequest<br/>(target, root, past points, draft)"]
            PROMPT_SUGGEST["Build Two-Tier Suggestion Prompts<br/>(Tier 1 Drafter + Tier 2 Compliance)"]
            CALL_SUGGEST["OpenRouter Two-Tier Calls<br/>Tier 1: Temp 0.7, Top_P 0.95<br/>Tier 2: Temp 0.1, Max Tokens: 9000"]
            VALIDATE_SUGGEST["Theological & Grounding Validation<br/>(3-12 points across Praise/Thank/Ask God, 4-15 words/line, starts with For/That/A/Because, non-parroting, no wishy-washy platitudes)"]
        end
    end

    OPENROUTER["Upstream LLM Provider (OpenRouter.ai)"]

    CLIENT -->|"POST /api/v1/suggest"| AUTH
    AUTH --> ROUTER
    
    ROUTER -->|"/api/v1/suggest"| PARSE_SUGGEST
    PARSE_SUGGEST --> PROMPT_SUGGEST
    PROMPT_SUGGEST --> CALL_SUGGEST
    CALL_SUGGEST <--> OPENROUTER
    CALL_SUGGEST --> VALIDATE_SUGGEST
    VALIDATE_SUGGEST -->|"SuggestResponse JSON"| CLIENT
```

---

## 5. Subsystem Deep Dive: Native Android Client (`android/`)

```mermaid
graph TD
    subgraph UI_Layer ["Presentation / UI Layer"]
        MAIN["MainActivity.kt<br/>(Activity & Navigation Host)"]
        LIFO["LifoBackStack.kt<br/>(LIFO Screen State Manager)"]
        
        subgraph Screens ["Screens (ui/screens/)"]
            HOME["HomeScreen.kt<br/>(Frontispiece Canvas)"]
            SANCTUARY["SanctuaryPrayerScreen.kt<br/>(Buttonless Contemplation, 30/40/30 Zones, Baseline-Synchronized Ruled Lines & Collapsed Prompts)"]
            LOG["LogPrayerScreen.kt<br/>(Lined Notepad Canvas, Drafting Text Zoom & Assistant)"]
            JOURNAL["JournalScreen.kt<br/>(Relational Vault, Inline Editable Draft Point, Edit Point Text Zoom, Ribbon & Collapsed Prompts)"]
            SETTINGS["SettingsScreen.kt<br/>(Folio Preferences & Binding)"]
            LIBRARY["LibraryScreen.kt<br/>(Theological Catalog & Bookshelf)"]
            READER["VolumeReaderScreen.kt<br/>(Immersive Folio Reader, Dynamic Baseline Rules & Pinch-to-Zoom)"]
            NOTES["NotesScreen.kt<br/>(Date Grouping Directory, Filed Notes List, Ruled Notepad Editor with Optional Titles, Pinch-to-Zoom & Grounded AI Prompts)"]
        end

        subgraph Components_Theme ["Components, Theme & Gestures"]
            THEME["Theme.kt<br/>(4 Chromatic Tiers, 4 Leather Finishes, High-Contrast AA)"]
            SPACING["Spacing.kt<br/>(37.52dp Margin, 45.52dp Inset, 8dp Grid)"]
            TYPO["Typography.kt<br/>(Dual-Engine Serif + Sans Scaling)"]
            GESTURES["TouchGestureModifier.kt<br/>(Directional & Edge Swipe, Double-Tap Word Selection, Multi-Touch Pinch-to-Zoom)"]
            NOTEPAD["components/LinedNotepad.kt<br/>(Baseline-Locked Ruled Canvas, AutoFocus & End-of-Last-Line Cursor Activation)"]
            RIBBON["components/SilkMarkerRibbon.kt<br/>(Swallow-Tail Bookmark Tab)"]
            SHIELD["components/ClosedFolioShield.kt<br/>(Privacy Concealment Cover)"]
            BACKUP_DIALOGS["components/VaultBackupDialogs.kt<br/>(Folio Passphrase Seal & Unseal Dialogs)"]
        end
    end

    subgraph Network_Layer ["Network Layer (network/)"]
        API_CLIENT["PrayerApiClient.kt<br/>(OkHttp, Serializer, Fallback)"]
    end

    subgraph Data_Layer ["Data & Storage Layer (data/)"]
        REPO["PrayerRepository.kt<br/>(Entity/Point CRUD, Anti-Neglect Queue, Library Progress, Vault Backup & Restore)"]
        DB_HELPER["PrayerDatabaseHelper.kt<br/>(SQLite Schema, DB v10 Migration, Multi-Entity Historic Seeding)"]
        CRYPTO["security/VaultBackupCrypto.kt<br/>(AES-256-GCM + PBKDF2WithHmacSHA256 Zero-Telemetry Engine)"]
        MODELS["Models.kt, LibraryModels.kt & VaultBackupPayload.kt<br/>(Domain & Data Classes)"]
        PRELOADED["PreloadedContent.kt & LibraryContent.kt<br/>(Loaders & memory caches)"]
        RAW_HISTORIC["res/raw/historic_prayers.json<br/>(30 historic prayers catalog)"]
        RAW_CALVIN["res/raw/library_calvin_prayer.json<br/>(Calvin: Of Prayer, 52 sections)"]
    end

    MAIN --> LIFO
    MAIN --> HOME
    MAIN --> SANCTUARY
    MAIN --> LOG
    MAIN --> JOURNAL
    MAIN --> LIBRARY
    MAIN --> READER
    MAIN --> NOTES
    MAIN --> SHIELD
    
    HOME --> THEME
    HOME --> SPACING
    SANCTUARY --> GESTURES
    SANCTUARY --> THEME
    SANCTUARY --> API_CLIENT
    LOG --> NOTEPAD
    LOG --> THEME
    NOTEPAD --> GESTURES
    JOURNAL --> THEME
    JOURNAL --> NOTEPAD
    JOURNAL --> RIBBON
    JOURNAL --> API_CLIENT
    JOURNAL --> SETTINGS
    SETTINGS --> THEME
    LIBRARY --> THEME
    READER --> GESTURES
    READER --> THEME
    READER --> RIBBON
    NOTES --> THEME
    NOTES --> NOTEPAD
    NOTES --> RIBBON
    NOTES --> API_CLIENT
    NOTES --> GESTURES

    MAIN --> REPO
    LOG --> REPO
    JOURNAL --> REPO
    SANCTUARY --> REPO
    LIBRARY --> REPO
    READER --> REPO
    NOTES --> REPO

    REPO --> DB_HELPER
    REPO --> MODELS
    REPO --> PRELOADED
    DB_HELPER --> MODELS
    PRELOADED --> RAW_HISTORIC
    PRELOADED --> RAW_CALVIN
```

---

## 6. System Data & Control Flows

### 6.1 Contemplative Sanctuary Prayer Flow
```mermaid
sequenceDiagram
    autonumber
    actor Believer as Believer (User)
    participant Home as HomeScreen
    participant Main as MainActivity
    participant Repo as PrayerRepository
    participant DB as SQLite / SQLCipher
    participant Sanct as SanctuaryPrayerScreen

    Believer->>Home: Taps "Start praying"
    Home->>Main: onStartPraying()
    Main->>Repo: getContemplativeTopics(blendHistoric)
    Repo->>DB: Query entities ordered by anti-neglect criteria
    DB-->>Repo: Return ordered entities
    Repo->>DB: Fetch active & answered points per entity
    DB-->>Repo: Return prayer points
    Repo-->>Main: List<TopicWithPoints>
    Main->>Repo: recordTopicInteraction(topic[0].id)
    Repo->>DB: Update last_interacted_at & increment interacted_count
    Main->>Sanct: Navigate with slide/crossfade animation
    Sanct-->>Believer: Display "Praying for [Name]" + Points ON Feint Rules (0 buttons, 0 cards)
    
    Believer->>Sanct: Swipe Left / Tap Right 30% (Next Topic)
    Sanct->>Main: onNextTopic()
    Main->>Repo: recordTopicInteraction(topic[1].id)
    Main->>Sanct: Animate horizontal slide (stiffness 320, damping 0.85)
    Sanct-->>Believer: Display Topic 2
    
    Believer->>Sanct: Swipe Down (Exit)
    Sanct->>Main: onExit()
    Main->>Home: Pop LIFO stack, restore Home
```

### 6.2 Lined Notepad Entry & Multi-Record Dotpoint Separation
```mermaid
sequenceDiagram
    autonumber
    actor Believer as Believer (User)
    participant Log as LogPrayerScreen (Lined Notepad)
    participant Main as MainActivity
    participant Repo as PrayerRepository

    Believer->>Log: Writes bullet points on baseline-locked ruled notepad
    Believer->>Log: Taps "Next" -> Selects Person/Group
    Log->>Log: splitIntoDotpoints(text) -> List<String>
    Log->>Main: onSavePrayerPoints(entityId, points)
    Main->>Repo: savePrayerPoints(entityId, points)
    Repo-->>Main: List<PrayerPoint> (each with distinct ID & ACTIVE status)
    Main-->>Believer: Quiet Celadon Autosave Pulse + Batch Undo Toast
```

### 6.3 Active Record Inline Draft Point Flow
```mermaid
sequenceDiagram
    autonumber
    actor Believer as Believer (User)
    participant Journal as JournalScreen (ENTITY_DETAIL)
    participant Repo as PrayerRepository
    participant DB as SQLite / SQLCipher

    Believer->>Journal: Views saved points for active record
    Believer->>Journal: Taps "+ Add prayer point"
    Journal->>Journal: Sets isAddingDraftPoint = true, autofocuses draft input
    Journal-->>Believer: Renders inline draft row with [ DRAFT ] margin pill
    Believer->>Journal: Types petition & taps "Save Point"
    Journal->>Journal: splitIntoDotpoints(text)
    Journal->>Repo: savePrayerPoints(entityId, points)
    Repo->>DB: INSERT into TABLE_POINTS
    DB-->>Repo: Saved PrayerPoints
    Repo-->>Journal: Returns saved points
    Journal->>Journal: Refreshes localPoints & clears draft state
    Journal-->>Believer: Displays newly committed point below list
```

---

## 7. Quality & Verification Contract Mapping

```mermaid
graph LR
    subgraph Spec_Tests ["Web / Layout Spec Tests (planning/tests/)"]
        P_GEO["layout_geometry.spec.js"]
        P_GES["gesture_engine.spec.js"]
        P_QUE["anti_neglect_queue.spec.js"]
        P_LEX["lexicon_contract.spec.js"]
        P_FLO["devotional_flows.spec.js"]
    end

    subgraph JVM_Tests ["Native JVM Tests (android/src/test/)"]
        J_GEO["LayoutGeometryTest.kt"]
        J_GES["GestureEngineTest.kt"]
        J_QUE["AntiNeglectQueueTest.kt"]
        J_LEX["LexiconContractTest.kt"]
        J_FLO["DevotionalFlowsTest.kt"]
        J_LIFO["LifoBackStackTest.kt"]
        J_MOD["DataModelsTest.kt"]
        J_API["PrayerApiClientTest.kt"]
        J_THEO["TheologicalGuardrailsTest.kt"]
    end

    subgraph System_Contracts ["System Quality Contracts"]
        C_GEO["0dp Planar Geometry & Folio Layout"]
        C_GES["Touch & Edge-Swipe Gestures (30/40/30)"]
        C_QUE["Anti-Neglect Priority Queue"]
        C_LEX["Reverent Devotional Lexicon"]
        C_THEO["Solus Christus & Confessional Guardrails"]
    end

    P_GEO -.-> C_GEO
    J_GEO -.-> C_GEO
    P_GES -.-> C_GES
    J_GES -.-> C_GES
    P_QUE -.-> C_QUE
    J_QUE -.-> C_QUE
    P_LEX -.-> C_LEX
    J_LEX -.-> C_LEX
    J_THEO -.-> C_THEO
```

### 7.1 Cross-Cutting Verification Suite Matrix

| Contract / Feature | Planning Specification Test (`planning/tests/`) | Native Android Unit Test (`android/app/src/test/`) | Contract Guarantee |
| :--- | :--- | :--- | :--- |
| **0dp Planar Geometry & Folio Layout** | `layout_geometry.spec.js` | [`LayoutGeometryTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/LayoutGeometryTest.kt) | Every tile has `border-radius: 0dp`, 0px gutters, directly abutting hairline seams, baseline-locked rules, and WCAG AA High-Contrast tokens. |
| **Buttonless Sanctuary** | `layout_geometry.spec.js` | [`LayoutGeometryTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/LayoutGeometryTest.kt) | Zero buttons, zero cards, zero borders in Sanctuary prayer mode. |
| **Swipe Gestures & Touch Zoning** | `gesture_engine.spec.js` | [`GestureEngineTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/GestureEngineTest.kt) | Horizontal swipe advances/returns; vertical swipe exits; left edge-swipe right pops back stack; 30/40/30 zones verified. |
| **Anti-Neglect Queue** | `anti_neglect_queue.spec.js` | [`AntiNeglectQueueTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/AntiNeglectQueueTest.kt) | Topics ordered by oldest `last_interacted_at` and lowest `interacted_count`. |
| **Lexicon Compliance** | `lexicon_contract.spec.js` | [`LexiconContractTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/LexiconContractTest.kt) | Zero occurrences of *target*, *entity*, *commit*, *ticket*, *Point 1*, *Point 2*. |
| **Devotional Flows** | `devotional_flows.spec.js` | [`DevotionalFlowsTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/DevotionalFlowsTest.kt) | End-to-end coverage: blank start, note capture, journal management, topic progression. |
| **LIFO Back Stack** | N/A (State Machine) | [`LifoBackStackTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/LifoBackStackTest.kt) | LIFO push/pop mechanics, root preservation, empty-stack safety. |
| **Data Serialization** | N/A | [`DataModelsTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/DataModelsTest.kt) | Wire JSON serialization/deserialization for entities, points, configs, and payloads. |
| **API Client & Networking** | N/A | [`PrayerApiClientTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/PrayerApiClientTest.kt) | OkHttp serialization, timeout enforcement, ambient suggestions. |
| **Theological Guardrails** | N/A (Tested in `api/test/`) | [`TheologicalGuardrailsTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/TheologicalGuardrailsTest.kt) | Verifies Solus Christus, rejection of prosperity/saints, and absence of direct AI prayer text. |

---

## 8. Complete File & Dependency Mapping Matrix

| Relative Path | Architectural Layer | Primary Responsibility | Direct Upstream Dependencies | Primary Downstream Consumers |
| :--- | :--- | :--- | :--- | :--- |
| **[`AGENTS.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/AGENTS.md)** | Root Governance | Workspace protocol, reading prerequisites, synchronization rules | Universal | All Agents & Developers |
| **[`planning/beliefs.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/beliefs.md)** | Specification / Theology | Doctrinal foundation, confessional standards, prayer theology | Historic Formularies | `BRD.md`, `technical.md`, `UX.md`, `system_prompt.txt` |
| **[`planning/BRD.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/BRD.md)** | Specification / Product | Business requirements, MVP scope, core features, folio design | `beliefs.md`, `android_journal_design_principles.md` | `technical.md`, `UX.md`, Android App |
| **[`planning/technical.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/technical.md)** | Specification / Tech | System architecture, SQLCipher schema, proxy specs, folio geometry | `beliefs.md`, `BRD.md`, `android_journal_design_principles.md` | `worker.js`, `PrayerRepository.kt`, `PrayerApiClient.kt` |
| **[`planning/UX.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/UX.md)** | Specification / Design | Modern Folio aesthetics, baseline sync, gestures, 48dp targets, privacy shield | `beliefs.md`, `BRD.md`, `android_journal_design_principles.md` | `Theme.kt`, `Spacing.kt`, `TouchGestureModifier.kt` |
| **[`android_journal_design_principles.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/android_journal_design_principles.md)** | Canonical Spec / Design | Authoritative visual design bible for the modern folio (Strictly read-only) | User-authored | All Agents, `UX.md`, `technical.md`, Android App |
| **[`api/worker.js`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/worker.js)** | AI API / Edge Proxy | Multi-route Cloudflare Worker router & inference proxy (mandates deployment via Wrangler script `npm run deploy` / `npx wrangler deploy` on change or edge sync) | `system_prompt.txt`, OpenRouter API | `PrayerApiClient.kt` |
| **[`api/wrangler.jsonc`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/wrangler.jsonc)** | AI API / Deployment | Cloudflare Workers deployment configuration (`wrangler deploy`) | Cloudflare CLI | Cloudflare Edge Runtime |
| **[`api/system_prompt.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/system_prompt.txt)** | AI API / Prompt | Authoritative compiled system prompt | `beliefs.md`, `BRD.md` | `worker.js` |
| **[`android/app/src/main/java/au/prayer/app/MainActivity.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/MainActivity.kt)** | Android / Activity | Root activity, lifecycle, LIFO navigation, privacy mask, launch state, `WindowInsets.safeDrawing` keyboard inset management | `PrayerRepository`, `LifoBackStack`, `Theme` | Android OS |
| **[`android/app/src/main/java/au/prayer/app/ui/screens/HomeScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/HomeScreen.kt)** | Android / Screen | Modern Folio frontispiece, bookplate actions, cold-boot opening revelation | `Theme.kt`, `Spacing.kt`, `SilkMarkerRibbon.kt` | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt)** | Android / Screen | Buttonless full-screen prayer mode with read-only AI prompts, 30/40/30 zones, instant inline status toggle on margin pencil icon / answered pill, and modal on long-press | `TouchGestureModifier.kt`, `Theme.kt`, `PrayerApiClient.kt` | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/screens/LogPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/LogPrayerScreen.kt)** | Android / Screen | Ruled lined notepad canvas, zero AI assistance, single-tap save, scrollable dialogs | `Theme.kt`, `Spacing.kt`, `LinedNotepad.kt` | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/screens/JournalScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/JournalScreen.kt)** | Android / Screen | Journal directory, date-separated entry listings, margin line-drawn pencil active indicator with 0ms optimistic status toggle, buttonless status in edit view with TopAppBar save, scrollable dialogs | `Theme.kt`, `Models.kt`, `PrayerApiClient.kt` | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/screens/SettingsScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/SettingsScreen.kt)** | Android / Screen | Modern Folio settings, leather dye swatches, live text scale previews, dialect selector, historic prayer & contrast toggles | `Theme.kt`, `Spacing.kt`, `Typography.kt`, `Models.kt` | `JournalScreen.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/components/LinedNotepad.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/components/LinedNotepad.kt)** | Android / Component | Baseline-synchronized ruled notepad canvas with auto-bulleting, autofocus, and end-of-last-line cursor positioning | Compose Foundation | `LogPrayerScreen.kt`, `JournalScreen.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/gestures/TouchGestureModifier.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/gestures/TouchGestureModifier.kt)** | Android / Gestures | High-precision swipe, 30/40/30 zoning, and edge-swipe detection | Android Compose Pointer API | `SanctuaryPrayerScreen.kt`, `JournalScreen.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/navigation/LifoBackStack.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/navigation/LifoBackStack.kt)** | Android / Navigation | LIFO screen state manager | Kotlin Collections | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/theme/Theme.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/theme/Theme.kt)** | Android / Theming | Folio leather and vellum schemes | Material 3 Compose | All Screens |
| **[`android/app/src/main/java/au/prayer/app/ui/theme/Spacing.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/theme/Spacing.kt)** | Android / Theming | `PrayerSpacing` 8dp grid spacing tokens & 48dp minimum targets | Compose Dp | All Screens |
| **[`android/app/src/main/java/au/prayer/app/ui/theme/Typography.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/theme/Typography.kt)** | Android / Theming | Dual-engine typographic scaling (Literary Serif + Ledger Sans) | Compose Typography | All Screens |
| **[`android/app/src/main/res/values/themes.xml`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/res/values/themes.xml)** | Android / Resources | Zero-flash Theme.Prayer with warm vellum windowBackground | Android OS Theme | Android Manifest |
| **[`android/app/src/main/res/values-v31/themes.xml`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/res/values-v31/themes.xml)** | Android / Resources | Android 12+ SplashScreen theme with debossed monogram icon | Android 12+ OS | Android Manifest |
| **[`android/app/src/main/res/drawable/ic_splash_monogram.xml`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/res/drawable/ic_splash_monogram.xml)** | Android / Resources | Debossed Latin cross monogram vector icon | Android Vector | themes.xml (v31) |
| **[`android/app/src/main/res/drawable/ic_launcher_background.xml`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/res/drawable/ic_launcher_background.xml)** | Android / Resources | Saddle Tan (#8C532B) leather folio cover launcher background | Android Vector | `ic_launcher.xml`, `ic_launcher_round.xml` |
| **[`android/app/src/main/res/drawable/ic_launcher_foreground.xml`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/res/drawable/ic_launcher_foreground.xml)** | Android / Resources | Garnet Silk Marker Ribbon (#8B2635) & debossed Latin cross (#4A2810) launcher foreground | Android Vector | `ic_launcher.xml`, `ic_launcher_round.xml` |
| **[`android/app/src/main/res/drawable/ic_launcher_monochrome.xml`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/res/drawable/ic_launcher_monochrome.xml)** | Android / Resources | Monochrome Latin cross & ribbon silhouette for Android 13+ Material You themed icons | Android Vector | `ic_launcher.xml`, `ic_launcher_round.xml` |
| **[`android/app/src/main/java/au/prayer/app/data/local/PrayerDatabaseHelper.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/local/PrayerDatabaseHelper.kt)** | Android / Database | SQLite schema definition (DB v10), entity/point tables, reading progress, suggestion_cache | Android SQLite | `PrayerRepository.kt` |
| **[`android/app/src/main/java/au/prayer/app/data/local/PrayerRepository.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/local/PrayerRepository.kt)** | Android / Repository | CRUD operations, Anti-Neglect queue query, suggestion cache persistence & retrieval | `PrayerDatabaseHelper.kt`, `Models.kt` | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/data/models/Models.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/models/Models.kt)** | Android / Domain Models | Enums and data classes | Kotlinx Serialization | Repository, API Client, UI |
| **[`android/app/src/main/java/au/prayer/app/data/models/PreloadedContent.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/models/PreloadedContent.kt)** | Android / Seed Data | Parses `res/raw/historic_prayers.json` into 30 `PreloadedHistoricTopic` pairs (entities + HISTORIC points); lazy singleton cache | `Models.kt`, Kotlinx Serialization, `res/raw/historic_prayers.json` | `PrayerDatabaseHelper.kt`, `PrayerRepository.kt` |
| **[`android/app/src/main/res/raw/historic_prayers.json`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/res/raw/historic_prayers.json)** | Android / Seed Data | Standalone structured catalog of the 30 public-domain historic prayers (Lord's Prayer, 1662 BCP collects, Apostles' Creed, Spurgeon pulpit prayers, early church from Potts/CCEL); single source of truth for historic content | User-authored (public domain texts) | `PreloadedContent.kt`, JVM test classpath (`AntiNeglectQueueTest.kt`) |
| **[`android/app/src/main/java/au/prayer/app/network/PrayerApiClient.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/network/PrayerApiClient.kt)** | Android / Network | OkHttp client, wire payloads (`suggest`), grouped ambient prompts (Praise God, Thank God, Ask God) | OkHttp, Kotlinx Serialization | `MainActivity.kt`, `SanctuaryPrayerScreen.kt`, `JournalScreen.kt` |
| **[`android/app/src/main/java/au/prayer/app/data/security/VaultBackupCrypto.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/security/VaultBackupCrypto.kt)** | Android / Security | AES-256-GCM authenticated encryption + PBKDF2WithHmacSHA256 (120,000 rounds) zero-telemetry vault backup/restore engine | `javax.crypto`, `VaultBackupPayload.kt` | `SettingsScreen.kt`, `PrayerRepository.kt` |
| **[`android/app/src/main/java/au/prayer/app/data/models/VaultBackupPayload.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/models/VaultBackupPayload.kt)** | Android / Models | Serializable DTOs for portable vault archive and restore summary | Kotlinx Serialization | `VaultBackupCrypto.kt`, `PrayerRepository.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/components/VaultBackupDialogs.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/components/VaultBackupDialogs.kt)** | Android / UI | Modern Folio passphrase seal & unseal modal dialogs with Merge/Replace restore strategy selection | Compose Material 3, FlatSquareShape | `SettingsScreen.kt` |
