# Omni Studio

**Agent-Native Creative Suite for Android**

Omni Studio is an Android-first creative editing project built around a simple idea: a human and a trusted AI agent should be able to understand and edit the same non-destructive project through explicit, inspectable and reversible operations.

The project currently starts from the ClearCut video-editor codebase and is being evolved into an Omni ecosystem application. It is **not yet a finished agent editor**; the current milestone establishes the product, build and OmniLink foundations without pretending roadmap features already exist.

## Why Omni Studio?

Traditional editors expose pixels and UI controls. Omni Studio is moving toward a semantic editing surface where a trusted agent can ask for project state, reason over a compact timeline representation, propose an edit transaction, preview its effects and commit only the approved operation.

```text
User intent
   |
Omni Dev
   |
OmniLink 3
   |
Omni Studio capability layer
   |
Project / Timeline / Media / Intelligence
   |
Command + transaction engine
   |
Media3 / FFmpeg / GPU / on-device ML
```

## Foundation status

- Android API 26+; compile/target API 37.
- Kotlin + Jetpack Compose.
- Media3 playback/transformation and muxing.
- FFmpegKitNext fallback/native processing paths.
- Room 3 project persistence.
- Hilt/KSP dependency injection.
- WorkManager background work.
- ONNX Runtime, MediaPipe and DeepFilterNet integrations inherited from the editor baseline.
- ABI-specific APK support plus a universal APK.
- Baseline-profile and visual/accessibility verification infrastructure.
- Supply-chain checks, SBOM generation and release-artifact verification.
- **OmniLink 3 first-party integration foundation.**
- Protected OmniLink ExtensionService with a minimal `studio.health` capability.
- Android CI for tests, lint and debug assembly.

## OmniLink 3

Omni Studio is a first-party Omni capability provider. Same-device privileged integration uses the OmniLink Android SDK and a signature-protected Binder service.

The foundation intentionally starts with one read-only capability:

| Capability | Mode | Risk | Purpose |
|---|---|---:|---|
| `studio.health` | IMMEDIATE | LOW | Report Omni Studio / OmniLink protocol integration status |

Future timeline and project capabilities will be added only when their domain adapters, transaction semantics, authorization and tests are implemented. The design explicitly avoids generic `executeAnything`, raw database and arbitrary shell surfaces.

> **Signing matters:** OmniLink first-party Binder trust expects official Omni apps to share the intended signing identity. A dependency alone does not grant trust.

## Dependency policy

OmniLink 3 is source-available first-party software and is not part of Omni Studio's generated **open-source** runtime notice inventory. Its own license and notices remain authoritative. The integration should use a verified immutable OmniLink 3 release; while release publication is being verified, CI may pin an immutable v3 source commit.

## Repository identity

The product/repository name is **Omni Studio**.

The inherited Android `applicationId` and namespace are currently `com.novacut.editor`. They are deliberately not renamed during foundation work because Android package/signing identity affects upgrade continuity and access to app-private projects. A future package migration must include an explicit export/import and distribution plan.

## Build

Requirements:

- JDK 17
- Android SDK matching compile SDK 37
- Python for repository verification scripts

Basic development gate:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
```

Hosted CI intentionally does not build a signed release. Release signing remains a separate local/release process because the inherited project enforces a pinned certificate identity.

## Architecture

Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the current-system analysis, target agent-native architecture, trust boundaries and technical debt.

## Roadmap

Read [docs/ROADMAP.md](docs/ROADMAP.md) for the staged plan from read-only project intelligence through transactional editing, local media understanding, semantic editing, creative skills, the Omni Creative Graph and cross-device workflows.

High-level direction:

```text
Foundation
   -> Read-only project intelligence
   -> Transactional timeline API
   -> Media intelligence
   -> Semantic editing
   -> Creative skill packs
   -> Omni Creative Graph
   -> Video + Audio + Image + Captions + Assets
   -> Cross-device creative runtime
```

## Security model

- Signature-protected first-party Binder endpoint.
- OmniLink same-signer validation remains fail-closed.
- Capabilities are explicit and narrowly scoped.
- Same signer proves identity; it does not imply unlimited authority.
- Destructive edits will require transactional preview/commit semantics.
- Large media must not be embedded in Binder JSON.
- Imported captions, metadata, project files and model-visible content are untrusted data, never agent authority.
- Secrets and private keys stay outside agent-visible capability payloads.

## Performance direction

Omni Studio is intended to remain useful on constrained Android hardware. The roadmap includes adaptive proxy resolution, bounded ML concurrency, content-addressed analysis caches, hardware codec preference, sparse contact-sheet/frame sampling for agents and device-tier-aware model selection. Final exports should continue to use original media rather than proxy quality.

## Upstream and licensing

Omni Studio is derived from the ClearCut codebase. Preserve all applicable upstream copyright, license and third-party notices. Native FFmpeg and other redistributed dependencies have additional notice/source-offer requirements enforced by the existing repository verification tooling.

See `LICENSE`, the in-app third-party notices, and the files under `third_party/` for authoritative terms.

## Project status

**Foundation / experimental.** Existing editor functionality is substantial, but the Omni agent-control surface is intentionally minimal until the semantic domain boundary is hardened.

---

**Omni Studio — create manually, automate semantically.**
