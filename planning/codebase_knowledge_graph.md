# Codebase Knowledge Graph & System Architecture Map

**Document:** [`planning/codebase_knowledge_graph.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/codebase_knowledge_graph.md)  
**Status:** Living Architectural Blueprint & Dependency Graph  
**Application Title:** *Pray Without Ceasing* (1 Thessalonians 5:17)  
**Last Updated:** 2026-09-07  
**Doctrinal Bedrock:** Classical Reformed, Historic Anglican (1662 BCP), Heidelberg Catechism Q&A 1  
**Platform Scope:** Mobile Client (Android Kotlin + Jetpack Compose) & Edge API Proxy (Cloudflare Worker)  

---

## 1. Overview & Architectural Topology

The *Pray Without Ceasing* repository is organized into three primary operational domains:

1. **Planning, Conceptualization & Product Specification ([`planning/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning))**:
   The doctrinal, functional, technical, and UX contract repository. It holds the living doctrinal specifications and the automated browser-based layout and gesture contract test suite.
2. **AI Agent API Production Engine ([`api/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api))**:
   A zero-cost, stateless Cloudflare Worker serverless edge proxy that interfaces anonymously with upstream LLM inference providers (OpenRouter) with strict Reformed theological guardrails, budget hard ceilings, and dual execution branches (Distillation & Post-Commit Auto-Titling).
3. **Native Android Production Client ([`android/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android))**:
   An offline-first, encrypted (Room / SQLCipher) mobile client implemented in Kotlin and Jetpack Compose adhering to an austere Material 3 0dp planar geometry, buttonless swipe gesture mechanics, and an anti-neglect intercession queue.

```mermaid
graph TD
    subgraph Planning_and_Specification ["1. Planning & Specifications (planning/)"]
        BELIEFS["beliefs.md<br/>(Theological Bedrock)"]
        BRD["BRD.md<br/>(Business Requirements)"]
        TECH["technical.md<br/>(Technical Reference)"]
        UX["UX.md<br/>(Design & Interaction)"]
        TEST_RUNNER["test_runner.html<br/>(DOM & Layout Contract)"]
        SPEC_TESTS["planning/tests/*.spec.js<br/>(Playwright Verification)"]
    end

    subgraph API_Edge_Proxy ["2. AI Agent API Proxy (api/)"]
        WORKER["worker.js<br/>(Cloudflare Worker Router)"]
        WRANGLER["wrangler.jsonc<br/>(Cloudflare Deployment Config)"]
        PROMPTS["api/prompts/*.txt<br/>(Modular Prompt Fragments)"]
        COMPILED_PROMPT["system_prompt.txt<br/>(Authoritative Assembly)"]
        BENCHMARK["api/test/run_stress_test.py<br/>(100-Case Stress Suite)"]
        CLI_TEST["interactive_guide.ps1<br/>(Interactive CLI Tester)"]
    end

    subgraph Android_Client ["3. Native Android Client (android/)"]
        MAIN["MainActivity.kt<br/>(Lifecycle & Routing)"]
        UI_SCREENS["ui/screens/<br/>(Home, Sanctuary, Log, Journal)"]
        UI_THEME["ui/theme/<br/>(FlatSquareShape, Spacing, Typography)"]
        UI_GESTURES["ui/gestures/<br/>(TouchGestureModifier, LIFO)"]
        DATA_LOCAL["data/local/<br/>(PrayerDatabaseHelper, Repository)"]
        DATA_MODELS["data/models/<br/>(Models.kt, PreloadedContent.kt)"]
        NETWORK["network/<br/>(PrayerApiClient.kt)"]
        JVM_TESTS["android/src/test/java/<br/>(65 Replicated JVM Tests)"]
    end

    BELIEFS -.->|"Doctrinal Constraints"| PROMPTS
    BELIEFS -.->|"Liturgical Gravity"| UX
    BRD -.->|"Functional Scope"| TECH
    UX -.->|"Planar 0dp Geometry"| UI_THEME
    UX -.->|"Buttonless Gestures"| UI_GESTURES
    TECH -.->|"Offline SQLCipher Spec"| DATA_LOCAL
    TECH -.->|"API Proxy Contracts"| WORKER
    
    COMPILED_PROMPT --> WORKER
    PROMPTS --> COMPILED_PROMPT

    WORKER <-->|"JSON over HTTPS<br/>(Masked Entities, X-Prayer-Gateway-Secret)"| NETWORK
    NETWORK --> MAIN
    MAIN --> UI_SCREENS
    UI_SCREENS --> DATA_LOCAL
    DATA_LOCAL --> DATA_MODELS
    
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
    PRAYER_POINT ||--o{ JOURNAL_UPDATE : "chronicles"
    APP_CONFIG ||--|| CLIENT_ENVIRONMENT : "configures"

    ROOT_CATEGORY {
        string code PK "PEOPLE | GROUPS | GENERAL"
        string displayTitle "People | Groups | General"
        int sortOrder "1 | 2 | 3"
    }

    INDIVIDUAL_ENTITY {
        string id PK "UUID"
        string rootCode FK "PEOPLE | GROUPS | GENERAL"
        string displayName "Individual name or collective title"
        string contextDescription "Relational / vocational background"
        boolean isPreloadedHistoric "Flag for preloaded collects/creeds"
        int interactedCount "Cumulative contemplative reviews"
        int64 lastInteractedAt "Epoch ms of last prayer engagement"
        int64 createdAt "Epoch ms of entity creation"
    }

    PRAYER_POINT {
        string id PK "UUID"
        string entityId FK "References INDIVIDUAL_ENTITY.id"
        string title "Concise 2-6 word telegraphic summary"
        string description "Telegraphic body (2-3 clause semicolon pattern)"
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
        string themeMode "MORNING_LIGHT (default) | QUIET_NIGHT"
        string textScale "LARGE (default) | REGULAR | COMPACT"
        boolean blendHistoricPrayers "Interleave historic collects into rotation"
    }
```

