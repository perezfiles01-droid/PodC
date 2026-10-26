# Style Guide — TWO styles: SAD and FUNNY

Every video is one or the other. Story JSON: `"style": "sad"` (default) or `"funny"`.

# SAD style (approved; how the Pawspective references are built)

Reference files: `references/videos/ref1_grandpa_tricycle.mp4`, `ref2_dad_promise_wedding.mp4`,
`ref3_delivery_rider.mp4` and `references/screenshots/*.png`.

## Format (all references)
- 720x1280 (we export 1080x1920), 9:16, **55–68 s**, 4–6 scenes that tell one story.
- **Audio:** no spoken voices at all. Each cat's own meme sound (meow, cry, scream) plays while
  that cat "talks", over a background song that runs the whole video and switches to the song's
  emotional climax for the ending.
- **POV box:** white rounded box near the top, black regular sans-serif, "POV:" then the title.
  Stays on screen the whole video. **We add no watermark.**
- **Backgrounds:** photoreal room/place per scene (home, kitchen, hallway, hospital, street,
  office, restaurant, wedding venue). New background = new scene.
- **Characters:** green-screen meme cats/dogs cut out and placed *in* the scene where the story
  puts them (at a table, on the floor, at a door, in a bed, on a vehicle), sized to the room.
- **Labels:** small white text with thin black outline right above each character's head
  ("Me:", "Mom:", "Delivery rider:"). Label appears/disappears with its character.
- **Speech:** small yellow text with thin black outline right above the label. Inner thoughts are
  written in *asterisks*.
- **Emotion:** the same character swaps to a different meme clip when the emotion changes
  (sad → crying → happy tears). Pick clips that match the emotion of the line.
- **Props** pasted on cats in the refs (crutches, bandage, food bag, ramen bowl, bow tie,
  banana suit, phone). Not supported by build.py yet.
- **Ending (last ~10–20 s):** screen dims, white centered closing lines, or a "Based on true
  story:" pill over a real clip / an illustrated art still. Music switches to its climax.

## Story formula
1. Normal moment or conflict (someone impatient, embarrassed, or asking a question)
2. Reveal of a hidden sacrifice or hardship (often a night/secret scene)
3. Time skip or turning point; kindness or a promise kept
4. Emotional payoff ending

## The three references, scene by scene
- **Ref 1, "When Grandpa tries his best just for you" (68 s):** school gate, classmates picked up
  by car → Grandpa arrives on a blue cargo tricycle ("Come on, get in") → city ride, classmates
  laugh "Poor guy" → Me: "I don't feel embarrassed at all" → dim: "It's all because Grandpa
  doesn't have the ability…" → garage: Grandpa upgrades the tricycle ("My child won't be
  embarrassed anymore") → school: tricycle with a TV, classmates "That's cool!" → illustrated
  art ending.
- **Ref 2, "When your Dad kept his promise" (60 s):** home office, Daughter on phone "Okay, Mom"
  → hospital, Dad in bed, "You're still going to attend my wedding" / "I'll just be a
  butterfly…" → wedding venue, "You kept your promise, Dad" + butterfly → real wedding clip with
  a butterfly.
- **Ref 3, "Behind every delay, there's a story" (66 s):** living room, Customer "Why hasn't my
  food arrived yet?" → stairwell, rider with bandaged leg → hallway: "Sorry ma'am, for the
  delay" / "I will only rate you 1 star" / "*Is his leg broken?*" / "I had an accident while
  delivering food" / "Have you eaten yet?" / "I have bread and water" → Customer brings ramen:
  "Sir, this is my way of apologizing for being rude earlier" → "Based on true story:" real clip.

---
# FUNNY style (reference: `references/funny_ref1.mp4`, "POV: Visiting your sister after she got married")

Studied frame by frame (1 fps contact sheets + close-ups) and sound every 0.5 s. Copy this exactly.

## Format
- 720x1280 in the ref (we export 1080x1920), 30 fps, **~45 s**, **3 locations**:
  porch 0–27.9 s → hallway 27.9–31.7 s (the chase) → bedroom 31.7–45.7 s.
- **Hard cuts** between locations. No fades, no dimming, no end card, no closing white lines.
  The video just ends on the punchline.
- **Backgrounds:** bright, sharp, realistic, colorful daylight photos (porch with wooden deck,
  hallway with runner rug, cozy bedroom). **NOT blurred or desaturated**, the opposite of the
  sad style's `bg_soft`.

## Text (all text uses a rounded comic-style bold font, like Comic Neue Bold)
- **POV title:** a separate white rounded "pill" behind EACH line, hugging that line's width
  (TikTok caption style), black text, stacked: "POV:" / line 1 / line 2. Sits around 22–33 % of
  the height and stays for the whole video. The ref has a "@CAT.exe" watermark under it, but we
  **don't copy that** (it's another creator's handle, and our rule is no watermark).
- **Speech:** WHITE text with a thick black outline, always in **"double quotes"**, 1–2 lines,
  right above the speaker's head. On screen only while that line is spoken (~2–3 s).
- **Labels:** go **BELOW** the character (at its feet, slightly to the right) and start with a
  colon: **":Me"**, **":Sister"**. White, black outline. A character lying in the background gets
  a normal label above it ("Brother in-law:").
- Censoring: "k*ll", "tdy" (internet spelling is part of the joke).

## Characters
- Meme cats and dogs, big, often bigger than the sad style. Rough cut-outs are fine and part of
  the meme look: some clips keep a square crop edge.
- **Swap clips constantly:** almost EVERY line gets a new reaction meme for the same character
  (Me: running dog → screaming hamster-cat → shocked orange cat → stressed kitten → angry
  "huh" cat → banana cat running → screaming white cat → tabby). The swap IS the joke.
- **Entrances and movement:** the first character runs in from the side (dog bouncing in from
  the left, 0–2 s). The sister peeks out from behind the door. In the chase, a character runs
  down the hallway, getting bigger as it comes toward the camera.
- **Stickers/props** stuck on characters: an emoji hand ✋ covering the eye, then a purple bruise
  "black eye" sticker, a fruit-basket gift carried by "Me". The injured husband: a black cat
  with bandages and blood marks lying on the bed.

## Sound
- **No background song.** Only the meme clips' own sounds (screams, "huh", meows, dog
  barks/laughs), loud and back-to-back. Every clip swap = a new sound. There are a few real
  silences (16.5 s, 31 s) right before a reaction, for comedic timing.

## Story formula (funny)
1. Cheerful arrival / normal setup (0–5 s): "Sisterrr !!!"
2. Something odd (hand over eye) → small talk "Long time no see. How are you sis?" / "I'm good..."
3. Suspicion → reveal of the "injury" → over-the-top rage: "WHO DID THAT?" / "How DARE him hit my
   sister" / "I'm gonna k*ll him tdy"
4. Chase scene (short, ~4 s): "No brother, wait....."
5. **Twist:** the husband is the injured one, so the sister beat HIM up. "Let me explain brother..."
6. Punchline flip: "Bro!" / "I'm sooo sorry bro" / "Take Care" / "Be carefull from my sister ....."
- Lines are short, quick, every ~2–3 s, no dead air. Twist at ~70 %, punchline in the last 6 s.
