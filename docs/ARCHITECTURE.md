# Architecture Analysis

## Current baseline

Omni Studio currently inherits a mature Android video-editing codebase rather than starting from an empty shell. The build targets Android API 37 with a minimum of API 26 and uses Kotlin, Jetpack Compose, Hilt/KSP, Room, WorkManager and AndroidX Media3. The project also contains native/ML-heavy paths including FFmpegKitNext, ONNX Runtime, MediaPipe and DeepFilterNet.

The existing build already contains unusually strong release engineering: ABI splits, release signing identity checks, runtime open-source notice generation, native dependency provenance/SBOM generation, advisory floors, lint baselines, screenshot/accessibility verification hooks, baseline profiles and artifact verification scripts.

## Identity boundary

The public product is now **Omni Studio**. The inherited Android namespace/application ID remains `com.novacut.editor` during the foundation phase. Changing it immediately would create a new Android application identity and can break upgrade access to app-private projects. Treat package migration as a separate, explicit migration project.

## Agent-native target architecture

```text
Omni Dev / trusted Omni client
          |
          | OmniLink 3 (same-signer Binder)
          v
OmniStudioExtensionService
          |
          v
Capability adapters
   |        |        |
 Project  Timeline  Analysis
   |        |        |
   +---- Domain layer-+
             |
      Command/transaction engine
             |
       Existing editor engine
             |
    Media3 / FFmpeg / GPU / ML
```

The OmniLink service must remain thin. It authenticates, authorizes, validates and translates protocol requests into domain operations. Editing logic belongs in domain services that are also callable by the UI and tests.

## Capability design

Use semantic, bounded capabilities. Prefer `studio.timeline.split` over `studio.execute`; prefer `studio.project.inspect` over database access. Read operations should return compact structured snapshots. Mutations should support project revision preconditions and dry-run/preview where practical.

Large binary media must not be serialized into Binder JSON. Use stable references and content-URI/FD or OmniLink large-payload/file-transfer mechanisms when they are implemented end to end.

## Trust model

Omni Studio consumes the official first-party `omni-link-sdk:v3.0.0`. It requests both OmniLink signature permissions (`BIND_EXTENSION` and `BIND_AGENT`), exposes its extension behind `BIND_EXTENSION`, and declares package-visibility queries for extension and Agent Gateway discovery. OmniLink's default same-signer validator remains in force. Same signer establishes identity, not unlimited authority; the app-level access controller still gates every capability.

The foundation exposes only `studio.health`. Editing capabilities are deliberately deferred until adapters, transaction semantics and tests exist.

## Recommended internal boundaries

- **UI:** Compose screens and editor interactions.
- **Project domain:** project lifecycle, revisions, persistence and portability.
- **Timeline domain:** tracks, clips, effects, keyframes and command history.
- **Media engine:** decode, preview, transform, compose and export.
- **Intelligence:** transcription, vision/audio analysis and caches.
- **Agent bridge:** OmniLink descriptors, validation, DTO mapping and audit.
- **Jobs:** durable long-running export/analysis tasks.
- **Security:** URI validation, grants, confirmation and audit policy.

## Technical debt observed during foundation review

- ClearCut/NovaCut naming remains in package names, comments, signing environment variables and distribution documentation.
- Release signing policy is tied to the inherited application's certificate lineage.
- The repository has a large build/release surface; CI should start with a dependable debug gate before attempting release signing in hosted runners.
- Agent-facing timeline contracts do not yet exist.
- The runtime notice generator assumes resolved dependencies are open-source; first-party source-available OmniLink is therefore explicitly excluded from the open-source notice inventory and must be documented separately.
- OmniLinkSDK `v3.0.0` is the official integration baseline. Its release tag resolves to the reviewed 3.0.0 source commit; Gradle dependency verification must pin the JitPack artifacts used by CI.

## Definition of done for agent editing

A capability is not considered implemented merely because it appears in a manifest. It is done only when:

