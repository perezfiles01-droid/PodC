"""QA: per-region jump scan (catches a small cat clip jumping, which whole-frame jumps.py misses).
Big spikes are only allowed at planned moments (scene cuts, actor t0/t1 swaps, props/subs on/off).
usage: python3 scripts/jumps_local.py out.mp4 stories/<story>.json"""
import sys, json, subprocess, numpy as np
raw = subprocess.run(["ffmpeg", "-v", "error", "-i", sys.argv[1], "-vf", "scale=90:160,format=gray", "-f", "rawvideo", "-"],
                     capture_output=True).stdout
f = np.frombuffer(raw, np.uint8).reshape(-1, 160, 90).astype(float)
m = np.abs(np.diff(f, axis=0)).reshape(-1, 16, 10, 10, 9).mean((2, 4)).max((1, 2))
st, planned, T = json.load(open(sys.argv[2])), set(), 0.0
for sc in st["scenes"]:
    planned.add(round(T, 2))
    for a in sc["actors"] + sc.get("props", []) + sc.get("subs", []):
        planned.add(round(T + a.get("t0", 0), 2)); planned.add(round(T + a.get("t1", sc["dur"]), 2))
    T += sc["dur"]
bad = [(round((i + 1) / 30, 2), round(v)) for i, v in enumerate(m)
       if v > 35 and not any(abs((i + 1) / 30 - p) < 0.05 for p in planned)]
print("unplanned big jumps (>35):", bad)
print("check these frames: a run of many = fast motion in the clip (ok); a single isolated spike = a hidden cut "
      "inside the clip → move that actor's \"ss\" past it")
