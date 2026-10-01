# Omni Studio Brand System

## Product mark

The mark is a bold **O** for Omni with two diagonal **editing cuts** and a small rounded
**media glyph**. The O anchors Studio in the ecosystem; the cuts communicate creating and
editing, and the center makes the media workspace recognizable at launcher size.

Use this single mark without extra frames, sparkles, outlines, badges or lettering.
The flat background and generous negative space keep it readable at 24–48 px.

## Palette

| Token | Value | Role |
|---|---|---|
| Ink | `#111122` | Full-bleed launcher/store background |
| Omni lavender | `#8E8DE5` | O and editing cuts |
| Highlight | `#F8F7FF` | Media glyph |

## Android identity

- `res/drawable/ic_omni_studio_background.xml`: flat full-bleed ink.
- `res/drawable/ic_omni_studio_foreground.xml`: cut O and media glyph.
- `res/drawable/ic_omni_studio_monochrome.xml`: identical geometry in one color.
- `res/mipmap-anydpi/ic_launcher.xml` and `ic_launcher_round.xml`: vector fallback.
- `res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`: adaptive versions.
- `res/values/ic_launcher_colors.xml`: palette.

All foreground geometry fits within a radius of 30 in the 108-unit viewport, inside the
central 66-unit adaptive safe circle. Android supplies the launcher mask; do not bake
rounded corners or a second badge into the background. Round and squircle masks preserve
the whole mark. Android 13+ themed icons retain the same silhouette and both cuts.
There are no density-specific raster launcher overrides.

## Store and repository

The same paths, proportions and flat colors are used in:

- `fastlane/metadata/android/en-US/images/_source/icon.svg`: store source, 1024-unit output.
- `fastlane/metadata/android/en-US/images/icon.png`: 512 × 512 store PNG.
- `icon.png`: identical repository PNG.
- `docs/branding/omni-studio-logo.svg`: scalable product mark.

Store PNGs are opaque, square and full-bleed. The listing generator reads the current
`_source/icon.svg`, so regeneration cannot restore the retired product icon. Update the
icon entry's source checksum in `asset_inventory.json` whenever the source changes.
