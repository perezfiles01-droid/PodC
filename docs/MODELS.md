# Voice catalog

*Auto-generated from `catalog/v1/models.json` on 2026-09-21 by `tools/catalog/build_model_list.py` (run from the weekly [catalog-refresh](../.github/workflows/catalog-refresh.yml) workflow). Do not edit by hand — the next refresh will overwrite your changes.*

## Summary

- **170 voices** across **8 model families**
- **38 languages** covered
- Bundle size: 21–635 MB (median 43 MB)
- **6 voices** support reference-audio cloning

### By family

| Family | Voices | Notes |
|---|---:|---|
| **piper** | 133 | Compact VITS-based voices from the rhasspy/piper project. 10–60 MB per voice, sub-second on a 2020+ phone, ~70 languages covered. |
| **kokoro** | 6 | Higher-quality multilingual VITS variant (Kokoro-82M). 80–360 MB per voice; English bundles ship 1–50 speakers in a single model. |
| **kitten** | 7 | Tiny English-only VITS distillations tuned for low-end phones. <60 MB, fastest synthesis on the catalog. |
| **matcha** | 4 | Diffusion-based Matcha-TTS voices. Ships a vocoder side-asset alongside the main weights — Browse handles the dual download. |
| **supertonic** | 2 | Newest (2026) multilingual model from Supertone. Single 100–200 MB bundle covering ~30 languages × 10 speakers. |
| **zipvoice** | 4 | Flow-matching voice-cloning model. Accepts a reference clip + transcript and synthesises the target text in the cloned voice. |
| **pocket** | 2 | Compact voice-cloning model. Same reference-audio API as ZipVoice but with a smaller voice-embedding cache and lighter weights. |
| **vits** | 12 | _(family blurb pending — add to `FAMILY_BLURB`)_ |

## piper (133)

Compact VITS-based voices from the rhasspy/piper project. 10–60 MB per voice, sub-second on a 2020+ phone, ~70 languages covered.

