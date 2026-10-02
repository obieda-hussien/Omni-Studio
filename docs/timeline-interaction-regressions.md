# Timeline direction and phone interaction contract

The timeline is a spatial time axis: earlier time is physically left, later time
is physically right. Its ruler, clip layout, thumbnails, trim handles, selection
ranges, playhead, snap indicators, track rail, and overview must share that axis.
`TimelineCoordinateSpace` enforces LTR inside the timeline even when the device
uses Arabic/RTL and the app strings remain English. The surrounding app retains
its normal direction and text localization.

## Interaction fixes

- Pan/pinch accumulates fractional milliseconds within each gesture. It does
  not depend on recomposition catching up between pointer events.
- Zoom and scroll are applied atomically and share the ViewModel's project
  bounds. Pinching preserves the time beneath the pinch center until a boundary
  is reached; reversing at a boundary responds immediately.
- Vertical swipes scroll the synchronized track rail and canvases. Horizontal
  clip gestures retain trim/slip/slide behavior. Tiny clips keep a center body
  zone and two distinct trim edges, based on their actual rendered width.
- Slide/slip updates compare their planned timing with the current displayed
  timing. Returning a cumulative gesture to zero restores the original timing.
  Existing gesture transactions retain a single undo step and cancel behavior.
- Playback auto-follow is suspended during editing, scrubbing, panning,
  pinching, and overview dragging, then restored at the existing gesture end.
- Overview dragging preserves the grab point inside the viewport; grabbing
  outside it recenters before dragging. Its touch target is 48dp.
- Compact timeline controls have their own scrollable width so they cannot
  consume the entire header and hide the time summary. Track-list scrolling is
  bounded to preserve the overview and tool rail. Snap haptics fire on entering
  a snap target rather than every pointer event at that target.

## Portrait and landscape

Portrait keeps the stacked preview/timeline/tools arrangement. Short windows
below 480dp high and at least 640dp wide use a 38% preview / 62% editing split.
The timeline takes the flexible editing height and the tool rail stays compact.
The trim hint banner is omitted in this short-window layout; numeric trim
controls remain available. Fullscreen preview still fills the workspace.

## Verification

Executed locally:

- Eleven JUnit tests: eight against the actual Android-free viewport, overview,
  zoom, and workspace policies, plus three existing code-ownership/toolbar
  contract checks. These ran using the standalone Kotlin 2.2.20 compiler; the
  repository-wide Kotlin/Android build remains unverified.
- Kotlin syntax parsing of every changed Kotlin file.
- `git diff --check`.

Added for the normal Android test pipeline:

- Tiny-clip trim/body gesture-zone coverage.
- Playback follow suspension and resumption coverage.
- Compose instrumentation of the real timeline under LTR and RTL hosts,
  asserting unchanged clip bounds and rightward drag advancing clip time.
- Compose instrumentation of the production landscape layout seam, checking
  preview/timeline/tool bounds and non-overlap.

The full Gradle build and Android instrumentation have **not** been executed
locally. The wrapper's Gradle distribution download failed with
`java.net.SocketException: Network is unreachable`; this environment also has
no configured Android SDK/device. Source syntax and standalone policy tests
are not a substitute for those checks.

Run the repository gates in an Android build environment:

```sh
./gradlew :app:testQaUnitTest :app:compileQaAndroidTestKotlin
./gradlew :app:connectedQaAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.novacut.editor.ui.editor.TimelineDirectionLayoutTest
```

Device review checklist (Arabic device + English app, and English device):

1. Add video/audio clips and scrub the ruler in both orientations.
2. Slide a clip right, left, and back to its starting point. Repeat with slip
   and both trim edges; verify undo, redo, locked tracks, and linked A/V.
3. Zoom until a clip is very narrow and verify each edge plus its center body.
4. Swipe vertically across clips to reveal more tracks; verify matching rail
   positions and that the bottom controls stay reachable.
5. Pan/pinch and drag the overview during playback. Verify the viewport follows
   the finger, then auto-follow resumes after release.
6. Rotate with selected clips, trim controls, multiple tracks, and expanded
   tools. Verify preview and tool access, then enter/exit fullscreen preview.
