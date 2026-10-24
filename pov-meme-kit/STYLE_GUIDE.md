# Style Guide — how the reference videos are built

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