| Title | Languages | Speakers | Size | Tier | Quality | RTF | License |
|---|---|---:|---:|---|---|---:|---|
| Alan | en-GB | 1 | 67 MB | low | low | — | MIT |
| Alan | en, gb | 1 | 36 MB | mid | low | — | MIT |
| Alan | en, gb | 1 | 21 MB | mid | low | — | MIT |
| Alan | en-GB | 1 | 67 MB | mid | medium | — | MIT |
| Alan | en, gb | 1 | 36 MB | mid | medium | — | MIT |
| Alan | en, gb | 1 | 21 MB | mid | medium | — | MIT |
| Alba | en-GB | 1 | 67 MB | mid | medium | — | MIT |
| Alba | en, gb | 1 | 36 MB | mid | medium | — | MIT |
| Alba | en, gb | 1 | 21 MB | mid | medium | — | MIT |
| Amy | en-US | 1 | 67 MB | low | low | — | MIT |
| Amy | en, us | 1 | 36 MB | mid | low | — | MIT |
| Amy | en, us | 1 | 21 MB | mid | low | — | MIT |
| Amy | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Amy | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Amy | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Bryce | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Bryce | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Bryce | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Cori | en-GB | 1 | 116 MB | high | high | — | MIT |
| Cori | en, gb | 1 | 59 MB | mid | high | — | MIT |
| Cori | en, gb | 1 | 35 MB | mid | high | — | MIT |
| Cori | en-GB | 1 | 67 MB | mid | medium | — | MIT |
| Cori | en, gb | 1 | 36 MB | mid | medium | — | MIT |
| Cori | en, gb | 1 | 21 MB | mid | medium | — | MIT |
| Danny | en-US | 1 | 67 MB | low | low | — | MIT |
| Danny | en, us | 1 | 36 MB | mid | low | — | MIT |
| Danny | en, us | 1 | 21 MB | mid | low | — | MIT |
| Dii | en-GB | 1 | 67 MB | high | — | — | MIT |
| Glados | en-US | 1 | 116 MB | high | — | — | MIT |
| Hfc_female | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Hfc_female | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Hfc_female | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Hfc_male | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Hfc_male | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Hfc_male | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Jenny_dioco | en-GB | 1 | 67 MB | mid | medium | — | MIT |
| Jenny_dioco | en, gb | 1 | 36 MB | mid | medium | — | MIT |
| Jenny_dioco | en, gb | 1 | 21 MB | mid | medium | — | MIT |
| Joe | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Joe | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Joe | en, us | 1 | 21 MB | mid | medium | — | MIT |
| John | en-US | 1 | 67 MB | mid | medium | — | MIT |
| John | en, us | 1 | 36 MB | mid | medium | — | MIT |
| John | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Kathleen | en-US | 1 | 67 MB | low | low | — | MIT |
| Kathleen | en, us | 1 | 36 MB | mid | low | — | MIT |
| Kathleen | en, us | 1 | 21 MB | mid | low | — | MIT |
| Kristin | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Kristin | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Kristin | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Kusal | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Kusal | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Kusal | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Lessac | en-US | 1 | 116 MB | high | high | — | MIT |
| Lessac | en, us | 1 | 58 MB | mid | high | — | MIT |
| Lessac | en, us | 1 | 35 MB | mid | high | — | MIT |
| Lessac | en-US | 1 | 67 MB | low | low | — | MIT |
| Lessac | en, us | 1 | 36 MB | mid | low | — | MIT |
| Lessac | en, us | 1 | 21 MB | mid | low | — | MIT |
| Lessac | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Lessac | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Lessac | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Ljspeech | en-US | 1 | 116 MB | high | high | — | MIT |
| Ljspeech | en, us | 1 | 59 MB | mid | high | — | MIT |
| Ljspeech | en, us | 1 | 34 MB | mid | high | — | MIT |
| Ljspeech | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Ljspeech | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Ljspeech | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Miro | en-GB | 1 | 67 MB | high | — | — | MIT |
| Miro | en-US | 1 | 67 MB | high | — | — | MIT |
| Norman | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Norman | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Norman | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Northern_english_male | en-GB | 1 | 67 MB | mid | medium | — | MIT |
| Northern_english_male | en, gb | 1 | 36 MB | mid | medium | — | MIT |
| Northern_english_male | en, gb | 1 | 21 MB | mid | medium | — | MIT |
| Piper (109 speakers) | en-GB | 109 | 80 MB | mid | medium | — | MIT |
| Piper (12 speakers) | en-GB | 12 | 80 MB | mid | medium | — | MIT |
| Piper (18 speakers) | en-US | 18 | 80 MB | mid | medium | — | MIT |
| Piper (24 speakers) | en-US | 24 | 80 MB | mid | medium | — | MIT |
| Piper (4 speakers) | en-GB | 4 | 80 MB | mid | medium | — | MIT |
| Piper (6 speakers) | en-GB | 6 | 80 MB | mid | — | — | MIT |
| Piper (8 speakers) | en-GB | 8 | 80 MB | mid | — | — | MIT |
| Piper (904 speakers) | en-US | 904 | 131 MB | high | high | — | MIT |
| Piper (904 speakers) | en-US | 904 | 82 MB | mid | medium | — | MIT |
| Reza_ibrahim | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Reza_ibrahim | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Reza_ibrahim | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Ryan | en-US | 1 | 116 MB | high | high | — | MIT |
| Ryan | en, us | 1 | 59 MB | mid | high | — | MIT |
| Ryan | en, us | 1 | 34 MB | mid | high | — | MIT |
| Ryan | en-US | 1 | 67 MB | low | low | — | MIT |
| Ryan | en, us | 1 | 36 MB | mid | low | — | MIT |
| Ryan | en, us | 1 | 21 MB | mid | low | — | MIT |
| Ryan | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Ryan | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Ryan | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Sam | en-US | 1 | 67 MB | mid | medium | — | MIT |
| Sam | en, us | 1 | 36 MB | mid | medium | — | MIT |
| Sam | en, us | 1 | 21 MB | mid | medium | — | MIT |
| Southern_english_female | en-GB | 1 | 67 MB | low | low | — | MIT |
| Southern_english_female | en, gb | 1 | 36 MB | mid | low | — | MIT |
| Southern_english_female | en, gb | 1 | 21 MB | mid | low | — | MIT |
| Speaker_0 | en, gb | 1 | 42 MB | mid | medium | — | MIT |
| Speaker_0 | en, gb | 1 | 23 MB | mid | medium | — | MIT |
| Speaker_0 | en, gb | 1 | 36 MB | mid | — | — | MIT |
| Speaker_0 | en, gb | 1 | 21 MB | mid | — | — | MIT |
| Speaker_0 | en, gb | 1 | 36 MB | mid | — | — | MIT |
| Speaker_0 | en, gb | 1 | 21 MB | mid | — | — | MIT |
| Speaker_0 | en, gb | 1 | 42 MB | mid | medium | — | MIT |
| Speaker_0 | en, gb | 1 | 23 MB | mid | medium | — | MIT |
| Speaker_0 | en, gb | 1 | 42 MB | mid | — | — | MIT |
| Speaker_0 | en, gb | 1 | 24 MB | mid | — | — | MIT |
| Speaker_0 | en, gb | 1 | 80 MB | mid | — | — | MIT |
| Speaker_0 | en, gb | 1 | 42 MB | mid | — | — | MIT |
| Speaker_0 | en, gb | 1 | 24 MB | mid | — | — | MIT |
| Speaker_0 | en, gb | 1 | 116 MB | mid | — | — | MIT |
| Speaker_0 | en, gb | 1 | 42 MB | mid | medium | — | MIT |
| Speaker_0 | en, gb | 1 | 23 MB | mid | medium | — | MIT |
| Speaker_0 | en, us | 1 | 42 MB | mid | medium | — | MIT |
| Speaker_0 | en, us | 1 | 23 MB | mid | medium | — | MIT |
| Speaker_0 | en, us | 1 | 67 MB | mid | — | — | MIT |
| Speaker_0 | en, us | 1 | 58 MB | mid | — | — | MIT |
| Speaker_0 | en, us | 1 | 35 MB | mid | — | — | MIT |
| Speaker_0 | en, us | 1 | 42 MB | mid | medium | — | MIT |
| Speaker_0 | en, us | 1 | 23 MB | mid | medium | — | MIT |
| Speaker_0 | en, us | 1 | 66 MB | mid | high | — | MIT |
| Speaker_0 | en, us | 1 | 36 MB | mid | high | — | MIT |
| Speaker_0 | en, us | 1 | 43 MB | mid | medium | — | MIT |
| Speaker_0 | en, us | 1 | 23 MB | mid | medium | — | MIT |
| Speaker_0 | en, us | 1 | 36 MB | mid | — | — | MIT |
| Speaker_0 | en, us | 1 | 21 MB | mid | — | — | MIT |
| Speaker_0 | fa, en | 1 | 67 MB | mid | — | — | MIT |

