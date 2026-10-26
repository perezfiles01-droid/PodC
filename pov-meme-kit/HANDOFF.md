# Session Handoff — Cat/Dog POV Meme Videos

> **NEW SESSION — START HERE (do these in order, don't skip):**
> 1. Get the kit: repo `perezfiles01-droid/PodC`, branch **`ccr-acf60340-s3bcq6`**, folder `pov-meme-kit/`.
>    If the repo isn't attached, attach it (add_repo), then
>    `git fetch origin ccr-acf60340-s3bcq6 && git checkout ccr-acf60340-s3bcq6`.
> 2. `cd pov-meme-kit && bash scripts/setup.sh` — installs numpy/pillow AND torch+diffusers (needed: every
>    video gets new backgrounds). Takes a few minutes; it must end with `setup OK`.
> 3. Prove the pipeline — all three must print `DONE` and avsync `OK`:
>    - SAD (old): `python3 scripts/build.py stories/mom-wasnt-hungry.json out` → `DONE 38.0`
>    - SAD + motion: `python3 scripts/build.py stories/stepdad-row-12.json out` → `DONE 45.5`
>    - FUNNY: `python3 scripts/build.py stories/one-more-episode.json out` → `DONE 55`
>    Compare with `examples/mom-wasnt-hungry_v13.mp4`, `stepdad-row-12_v1.mp4`, `one-more-episode_v2.mp4`
>    (verified SAME from a fresh clone on 2026-10-26; `python3 scripts/same.py out/<name>_with-sound.mp4 examples/<file>.mp4` must print SAME).
> 4. Read **TWO STYLES** and **CURRENT RULES** below, STYLE_GUIDE.md (both styles), SFX_LIST.md, and
>    look at references/ (sad refs + `funny_ref1.mp4`). Everything under "History" is background only —
>    where history and the rules disagree, the rules win.
> 5. Templates — copy the structure of an existing story file, don't invent a new format:
>    SAD → `stories/stepdad-row-12.json` (motion, props, flash, grey) ·
>    FUNNY → `stories/one-more-episode.json` (style funny, no music key, swaps with t0/t1 + "swap": true,
>    emoji props, sfx, slide_from / slide_to, scene "transition": "slide", "shake", "dark").
>    (grandma-tiktok_v4 / brother-gps_v1 were made before idle bob + swap cross-fades; rebuilding them now
>    gives the newer look, not SAME.)
> 6. If every shell command fails with "auto mode classifier gave no verdict": that's the session's
>    Auto-mode safety check being down. Ask the user to switch the mode dropdown (next to the message
>    box) to **Accept edits**. Text files can still be saved with the GitHub connector (push_files).

## TWO STYLES (user, 2026-10-26): every video is SAD or FUNNY
- **SAD** = everything in CURRENT RULES below (approved, unchanged).
- **FUNNY** = copy `references/funny_ref1.mp4` exactly; full spec in STYLE_GUIDE.md › FUNNY style
  (sharp bright backgrounds, comic font, white "quoted" speech, ":Me" labels BELOW characters,
  per-line POV pills, constant clip swaps, emoji stickers, NO song, only the meme sounds, hard cuts,
  ends on the punchline). Still human POV, US/CA audience, no watermark, plan-first workflow.
- FUNNY: **sound from frame 1, no silent stretches** (user: 'first 2 seconds has no sound'). Check the
  catalog `sound` field — 'silent' / 'low voice' clips (C15, C19…) need an SFX or a swap. QA: scan the
  audio for gaps >0.5 s; only keep a deliberate comedic pause (≤1.2 s) before a reaction.
- **Smooth clips (user: 'cuts of the cat clips not smooth'):** funny stories (or `"smooth_clips": true`)
  start every clip from its own beginning when it appears, play it forward only (no ping-pong), at REAL
  speed (slowing = repeated frames = stutter), then hold the last frame; the clip's sound is taken from the same
  point so picture and sound match. QA: per-region jump scan — big spikes allowed only at swaps/cuts.
  **Avoid C21** in funny videos: its first 3 s are pop-cat (mouth flashing) and the driving part is a
  rapid montage of cuts — use a calm clip (e.g. C29) and let the car background show the driving.
  Long-running smooth idle clips: C29 (12 s), D22 (8.7 s), C2 (6 s), C47 (5.2 s).
  Known hidden cut: **C48 cuts at 8.4 s** (close face → open mouth) — use `"ss": 8.5` for the scream.
- **FUNNY length (user, 2026-10-26):** no 40+ s rule — any length up to **60 s max**, as long as there is
  no dead air and every joke/element is shown clearly (give each line ~3–4 s to read, let visual gags
  breathe). Priority = making viewers laugh. (SAD keeps its 35–50 s rule.)
- **FUNNY clips must MOVE (user):** pick only clips that are in motion, smooth (no hidden cuts) and have
  sound — measured list: C39 C48 C46 C51 C37 C32 C44 D29 D5 D18 C11 C12 (lots of motion); C53 D26 C52
  C23 D2 C5 C54 C27 C4 D3 C42 C14 C30 C13 C20 C26 D17 (steady motion). Near-static, avoid as main
  clips: C2 C3 C15 C16 C22 C35 C36 C38 C40 C41 C45 C50 D1 D4 D8 D10 D12 D13 D16 D19 D20 D21 D24 D28.
- **FUNNY transitions (user wants smooth) — built into build.py:** idle bob on every funny character
  (automatic; `"no_bob": true` to stop), 0.2 s cross-fade on every `"swap": true` clip change (automatic),
  scene `"transition": "slide"` (whip-slide over the previous scene's last frame), scene `"shake": [[t0, t1]]`
  (screen shake on a scream), actor `"slide_from"` (run/burst in) and `"slide_to"` + `"slide_at"` (exit /
  collapse out of frame).
- **EMOTION CHECK on every line (user caught a 'laughing' cat at a panic line, and a cute kitten as an
  angry Mom):** after building, run contact.py at the middle of EVERY line and ask "does this face read as
  the line's emotion to a stranger?". Gotchas: C48's open-mouth part reads as yawning/laughing (not panic —
  use C37 or C14); C51 reads cute/laughing (not angry — angry parent = C4/C5 or D18); C11 is just a cat
  turning away. Panic = C37 C14 · angry/yelling = C4 C5 D18 · crying = C39 C27 · happy = C46 C32 C44 ·
  shocked = C26 · begging = C52.
- **Sound gotcha (smooth mode):** a clip's sound only plays while it has a line (or `"speak": [[t0, t1]]`),
  and only until the clip runs out — start a short clip AT its line (put another clip before it), and
  give a silent continuation clip an explicit `"speak"`. silence.py catches it.
- When the user asks for a video, ask (or infer from the request) which style; put it in the plan.
- **FUNNY sound effects (user request):** 71 meme SFX in assets/sfx/ (list: SFX_LIST.md, ids S1–S71,
  e.g. S10 Bruh, S12 Sheesh, S15 FBI Open Up, S19 A Few Moments Later, S21 GTA Wasted, S55 Crowd
  Laughing, S58 Record Scratch, S65 Crickets, S67 Ba Dun Tsss, S69 Sorry Bro, S71 smirk-cat laugh).
  Use in a scene: `"sfx": [{"id": "S10", "t": 2.0}]` (optional len, vol). Funny videos = meme clip
  sounds + SFX on the jokes; SAD videos never use SFX.

## CURRENT RULES (authoritative, as of v13)
**Look & format**
- 1080x1920, 30 fps, ~35–45 s, 4–6 scenes, no dead air (lines back-to-back, <=0.3 s gaps, ~3 s each).
- White rounded POV box at top for the whole video; **no watermark** (`"handle": ""`).
- Photoreal background per scene (assets/backgrounds/ or new ones via scripts/bg.py).
- **Always generate NEW backgrounds for every video — never reuse an existing one, and every scene
  gets its own freshly generated background even when two scenes are in the same place** (user
  rule). Save them as assets/backgrounds/<story>/sceneN.png (scripts/bg.py with a new seed each).
- Backgrounds must stay calm so they never overpower the characters: new stories set
  **`"bg_soft": true`** (light blur, less color, slightly dimmer) and bg.py prompts should be
  simple/plain ("simple plain …, minimal decor, soft natural light, eye-level").
- Characters = green-screen clips from the catalog, placed where the plot puts them (floor, table,
  bed…), **`"actor_scale": 1.3`** (bigger), never overlapping each other or each other's text.
- New stories set **`"tight_bottom": true`** (crops at the character's feet — portrait clips like C30
  otherwise float). Older stories (mom-wasnt-hungry) leave it off so they rebuild identically.
- Label (white) right above each character's head, its speech (yellow) right above the label; both
  appear/disappear with that character. Thoughts in *asterisks*.
- An actor can carry `"t0"`/`"t1"` (scene seconds) to appear only part of a scene — ALWAYS set them
  on clip swaps, or both clips show the whole scene stacked on each other.
- Clip emotion must match the line (sad → sad/crying clip, happy → happy clip). A character may
  swap clips mid-scene for an emotion change (same x/y, consecutive t0/t1).
- **Motion (new stories set `"motion": true`, user asked for 'presenting a story' feel):** slow
  zoom/pan on every background (scene `"cam": "in"|"out"|"left"|"right"`), characters slide up in
  (`"enter"`, `"swap": true` on a mid-scene emotion swap so it doesn't slide), speech pops in
  (`"shake": true` on angry lines), story props pop in (scene `"props"`: cut-out PNGs in
  assets/props/<story>/), 0.3 s fade into each scene, soft white flash on the twist (`"flash_at"`),
  `"grey": true` for funeral/memory scenes. Hook scene's first frame stays static + fully visible.
- Ending: dim + white closing lines; song switches to its climax.

**Stories**
- EVERY story is a **human POV** (family, school, work, sacrifice…). Cats and dogs are only the
  ACTORS playing people — never stories about cats/dogs/pets/shelters. (User rule.)

- Stories must NOT be generic. Each needs: one **specific, concrete detail** planted early (an object,
  a habit, a phrase) that pays off at the end; a **real plot twist** the viewer doesn't see coming
  (a hidden sacrifice, secret, misunderstanding reversed); the reveal shown, not explained; a final
  line that hits hard. Aim for goosebumps. Test: if the ending can be guessed from the title, rewrite.

**Audience: US & Canada** (user's niche target)
- American English and US/Canadian settings and culture: high school, cafeteria, prom, college
  tuition, night shifts, diners, Thanksgiving/Christmas, hockey/football/soccer, snow days, etc.
  Avoid region-specific words (e.g. "Kuya") unless asked.
- BUT stay **universal**: the emotion (family sacrifice, love, regret, kindness) must land for any
  viewer anywhere. Use simple English, no slang or US-only references that need explaining,
  settings everyone recognizes (home, school, hospital, work). US/CA flavor = the setting and
  wording; the heart of the story = universal.
- Captions in natural American voice; hashtags that work in NA (#pov #storytime #catsoftiktok-
  style tags, #momlove #dadlove #fyp #reels #emotional, plus 1–2 niche tags).
- When giving posting tips, use North American times (about 7–10 pm Eastern / 4–7 pm Pacific,
  weekday evenings + weekend mornings).

**Algorithm (FB Reels / IG / TikTok, US-CA)** — new stories set `"hook_first_frame": true`
- No fade-in: the very first frame already shows the POV title + characters (that frame is the
  preview and the 1-second hook decides if people keep watching).
- Strong conflict in the first 3 s (a hurtful line or question), twist in the last third.
- 35–50 s total, no dead air (completion + rewatches are the top ranking signals).
- End card adds a yellow share line ("Share this with someone who needs to see it ❤️") — shares
  and comments push reach more than likes. Captions end with a question or "tag someone".
- Keep all text out of the bottom ~18% and the right edge (platform buttons cover it).
- Post as a Reel, 9:16, music added in-app when possible; 1–2 posts/day at 7–10 pm ET; reply to
  the first comments within an hour.

**Sound**
- Each character plays **its own clip's sound** while it talks (the v7 behaviour), song underneath
  (assets/music/chasing_cars_hale_cover.mp3: bed from 0:00, ending from 2:48, `switch` = ending start).
- Do NOT use: Kokoro/TTS voices, AI-generated meows, borrowed meows, music-only. (All rejected.)
- Ask the user before changing anything about the sound.

**Clips**
- Use catalog ids: actor `"clip": "C7"` (cats C1–C46, dogs D1–D29; mix cats and dogs as fits).
  build.py picks a safe range (skips the 1 s zoom-in intro, stays inside the clip → no flicker).
- **C30 (German painter kitten) always plays its COMPLETE sound** (whole 4.9 s clip, real speed) —
  build.py does it automatically (FULL_SOUND set; or `"full_sound": true` on any actor). Give its
  line ~5 s and start the next speaker after it.
- If a clip looks twitchy/fast, add `"speed": 0.7`. Short clips (<3 s usable) are slowed automatically
  (build.py) so their ping-pong loop never looks fast-forwarded; intro skip = min(1 s, 15% of clip).
- Every scene is rendered to exactly dur×30 frames (no picture/sound drift between scenes).
- C39 = banana cat crying with hands up (36.2–40.9); the spinning banana is C46 (41.0–46.37).
- Catalog: CLIP_CATALOG.pdf (pictures) + assets/packs/catalog.json. New upload → copy to
  assets/packs/, add a PACKS entry in scripts/catalog_segments.py + description in
  scripts/make_catalog.py, run `python3 scripts/make_catalog.py`, check pictures aren't blank.

**Workflow with the user**
1. New video request → decide **SAD or FUNNY** (ask if unclear). Story ideas: SAD = 4 options with hook /
   planted detail / twist / last line; FUNNY = 4 options with setup / odd / overreaction / twist /
   punchline. Then FIRST reply with the full plan as a table: Scene | Time | Place | Who = clip | Lines |
   (SAD: Motion & elements · FUNNY: SFX + Stickers). Wait for the user's OK.
2. Backgrounds: `scripts/bg_batch.py plan.json` (new image per scene, landscape-crop, look at them; redo
   tiled/top-down ones with another seed). Props: emoji props, `scripts/prop_cutout.py`, or a PIL drawing.
3. Build → QA, ALL of these, every time:
   - `scripts/avsync.py out.mp4` → OK
   - `scripts/cuts.py out.mp4` → only the planned scene cuts (+ a planned flash)
   - `scripts/jumps.py out.mp4` and `scripts/jumps_local.py out.mp4 stories/x.json` → spikes only at
     planned moments; an isolated spike inside a clip = hidden cut → move that actor's "ss" past it
   - FUNNY also: `scripts/silence.py out.mp4` → OK (sound from frame 1; check the catalog "sound"
     field — silent / low-voice clips need an SFX or a swap)
   - `scripts/contact.py out.mp4 sheet.png <times>` and LOOK: text overlaps, clipped/floating cats,
     missing glyphs (Comic Neue has no ● ▶ → emoji props), oversized clips (C40 h≈200).
4. Copy to `examples/<name>_vN.mp4` (+ `_clips.md`), drop the previous version of that video.
   Send ONLY the with-sound mp4 + the scene table (Scene | Time | Place | Who = clip, plus SFX for funny)
   + the updated CLIP_CATALOG.pdf + this HANDOFF.md.
   ALSO always include ready-to-copy **Facebook captions and hashtags in SEPARATE code blocks**
   (one block per caption, one block for hashtags — user asked to keep them apart) (2 caption options,
   5–8 hashtags adapted to the story; SAD e.g. `#pov #storytime #momlove #emotional #catsoftiktok
   #fbreels`, FUNNY e.g. `#pov #funny #catmemes #relatable #catsoftiktok #fbreels #fyp`). Caption = hook
   + a question or "tag someone". Posting tip: 7–10 pm Eastern (4–7 pm Pacific); funny also 12–1 pm ET.
5. Swap requests ("scene 3 C23 → D12" / "S54 → S21"): set `"clip"` (or sfx `"id"`), delete that actor's
   `"ss"`/`"len"`/`"speed"`, keep the rest, check placement, rebuild, QA, send as above.
6. After every change: update this file (rules if a rule changed + a History line), commit, push to the
   same branch. Never leave work uncommitted at the end of a turn (a stop hook checks).

**Files to give the user on request (private repo — sign in as perezfiles01-droid)**
- Clip list PDF: https://github.com/perezfiles01-droid/PodC/raw/ccr-acf60340-s3bcq6/pov-meme-kit/CLIP_CATALOG.pdf
- This handoff: https://github.com/perezfiles01-droid/PodC/blob/ccr-acf60340-s3bcq6/pov-meme-kit/HANDOFF.md
- Videos: examples/mom-wasnt-hungry_v13.mp4, mom-missed-my-show_v2.mp4, dad-sold-his-car_v1.mp4,
  old-shelter-dog_v1.mp4, brother-skipped-college_v1.mp4, old-shoes_v1.mp4, grandmas-birthday_v2.mp4, class-clown_v2.mp4, dad-voicemails_v1.mp4,
  cafeteria-leftovers_v1.mp4, stepdad-row-12_v1.mp4, grandma-tiktok_v4.mp4, brother-gps_v1.mp4,
  one-more-episode_v2.mp4 (same raw/ link pattern)
- Whole kit: https://github.com/perezfiles01-droid/PodC/tree/ccr-acf60340-s3bcq6/pov-meme-kit

---
# History (background — CURRENT RULES above win)
## Download links (give these to the user whenever they ask for "the clip list" / "the handoff")
Repo is private: the user must be signed in to GitHub as perezfiles01-droid.
- Clip list (PDF, with pictures) — user wants PDF, not Word: https://github.com/perezfiles01-droid/PodC/raw/ccr-acf60340-s3bcq6/pov-meme-kit/CLIP_CATALOG.pdf
  (Word source kept as CLIP_CATALOG.docx; make_catalog.py writes both)
- This handoff: https://github.com/perezfiles01-droid/PodC/blob/ccr-acf60340-s3bcq6/pov-meme-kit/HANDOFF.md
- Whole kit folder: https://github.com/perezfiles01-droid/PodC/tree/ccr-acf60340-s3bcq6/pov-meme-kit
If the catalog changes (new packs/clips), run `python3 scripts/make_catalog.py`, commit, push — the
links stay the same.

_Last updated: 2026-10-24 (v13, catalog C1–C54 + D1–D29). Paste this whole file into a new session to resume._

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
    scene cuts) and `jumps.py out.mp4` (spikes only at fade-in + scene cuts). Avoid repeating past problems.
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
37. **USER CHOSE v7 AS THE APPROVED VERSION ("rollback to v7, that is currently what I want").**
    The kit was rolled back to the v7 state (commit f958fdd): v7 story, v7 build.py, v7 example.
    Later experiments v8–v11 were REJECTED by the user and removed from the kit; they're still in
    git history (commits 1a0fe6e … 366698c) if ever needed:
    - v8/v9: re-cast clips + auto-muting "bad" clip audio + borrowed meows → "cat voices the same
      throughout, doesn't match the story".
    - v10: one voice per character, silent thoughts → still not wanted.
    - v11: AI-generated cat voices (AudioLDM) + room ambience → user rolled back to v7.
    RULE: start new videos from the v7 approach (each clip plays its OWN meme sound while the cat
    speaks, song underneath). Do not re-introduce v8–v11 sound changes unless the user asks.
    Known v7 caveat the user raised later: some clips' own audio contains a woman laughing /
    human voices (big-eyed kitten clip 28.53–38.37). Ask the user before changing sound again.
38. RULE (user): **use the voices of the clips themselves** — each cat/dog plays its OWN clip audio
    while it talks (this is the v7 behaviour; keep it).
39. **CLIP CATALOG** — `CLIP_CATALOG.docx` (numbered list with a picture of every clip) and
    `assets/packs/catalog.json`. Ids: C1–C29 = cat pack, D1–D29 = dog pack. Each has time range,
    description, "good for" mood and its sound type (animal / low voice-check / silent).
    Regenerate with `python3 scripts/make_catalog.py`.
    In a story an actor can just say `"clip": "C7"` — build.py picks a safe range (1 s after the
    clip starts, 0.2 s before it ends) automatically.
40. RULE (user): **when the user asks for a new video, FIRST reply with the plan listing every clip
    number you will use per character and scene** (e.g. "Scene 1 kitchen: Me = C6, Mom = C7"),
    mixing cat AND dog clips as fits the story. Wait for the user's OK, then build.
41. RULE: **with EVERY video sent, also list the clips per scene** (build.py prints it and writes
    out/<name>_clips.txt), e.g. "Scene 3 (office): Me (10 years later) = C23".
42. RULE: **swaps by number** — the user will say e.g. "replace scene 3 with C5 instead of C23".
    Do it by setting that actor's `"clip": "C5"` and DELETING its `"ss"`/`"len"` (so the safe range
    of the new clip is used), keep everything else (position, size, label, lines), rebuild, run QA,
    send the video + the updated clip list. Each actor has "clip" + "name" in the story JSON.
    Current approved v7 clips: Scene 1 kitchen: Mom = C7, Me = C6 | Scene 2 night: Mom = C20,
    Me (peeking) = C13, Me (crying) = C14 | Scene 3 office: Me (10 years later) = C23 |
    Scene 4 restaurant: Mom (sad) = C7, Mom (crying) = C18, Me = C23 | Scene 5 ending: C18, C14.
43. Catalog pictures were blank at first (bad ffmpeg overlay); fixed — make_catalog.py now
    flattens the cut-out on white with PIL and verifies. Check pictures before sending the doc.
44. RULE: send the clip list as **PDF** (CLIP_CATALOG.pdf), not Word.
45. RULE: **with every final video, send a TABLE**: Scene | Time in video | Place | Who = clip
    (with each character's on-screen time). build.py prints it and writes out/<name>_clips.md.
    The user watches, then says what to change by scene/time/clip number.
46. New clips added (all cats, numbering continues): C30 German painter kitten
    (cat_german_painter.mp4), C31 orange cat screaming/laughing (cat_screaming_orange.mp4,
    0.6–3.5 s), C32 'happy happy happy' dancing cat (cat_happy_happy.mp4), C33–C43 banana-suit
    cat pack (cat_banana_pack.mp4; 0–2.07 s skipped because of a big Chinese title; small corner
    watermark is auto-ignored). Packs list lives in scripts/catalog_segments.py (PACKS);
    descriptions in scripts/make_catalog.py. Adding a new upload = copy to assets/packs/, add a
    PACKS entry + descriptions, run make_catalog.py, check pictures, commit, push.
47. build.py now: samples each video's own green + frame size (portrait/1080p ok); bright pure-green
    screens use chromakey WITHOUT despill (despill turned the yellow banana suit orange);
    cut-outs keep only the main block of rows/columns so corner watermarks never show.
48. v12 (user swaps): scene 2 "Me (peeking)" C13 → C26, scene 3 "Me (10 years later)" C23 → D12.
    Note: after a swap, CHECK placement — C26 inherited the edge-peeker's spot and floated; moved
    to the floor (x 230, y 1720, h 420). Story file now = v12.
49. New clip C44 = 'yapapa' orange cat singing (cat_yapapa.mp4, 0–8.0 s, sound = the meme song,
    ~451 Hz). Catalog now C1–C44 cats + D1–D29 dogs (73 clips, 29-page PDF).
50. RULE: **right after every generated video, also send the user the updated CLIP_CATALOG.pdf and
    this HANDOFF.md** (after committing + pushing them), together with the scene/time/clip table.
51. User: "make the clips a bit larger, they look small on my end". NEW story setting
    `"actor_scale": 1.3` multiplies every character's height (feet stay put, labels/speech move
    up). RULE: use actor_scale 1.3 for all new videos. When characters get bigger, re-check that
    they and their text don't overlap (restaurant cats moved apart to x 270 / 820).
52. v13 sent = v12 + actor_scale 1.3 + restaurant spacing. avsync/cuts OK.
53. New clip C45 = cream ragdoll-type cat standing, chewing/talking, calm stare
    (cat_ragdoll_chewing.mp4, 0–5.2 s; empty after). Catalog now C1–C45 + D1–D29 (74 clips).

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
- Approved example: `stories/mom-wasnt-hungry.json` → `examples/mom-wasnt-hungry_v7.mp4`.
- Positions on the 1080x1920 canvas for the existing backgrounds: kitchen table top y≈1430
  (x 240–800); night kitchen floor y≈1720; office floor y≈1680; restaurant front table y≈1420
  (x 120–900).
- TODO ideas: props (crutches, food bag, bowl...), dog pack exact cuts (`cuts.py dog_pack.mp4`),
  "Based on true story:" pill ending, vehicles sliding across the scene.

## Superseded (don't use)
- v1 Kokoro TTS voices, compose.sh/overlays.py single-scene pipeline, silent exports.
54. User picked clips C12 C14 C18 C19 C22 C26 C30 C31 C38 C39 C44 D1 and asked for story options;
    chose **Option B "When Mom couldn't come to my school performance"** → stories/mom-missed-my-show.json,
    examples/mom-missed-my-show_v1.mp4 (47 s, 7 scenes; table in examples/*_clips.md). New
    backgrounds: stage.png (auditorium seen from the stage), backstage.png, school_hall.png.
    Added `tight_bottom` (see CURRENT RULES). Known weak spot: C12 (brother) is a tiny dark cat in
    its source — offer a swap if the user dislikes it.
55. User: "parts are fast-forwarded and not properly started" (mom-missed-my-show v1). Causes found:
    (a) short clips (C31 1.7 s, C14 2.6 s usable) ping-ponged 2–4× per scene; (b) scenes were 1
    frame short (209 frames vs 7.00 s audio) → picture drifted ahead of sound scene after scene;
    (c) C39's second half was a spinning banana with cuts every 0.5 s. Fixes in CURRENT RULES.
    v13 example re-exported with the frame fix (v13 story pins its clip ranges so it still
    matches). mom-missed-my-show_v2 sent: every scene exact frames, avsync/cuts/jumps clean.
56. User asked for new ideas mixing CAT AND DOG clips; picked two: "Dad sold his car to pay for my
    school" (stories/dad-sold-his-car.json, 48 s) and "Nobody wanted the old shelter dog"
    (stories/old-shelter-dog.json, 46 s). New backgrounds: living_room, night_street, bus_stop,
    shelter. bg.py now takes an optional seed (3rd arg) — SD-Turbo sometimes makes tiled/top-down
    rooms; try another seed. Both videos: avsync OK, no in-scene jumps.
57. Two more mixed cat+dog videos: "Grandma's last birthday wish" (stories/grandmas-birthday.json,
    46 s) and "The class clown was hiding his pain" (stories/class-clown.json, 47 s). New
    backgrounds: classroom, bedroom. C32 (happy-happy cat) is small inside its frame — use h ~620.
58. User: backgrounds too overpowering — make them simple/normal but realistic. Added `bg_soft`
    (gblur 6, saturation 0.55, brightness -0.07, contrast 0.88). Grandma + clown re-sent as v2.
    Older videos unchanged (option off by default).
59. User rule: stories must be human POVs only (see CURRENT RULES › Stories). Built "My big brother
    skipped college so I could go" + "Bullied for my old shoes" (bg_soft on). Found the v7
    rollback had removed `keep_top` from build.py (hands showed under C27) — restored; dad-sold-
    his-car re-exported with it.
60. User rule: no recycled backgrounds — new ones for every video and every scene (see CURRENT RULES).
61. User: C30's sound always got cut — now always played in full (see CURRENT RULES). Brother video re-sent (49 s).
62. User: stories too generic — must give goosebumps with a heart-moving plot twist (rule above).
63. User: target US & Canada viewers (rules above). Building lunchbox-notes, dad-never-came, grandpa-did-you-eat with 21 new backgrounds (scripts/bg_batch.py).
64. Sent moms-lunchbox-notes, dad-never-came, grandpa-did-you-eat (48 s each; new backgrounds per scene, hook_first_frame, share CTA). Fixed a build.py syntax slip in the fade code.
    Lesson: never chain background jobs with `pgrep -f` waits — they matched each other and deadlocked.
65. Sent extra-fries_v1 (48 s, diner twist: tip jar 'For the kid's fries').
66. Sent dad-voicemails_v1 (Dad recorded 365 voicemails before dying; 'Drive safe, kiddo') and
    cafeteria-leftovers_v1 (the 'weird kid' took leftovers to feed ME). 40.5 s each, 10 new backgrounds.
    Lessons: SD-Turbo tall rooms often tile — for big halls render 1024x576 and crop the centre;
    torch/diffusers aren't in setup.sh's light install (pip install torch diffusers transformers accelerate);
    bg.py arg order = path, prompt, seed. C13 (edge-peeker) must touch the screen edge (x≈105).
    C31 needs speed 0.7 (short loop).
67. stepdad-row-12_v1 (45.5 s): first video with motion (see CURRENT RULES). Props: ROW 12 sign drawn in
    PIL, coffee cup drawn in PIL (SD cup didn't cut out cleanly), framed photo = SD on white + flood-fill
    cut-out. Session note: when the shell's Auto-mode safety check is down, ask the user to switch the
    mode dropdown to "Accept edits"; GitHub connector (push_files) can still save text files.
68. User sent a FUNNY reference (funny_ref1.mp4) → second style added (see TWO STYLES + STYLE_GUIDE).
69. Added 71 meme sound effects (assets/sfx, build.py `sfx`) for FUNNY videos, and cat_pack_4.mp4 → C47–C54 (catalog now C1–C54 + D1–D29).
70. First FUNNY video: grandma-tiktok_v1 (45 s, 3 locations, 11 SFX). build.py funny switch = story
    `"style": "funny"` + NO "music" key: Comic Neue Bold (assets/fonts), POV pills, white quoted speech
    above head, ":Me" labels below (lx), emoji stickers (props `"emoji": "📱"`, Noto Color Emoji),
    actor `"slide_from": [x, y]` + `"slide_dur"` (e.g. falling down stairs), no song, hard ending.
    Comic Neue has no ● or ▶ glyphs → use emoji props (🔴 ▶️). C40 (collapsed banana) needs h ~200.
71. grandma-tiktok_v2: opening was silent 0–2.7 s (C15 silent) → Grandma opens with C3 (meowing), +S38 at 4.7 s, +S42 at the end.
72. grandma-tiktok_v4: smooth clips (start at own beginning, forward only, no ping-pong) + C48 hidden-cut fix.
73. Handoff audit: setup installs torch, 3 proof builds (sad/motion/funny) verified SAME from a fresh clone, QA scripts silence.py / jumps_local.py / contact.py / same.py, bg_batch landscape-crop, prop_cutout.py, story templates, Auto-mode outage note.
74. brother-gps_v1 (funny, 45 s): C21 replaced by C29 (pop-cat flicker / montage), prop `slide_from` (⛽ 🐄 drive past), no auto slow-down in funny mode (stutter) → grandma-tiktok re-exported as v4.
75. User: funny videos up to 60 s (clarity over length), moving clips only, smooth swaps + scene transitions.
76. one-more-episode_v1 (funny, 55 s): first with moving-only clips, idle bob, swap cross-fades, slide transitions, shake, slide_to collapse. Funny proof build switched to it.
77. one-more-episode_v2: emotion fixes — 7 AM panic C48→C37, angry Mom C51→C4, cliffhanger C11→C26.
