# Omni Studio Brand System

## Product mark

Omni Studio uses a vector-first Android identity designed to remain recognizable at launcher size and
to sit naturally beside the rest of the Omni ecosystem.

The mark combines four ideas:

- an **organic dark Omni badge** shared with the wider family;
- **three overlapping creative frames** representing video, audio, image and layered composition;
- an **S-shaped ribbon/timeline path** representing Studio and non-destructive editing;
- two restrained **creative sparkles** representing agent-assisted creative work without making AI the
  entire product identity.

The icon intentionally avoids a play triangle because Omni Studio is a creative suite, not only a video
player/editor.

## Palette

| Token | Value | Role |
|---|---:|---|
| Ink | `#0B0B1F` | launcher/background base |
| Indigo | `#4F46E5` | first creative frame |
| Violet | `#7C3AED` | ecosystem primary |
| Lavender | `#C084FC` | inner frame/highlight |
| Pink | `#F472B6` | creative continuation accent |
| Highlight | `#F8F7FF` | ribbon/spark |

## Android source of truth

Runtime launcher identity is XML/vector-first:

```text
res/drawable/ic_omni_studio_background.xml
res/drawable/ic_omni_studio_foreground.xml
res/drawable/ic_omni_studio_monochrome.xml
res/mipmap-anydpi/ic_launcher.xml
res/mipmap-anydpi/ic_launcher_round.xml
res/mipmap-anydpi-v26/ic_launcher.xml
res/mipmap-anydpi-v26/ic_launcher_round.xml
res/values/ic_launcher_colors.xml
```

There are deliberately no density-specific `ic_launcher*.png` files. This prevents a device,
launcher shape, or API-level fallback from resurfacing the retired ClearCut raster identity.

Android 13+ themed icons use the dedicated monochrome vector.

## Store/repository artwork

The same geometry and palette are mirrored into:

- `fastlane/metadata/android/en-US/images/_source/icon.svg`
- `fastlane/metadata/android/en-US/images/icon.png`
- `icon.png`

Store artwork may use richer gradients and soft depth, but it must preserve the same silhouette and
central S/layered-frame motif as the runtime vector.