## kokoro (6)

Higher-quality multilingual VITS variant (Kokoro-82M). 80–360 MB per voice; English bundles ship 1–50 speakers in a single model.

| Title | Languages | Speakers | Size | Tier | Quality | RTF | License |
|---|---|---:|---:|---|---|---:|---|
| Kokoro (103 speakers) | zh-CN, en-US | 103 | 365 MB | high | — | — | Apache-2.0 |
| Kokoro (11 speakers) | en-US | 11 | 320 MB | high | — | — | Apache-2.0 |
| Kokoro (54 speakers) | zh-CN, en-US | 54 | 350 MB | high | — | — | Apache-2.0 |
| Speaker_0 | en | 1 | 103 MB | high | — | — | Apache-2.0 |
| Speaker_0 | en | 1 | 132 MB | high | — | — | Apache-2.0 |
| Speaker_0 | en | 1 | 147 MB | high | — | — | Apache-2.0 |

## kitten (7)

Tiny English-only VITS distillations tuned for low-end phones. <60 MB, fastest synthesis on the catalog.

| Title | Languages | Speakers | Size | Tier | Quality | RTF | License |
|---|---|---:|---:|---|---|---:|---|
| Kitten (8 speakers) | en-US | 8 | 44 MB | low | — | — | Apache-2.0 |
| Kitten (8 speakers) | en-US | 8 | 157 MB | low | — | — | Apache-2.0 |
| Kitten (8 speakers) | en-US | 8 | 68 MB | low | — | — | Apache-2.0 |
| Kitten (8 speakers) | en-US | 8 | 27 MB | low | — | — | Apache-2.0 |
| Kitten (8 speakers) | en-US | 8 | 27 MB | low | — | — | Apache-2.0 |
| Kitten (8 speakers) | en-US | 8 | 64 MB | low | — | — | Apache-2.0 |
| Kitten (8 speakers) | en-US | 8 | 31 MB | low | — | — | Apache-2.0 |