### 2.1 Confessional Guardrails & Doctrinal Invariants

All AI prompts, client UI strings, and database models are bound by the following confessional rules:

| Doctrinal Invariant | Architectural Enforcement Point | System Guarantee |
| :--- | :--- | :--- |
| **Exclusivity of Christ (*Solus Christus*)** | [`api/prompts/PROMPT_THEOLOGY.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_THEOLOGY.txt), [`TheologicalGuardrailsTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/TheologicalGuardrailsTest.kt) | Prayers directed exclusively to God in the name of Jesus Christ; zero saint, relic, Mary, or angelic intercession. |
| **Sovereignty of God (*Soli Deo Gloria*)** | [`api/prompts/PROMPT_THEOLOGY.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_THEOLOGY.txt), [`api/worker.js`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/worker.js) | Complete rejection of prosperity dogmas, word-faith decrees, positive manifestation, and bargaining. |
| **Heidelberg Catechism Q&A 1 Comfort** | [`api/prompts/PROMPT_THEOLOGY.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_THEOLOGY.txt) | Suffering and anxiety framed in Christ's ownership, Father's sovereign preservation, and Holy Spirit assurance. |
| **Believer's Agency (No AI Prayer Writing)** | [`api/prompts/PROMPT_PERSONA.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_PERSONA.txt), [`PROMPT_CARD_STYLE.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_CARD_STYLE.txt) | AI strictly distills user thoughts into telegraphic prayer points; never writes actual prayers addressing God ("Dear Lord..."). |
| **Prohibition of Presumed Burdens** | [`api/prompts/PROMPT_INQUIRY_FLOW.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_INQUIRY_FLOW.txt) | AI must never invent cancer, hospitalizations, surgeries, or deaths from bare names; vague input mandates inquiry. |
| **Prohibition of Ordinal / Sequential Badges** | [`planning/UX.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/UX.md), [`LexiconContractTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/LexiconContractTest.kt) | Never code or display generic ordinal labels (*Point 1*, *Point 2*, *Item 1 of N*); prayer points are sacred burdens. |
| **Minimal Contextual Data Exposure** | [`planning/BRD.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/BRD.md), [`SanctuaryPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt) | Internal progression counters, queue tallies, and root tags are suppressed from devotional presentation. |
| **Personal Privacy & Confidentiality** | [`api/prompts/PROMPT_TAXONOMY_PRIVACY.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_TAXONOMY_PRIVACY.txt), [`PrayerApiClient.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/network/PrayerApiClient.kt) | Entity names are masked locally before transmission; entity resolution remains 100% on-device. |

---

## 3. Subsystem Deep Dive: Planning & Contracts (`planning/`)

The [`planning/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning) folder houses the specifications and layout verification suites.

