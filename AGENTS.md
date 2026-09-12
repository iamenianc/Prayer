# Project Rules & Agent Guidelines

## 1. Project Purpose & Scope

This repository maintains three distinct domains:

1. **Planning, Conceptualization & Product Specification (`planning/`)**:
   - The architectural foundation and product specification (*Pray Without Ceasing*).
   - **Strategic Focus**: Product requirements, domain modeling, user workflows, system architecture, feature roadmaps, and scenario analysis.
   - **Liturgical Integrity & Design Contracts**: Maintains `beliefs.md`, `BRD.md`, `technical.md`, `UX.md`, and `codebase_knowledge_graph.md` as living doctrinal and system contracts. Canonical journal design principles are specified in `android_journal_design_principles.md` (to be added; strictly immutable and read-only for agents).

2. **AI Agent API Production Code (`api/`)**:
   - The serverless proxy and inference engine supporting the companion app is **active production code**.
   - Located under [`api/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/api): Cloudflare Worker proxy (`worker.js`), deployment configuration (`wrangler.jsonc`), package manifests, and authoritative compiled system prompt (`system_prompt.txt`).
   - The agent **is permitted and expected** to maintain, update, and deploy the API production codebase.
   - **CRITICAL RESTRICTION**: **Do not run any AI API tests.** All AI API test suites, benchmarks, and test clients have been removed from the repository. The agent must never execute automated requests, benchmarks, or test scripts against the AI API/OpenRouter proxy.

3. **Native Android Production Codebase (`android/`)**:
   - The native mobile client implementation targeting Samsung Galaxy Flip (and standard Android) devices is **active production code**.
   - Located under `android/`: Kotlin + Jetpack Compose application, offline encrypted database (Room with SQLCipher), Samsung Flip gesture engine, Cloudflare Worker API bridge, and replicated test suite.
   - The agent **is permitted and expected** to maintain, update, test, compile, and export sideloadable APK packages to `G:\My Drive\myApps\Prayer.apk`.

---

## 1.5. Mandatory Deploy & Git Commit Protocol (Android Client, Cloudflare Worker API & Version Control)

**Whenever any production or workspace work is complete** — whether Android client modifications, Cloudflare Worker API changes, planning specifications, or general repository updates — the agent **MUST proactively execute the required deployment procedures AND commit all changes to Git before declaring the task finished.**

### 1.5.1 Android Client Deployment (`deployToDrive`)
- **Trigger**: Any changes to Kotlin source code, XML resources, assets, manifests, or build scripts under `android/`.
- **Procedure**:
  1. From the `android/` directory, run the bundled Gradle task:
     ```powershell
     .\gradlew.bat :app:deployToDrive
     ```
     This task compiles the release APK (`assembleRelease`) and copies it to `G:\My Drive\myApps\Prayer.apk` (overwrite).
  2. The destination `G:\My Drive\myApps` folder **must exist** before running. If it does not, halt and ask the user to mount/start Google Drive for Desktop rather than inventing an alternate path.
  3. After the build, **verify** the deployed file with `Get-Item -LiteralPath "G:\My Drive\myApps\Prayer.apk"` and confirm a non-zero `Length` and a fresh `LastWriteTime`.
  4. **Do not** deploy if the release build fails to compile. Fix all compile errors first, then re-run the deploy task.

### 1.5.2 Cloudflare Worker API Deployment (Wrangler Script)
- **Trigger**: Any changes to `api/worker.js`, `api/system_prompt.txt`, prompt definitions, dependencies (`package.json`), or configuration (`wrangler.jsonc`) under `api/`, or when explicitly requested / needed to synchronize the edge API.
- **Procedure**:
  1. From the `api/` directory, verify syntax first:
     ```powershell
     node --check worker.js
     ```
  2. Run the Wrangler deployment script:
     ```powershell
     npm run deploy
     ```
     *(alternatively: `npx wrangler deploy`)*
  3. Confirm that the deployment succeeded, verify the active endpoint (`https://pray-proxy.reflex-game.workers.dev`), and report the deployment status and Version ID in the closing summary.

### 1.5.3 Mandatory Post-Deployment Git Commit Protocol
- **Trigger**: Any changes (modified, added, or deleted files) in the Git working tree upon completing development, testing, and deployment.
- **Procedure**:
  1. Inspect the state of the repository using:
     ```powershell
     git status
     ```
  2. Stage all relevant modifications, deletions, and new files:
     ```powershell
     git add -A
     ```
     *(Ensure no temporary test dumps or sensitive environment files outside `.gitignore` are accidentally included).*
  3. Commit the staged changes with a descriptive, conventional commit message:
     ```powershell
     git commit -m "<type>(<scope>): <concise description of changes>"
     ```
     *(e.g., `git commit -m "feat(android): add library reader and stationery textures"`, `git commit -m "docs: update AGENTS.md post-deployment commit protocol"`)*
  4. Verify the commit was recorded cleanly with `git status` and `git log -n 1 --oneline`.
  5. Include the commit hash and message in your turn closing summary.

### When to Skip
- Skip Android deployment (`deployToDrive`) if no Kotlin source, XML resources, or files under `android/` were modified.
- Skip Wrangler deployment (`npm run deploy`) if no files under `api/` were modified and the edge API is already up to date.
- Skip Git commit only if `git status` reveals no modified or untracked files (clean tree), or if the user explicitly instructs not to commit.
- Skip when the user explicitly instructs a build-only or no-deploy run for this turn.

### Mandatory End-of-Turn Checklist
Before concluding any turn:
1. **Android changes present?** $\rightarrow$ Execute `.\gradlew.bat :app:deployToDrive`, verify `Prayer.apk` on Drive, and report size/timestamp.
2. **API changes present / edge sync needed?** $\rightarrow$ Execute `npm run deploy` (or `npx wrangler deploy`) in `api/`, verify live endpoint, and report Version ID.
3. **Uncommitted changes present in Git?** $\rightarrow$ Execute `git add -A` and `git commit -m "<descriptive message>"`, verify clean working tree, and report commit hash and message.
4. Report the completion status of all relevant deployment actions and the Git commit in the final response.

---

## 1.6. Multi-Session Concurrency & Peer Session Coordination Protocol

When multiple agent sessions run concurrently within the workspace (e.g. parallel prompts or subagent workflows), agents **MUST adhere to strict anti-collision and serialization protocols** to prevent working tree corruption, compiler daemon crashes, and deployment race conditions.

### 1.6.1 Pre-Flight Concurrency & Process Awareness
- Before launching Gradle builds, heavy file refactors, or deployments, check if other background tasks or Gradle daemons are active (`manage_task list` or PowerShell `Get-Process | Where-Object { $_.ProcessName -match "java|gradle" }`).
- Treat the repository as a shared concurrent environment: never assume your session is the sole actor in the workspace.

### 1.6.2 Strict Working Copy & Shared File Rules
- **Fresh Context Verification**: Always re-read target line ranges (`view_file`) immediately prior to making edits to shared files (e.g., [`JournalScreen.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/ui/screens/JournalScreen.kt), [`MainActivity.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/MainActivity.kt), [`PrayerRepository.kt`](file:///c:/Users/ianch/sourcecode/repos/Prayer/android/app/src/main/java/au/prayer/app/data/local/PrayerRepository.kt)) to ensure you do not overwrite changes committed by a concurrent session.
- **Unambiguous Scope Matching**: In `replace_file_content`, use sufficiently unique target anchors. Never replace generic closing brace blocks (`}\n}\n}`) that risk truncating outer function or composable scopes.
- **Do Not Revert Peer Edits**: If you encounter unexpected code added by another session, do not revert or discard it unless explicitly instructed. Re-read and integrate cleanly around it.

### 1.6.3 Strict Gradle Serialization & Daemon Protection
- **No Concurrent Gradle Executions**: Never launch `.\gradlew.bat` (test, build, or deploy) while another session is actively running a Gradle task. Wait for the running Gradle task to terminate before starting your own.
- **ABSOLUTE BAN on `.\gradlew.bat --stop` & `clean` during Multi-Session Work**:
  - **NEVER** run `.\gradlew.bat --stop` or `.\gradlew.bat clean` while other sessions or background tasks are active.
  - Abruptly stopping daemons terminates in-flight Kotlin compiler workers in peer sessions, breaks daemon socket connections (`SocketException: Connection reset`), and corrupts shared compilation output.
- **Isolated Compiler Mode**: When running test tasks while another session may be active, use isolated execution:
  ```powershell
  .\gradlew.bat testDebugUnitTest --no-daemon
  ```

### 1.6.4 Strict Multi-Session Deployment & Commit Invariant (Single-Deployer Law)
- **No Concurrent Deployments**: Never run `.\gradlew.bat :app:deployToDrive` while another session is building or deploying. Concurrent release packaging corrupts `build/` artifacts and causes Windows `SharingViolationException` write-lock failures on `G:\My Drive\myApps\Prayer.apk`.
- **Consolidated Deployment**: If a peer session has already built and deployed a release APK that includes your changes, verify `Get-Item -LiteralPath "G:\My Drive\myApps\Prayer.apk"` and avoid launching a redundant, competing deployment build.
- **Broken Tree Invariant**: Never deploy if any file in the working tree has syntax or compilation errors introduced by an in-flight peer session. Resolve or await clean compilation before deploying.
- **Atomic Post-Deployment Commits**: When committing after deployment in a multi-session context, ensure peer sessions are not actively writing partial edits or running in-flight builds before staging and committing.

### 1.6.5 Peer Messaging & Orchestration
- Use `send_message` to communicate status, coordinate build locks, and alert peer sessions when major files are updated or builds are in flight.

---

## 2. Prerequisite: Context Awareness & Markdown Reading Protocol

**The agent MUST read the core workspace markdown files (`AGENTS.md`, `planning/beliefs.md`, `planning/BRD.md`, `planning/technical.md`, `planning/UX.md`, `planning/codebase_knowledge_graph.md`, and `android_journal_design_principles.md` once present) at the start of a session, and whenever it needs a refresher during a long or context-heavy session.**

- **Session Start & Refreshers**: Read the core files upon commencing a session to establish full context, or during extended conversations when context has drifted, truncated, or when explicit verification is needed before major architectural updates.
- **Avoid Redundant Re-reading**: Do **not** mechanically re-read all context files for every consecutive queued prompt or rapid iterative message when the active context already contains the current document states.
- **Context Continuity**: Ensure decisions remain faithful to established domain rules, theological foundations, scope boundaries, technical architectures, and user journeys.
- **Knowledge Graph as Living Map**: Treat `planning/codebase_knowledge_graph.md` as the authoritative cross-layer architecture map. Read it alongside the other core files to understand how `api/`, `android/`, and `planning/` connect, and update it whenever code structure, module relationships, or the architecture changes (see §6).
- **Android Journal Design Principles Protocol (`android_journal_design_principles.md`)**:
  - The document `android_journal_design_principles.md` (to be added to the repository) defines canonical design principles for the Android journal experience.
  - **MANDATORY READING**: Whenever this file is present in the workspace (whether in the root directory or under `planning/`), the agent **MUST READ** it before making any Android journal or UI/UX architectural decisions or code changes.
  - **ABSOLUTE EDIT BAN**: The agent **MUST NEVER EVER EDIT, MODIFY, OVERWRITE, TRUNCATE, OR DELETE `android_journal_design_principles.md` UNDER ANY CIRCUMSTANCES**. It is strictly a read-only, immutable canonical contract for agents. All edits to this document are reserved exclusively for the human user.

---

## 3. Agent Behavior & Persona

- **Role**: Strategic Systems Architect & Product Planner.
- **Clarity & Precision**: Present structured, unambiguous specifications. Avoid fluff and overly verbose explanations.
- **Proactive Clarification**: Identify assumptions, missing requirements, dependencies, and potential risks early. Ask targeted questions when requirements are underspecified.
- **Constructive Challenge**: Identify edge cases, trade-offs, scalability considerations, and user experience bottlenecks before plans are locked in.

---

## 4. Planning & Documentation Standards

All planning artifacts and specifications must adhere to the following conventions:

### Document Structure
1. **Objective / Problem Statement**: Clear definition of what needs to be solved and why.
2. **User Personas & Use Cases**: Who the feature or system serves and their specific workflows.
3. **Functional & Non-Functional Requirements**: Measurable criteria for success, performance, security, and accessibility.
4. **Conceptual Architecture / Domain Model**: Entity relationships, data flows, and state transitions (use Mermaid diagrams where helpful).
5. **Milestones & Phasing**: Logical breakdown into phases or iterations.
6. **Assumptions & Open Questions**: Explicitly catalog any unknowns, trade-offs, or decisions awaiting user input.

### Format & Communication
- Use GitHub Flavored Markdown with clear headings and bulleted lists.
- Leverage Mermaid diagrams (`mermaid`) for visual workflows, state machines, and sequence diagrams.
- Keep specifications modular and easy to navigate.

---

## 5. Core Specification Deliverables: Beliefs, BRD, Technical, and UX Documents

The project maintains five central, living specification documents inside [`planning/`](file:///c:/Users/ianch/sourcecode/repos/Prayer/planning):
1. **`planning/beliefs.md`** (Confessional Foundation & Theological Grounding): Mandatory theological bedrock defining the doctrinal standards, historic Anglican formularies, creeds, and prayer theology governing all app design, AI boundaries, and features.
2. **`planning/BRD.md`** (Business Requirements Document): Single source of truth for business purpose, scope boundaries, domain rules, governance, and conceptual architecture.
3. **`planning/technical.md`** (Technical Reference): Approved technical decisions, data model, security/storage architecture, OpenRouter suggestion engine specs, theological guardrails, and open-ended technical questions.
4. **`planning/UX.md`** (User Experience Specification): Business- and product-owner-facing document defining emotional tone, visual identity, core user journeys, ergonomics, and liturgical reverence.
5. **`planning/codebase_knowledge_graph.md`** (Architecture Map & Cross-Layer Knowledge Graph): Authoritative map of how `api/`, `android/`, and `planning/` connect, including module relationships, data flows, and structural contracts. Must be read for full system context and updated whenever code structure, module relationships, or architecture changes (see §6).

Additionally, the project incorporates a canonical, read-only design principles specification:
6. **`android_journal_design_principles.md`** (Canonical Android Journal Design Principles — Added Later): Authoritative design principles governing the Android journal experience. **STRICTLY READ-ONLY**: Agents must read and adhere to this document, but **must NEVER EVER edit or modify it**.

---

## 6. Mandatory Document Synchronization Rule

**Always update `planning/beliefs.md`, `planning/BRD.md`, `planning/technical.md`, `planning/UX.md`, and `planning/codebase_knowledge_graph.md` whenever decisions are made between the user and the agent.**

### Synchronization Protocol:
- **Simultaneous Updates**: Whenever a scope clarification, theological alignment, feature addition, architectural choice, or constraint is agreed upon during conversation, update **`planning/beliefs.md`**, **`planning/BRD.md`**, **`planning/technical.md`**, **`planning/UX.md`**, and **`planning/codebase_knowledge_graph.md`** promptly.
- **Complementary Scopes**:
  - Update **`planning/beliefs.md`** for confessional commitments, doctrinal boundaries, hermeneutical standards, and theology of prayer.
  - Update **`planning/BRD.md`** for business intent, scope boundaries (MVP vs. future), user policies, and high-level architecture.
  - Update **`planning/technical.md`** for technical system architecture, data models, API schemas, security constraints, theological guardrails in prompts, and open-ended technical decisions.
  - Update **`planning/UX.md`** for user journeys, interaction patterns, design tokens, liturgical reverence, and ergonomic flows.
  - Update **`planning/codebase_knowledge_graph.md`** whenever code structure, module relationships, data flows, file/folder layout, or the architecture of `api/`, `android/`, or `planning/` changes in any way — including new files, renamed modules, deleted components, or revised cross-layer contracts.
- **Active State Principle**: Keep the documents lean and current. Do not retain full audit logs, historical trial decisions, or overruled/redundant decisions. Directly reflect all current decisions in the text.

### STRICT EXCEPTION: Immutable Canonical Documents (DO NOT EDIT)
- **`android_journal_design_principles.md`**:
  - While living specification documents (`beliefs.md`, `BRD.md`, `technical.md`, `UX.md`, `codebase_knowledge_graph.md`) are kept synchronized with project decisions, **`android_journal_design_principles.md` is strictly immutable to agents**.
  - **NEVER EVER EDIT**: Under no circumstances should an agent ever edit, modify, overwrite, append to, or delete `android_journal_design_principles.md` (or any file matching `android_journal_design_principles*`).
  - **READ-ONLY MANDATE**: Agents must read this document when present in the workspace to align all code and designs with its principles, but any modifications to it must come directly from the user.

---

## 7. Repository Directory Structure

```
Prayer/
├── AGENTS.md                          # Root instructions & workspace guidelines
├── .gitignore                         # Environment & tool ignore rules
├── planning/                          # Planning, specification & prototyping vault
│   ├── beliefs.md                     # Theological grounding & confessional standards
│   ├── BRD.md                         # Business requirements document
│   ├── technical.md                   # Technical decisions & architecture reference
│   ├── UX.md                          # UX & product design specification
│   ├── codebase_knowledge_graph.md    # Architecture map & cross-layer knowledge graph
│   ├── android_journal_design_principles.md # Canonical journal design principles (Read-only for agents — NEVER edit; added later)
│   ├── test_runner.html               # In-browser spec & layout contract validator
│   └── tests/                         # Automated Playwright layout & gesture test suite
│       ├── anti_neglect_queue.spec.js # Anti-neglect priority queue tests
│       ├── devotional_flows.spec.js   # End-to-end devotional workflow tests
│       ├── gesture_engine.spec.js     # Touch & swipe gesture interaction tests
│       ├── layout_geometry.spec.js    # 0dp planar geometry & seam tests
│       ├── lexicon_contract.spec.js   # Reverent devotional lexicon contract tests
│       ├── package.json               # Test runner dependencies & scripts
│       ├── playwright.config.js       # Playwright browser runner configuration
│       ├── test_helpers.js            # Shared DOM & assertion test utilities
│       └── test_runner.spec.js        # Root harness verification test
├── api/                               # Production AI Agent API codebase
│   ├── worker.js                      # Cloudflare Worker serverless proxy (multi-route)
│   ├── wrangler.jsonc                 # Cloudflare Worker deployment configuration
│   ├── package.json                   # Worker dependencies & scripts (dev, deploy)
│   └── system_prompt.txt              # Authoritative compiled system prompt
└── android/                           # Production Native Android Client (Kotlin + Jetpack Compose)
    ├── app/                           # Android application module
    │   ├── build.gradle.kts           # Application build script & dependencies
    │   ├── proguard-rules.pro         # ProGuard / R8 code shrinking rules
    │   └── src/
    │       ├── main/
    │       │   ├── AndroidManifest.xml # Android application manifest
    │       │   ├── java/au/prayer/app/ # Application Kotlin source code
    │       │   │   ├── MainActivity.kt # Root activity & LIFO back stack manager
    │       │   │   ├── data/
    │       │   │   │   ├── local/      # SQLite / SQLCipher database & repository
    │       │   │   │   │   ├── PrayerDatabaseHelper.kt
    │       │   │   │   │   └── PrayerRepository.kt
    │       │   │   │   └── models/     # Domain models & seed content
    │       │   │   │       ├── Models.kt
    │       │   │   │       └── PreloadedContent.kt
    │       │   │   ├── network/        # API bridge
    │       │   │   │   └── PrayerApiClient.kt
    │       │   │   └── ui/             # Jetpack Compose UI
    │       │   │       ├── components/ # Custom components (LinedNotepad)
    │       │   │       │   └── LinedNotepad.kt
    │       │   │       ├── gestures/   # Gesture detection engine
    │       │   │       │   └── TouchGestureModifier.kt
    │       │   │       ├── navigation/ # LIFO stack state manager
    │       │   │       │   └── LifoBackStack.kt
    │       │   │       ├── screens/    # Screens (Home, Sanctuary, Log, Journal)
    │       │   │       │   ├── HomeScreen.kt
    │       │   │       │   ├── JournalScreen.kt
    │       │   │       │   ├── LogPrayerScreen.kt
    │       │   │       │   └── SanctuaryPrayerScreen.kt
    │       │   │       └── theme/      # Planar 0dp theme, spacing & typography
    │       │   │           ├── Spacing.kt
    │       │   │           ├── Theme.kt
    │       │   │           └── Typography.kt
    │       │       └── res/                # App drawables, mipmaps, strings, colors
    │           └── raw/            # Standalone structured content documents
    │               └── historic_prayers.json  # 14 public-domain historic prayers (single source of truth)
    │       └── test/java/au/prayer/app/ # Replicated JVM unit test suite (65 tests)
    │           ├── AntiNeglectQueueTest.kt
    │           ├── DataModelsTest.kt
    │           ├── DevotionalFlowsTest.kt
    │           ├── GestureEngineTest.kt
    │           ├── LayoutGeometryTest.kt
    │           ├── LexiconContractTest.kt
    │           ├── LifoBackStackTest.kt
    │           ├── PrayerApiClientTest.kt
    │           └── TheologicalGuardrailsTest.kt
    ├── gradle/                        # Version catalogs and Gradle wrapper
    │   ├── libs.versions.toml         # Version catalog
    │   └── wrapper/
    │       ├── gradle-wrapper.jar
    │       └── gradle-wrapper.properties
    ├── build.gradle.kts               # Root build configuration
    ├── gradle.properties              # JVM & Gradle build properties
    ├── gradlew                        # Gradle wrapper script (Unix)
    ├── gradlew.bat                    # Gradle wrapper batch file (Windows)
    └── settings.gradle.kts            # Project settings
```
