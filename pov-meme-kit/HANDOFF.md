# Session Handoff — Cat/Dog POV Meme Videos

_Last updated: 2026-10-24. Paste this whole file into a new session to resume._

## Goal
Make short vertical (9:16) emotional "POV" meme videos in the style of @Pawspective:
AI/stock room background, green-screen meme cats/dogs cut out and placed in the scene,
white labels ("Me:", "Mom:"), white rounded "POV: ..." title box at top, yellow subtitles
for spoken lines, sad/heartwarming twist.

## Everything the user asked for (in order)
1. "Create similar content as this" — the 4 example screenshots (girlfriend toy, doctor son,
   grandpa school pickup, father's sacrifice). Match that look.
2. Follow the "Video Edits" guide idea: folder called **Video Edits**, video-use + HyperFrames
   style workflow, captions (2–3 words, bold, uppercase, yellow key words), title card in
   first 3s, save a reusable **style guide** once and reuse it.
3. Use **free tools only** — no paid apps (no Figma credits, no Floot upgrade, no ElevenLabs).
4. Use **Kokoro** for voices instead of ElevenLabs.
5. Use the uploaded **cat green-screen pack** (videoplayback.mp4, 200s, 640x360) for cats.
6. Uploaded a **dog green-screen pack** (videoplayback_1.mp4, 201s, 640x360) for dogs.
7. **Save the clips somewhere free and persistent** (GitHub or another free app I can access).
8. **Explain in detail what I'm doing and planning** so the user can correct anything.
9. **Keep this handoff updated** with every request so the next session does exactly what
   the user wants.
10. **Use Google Drive** for storage.
11. **Drive rule:** put NO videos in Drive except reference files the user explicitly asks for.
    Finished videos are sent **directly in chat** when the user asks, so they can download
    and edit them.
12. User sent **3 reference MP4s** = the target final product (60–68s multi-scene stories).
    Saved as a written breakdown in Drive: "STYLE GUIDE - Pawspective reference breakdown"
    (https://drive.google.com/file/d/1TvfKKgX5NnIqBgSHh7QpD0xi_w2nHKBo/view). Read it first.
13. User asked to **save the reference MP4s in Drive**. Not possible via connector (too big) —
    user should drag the 3 MP4s into the Video Edits folder themselves; the breakdown is the
    readable copy for me.
14. ~~Export both versions~~ → CHANGED (#27): send ONLY the with-sound video.
15. v1 (13s, Kokoro voices, 1 scene) — user REJECTED: "not what I want, do it like the
    reference videos, voices the same as the ones in the vid".
16. KEY FINDING: reference videos have NO spoken voices. Audio = each cat's own meme sound
    (meows/cries) while it "talks" + background song, switching to the song's climax at the
    ending. Kokoro is NOT used anymore.
17. User's background song: "Hale - Chasing Cars (Snow Patrol cover)" mp3, in Drive Video Edits
    folder (too big for connector; user re-uploads in chat). Bed = 0:00-0:50, ending = 2:48+
    (loudest climax). Note: copyrighted song; platform may mute/claim it.
18. v2 sent (53s, 5 scenes, story "Mom always said she wasn't hungry", cat sounds + song).
19. RULE: text goes **where the characters are** — each cat's label sits right above its head
    and its speech right above the label; label + speech appear/disappear WITH that cat.
20. RULE: cats are **placed in the scene per the plot** (at the table, on the kitchen floor,
    peeking from a doorway, seated at a restaurant table), sized to the room — like the refs.
    A character can swap meme clips mid-scene for emotion (same x/y, consecutive t0/t1).
21. Audio technique = refs: a cat's own meme sound plays only while it speaks; the song runs
    underneath and switches to the climax for the ending. (build.py "say" auto-adds sound.)
22. v3 sent (55s) with rules 19–21 applied.
23. RULE: **no flicker** — a clip range must never cross a cut in the pack (v3 bug: flashes of the
    neighbouring cat). Use `cuts.py <pack.mp4>` and keep >=0.2s inside each segment.
    Cat pack exact cuts (s): 0 6.73 13.93 19.83 24.67 28.53 38.37 43.67 53.87 54.77 55.63 56.47
    57.23 58.00 58.87 63.73 70.00 74.43 79.37 83.17 92.43 95.77 103.87 110.70 118.53 124.10
    (124–132 rapid cuts) 132.10 136.97 152.60 155.60 162.03 168.50 175.77 181.20 194.63
24. RULE: **match the emotion** — sad story beats use sad/crying clips (sad tabby 38.6–43.5,
    crying grey cat 79.6–83.0, crying dark cat 104.1–110.5); happy/proud beats use the talking
    orange cat 137.3–152.3; innocent kid = big-eyed kitten 28.8–38.1.
25. RULE: **no watermark** at all (handle = "").
26. v4 sent (55s): flicker fixed (verified with cuts.py on output), re-cast by emotion, no watermark.
27. RULE: send **only 1 video, the one with sound**.
28. RULE: **smooth story, no fast-forward look.** Cause found: meme clips start with a quick
    zoom-in/intro motion, and looping replayed it. Fixes now in build.py: (a) start every clip
    >=1.0s after its cut, (b) ping-pong loop (forward+reverse) so loops never jump.
    Safe ranges: kitten 30.0+8.0, sad tabby 38.9+4.5, bowl 119.3+4.6, peek 75.2+4.0,
    crying grey 80.0+3.0, crying dark 104.6+5.9, orange talking 137.8+14.5.
29. QA before sending, every time: `avsync.py out.mp4` (OK), `cuts.py out.mp4` (only planned
    scene cuts), `jumps.py out.mp4` (spikes only at fade-in + scene cuts/clip swaps) and
    `pitch.py` on each scene's lines (all CAT, see #42). Avoid repeating past problems.
30. v5 sent (55s, with sound only) — passes both checks.
31. Everything is saved to GitHub (`pov-meme-kit/` on branch ccr-acf60340-s3bcq6) so a new chat
    can continue fully: paste `START_PROMPT.md` and it rebuilds the same way.
32. User: v5's first seconds still looked fast-forwarded. Verified with burned-in timestamps that
    playback speed is correct — the big-eyed kitten meme itself is twitchy. FIX: per-actor
    `"speed"` (0.7 for the kitten) slows video + sound. RULE: slow down any twitchy/fast clip,
    especially in the opening seconds.
33. RULE: **no dead air.** Lines follow each other with <=0.3 s gaps; each line on screen
    ~3 s; scenes only as long as their lines (6–10 s each). v6 = 38 s instead of 55 s.
34. v6 sent (38 s) — passes cuts.py + jumps.py.
35. User: first 3–5 s STILL fast-forwarded. REAL ROOT CAUSE: the audio track started at 3.4 s
    (scene audio mix took its start time from the first cat sound), so phone players raced the
    video to catch up. FIX in build.py: silence input first in each scene's amix + asetpts.
    NEW QA: `scripts/avsync.py out.mp4` must print OK (audio+video start at 0, same length).
36. v7 sent (38 s) — passes avsync + cuts + jumps. QA before EVERY send = all three scripts.
37. User: remove the **woman laughing** and make sure it never appears again. (First attempt
    muted the wrong clip — grey kitten 82–83 is actually a cat.) DONE via the manual
    `assets/packs/cat_pack.notes.json` — build.py auto-mutes every "mute" stretch in every video.
    RULE: when the user flags a bad sound, add it to the pack's notes.json "mute" list.
38. RULE: **check every clip's audio before using it** (spectrogram / whisper). Many clips carry
    music, human singing or laughs instead of meows (all listed in notes.json). A cat with no
    usable sound borrows a clean meow via `"sound_ss"` (see notes.json "clean_meows").
39. User: grey kitten (screaming) and orange cat (eyes half closed, human singing) did NOT match
    the story. Re-cast: "You lied to me..." = teary-eyed tabby 169.0+6.0 with `keep_top` 0.55
    (hides the hands holding it); office = excited waving white cat 111.0+7.3; restaurant Me =
    polite standing cream cat 44.0+9.5. Don't use the orange singing cat for sad/heartfelt lines.
40. v8 sent (38 s) — passes avsync + cuts + jumps.
41. User: "the laughing woman is in the white cat clip". ROOT CAUSE: the office white cat borrowed
    its sound from the big-eyed kitten clip (28.53–38.37), which is ENTIRELY a woman's voice
    (laugh 28.7–31.9, talking 34.1–37.9, pitch ~245 Hz). Also used under the kitten and cream cat.
    FIX: whole kitten clip muted in notes.json; "huh" cat (0–6.73, 122 Hz) and bowl cat
    (119.3–124.1, 315 Hz) muted too. Borrow ONLY from notes.json "clean_meows".
42. NEW QA tool `scripts/pitch.py <audio> START END ...` — cat >= ~380 Hz, human ~90–330 Hz.
    RULE: pitch-check every speaking line on the per-scene audio (out/tmp/sceneN.mp4, before the
    song is mixed in — the song itself has a human singer). All lines must read CAT.
43. v9 sent (38 s) — avsync OK, cuts OK, every line pitch-checked CAT.
44. User on v9: "cat voices are the same all throughout... doesn't match the story at all".
    (User was offered "music only" and chose it, then said that was even worse — they DO want
    cat sounds, just fitting ones. Don't propose music-only again.)
    RULES now:
    - **One consistent voice per character**: little Me = grey-kitten meow (80.0), grown-up Me =
      adult white-cat meows (20.0 / 24.7), Mom = her own sad-tabby meow (38.9+), Mom crying =
      her own sobbing clip (104.6+).
    - **Sound must match the line's emotion** (sad → sad meow, happy → excited meow, crying → sobs).
    - **Inner thoughts in *asterisks* are SILENT** (`"sound": false`); the song carries them.
    - A character's 2nd/3rd line continues further into its sound — never the same meow twice
      (build.py does this automatically now).
45. v10 sent (38 s) with #44 applied — avsync, cuts, pitch all OK.

## Decisions / facts so far
- Figma: user only has View seats (one plan is their AvePoint work org) — don't use.
- Floot: free plan full (5 projects) — don't use.
- Network blocks free web image sites (pollinations, unsplash, pexels, wikimedia).
  PyPI + HuggingFace work → generate backgrounds locally with Stable Diffusion Turbo (CPU).
- pytorch.org download index is blocked → torch from PyPI only. Voices use **kokoro-onnx**
  (model from GitHub releases: kokoro-v1.0.onnx + voices-v1.0.bin). Voices: af_sky (kid,
  pitch-shifted up), af_heart (mom).
- ffmpeg + DejaVu/Liberation fonts available in container.
- GitHub access: only repo `perezfiles01-droid/PodC` (an Android app) — branch
  `ccr-acf60340-s3bcq6`. Google Drive is connected.

## Cat pack timestamps (good clips)
| Time | Cat | Use |
|---|---|---|
| 0:00–0:04 | "huh" wide-eyed cat | shock |
| 0:04–0:14 | fluffy orange looking up | wishful |
| 0:16–0:28 | white/grey screaming cat | yelling / demanding kid |
| 0:29–0:37 | big-eyed orange kitten | **Me** (first video) |
| 0:38–0:43 | sad tabby (Doctor cat) | **Mom** (first video) |
| 0:44–0:52 | standing cream cat | |
| 0:56 | kitten in towel | baby |
| 1:20–1:28 | chubby white cat | |
| 1:36 | Scottish fold sad | Son/Dad (like examples) |
| 2:08 | white cat driving | driver / grandpa |
| 2:16–2:32 | orange talking cat | talking parent |
| 2:56 | white cat | |
| 3:04 | brown standing cat | |
| 3:12+ | black end card — skip | |

## Dog pack timestamps (sampled every 8s; exact clip edges need checking)
| Time | Dog |
|---|---|
| 0:00 | white fluffy chihuahua, eyes closed |
| 0:08 | jack russell jumping, happy |
| 0:16 | german shepherd w/ butterfly on nose |
| 0:24 | husky close-up nose (shock/zoom) |
| 0:32 | dog under blanket |
| 0:40 | black lab side-eye |
| 0:48–0:56 | pug walking |
| 1:04 | black/white staffy |
| 1:12 | chihuahua bug-eyed (shock) |
| 1:20 | small white dog grinning |
| 1:28 | corgi smiling |
| 1:36 | chocolate lab |
| 1:44 | corgi puppy eating watermelon |
| 1:52 | beagle grimace |
| 2:00 | tan terrier stare |
| 2:08 | pointer standing |
| 2:16 | husky howling |
| 2:24 | boston terrier sitting |
| 2:32 | dog standing on hind legs |
| 2:40 | small black dog |
| 2:48 | bull terrier in green hood |
| 2:56 | golden retriever w/ headset + tie (office/doctor) |
| 3:04 | white dog sitting howling |
| 3:12+ | black end card — skip |

## Storage
- **GitHub (main home):** repo `perezfiles01-droid/PodC` (private), branch
  `ccr-acf60340-s3bcq6`, folder `pov-meme-kit/` — scripts, packs, song, backgrounds,
  references, approved examples, this handoff. Commit + push every change here.
- Google Drive "Video Edits" folder (https://drive.google.com/drive/folders/1avizMlcGfcpEpT4UJyqPuyKJkVNuQPxG)
  has text copies (handoff, style guide, build.py, story) + the user's own uploads. The Drive
  connector can only move small text files, never MP4/MP3.
- Finished videos: send in chat, with-sound version only.

## Pipeline (all in pov-meme-kit/)
- `scripts/build.py stories/x.json out` — multi-scene builder (see README for the JSON format).
  Chromakey 0x50BB4A:0.10:0.04 + despill, auto-crop per clip, ping-pong loops, labels/speech
  attached to each cat, cat sound while it speaks, song bed → climax at `switch`, fades.
- `scripts/bg.py` — SD-Turbo backgrounds; `scripts/cuts.py` / `scripts/jumps.py` — QA.
- Approved example: `stories/mom-wasnt-hungry.json` → `examples/mom-wasnt-hungry_v10.mp4`.
- Positions on the 1080x1920 canvas for the existing backgrounds: kitchen table top y≈1430
  (x 240–800); night kitchen floor y≈1720; office floor y≈1680; restaurant front table y≈1420
  (x 120–900).
- TODO ideas: props (crutches, food bag, bowl...), dog pack exact cuts (`cuts.py dog_pack.mp4`),
  "Based on true story:" pill ending, vehicles sliding across the scene.

## Superseded (don't use)
- v1 Kokoro TTS voices, compose.sh/overlays.py single-scene pipeline, silent exports.
