import sys, subprocess, numpy as np
src = sys.argv[1]
w, h = 160, 90
raw = subprocess.run(["ffmpeg", "-v", "error", "-i", src, "-vf", f"scale={w}:{h},format=rgb24", "-f", "rawvideo", "-"],
                     capture_output=True).stdout
f = np.frombuffer(raw, np.uint8).reshape(-1, h, w, 3).astype(int)
green = (f[..., 1] > 150) & (f[..., 0] < 120) & (f[..., 2] < 120)
fg = ~green
cuts = [0.0]
for i in range(1, len(fg)):
    a, b = fg[i - 1], fg[i]
    union = (a | b).sum()
    iou = (a & b).sum() / union if union else 1
    ca = f[i-1][a].mean(0) if a.any() else np.zeros(3)
    cb = f[i][b].mean(0) if b.any() else np.zeros(3)
    if iou < 0.55 or np.abs(ca - cb).max() > 18:
        cuts.append(round(i / 30, 2))
merged = []
for c in cuts:
    if not merged or c - merged[-1] > 0.4:
        merged.append(c)
print(" ".join(f"{c:.2f}" for c in merged))
