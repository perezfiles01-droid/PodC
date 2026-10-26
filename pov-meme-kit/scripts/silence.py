"""QA for FUNNY videos: list silent stretches >= 0.5 s (sound must start at frame 1; only deliberate
comedic pauses <= 1.2 s right before a reaction are allowed).  usage: python3 scripts/silence.py out.mp4"""
import sys, subprocess, numpy as np
raw = subprocess.run(["ffmpeg", "-v", "error", "-i", sys.argv[1], "-ac", "1", "-ar", "8000", "-f", "s16le", "-"],
                     capture_output=True).stdout
a = np.frombuffer(raw, np.int16).astype(float)
q = [i * 0.25 for i in range(0, len(a) // 2000) if 20 * np.log10(np.sqrt((a[i * 2000:(i + 1) * 2000] ** 2).mean()) + 1) < 30]
runs, s, p = [], None, None
for t in q:
    if s is None or t - p > 0.26:
        if s is not None:
            runs.append((s, p + 0.25))
        s = t
    p = t
if s is not None:
    runs.append((s, p + 0.25))
runs = [r for r in runs if r[1] - r[0] >= 0.5]
print("silent stretches >=0.5s:", runs)
print("OK" if not any(r[0] < 0.1 or r[1] - r[0] > 1.25 for r in runs) else "FIX: silent opening or gap > 1.2 s")