```mermaid
graph LR
    subgraph Documents ["Living Specification Documents"]
        BELIEFS["beliefs.md<br/>(Doctrinal Bedrock)"]
        BRD["BRD.md<br/>(Business Requirements)"]
        TECH["technical.md<br/>(Technical Reference)"]
        UX["UX.md<br/>(UX & Aesthetics)"]
        GRAPH["codebase_knowledge_graph.md<br/>(System Architecture Graph)"]
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
- **[`beliefs.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/beliefs.md)**: Establishes confessional commitments, 1662 BCP liturgical roots, the Heidelberg Q1 comfort model, and explicit prohibitions against prosperity gospel, saintly intercession, and AI prayer generation.
- **[`BRD.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/BRD.md)**: Product scope, target audience (<100 users, personal distribution), 3-choice home canvas, passive prayer mode, lined notepad entry with pushed-down assistant, and $5/month operational budget ceiling.
- **[`technical.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/technical.md)**: SQLCipher database schema, zero-leakage API key security, Cloudflare Worker proxy specs, OkHttp client configurations, and anti-neglect queue balancing logic.
- **[`UX.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/UX.md)**: Material 3 0dp planar geometry, 8dp spacing tokens (`PrayerSpacing`), buttonless smartphone swipe navigation, LIFO back stack transitions, Morning Light vs Quiet Night visual modes, and sacred terminology lexicon.

### 3.2 Automated Specification Verification (`planning/tests/`)
Driven by Playwright, validating the specification contracts directly against [`test_runner.html`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/test_runner.html):
- **`layout_geometry.spec.js`**: Enforces `border-radius: 0px`, 0px gutters/margins between adjacent tiles, 1px hairline seams, and full-screen buttonless sanctuary layout.
- **`gesture_engine.spec.js`**: Validates horizontal swipes (advance/previous topic), vertical swipe down (exit sanctuary), and left edge-swipe right LIFO back stack navigation.
- **`anti_neglect_queue.spec.js`**: Proves FIFO topic rotation sorted by least recently prayed and lowest interaction counts.
- **`lexicon_contract.spec.js`**: Validates zero occurrences of forbidden engineering jargon (*target*, *entity*, *commit*, *ticket*, *Point 1*, *Point 2*).
- **`devotional_flows.spec.js`**: Exercises end-to-end user journeys from blank launch to prayer, notepad writing, and journal management.

---

## 4. Subsystem Deep Dive: AI Agent API (`api/`)

The [`api/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api) directory contains the production serverless proxy deployed on Cloudflare Workers.

```mermaid
flowchart TD
    CLIENT["Android Client (PrayerApiClient.kt)"]
    
    subgraph CF_Worker ["Cloudflare Worker Proxy (worker.js)"]
        CORS["CORS Preflight (OPTIONS)"]
        HEALTH["GET /health, GET /<br/>Route Discovery"]
        AUTH["Validate X-Prayer-Gateway-Secret"]
        ROUTER{Route Path}
        
        subgraph Guide_Branch ["Distillation & Guidance Branch"]
            PARSE_GUIDE["Parse GuideRequest<br/>(initial_reflection, root, group, turn)"]
            PROMPT_ASSISTANT["Build Two-Tier Prompts<br/>(Tier 1 Drafter + Tier 2 Harness)"]
            CALL_GUIDE["OpenRouter Two-Tier Calls<br/>(nvidia/nemotron-3.5-lightning)<br/>Tier 1: Temp 1.0, Top_P 0.95<br/>Tier 2: Temp 0.1, Max Tokens: 9000"]
            VALIDATE_GUIDE["Theological & Schema Validation<br/>(Inquiry first, 2 cards, 2-6 words)"]
        end

        subgraph Title_Branch ["Post-Commit Auto-Titling Branch"]
            PARSE_TITLE["Parse TitleRequest<br/>(body text, dialect)"]
            PROMPT_TITLE["Build Two-Tier Title Prompts<br/>(Tier 1 Brainstorm + Tier 2 Harness)"]
            CALL_TITLE["OpenRouter Two-Tier Calls<br/>Tier 1: Temp 1.0, Top_P 0.95<br/>Tier 2: Temp 0.1, Max Tokens: 9000"]
            RETURN_TITLE["Return Clean JSON Title"]
        end
    end

    OPENROUTER["Upstream LLM Provider (OpenRouter.ai)"]

    CLIENT -->|"POST /api/v1/guide"| AUTH
    CLIENT -->|"POST /api/v1/title"| AUTH
    AUTH --> ROUTER
    
    ROUTER -->|"/api/v1/guide"| PARSE_GUIDE
    PARSE_GUIDE --> PROMPT_ASSISTANT
    PROMPT_ASSISTANT --> CALL_GUIDE
    CALL_GUIDE <--> OPENROUTER
    CALL_GUIDE --> VALIDATE_GUIDE
    VALIDATE_GUIDE -->|"GuideResponse JSON"| CLIENT

    ROUTER -->|"/api/v1/title"| PARSE_TITLE
    PARSE_TITLE --> PROMPT_TITLE
    PROMPT_TITLE --> CALL_TITLE
    CALL_TITLE <--> OPENROUTER
    CALL_TITLE --> RETURN_TITLE
    RETURN_TITLE -->|"TitleResponse JSON"| CLIENT
```

