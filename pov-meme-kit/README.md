# POV meme video kit

Everything needed to make Pawspective-style emotional "POV" cat/dog meme videos with free tools
(ffmpeg + Python, SD-Turbo for backgrounds). Not part of the HayaiTTS app.

**New chat? Paste [`START_PROMPT.md`](START_PROMPT.md).**

| Path | What it is |
|---|---|
| `HANDOFF.md` | Every request and standing rule from the user, clip catalogue, decisions. Read first. |
| `STYLE_GUIDE.md` | How the reference videos are built, scene by scene. |
| `scripts/setup.sh` | One-time pip installs for a fresh session. |
| `scripts/build.py` | Builds a video from a story JSON → `<name>_with-sound.mp4` (+ a silent copy). |
| `scripts/bg.py` | Generates scene backgrounds (SD-Turbo, CPU, ~45 s each). |
| `scripts/cuts.py` | Lists exact cuts in a clip pack or a finished video (flicker QA). |
| `scripts/jumps.py` | Lists sudden frame jumps in a finished video (smoothness QA). |
| `scripts/avsync.py` | Audio and video must both start at 0 (else players fast-forward the start). |
| `stories/` | Story JSONs. `mom-wasnt-hungry.json` is the approved example. |
| `assets/packs/` | Green-screen meme packs: `cat_pack.mp4`, `dog_pack.mp4`, plus `*.notes.json` = the manual of bad audio stretches (auto-muted) and clean meows to borrow. |
| `assets/music/` | Background song(s). |
| `assets/backgrounds/` | Generated scene backgrounds. |
| `references/` | The 3 target reference videos + 4 screenshots from the user. |
| `examples/` | Approved finished videos. |

## Make a video
```bash
cd pov-meme-kit
bash scripts/setup.sh
python3 scripts/bg.py assets/backgrounds/hospital "photo of a hospital room with a bed, realistic interior photography, empty, no people, wide angle"
python3 scripts/build.py stories/<story>.json out
python3 scripts/avsync.py out/<name>_with-sound.mp4  # must print OK
python3 scripts/cuts.py out/<name>_with-sound.mp4    # only the planned scene cuts may appear
python3 scripts/jumps.py out/<name>_with-sound.mp4   # spikes only at fade-in + scene cuts
```

## Story JSON
```jsonc
{
  "name": "file-name", "title": "Title after POV:", "handle": "",
  "packs": {"cat": "assets/packs/cat_pack.mp4", "dog": "assets/packs/dog_pack.mp4"},
  "music": {"bed": "assets/music/x.mp3", "bed_ss": 0, "end": "assets/music/x.mp3", "end_ss": 168,
            "switch": 46, "vol": 0.45},          // switch = second the ending music takes over
  "scenes": [{
    "bg": "assets/backgrounds/kitchen.png", "dur": 11,
    "dark": false, "dim_at": null,              // dark = night look; dim_at = second to dim (ending)
    "actors": [{
      "pack": "cat", "ss": 30.0, "len": 8.0,    // clip start/length INSIDE one segment (see HANDOFF)
      "h": 420, "x": 320, "y": 1440,             // height px, center x, bottom y on a 1080x1920 canvas
      "t0": 0, "t1": 11, "flip": false, "vol": 1.0, "speed": 1.0,   // speed<1 calms twitchy clips
      "keep_top": 0.55, "sound_ss": 30.0,        // crop away bottom (hands); borrow sound from another time
      "label": "Me:", "lx": 0,                   // lx shifts label/speech sideways
      "say": [{"text": "Line one\nline two", "t0": 0.6, "t1": 4.6}]   // plays the cat's sound too
    }],
    "subs": [{"text": "centered text", "x": 540, "y": 700, "t0": 0, "t1": 9, "color": "white", "size": 58}]
  }]
}
```
To swap a character's clip mid-scene (emotion change), add a second actor at the same x/y with
consecutive `t0`/`t1`.

## Copyright
The meme packs and the song belong to other people. Keep this repo **private**; when posting,
prefer adding music from the platform's own library to avoid mutes and claims.
