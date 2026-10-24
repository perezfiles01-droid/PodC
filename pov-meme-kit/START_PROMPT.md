# Start prompt — paste this into a new Claude Code chat

Copy everything inside the box below into a new chat (Claude Code on the web, with the
`perezfiles01-droid/PodC` repo attached).

```
I make Pawspective-style "POV" cat/dog meme videos. Everything you need is in my repo
perezfiles01-droid/PodC, branch ccr-acf60340-s3bcq6, folder pov-meme-kit/.

1. Check out that branch and read, in this order:
   pov-meme-kit/README.md, pov-meme-kit/HANDOFF.md, pov-meme-kit/STYLE_GUIDE.md.
   Follow every RULE in HANDOFF.md exactly — they are my standing instructions.
2. Look at pov-meme-kit/references/ (3 reference videos + 4 screenshots) and
   pov-meme-kit/examples/mom-wasnt-hungry_v7.mp4 (the approved result) so you know the look.
3. Run pov-meme-kit/scripts/setup.sh, then rebuild the example to prove the pipeline works:
   cd pov-meme-kit && python3 scripts/build.py stories/mom-wasnt-hungry.json out
4. Then make me a NEW video: [WRITE YOUR STORY IDEA HERE, or say "you pick a sad story"].
   First show me the plan: every scene and the clip numbers you'll use for each character
   (C# = cat, D# = dog, from pov-meme-kit/CLIP_CATALOG.pdf). After I say OK, build it, run the QA checks
   (scripts/avsync.py, scripts/cuts.py and scripts/jumps.py), and send me ONLY the version with sound.
5. Keep HANDOFF.md updated with anything new I ask for, and commit + push the kit changes
   (new stories, backgrounds, rules) to the same branch so the next chat can continue.
```