### 4.1 Modular Prompts & System Assembly
The system prompt is organized under [`api/prompts/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts) and compiled into [`api/system_prompt.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/system_prompt.txt):
- **[`PROMPT_PERSONA.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_PERSONA.txt)**: Concise distillation engine; zero therapeutic filler, zero artificial empathy.
- **[`PROMPT_INQUIRY_FLOW.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_INQUIRY_FLOW.txt)**: Enquire first policy; turn limits (max 2 turns); 6–12 word plain English questions; burden triage; unconditional skipping.
- **[`PROMPT_THEOLOGY.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_THEOLOGY.txt)**: Reformed confessional guardrails; Solus Christus; Heidelberg Q1 comfort; prayer for unbelievers focused on repentance/faith; strict prohibition against composing actual prayers.
- **[`PROMPT_TAXONOMY_PRIVACY.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_TAXONOMY_PRIVACY.txt)**: Three root mapping rules (`PEOPLE`, `GROUPS`, `GENERAL`); personal prayer points under `PEOPLE` (*Me*); single-root invariant; on-device entity privacy masking.
- **[`PROMPT_CARD_STYLE.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_CARD_STYLE.txt)**: Strictly 2 candidate points; title hard limit: strictly 2 to 6 words; description ceiling: 20–25 words; 2-to-3 clause semicolon structure (`Clause 1; Clause 2; Clause 3`); no redundant prefixes.
- **[`PROMPT_OUTPUT_SCHEMA.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts/PROMPT_OUTPUT_SCHEMA.txt)**: Strict JSON output schema with conditional branching for questions vs. prayer cards.

### 4.2 API Testing & Benchmark Suite (`api/test/`)
- **`prayer_requests_stress_test.json`**: 100 diverse, realistic prayer burdens spanning all three roots, emotional states, pastoral trials, theological traps (e.g. prosperity decrees, praying to Mary), and privacy boundaries.
- **`run_stress_test.py`**: Automated stress runner querying the live proxy or local endpoint and persisting raw model completions to `stress_test_responses.json`.
- **`BENCHMARK_RESULTS.md`**: Systematic quantitative analysis measuring compliance across root mapping, question generation, card counts, title word counts, description length ceilings, and theological boundaries.
- **`AI_GENERATED_TEXT_REPORT.md`**: Qualitative linguistic analysis assessing telegraphic brevity, semicolon clause syntax, vocabulary tone, and absence of conversational filler.
- **`interactive_guide.ps1`**: Interactive PowerShell terminal client simulating the multi-turn mobile companion flow.

---

## 5. Subsystem Deep Dive: Native Android Client (`android/`)

The native mobile app is structured using standard clean architecture principles for modern Android with Kotlin and Jetpack Compose.