## matcha (4)

Diffusion-based Matcha-TTS voices. Ships a vocoder side-asset alongside the main weights — Browse handles the dual download.

| Title | Languages | Speakers | Size | Tier | Quality | RTF | License |
|---|---|---:|---:|---|---|---:|---|
| En | zh-CN, en-US | 1 | 79 MB | high | — | — | MIT |
| Ljspeech | en-US | 1 | 77 MB | high | — | — | MIT |
| Speaker_0 | fa, en | 1 | 77 MB | high | — | — | MIT |
| Speaker_0 | fa, en | 1 | 77 MB | high | — | — | MIT |

## supertonic (2)

Newest (2026) multilingual model from Supertone. Single 100–200 MB bundle covering ~30 languages × 10 speakers.

| Title | Languages | Speakers | Size | Tier | Quality | RTF | License |
|---|---|---:|---:|---|---|---:|---|
| Speaker_0 | en | 1 | 85 MB | high | — | — | openrail |
| Supertonic (10 speakers) | 31 languages | 10 | 129 MB | high | — | — | openrail |

## zipvoice (4)

Flow-matching voice-cloning model. Accepts a reference clip + transcript and synthesises the target text in the cloned voice.

| Title | Languages | Speakers | Size | Tier | Quality | RTF | License |
|---|---|---:|---:|---|---|---:|---|
| Speaker_0 · 🎤 cloning | zh, en | 1 | 478 MB | mid | — | — | Apache-2.0 |
| Speaker_0 · 🎤 cloning | zh, en | 1 | 109 MB | mid | — | — | Apache-2.0 |
| Speaker_0 · 🎤 cloning | zh, en | 1 | 635 MB | mid | — | — | Apache-2.0 |
| Speaker_0 · 🎤 cloning | zh, en | 1 | 635 MB | mid | — | — | Apache-2.0 |

## pocket (2)

Compact voice-cloning model. Same reference-audio API as ZipVoice but with a smaller voice-embedding cache and lighter weights.

| Title | Languages | Speakers | Size | Tier | Quality | RTF | License |
|---|---|---:|---:|---|---|---:|---|
| Speaker_0 · 🎤 cloning | en | 1 | 168 MB | mid | — | — | Apache-2.0 |
| Speaker_0 · 🎤 cloning | en | 1 | 98 MB | mid | — | — | Apache-2.0 |

## vits (12)

| Title | Languages | Speakers | Size | Tier | Quality | RTF | License |
|---|---|---:|---:|---|---|---:|---|
| Speaker_0 | en | 1 | 115 MB | mid | — | — | Apache-2.0 |
| Speaker_0 | en | 1 | 115 MB | mid | — | — | Apache-2.0 |
| Speaker_0 | en | 1 | 122 MB | mid | — | — | Apache-2.0 |
| Speaker_0 | en, us | 1 | 32 MB | low | — | — | Apache-2.0 |
| Speaker_0 | en, us | 1 | 74 MB | mid | — | — | Apache-2.0 |
| Speaker_0 | en | 1 | 109 MB | mid | — | — | Apache-2.0 |
| Speaker_0 | en | 1 | 163 MB | mid | — | — | Apache-2.0 |
| Speaker_0 | zh, en | 1 | 167 MB | mid | — | — | Apache-2.0 |
| Speaker_0 | en | 1 | 108 MB | low | — | — | Apache-2.0 |
| Speaker_0 | en | 1 | 152 MB | mid | — | — | Apache-2.0 |
| V2 | en-US | 1 | 43 MB | mid | — | — | Apache-2.0 |
| V2 | en-US | 1 | 22 MB | mid | — | — | Apache-2.0 |

---

_Source of truth: [`catalog/v1/models.json`](../catalog/v1/models.json). Each voice entry includes the bundle URL, sha256, sample rate, and per-(speaker, language) audition URLs in the JSON; this page is just the human-readable index._
