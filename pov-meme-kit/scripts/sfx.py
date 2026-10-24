"""Generate cat voices + scene ambience for a story with AudioLDM (free, CPU, ~10 s per take).

usage (from pov-meme-kit/): python3 scripts/sfx.py stories/<story>.json
Reads, in the story JSON:
  "voices": {"kid": "a tiny kitten meowing", ...}          one base prompt per character
  actor "voice": "kid"                                      which voice the actor uses
  say "emotion": "sad, slow, mournful"                      added to the voice prompt
  scene "ambience": "quiet kitchen at night, fridge hum"    room sound for the scene
Writes assets/sfx/<story>/*.wav and stores the chosen file in the story as say["sfx"] and
scene["ambience_file"]. Takes are scored with the cat-pitch check (scripts/pitch.py logic):
a take must read as a cat (>= 380 Hz) and the most-voiced take wins.
Thought lines (say "sound": false) get no voice.
"""
import json, os, sys
import numpy as np, soundfile as sf, torch
from diffusers import AudioLDMPipeline

NEG = "music, human voice, speech, singing, laughter, low quality, noise"
SR = 16000


def cat_score(w):
    """(is_cat, voiced_frames): autocorrelation pitch per 64 ms frame."""
    f0s = []
    for i in range(0, len(w) - 1024, 512):
        fr = w[i:i + 1024] * np.hanning(1024)
        if np.sqrt((fr ** 2).mean()) < 0.01:
            continue
        ac = np.correlate(fr, fr, "full")[1023:]
        lag = 13 + np.argmax(ac[13:200])
        if ac[lag] / ac[0] > 0.5:
            f0s.append(SR / lag)
    return (bool(f0s) and np.median(f0s) >= 380), len(f0s)


def main():
    path = sys.argv[1]
    story = json.load(open(path))
    out = f"assets/sfx/{story['name'].split('_v')[0]}"
    os.makedirs(out, exist_ok=True)
    pipe = AudioLDMPipeline.from_pretrained("cvssp/audioldm-s-full-v2", torch_dtype=torch.float32)

    def gen(prompt, seconds, n, seed, neg=NEG):
        return pipe(prompt, negative_prompt=neg, num_inference_steps=50, audio_length_in_s=seconds,
                    num_waveforms_per_prompt=n, generator=torch.Generator().manual_seed(seed)).audios

    for si, sc in enumerate(story["scenes"]):
        if sc.get("ambience"):
            f = f"{out}/scene{si}_ambience.wav"
            if not os.path.exists(f):
                sf.write(f, gen(sc["ambience"] + ", ambient room tone, no music", min(sc["dur"], 10), 1, 7,
                                 neg="music, speech, singing, animals")[0], SR)
            sc["ambience_file"] = f
        for ai, a in enumerate(sc["actors"]):
            base = story.get("voices", {}).get(a.get("voice", ""), "")
            for k, say in enumerate(a.get("say", [])):
                if not base or say.get("sound", True) is False:
                    continue
                f = f"{out}/scene{si}_{a.get('voice')}_{k}.wav"
                if not os.path.exists(f):
                    dur = min(say["t1"] - say["t0"], 4.5)
                    prompt = f"{base}, {say.get('emotion', '')}, close up, clear, single cat"
                    takes = []
                    for rnd in range(4):  # retry with new seeds until a clearly voiced cat take exists
                        takes += list(gen(prompt, dur, 4, 100 + si * 10 + k + 1000 * rnd))
                        scored = sorted(((cat_score(w), i) for i, w in enumerate(takes)), reverse=True)
                        (is_cat, voiced), best = scored[0]
                        if is_cat and voiced >= 15:
                            break
                    print(f"scene {si} {a.get('voice')} line {k}: best take {best} cat={is_cat} voiced={voiced}", flush=True)
                    sf.write(f, takes[best], SR)
                say["sfx"] = f
    json.dump(story, open(path, "w"), indent=1)


main()