1. the UI and agent call the same domain operation;
2. inputs have a schema and bounded size;
3. trust/risk/data scopes are accurate;
4. project revision conflicts are detected;
5. mutation is undoable or explicitly irreversible;
6. destructive work has preview/confirmation where required;
7. process death/retry behavior is defined;
8. audit events are emitted;
9. unit/contract tests cover success and denial paths;
10. documentation matches runtime behavior.

## Workspace organization

### Editor

The creator bar keeps a 56dp height and 48dp action targets. Project name and save
status remain together, with export as the primary action. Preview tools now use
fullscreen plus one overflow menu instead of a vertical stack over the footage.
Scopes, composition guides and comparison retain their original callbacks.

The bottom category rail stays 64dp. Text, edit and advanced workflows open a
scrollable adaptive grid in a modal workbench. Opening it does not resize the
preview or timeline. Selecting an action hides the workbench before opening the
next panel; repeated taps are gated while dismissal runs. Text-overlay edit and
delete actions use the same path. No existing action ID is removed.

Shared panels have a smaller fixed title/close header, with scrolling applied to
the body when requested. Nested cards use calm flat surfaces rather than stacked
accent gradients. Timeline buttons use a tighter rhythm and neutral backgrounds;
split, deletion and active states retain emphasis.

### Settings

All 13 existing sections remain available in four destinations:

| Destination | Sections |
|---|---|
| Editor | Timeline, appearance, editor behavior |
| Export | Export defaults, export notifications |
| Storage | Local AI models, project storage |
| App | Tutorial, diagnostics, privacy, licenses, updates, about |

Only the current destination is composed. Each has its own scroll container; the
chosen destination is saveable. Feedback and confirmation flows keep their
original view-model callbacks. The old repeated overview metrics are removed.
Section headings retain their normal casing and related rows sit in one card.
New destination labels are translated in both shipped locales (English/Spanish).

### Projects and motion

The project home uses a smaller heading and shorter visual hierarchy. Sort options
live in one menu, leaving the existing collection filters separate. Shared chrome
uses Omni lavender for selection and a clear on-accent label color.

Motion tokens are 100ms for feedback, 160ms for small changes, 220ms for panels and
300ms for larger reveals. Primary feedback springs are damped without overshoot.
Navigation combines a short fade with a small directional offset, mirrored in RTL.
Compose's system duration scale remains in force; there is no custom frame loop,
blur layer or forced animation scale in this change.

### Verification

- Existing action IDs and settings callback inventory were compared before/after.
- Both resource XML files parse without duplicate keys.
- Existing smoke navigation selects the App settings destination.
- Added an instrumented regression for switching destinations and control reachability.
- CI compiles the UI regression tests alongside unit tests, release lint and release builds.

An emulator and Android SDK are unavailable in the editing environment. Local
Gradle compilation was blocked at distribution download by network access. Device
screenshots, real scrolling behavior and frame timing require the device QA lane;
this change does not claim measured FPS or completed visual verification.

### Creating projects and delivering exports

Project creation starts with a name and canvas ratio. An empty name falls back to
Untitled; the chosen name and canvas flow into the existing project creation
operation. Built-in and saved templates have separate destinations. The template
library uses one adaptive lazy grid, with canvas-ratio filtering and schematic
canvas previews. Saved template sharing, import and confirmed deletion remain
available. Destination changes retain the draft project name and canvas.

Export keeps its header and one primary action outside the scrolling body in
both phone sheets and embedded desktop panes. Setup contains presets and explicit
resolution, frame-rate, codec, quality and audio choices. Options contains range,
alternate output types, watermark, encoder/privacy controls, target size, naming
and timeline exchange. Review contains output capability/provenance reports and
recent exports. The range validity gate and all export state actions remain in
force. A manual specification clears the selected platform preset so its label
does not contradict the output. Preset controls wrap on narrow screens.

JVM UI regression covers creating a named portrait project after browsing both
libraries and checks that every export destination has exactly one visible export
action and a visible close action. Device gesture and frame-time validation still
require the device QA lane.
