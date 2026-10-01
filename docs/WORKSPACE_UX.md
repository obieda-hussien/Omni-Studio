# Omni Studio workspace organization

## Editor

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

## Settings

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

## Projects and motion

The project home uses a smaller heading and shorter visual hierarchy. Sort options
live in one menu, leaving the existing collection filters separate. Shared chrome
uses Omni lavender for selection and a clear on-accent label color.

Motion tokens are 100ms for feedback, 160ms for small changes, 220ms for panels and
300ms for larger reveals. Primary feedback springs are damped without overshoot.
Navigation combines a short fade with a small directional offset, mirrored in RTL.
Compose's system duration scale remains in force; there is no custom frame loop,
blur layer or forced animation scale in this change.

## Verification

- Existing action IDs and settings callback inventory were compared before/after.
- Both resource XML files parse without duplicate keys.
- Existing smoke navigation selects the App settings destination.
- Added an instrumented regression for switching destinations and control reachability.
- CI compiles the UI regression tests alongside unit tests, release lint and release builds.

An emulator and Android SDK are unavailable in the editing environment. Local
Gradle compilation was blocked at distribution download by network access. Device
screenshots, real scrolling behavior and frame timing require the device QA lane;
this change does not claim measured FPS or completed visual verification.