```mermaid
graph TD
    subgraph UI_Layer ["Presentation / UI Layer"]
        MAIN["MainActivity.kt<br/>(Activity & Navigation Host)"]
        LIFO["LifoBackStack.kt<br/>(LIFO Screen State Manager)"]
        
        subgraph Screens ["Screens (ui/screens/)"]
            HOME["HomeScreen.kt<br/>(Tripartite Blank Canvas)"]
            SANCTUARY["SanctuaryPrayerScreen.kt<br/>(Buttonless Contemplation)"]
            LOG["LogPrayerScreen.kt<br/>(Lined Notepad & Assistant)"]
            JOURNAL["JournalScreen.kt<br/>(Relational Vault & Settings)"]
        end

        subgraph Theme_Gestures ["Theme & Gestures"]
            THEME["Theme.kt<br/>(FlatSquareShape, Color Schemes)"]
            SPACING["Spacing.kt<br/>(PrayerSpacing 8dp Rhythm)"]
            TYPO["Typography.kt<br/>(Text Scale Engine)"]
            GESTURES["TouchGestureModifier.kt<br/>(Directional & Edge Swipe)"]
        end
    end

    subgraph Network_Layer ["Network Layer (network/)"]
        API_CLIENT["PrayerApiClient.kt<br/>(OkHttp, Serializer, Fallback)"]
    end

    subgraph Data_Layer ["Data & Storage Layer (data/)"]
        REPO["PrayerRepository.kt<br/>(Entity/Point CRUD, Anti-Neglect Queue)"]
        DB_HELPER["PrayerDatabaseHelper.kt<br/>(SQLite Schema & Table Contracts)"]
        MODELS["Models.kt<br/>(Domain & Data Classes)"]
        PRELOADED["PreloadedContent.kt<br/>(BCP Collects, Creed, Lord's Prayer)"]
    end

    MAIN --> LIFO
    MAIN --> HOME
    MAIN --> SANCTUARY
    MAIN --> LOG
    MAIN --> JOURNAL
    
    HOME --> THEME
    HOME --> SPACING
    SANCTUARY --> GESTURES
    SANCTUARY --> THEME
    LOG --> API_CLIENT
    LOG --> THEME
    JOURNAL --> THEME

    MAIN --> REPO
    LOG --> REPO
    JOURNAL --> REPO
    SANCTUARY --> REPO

    REPO --> DB_HELPER
    REPO --> MODELS
    REPO --> PRELOADED
    DB_HELPER --> MODELS
```

### 5.1 Component Inventory & Class Responsibilities

#### Presentation Layer (`au.prayer.app.ui`)
- **[`MainActivity.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/MainActivity.kt)**: Root activity. Initializes `PrayerDatabaseHelper`, `PrayerRepository`, and `PrayerApiClient`. Hosts the `LifoBackStack` and renders directional slide/fade transitions via `AnimatedContent`. Launches asynchronous background auto-titling after prayer point committal.
- **[`screens/HomeScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/HomeScreen.kt)**: Renders the pristine tripartite launch screen: **Start praying**, **Open Journal**, and **Add prayer points**. Implements 0dp flat rectangular tiles directly abutting each other with 1px hairline dividing seams.
- **[`screens/SanctuaryPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt)**: Full-screen buttonless prayer mode. Displays solely `Praying for [Name]`, followed by unadorned prayer points and expandable answered prayers. Employs `TouchGestureModifier` for swipe navigation and responsive touch zones.
- **[`screens/LogPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/LogPrayerScreen.kt)**: Handles entity-first prayer point addition. Renders an authentic lined ruled notepad with auto-bulleting (`• `) and pushes the secondary `Prayer Assistant` action to the bottom slab. Supports entity renaming, category shifting, and cascading deletion.
- **[`screens/JournalScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/JournalScreen.kt)**: Directory view of all people, groups, and general topics. Provides one-click editing of prayer points, swipe-to-answer, swipe-to-delete, search filtering, and access to sequestered application settings.
- **[`gestures/TouchGestureModifier.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/gestures/TouchGestureModifier.kt)**: High-precision pointer gesture detector. Distinguishes horizontal swipes ($\Delta X \ge 50\text{dp}$), vertical dismiss swipes ($\Delta Y \ge 50\text{dp}$), and left edge-swipe right navigation ($X_{start} \le 25\text{dp}$).
- **[`navigation/LifoBackStack.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/navigation/LifoBackStack.kt)**: LIFO navigation stack ensuring predictable, sequential back navigation across screen states.
- **[`theme/Theme.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/theme/Theme.kt)**: Material 3 theme configuration enforcing `FlatSquareShape` (`RoundedCornerShape(0.dp)`) across all M3 components. Defines high-contrast monochrome palettes for Morning Light and Quiet Night.
- **[`theme/Spacing.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/theme/Spacing.kt)**: Design tokens adhering to an 8dp spacing rhythm (`PrayerSpacing`).
- **[`theme/Typography.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/theme/Typography.kt)**: Dynamic typography scaling supporting `LARGE`, `REGULAR`, and `COMPACT` modes with high-legibility sans-serif styles.

#### Data & Local Persistence Layer (`au.prayer.app.data`)
- **[`local/PrayerDatabaseHelper.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/local/PrayerDatabaseHelper.kt)**: SQLite helper configuring tables (`entities`, `points`, `app_config`), column schemas, foreign key indices, and initial preloaded seed injection.
- **[`local/PrayerRepository.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/local/PrayerRepository.kt)**: Encapsulates database transactions. Implements the **Anti-Neglect Devotional Queue Algorithm**:
  ```sql
  SELECT e.* FROM entities e
  WHERE e.is_historic = 0
  AND EXISTS (
      SELECT 1 FROM points p 
      WHERE p.entity_id = e.id 
      AND p.status IN ('ACTIVE', 'HISTORIC')
  )
  ORDER BY 
      e.last_interacted_at IS NOT NULL ASC,
      e.last_interacted_at ASC,
      e.interacted_count ASC,
      RANDOM()
  ```
