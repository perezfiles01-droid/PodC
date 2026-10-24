"""QA: is a stretch of a pack's audio a cat or a human? Prints median pitch of voiced frames.
Cats (meows) are usually 400-1000 Hz; human talk/laughs/singing ~90-330 Hz.
usage: python3 scripts/pitch.py assets/packs/cat_pack.mp4 START END [START END ...]"""
import subprocess, sys, numpy as np
src, rng = sys.argv[1], list(map(float, sys.argv[2:]))
for s, e in zip(rng[::2], rng[1::2]):
    raw = subprocess.run(["ffmpeg", "-v", "error", "-ss", str(s), "-to", str(e), "-i", src, "-vn", "-ac", "1",
                          "-ar", "16000", "-f", "f32le", "-"], capture_output=True).stdout
    x = np.frombuffer(raw, np.float32)
    f0s = []
    for i in range(0, len(x) - 1024, 512):
        fr = x[i:i + 1024] * np.hanning(1024)
        if np.sqrt((fr ** 2).mean()) < 0.01:
            continue
        ac = np.correlate(fr, fr, "full")[1023:]
        lo, hi = 16000 // 1200, 16000 // 80
        lag = lo + np.argmax(ac[lo:hi])
        if ac[lag] / ac[0] > 0.5:
            f0s.append(16000 / lag)
    if f0s:
        m = np.median(f0s)
        print(f"{s:7.2f}-{e:7.2f}  voiced frames {len(f0s):3d}  median pitch {m:5.0f} Hz  ->", "CAT" if m >= 380 else "HUMAN?")
    else:
        print(f"{s:7.2f}-{e:7.2f}  no clear voice")
