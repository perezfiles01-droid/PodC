"""Cut a prop out of an SD image rendered on a plain background (flood fill from the edges).
usage: python3 scripts/prop_cutout.py in.png out.png [threshold=45] [x0 y0 x1 y1 crop]
Look at the result on a coloured background; if the object is too close to the background colour,
draw a simple sticker with PIL instead (see the coffee cup in assets/props/stepdad-row-12/), or use an
emoji prop ({"emoji": "📱"}) in the story."""
import sys
import numpy as np
from PIL import Image, ImageDraw, ImageFilter
im = Image.open(sys.argv[1]).convert("RGB")
thr = int(sys.argv[3]) if len(sys.argv) > 3 else 45
if len(sys.argv) > 7:
    im = im.crop(tuple(int(v) for v in sys.argv[4:8]))
w, h = im.size
m = im.copy()
for seed in [(0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1), (w // 2, 0), (0, h // 2), (w - 1, h // 2)]:
    ImageDraw.floodfill(m, seed, (255, 0, 255), thresh=thr)
a = np.array(m)
bg = (a[..., 0] == 255) & (a[..., 1] == 0) & (a[..., 2] == 255)
al = Image.fromarray(((~bg) * 255).astype(np.uint8)).filter(ImageFilter.MinFilter(3)).filter(ImageFilter.GaussianBlur(1.2))
im.putalpha(al)
im.crop(im.getbbox()).save(sys.argv[2])
print("saved", sys.argv[2])
