# Omni Studio Roadmap

> **Vision:** build an agent-native creative suite where humans and trusted agents edit the same non-destructive project through explicit, inspectable, reversible capabilities.

This roadmap is intentionally architectural. It does not claim that planned capabilities already exist.

## Product principles

1. **Human-first editing.** Manual editing remains complete and authoritative.
2. **Agent-native, not agent-only.** Omni Dev uses semantic APIs instead of screen automation.
3. **Non-destructive by default.** Source media is immutable; edits are project operations.
4. **Preview before irreversible work.** High-risk actions use prepare/preview/commit.
5. **Least privilege.** OmniLink capabilities are narrow, typed, trust-scoped and auditable.
6. **Local-first intelligence.** Prefer on-device analysis when hardware permits; cloud providers are optional.
7. **Adaptive execution.** Proxy quality, model size and concurrency scale to device capability.
8. **Deterministic projects.** Agent operations are replayable, diffable and recoverable.

## Phase 0 — Foundation

- [x] Establish **Omni Studio** product identity while retaining the legacy Android package identity until a migration decision is made.
- [x] Add the official `v3.0.0` release of OmniLink 3 as the first-party dependency and protect the ExtensionService.
- [x] Expose a minimal `studio.health` capability.
- [x] Add CI for optimized release APK/AAB builds, unit tests and release lint.
- [x] Document architecture, trust boundaries and roadmap.
- [ ] Verify shared Omni debug/release signing strategy before privileged cross-app testing.
- [ ] Decide whether package migration from `com.novacut.editor` is worth breaking the existing install lineage.
- [x] Add foundation contract tests for the official OmniLink coordinate, privileged manifest surfaces, vector launcher identity and release CI.
- [ ] Add runtime Binder contract tests for manifest discovery and same-signer rejection.

## Phase 1 — Read-only project intelligence

Target capabilities:

- `studio.project.get`
- `studio.timeline.inspect`
- `studio.timeline.range.inspect`
- `studio.media.list`
- `studio.media.metadata`
- `studio.export.status`
- `studio.capabilities.describe`

Deliver a compact **Agent Timeline Snapshot** containing tracks, clips, time ranges, transforms, effects, captions, audio metadata and stable object IDs. Large media never travels inline through Binder.

## Phase 2 — Transactional timeline editing

Introduce an internal edit transaction layer:

```text
prepare -> validate -> diff -> preview -> confirm -> commit
                                  \-> rollback
```

Target operations include split, trim, move, ripple delete, track creation, caption edits, transforms, transitions and keyframes.

Requirements:

- stable operation IDs and idempotency keys;
- project revision preconditions to prevent stale writes;
- undo/redo integration;
- exact affected-resource diff;
- audit trail with caller, request, revision and result;
- dry-run support where meaningful.

## Phase 3 — Media intelligence

Build a local-first analysis graph for:

- speech transcription and word timing;
- silence and filler detection;
- scene/shot boundaries;
- face/person/object tracks;
- motion and camera-shake scoring;
- loudness/noise analysis;
- beat/onset detection;
- saliency and crop anchors;
- duplicate/near-duplicate detection.

Analysis results are cached by content fingerprint and model version.

## Phase 4 — Semantic editing

Omni Dev translates natural-language intent into validated edit plans such as:

- remove dead air while preserving sentence rhythm;
- build a 30-second highlight cut;
- duck music under speech;
- generate and style captions;
- smart reframe 16:9 to 9:16 around tracked subjects;
- create multiple aspect-ratio variants from one project.

The agent proposes operations; Omni Studio remains the execution authority.

## Phase 5 — Creative skills

Add declarative, reviewable skill packs for editing styles:

- short-form / reels;
- gaming montage;
- podcast clips;
- product ads;
- documentary;
- cinematic;
- sports highlights.

Skills may select approved operations and parameters but cannot execute arbitrary code.

## Phase 6 — Omni Creative Graph

Represent sources, analyses, edits and outputs as a dependency graph.

```text
source media -> analysis -> timeline -> captions/effects -> render variants
```

Changing an upstream asset marks dependent outputs stale and allows selective recomputation.

## Phase 7 — Suite expansion

Evolve Omni Studio beyond video without coupling every tool into one monolith:

- **Video** — timeline, compositing and export.
- **Audio** — cleanup, mixing, voice and mastering.
- **Image** — still-image editing and generated assets.
- **Captions** — transcription, translation and typography.
- **Assets** — templates, LUTs, fonts, music and reusable components.
- **Automations** — repeatable batch workflows.

Each domain exposes its own OmniLink capability namespace.

## Phase 8 — Cross-device and desktop

Use `omni-link-transport` for authenticated Android/desktop collaboration:

- resumable media transfer;
- remote render workers;
- project handoff;
- desktop preview/control;
- capability-specific ACLs;
- no assumption that LAN/ADB connectivity equals trust.

## Performance roadmap

- Device-tier-aware proxies and analysis.
- Hardware codec preference with measured fallbacks.
- Bounded concurrent inference.
- Background jobs with resumable state.
- Frame/contact-sheet sampling instead of sending whole videos to an LLM.
- Cache analysis by media hash, model version and parameters.
- Baseline/macrobenchmark budgets for launch, timeline scrubbing and export setup.

## Security roadmap

- Same-signer Binder for official Omni apps.
- Exact capability allowlists.
- No raw shell, SQL or arbitrary-command capability.
- Confirmation tokens bound to exact prepared mutations.
- Content URIs / file descriptors for large payloads.
- Treat media metadata, captions and imported project text as untrusted data.
- Durable, privacy-aware audit records.
- Immediate grant/session revocation.
