# Omni Studio Brand System

## Product mark

Studio belongs to the same visual family as OmniDev, Omni Launcher, OmniMemoria,
OmniNote and OmniEqualizer: a softly scalloped dark badge carrying one bold symbol.
The eight-lobe badge establishes the ecosystem silhouette. A single rounded **S ribbon**
represents Studio and a flowing editing timeline. Its lavender-to-orchid gradient echoes
the creative palette of the family without adding extra pictograms or nested frames.

Keep the symbol large, the curves soft, and the negative space clear. Do not add play
buttons, concentric rings, overlapping frames, lettering, outlines or sparkle clusters.

## Palette

| Token | Value | Role |
|---|---|---|
| Ink | `#0E0E1B` | Opaque adaptive/store background |
| Badge top | `#302467` | Deep purple family badge |
| Badge base | `#17182D` | Quiet navy depth |
| Lavender | `#BEB8FF` | Ribbon top |
| Violet | `#9B83EE` | Ribbon middle |
| Orchid | `#DB7DD5` | Ribbon end |

Both gradients use identical endpoints and stops in Android XML and store SVG.
There are no shadows, textures or tiny decorative elements.

## Android identity

- `res/drawable/ic_omni_studio_background.xml`: full-bleed ink for launcher masks.
- `res/drawable/ic_omni_studio_foreground.xml`: scalloped badge and Studio ribbon.
- `res/drawable/ic_omni_studio_monochrome.xml`: white Studio ribbon for themed icons.
- `res/mipmap-anydpi/ic_launcher.xml` and `ic_launcher_round.xml`: vector fallback.
- `res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`: adaptive versions.
- `res/values/ic_launcher_colors.xml`: palette.

The badge fits inside a radius of 32.5 in the 108-unit viewport, within the central
66-unit adaptive safe circle. The ribbon has generous clearance inside the badge.
Android supplies the outer circle/squircle mask. The background remains opaque for
predictable launcher rendering; the family badge remains visible within that mask.
Android 13+ themed icons use only the S ribbon so the launcher can supply its colors
without turning the badge into a solid monochrome blob. There are no raster overrides.

## Store and repository

- `fastlane/metadata/android/en-US/images/_source/icon.svg`: opaque store source.
- `fastlane/metadata/android/en-US/images/icon.png`: 512 × 512 store PNG.
- `icon.png`: byte-identical repository PNG.
- `docs/branding/omni-studio-logo.svg`: transparent standalone family badge.

All sources share the same badge and ribbon paths. The listing generator reads the
current `_source/icon.svg`; it cannot restore the inherited icon. Update the icon
source checksum in `asset_inventory.json` whenever the source changes.