- **[`models/Models.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/models/Models.kt)**: Domain entities (`RootCode`, `PrayerStatus`, `LocaleDialect`, `ThemeMode`, `TextScale`, `IndividualEntity`, `PrayerPoint`, `TopicWithPoints`, `AppConfig`).
- **[`models/PreloadedContent.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/models/PreloadedContent.kt)**: Seed content comprising The Lord's Prayer, classic 1662 BCP collects, and the Apostles' Creed.

#### Network Layer (`au.prayer.app.network`)
- **[`network/PrayerApiClient.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/network/PrayerApiClient.kt)**: OkHttp client connecting to the Cloudflare Worker proxy (`POST /api/v1/guide`, `POST /api/v1/title`). Handles JSON serialization, security header injection, and offline fallback title generation.

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
    Sanct-->>Believer: Display "Praying for [Name]" + Points (0 buttons, 0 cards)
    
    Believer->>Sanct: Swipe Left (Next Topic)
    Sanct->>Main: onNextTopic()
    Main->>Repo: recordTopicInteraction(topic[1].id)
    Main->>Sanct: Animate horizontal page-turn (FastOutSlowInEasing)
    Sanct-->>Believer: Display Topic 2
    
    Believer->>Sanct: Swipe Down (Exit)
    Sanct->>Main: onExit()
    Main->>Home: Pop LIFO stack, restore Home
```

### 6.2 Lined Notepad Entry & Asynchronous Auto-Titling

```mermaid
sequenceDiagram
    autonumber
    actor Believer as Believer (User)
    participant Log as LogPrayerScreen (Lined Notepad)
    participant Main as MainActivity
    participant Repo as PrayerRepository
    participant API as PrayerApiClient
    participant Worker as Cloudflare Worker (/api/v1/title)

    Believer->>Log: Writes bullet points on ruled notepad
    Believer->>Log: Taps "Save to [Name]"
    Log->>Main: onSavePrayerPoint(entityId, body, initialTitle=null)
    Main->>API: generateOfflineFallbackTitle(body)
    API-->>Main: Return first 4-5 words fallback
    Main->>Repo: savePrayerPoint(entityId, fallbackTitle, body)
    Repo-->>Main: Saved PrayerPoint (ID: abc-123)
    Main-->>Believer: Show subtle snackbar ("Saved prayer point")
    
    Note over Main,Worker: Asynchronous Background Auto-Titling
    Main->>API: generateTitle(body)
    API->>Worker: POST /api/v1/title {"text": body}
    Worker-->>API: 200 OK {"title": "Patience & Wisdom"}
    API-->>Main: Result.success("Patience & Wisdom")
    Main->>Repo: updatePrayerPointTitle("abc-123", "Patience & Wisdom")
    Note over Repo: Title updated silently without interrupting user
```

### 6.3 Prayer Assistant Guided Distillation Flow

```mermaid
sequenceDiagram
    autonumber
    actor Believer as Believer (User)
    participant Log as LogPrayerScreen
    participant API as PrayerApiClient
    participant Worker as Cloudflare Worker (/api/v1/guide)
    participant LLM as OpenRouter (LLM)

    Believer->>Log: Taps "Prayer Assistant"
    Log-->>Believer: Opens modal ("What is on your heart?")
    Believer->>Log: Inputs raw text ("Worrying about my job restructuring")
    Log->>API: getGuidance(initial_reflection, root="PEOPLE", group=null)
    API->>Worker: POST /api/v1/guide (JSON payload)
    Worker->>LLM: Distillation prompt with theological guardrails
    LLM-->>Worker: JSON {"skip_question": false, "clarifying_question": "What is causing this concern?", "candidate_prayer_points": []}
    Worker-->>API: 200 OK GuideResponse
    API-->>Log: GuideResponse
    Log-->>Believer: Displays single clarifying inquiry (6-12 words)
    
    Believer->>Log: Enters response or taps "Skip to prayer points"
    Log->>API: getGuidance(..., user_response="...")
    API->>Worker: POST /api/v1/guide (Turn 2)
    Worker->>LLM: Formulate candidate cards
    LLM-->>Worker: JSON {"skip_question": true, "candidate_prayer_points": [Point1, Point2]}
    Worker-->>API: 200 OK GuideResponse
    API-->>Log: GuideResponse
    Log-->>Believer: Displays strictly 2 candidate cards (2-6 word titles, semicolon syntax)
    
    Believer->>Log: Taps "Save to [Name]" on selected card
    Log->>Main: Commits prayer point directly to database
```

---

## 7. Quality & Verification Contract Mapping

The repository maintains two independent test suites guaranteeing identical behavior across specifications and native production code:

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
        C_GEO["0dp Planar Geometry & 0px Gutters"]
        C_GES["Touch & Edge-Swipe Gestures"]
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
| **0dp Planar Geometry** | `layout_geometry.spec.js` | [`LayoutGeometryTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/LayoutGeometryTest.kt) | Every tile has `border-radius: 0dp`, 0px gutters, directly abutting 1px seams. |
| **Buttonless Sanctuary** | `layout_geometry.spec.js` | [`LayoutGeometryTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/LayoutGeometryTest.kt) | Zero buttons, zero cards, zero borders in Sanctuary prayer mode. |
| **Swipe Gestures** | `gesture_engine.spec.js` | [`GestureEngineTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/GestureEngineTest.kt) | Horizontal swipe advances/returns; vertical swipe exits; left edge-swipe right pops back stack. |
| **Anti-Neglect Queue** | `anti_neglect_queue.spec.js` | [`AntiNeglectQueueTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/AntiNeglectQueueTest.kt) | Topics ordered by oldest `last_interacted_at` and lowest `interacted_count`. |
| **Lexicon Compliance** | `lexicon_contract.spec.js` | [`LexiconContractTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/LexiconContractTest.kt) | Zero occurrences of *target*, *entity*, *commit*, *ticket*, *Point 1*, *Point 2*. |
| **Devotional Flows** | `devotional_flows.spec.js` | [`DevotionalFlowsTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/DevotionalFlowsTest.kt) | End-to-end coverage: blank start, note capture, journal management, topic progression. |
| **LIFO Back Stack** | N/A (State Machine) | [`LifoBackStackTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/LifoBackStackTest.kt) | LIFO push/pop mechanics, root preservation, empty-stack safety. |
| **Data Serialization** | N/A | [`DataModelsTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/DataModelsTest.kt) | Wire JSON serialization/deserialization for entities, points, configs, and payloads. |
| **API Client & Networking** | N/A | [`PrayerApiClientTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/PrayerApiClientTest.kt) | OkHttp serialization, timeout enforcement, fallback title generation. |
| **Theological Guardrails** | N/A (Tested in `api/test/`) | [`TheologicalGuardrailsTest.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/test/java/au/prayer/app/TheologicalGuardrailsTest.kt) | Verifies Solus Christus, rejection of prosperity/saints, and absence of direct AI prayer text. |

---

## 8. Complete File & Dependency Mapping Matrix

| Relative Path | Architectural Layer | Primary Responsibility | Direct Upstream Dependencies | Primary Downstream Consumers |
| :--- | :--- | :--- | :--- | :--- |
| **[`AGENTS.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/AGENTS.md)** | Root Governance | Workspace protocol, reading prerequisites, synchronization rules | Universal | All Agents & Developers |
| **[`planning/beliefs.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/beliefs.md)** | Specification / Theology | Doctrinal foundation, confessional standards, prayer theology | Historic Formularies | `BRD.md`, `technical.md`, `UX.md`, `PROMPT_*.txt` |
| **[`planning/BRD.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/BRD.md)** | Specification / Product | Business requirements, MVP scope, core features | `beliefs.md` | `technical.md`, `UX.md`, Android App |
| **[`planning/technical.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/technical.md)** | Specification / Tech | System architecture, SQLCipher schema, proxy specs | `beliefs.md`, `BRD.md` | `worker.js`, `PrayerRepository.kt`, `PrayerApiClient.kt` |
| **[`planning/UX.md`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/UX.md)** | Specification / Design | Planar 0dp aesthetics, gestures, lexicon, motion tokens | `beliefs.md`, `BRD.md` | `Theme.kt`, `Spacing.kt`, `TouchGestureModifier.kt` |
| **[`planning/test_runner.html`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning/test_runner.html)** | Specification Harness | Interactive spec prototype and contract verification DOM | `UX.md`, `technical.md` | `planning/tests/*.spec.js` |
| **[`api/worker.js`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/worker.js)** | AI API / Edge Proxy | Multi-route Cloudflare Worker router & inference proxy | `system_prompt.txt`, OpenRouter API | `PrayerApiClient.kt`, `interactive_guide.ps1` |
| **[`api/wrangler.jsonc`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/wrangler.jsonc)** | AI API / Deployment | Cloudflare Workers deployment configuration | Cloudflare CLI | Cloudflare Edge Runtime |
| **[`api/system_prompt.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/system_prompt.txt)** | AI API / Prompt | Authoritative compiled system prompt | `api/prompts/*.txt` | `worker.js`, `run_stress_test.py` |
| **[`api/prompts/PROMPT_*.txt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/prompts)** | AI API / Modular Prompts | Discrete prompt modules complying with 5.1kB limit | `beliefs.md`, `BRD.md` | `system_prompt.txt`, `worker.js` |
| **[`api/test/prayer_requests_stress_test.json`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api/test/prayer_requests_stress_test.json)** | AI API / Benchmark | 100-request benchmark suite for model compliance | `BRD.md`, `beliefs.md` | `run_stress_test.py`, `generate_report.py` |
| **[`android/app/src/main/java/au/prayer/app/MainActivity.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/MainActivity.kt)** | Android / Activity | Root activity, lifecycle, LIFO navigation, auto-titling | `PrayerRepository`, `LifoBackStack`, `Theme` | Android OS |
| **[`android/app/src/main/java/au/prayer/app/ui/screens/HomeScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/HomeScreen.kt)** | Android / Screen | Tripartite home launch screen | `Theme.kt`, `Spacing.kt` | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/SanctuaryPrayerScreen.kt)** | Android / Screen | Buttonless full-screen prayer mode | `TouchGestureModifier.kt`, `Theme.kt` | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/screens/LogPrayerScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/LogPrayerScreen.kt)** | Android / Screen | Ruled lined notepad & Prayer Assistant modal | `PrayerApiClient.kt`, `Theme.kt` | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/screens/JournalScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/JournalScreen.kt)** | Android / Screen | Journal directory, prayer point editing, settings | `Theme.kt`, `Models.kt` | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/gestures/TouchGestureModifier.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/gestures/TouchGestureModifier.kt)** | Android / Gestures | High-precision swipe and edge-swipe detection | Android Compose Pointer API | `SanctuaryPrayerScreen.kt`, `JournalScreen.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/navigation/LifoBackStack.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/navigation/LifoBackStack.kt)** | Android / Navigation | LIFO screen state manager | Kotlin Collections | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/ui/theme/Theme.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/theme/Theme.kt)** | Android / Theming | `FlatSquareShape` (0dp), monochrome color schemes | Material 3 Compose | All Screens |
| **[`android/app/src/main/java/au/prayer/app/ui/theme/Spacing.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/theme/Spacing.kt)** | Android / Theming | `PrayerSpacing` 8dp grid spacing tokens | Compose Dp | All Screens |
| **[`android/app/src/main/java/au/prayer/app/ui/theme/Typography.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/theme/Typography.kt)** | Android / Theming | Multi-scale typographic hierarchies | Compose Typography | All Screens |
| **[`android/app/src/main/java/au/prayer/app/data/local/PrayerDatabaseHelper.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/local/PrayerDatabaseHelper.kt)** | Android / Database | SQLite schema definition and table creation | Android SQLite | `PrayerRepository.kt` |
| **[`android/app/src/main/java/au/prayer/app/data/local/PrayerRepository.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/local/PrayerRepository.kt)** | Android / Repository | CRUD operations, Anti-Neglect queue query | `PrayerDatabaseHelper.kt`, `Models.kt` | `MainActivity.kt` |
| **[`android/app/src/main/java/au/prayer/app/data/models/Models.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/models/Models.kt)** | Android / Domain Models | Enums and data classes | Kotlinx Serialization | Repository, API Client, UI |
| **[`android/app/src/main/java/au/prayer/app/data/models/PreloadedContent.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/models/PreloadedContent.kt)** | Android / Seed Data | Historic Reformed collects, creeds, Lord's prayer | `Models.kt` | `PrayerDatabaseHelper.kt`, `PrayerRepository.kt` |
| **[`android/app/src/main/java/au/prayer/app/network/PrayerApiClient.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/network/PrayerApiClient.kt)** | Android / Network | OkHttp client, wire payloads, offline fallback | OkHttp, Kotlinx Serialization | `MainActivity.kt`, `LogPrayerScreen.kt` |
