"""Compare two videos frame-by-frame + audio (decoded), e.g. a rebuild against examples/.
usage: python3 scripts/same.py a.mp4 b.mp4   → SAME, or the max difference"""
import sys, subprocess, numpy as np
def dec(p, args):
    return np.frombuffer(subprocess.run(["ffmpeg", "-v", "error", "-i", p, *args, "-"], capture_output=True).stdout, np.uint8)
v = [dec(p, ["-vf", "scale=180:320,format=gray", "-f", "rawvideo"]).astype(int) for p in sys.argv[1:3]]
a = [dec(p, ["-ac", "1", "-ar", "8000", "-f", "s16le"]) for p in sys.argv[1:3]]
if len(v[0]) != len(v[1]):
    print("DIFFERENT length", len(v[0]), len(v[1])); sys.exit(1)
dv = np.abs(v[0] - v[1]).mean()
da = 0 if len(a[0]) == len(a[1]) and (a[0] == a[1]).all() else 1
print("SAME" if dv < 0.5 and not da else f"DIFFERENT (video mean diff {dv:.2f}, audio differs {bool(da)})")
