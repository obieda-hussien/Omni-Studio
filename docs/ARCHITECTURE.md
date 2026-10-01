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

Omni Studio consumes the first-party `omni-link-sdk`. The exported service is protected by `com.omnilink.sdk.permission.BIND_EXTENSION`, and OmniLink's default same-signer validator remains in force. Same signer establishes identity, not unlimited authority; the app-level access controller still gates every capability.

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
- OmniLink 3 publication should be treated as an external release dependency. Until the `v3.0.0` artifact is verified, pinning a known v3 source commit is safer for CI.

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
