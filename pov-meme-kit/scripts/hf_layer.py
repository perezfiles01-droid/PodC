"""FUNNY videos: animated text + sticker layer drawn by HyperFrames (user chose it over build.py's text).

usage (from pov-meme-kit/): python3 scripts/hf_layer.py stories/<story>.json out
  1. build.py with NO_TEXT=1  → out/<name>_plain.mp4 (picture + sound: backgrounds, cats, motion, SFX)
  2. writes a HyperFrames project out/hf_<name>/ (POV pills, labels, speech, subs, props, emoji)
     — same positions/timings as build.py's funny text, plus GSAP animation (bounce pop, shake, glide,
     prop pop/spin/rock/tremble)
  3. hyperframes check + render → out/<name>_with-sound.mp4 (final; QA runs on this file)
"""
import html, json, os, shutil, subprocess, sys
from PIL import Image, ImageDraw, ImageFont

KIT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BIN = os.path.join(KIT, "motion", "node_modules", ".bin")
story_path, OUT = sys.argv[1], sys.argv[2]
st = json.load(open(story_path))
assert st.get("style") == "funny", "hf_layer.py is for funny stories"
NAME = st["name"]
AS = st.get("actor_scale", 1.0)
EMOJI = "/usr/share/fonts/truetype/noto/NotoColorEmoji.ttf"

# 1. picture + sound only
plain = f"{OUT}/{NAME}_with-sound.mp4"
subprocess.run([sys.executable, os.path.join(KIT, "scripts", "build.py"), story_path, OUT],
               check=True, env=dict(os.environ, NO_TEXT="1"))
os.replace(plain, f"{OUT}/{NAME}_plain.mp4")

# 2. HyperFrames project
P = os.path.abspath(f"{OUT}/hf_{NAME}")
shutil.rmtree(P, ignore_errors=True)
os.makedirs(f"{P}/assets/fonts")
# short keyframe interval so HyperFrames seeks the footage frame-accurately
subprocess.run(["ffmpeg", "-v", "error", "-y", "-i", f"{OUT}/{NAME}_plain.mp4", "-c:v", "libx264", "-crf", "16",
                "-r", "30", "-g", "30", "-keyint_min", "30", "-c:a", "copy", f"{P}/assets/plain.mp4"], check=True)
shutil.copy(f"{KIT}/assets/fonts/ComicNeue-Bold.ttf", f"{P}/assets/fonts/")
# HyperFrames' headless Chrome can't fetch CDN scripts through the proxy → local GSAP
shutil.copy(f"{KIT}/motion/node_modules/gsap/dist/gsap.min.js", P)
json.dump({"paths": {"assets": "assets"}, "media": {"autoProxy": True}}, open(f"{P}/hyperframes.json", "w"))
json.dump({"id": NAME, "name": NAME}, open(f"{P}/meta.json", "w"))

els, js, n = [], [], [0]
total = sum(sc["dur"] for sc in st["scenes"])


def nid():
    n[0] += 1
    return f"e{n[0]}"


def text_el(txt, x, y, t0, t1, size, color, anim, stroke=4):
    i = nid()
    lines = txt.split("\n")
    w = min(1040, max(len(l) for l in lines) * size * 0.52 + 20)
    x = min(max(x, 20 + w / 2), 1060 - w / 2)  # keep inside the frame (build.py clamps the same way)
    body = "<br>".join(html.escape(l) for l in lines)
    els.append(f'<div id="{i}" class="clip txt" data-start="{t0:.3f}" data-duration="{t1 - t0:.3f}" '
               f'style="left:{x - 520:.0f}px;top:{y:.0f}px;font-size:{size}px;color:{color};'
               f'-webkit-text-stroke:{stroke * 2}px #000"><span class="in">{body}</span></div>')
    if anim == "pop":
        js.append(f'tl.from("#{i} .in",{{scale:0,duration:0.45,ease:"back.out(2.6)"}},{t0:.3f});')
    elif anim == "shake":
        js.append(f'tl.from("#{i} .in",{{scale:0,duration:0.35,ease:"back.out(3)"}},{t0:.3f});'
                  f'tl.to("#{i} .in",{{keyframes:{{x:[-14,14,-10,10,-6,6,0]}},duration:0.6,ease:"none"}},{t0 + 0.35:.3f});')
    elif anim == "glide":
        js.append(f'tl.from("#{i} .in",{{y:30,opacity:0,duration:0.3,ease:"power3.out"}},{t0:.3f});')


def prop_png(pr):
    if pr.get("emoji"):
        f = ImageFont.truetype(EMOJI, 109)
        im = Image.new("RGBA", (160, 160), (0, 0, 0, 0))
        ImageDraw.Draw(im).text((80, 80), pr["emoji"], font=f, embedded_color=True, anchor="mm")
        im = im.crop(im.getbbox())
    else:
        im = Image.open(f"{KIT}/{pr['img']}").convert("RGBA")
    im = im.resize((max(1, int(im.width * pr["h"] / im.height)), pr["h"]), Image.LANCZOS)
    name = f"assets/p{n[0] + 1}.png"
    im.save(f"{P}/{name}")
    return name, im.width


T0 = 0.0
for si, sc in enumerate(st["scenes"]):
    hook = si == 0 and st.get("hook_first_frame")
    still = lambda t: hook and t == 0  # frame 0 is the thumbnail: everything already in place
    shown = set()  # (label, end) so a clip swap doesn't re-animate the same label
    for a in sc["actors"]:
        t0, t1 = T0 + a.get("t0", 0), T0 + a.get("t1", sc["dur"])
        x, y, h = a["x"], a["y"], int(a["h"] * AS)
        if a.get("path"):
            pt = a["path"][a.get("text_at", -1)]
            x, y, h = pt[1], pt[2], int(h * pt[3])
        lab_y = max(y - h, 470) + 6
        if a.get("label"):
            key = (a["label"], round(t0, 2))
            anim = None if still(a.get("t0", 0)) or key in shown else "glide"
            text_el(a["label"], x + a.get("lx", 90), y + 4, t0, t1, 52, "white", anim)
            shown.add((a["label"], round(t1, 2)))
        for s in a.get("say", []):
            nl = s["text"].count("\n") + 1
            anim = None if still(s["t0"]) else ("shake" if s.get("shake") or "!" in s["text"] and s["text"].isupper() else "pop")
            text_el(s["text"], x + a.get("sx", 0), lab_y - nl * 62 - 10, T0 + s["t0"], T0 + s["t1"], 52,
                    s.get("color", "white"), anim)
    for s in sc.get("subs", []):
        text_el(s["text"], s["x"], s["y"], T0 + s["t0"], T0 + s["t1"], s.get("size", 50),
                s.get("color", "#FFE600"), None if still(s["t0"]) else "pop", s.get("stroke", 3))
    for pr in sc.get("props", []):
        i = nid()
        src, w = prop_png(pr)
        a0, a1 = T0 + pr.get("t0", 0), T0 + pr.get("t1", sc["dur"])
        els.append(f'<div id="{i}" class="clip prop" data-start="{a0:.3f}" data-duration="{a1 - a0:.3f}" '
                   f'style="left:{pr["x"] - w // 2}px;top:{pr["y"] - pr["h"]}px"><img class="in" src="{src}"></div>')
        if pr.get("slide_from"):
            dx, dy = pr["slide_from"][0] - pr["x"], pr["slide_from"][1] - pr["y"]
            du = pr.get("slide_dur", a1 - a0)
            js.append(f'tl.from("#{i} .in",{{x:{dx},y:{dy},duration:{du:.3f},ease:"power2.out"}},{a0:.3f});')
        elif not still(pr.get("t0", 0)):
            js.append(f'tl.from("#{i} .in",{{scale:0,rotation:20,duration:0.4,ease:"back.out(2.5)"}},{a0:.3f});')
        if pr.get("tremble"):
            b0, b1 = T0 + pr["tremble"][0], T0 + pr["tremble"][1]
            k = max(1, int((b1 - b0) / 0.12))
            js.append(f'tl.to("#{i} .in",{{keyframes:{{rotation:[{",".join(["-7,7"] * k)},0]}},'
                      f'duration:{b1 - b0:.3f},ease:"none"}},{b0 + 0.05:.3f});')
        elif a1 - a0 > 1.2:
            js.append(f'tl.to("#{i} .in",{{keyframes:{{rotation:[-6,6,-4,4,0]}},duration:1.2,ease:"sine.inOut"}},{a0 + 0.5:.3f});')
    T0 += sc["dur"]

pills = "".join(f'<div class="pill"><span>{html.escape(t)}</span></div>' for t in ["POV:"] + st["title"].split("\n"))
page = f'''<!doctype html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=1080, height=1920">
<script src="gsap.min.js"></script><style>
@font-face{{font-family:"Comic Neue";src:url("assets/fonts/ComicNeue-Bold.ttf");font-weight:700}}
*{{margin:0;padding:0;box-sizing:border-box}}
html,body{{width:1080px;height:1920px;overflow:hidden;background:#000}}
#root{{position:relative;width:1080px;height:1920px;font-family:"Comic Neue";font-weight:700}}
#bg{{position:absolute;left:0;top:0;width:1080px;height:1920px;z-index:0}}
#pov{{position:absolute;left:0;top:422px;width:1080px;z-index:3;display:flex;flex-direction:column;align-items:center;gap:8px}}
.pill span{{display:inline-block;background:#fff;color:#000;font-size:56px;line-height:66px;padding:0 22px;border-radius:18px}}
.txt{{position:absolute;width:1040px;text-align:center;line-height:1.19;z-index:5;paint-order:stroke fill}}
.txt .in{{display:inline-block}} .prop{{position:absolute;z-index:4}} .prop .in{{display:block}}
</style></head><body>
<div id="root" data-composition-id="main" data-start="0" data-duration="{total}" data-width="1080" data-height="1920" data-layout-allow-overlap data-layout-allow-overflow>
<video id="bg" class="clip" src="assets/plain.mp4" data-start="0" data-duration="{total}" data-has-audio="true"></video>
<div id="pov" class="clip" data-start="0" data-duration="{total}">{pills}</div>
{chr(10).join(els)}
</div><script>
const tl = gsap.timeline({{ paused: true }});
{chr(10).join(js)}
window.__timelines["main"] = tl; tl.seek(0);
</script></body></html>'''
open(f"{P}/index.html", "w").write(page)

# 3. check + render
env = dict(os.environ, PATH=BIN + os.pathsep + os.environ["PATH"])
chk = subprocess.run(["hyperframes", "check", "--json"], cwd=P, env=env, capture_output=True, text=True)
try:
    ok = json.loads(chk.stdout).get("ok")
except ValueError:
    ok = False
if not ok:
    print(chk.stdout[-3000:])
    sys.exit("hyperframes check failed")
subprocess.run(["hyperframes", "render", "-o", os.path.abspath(f"{OUT}/{NAME}_with-sound.mp4"), "--quiet"],
               cwd=P, env=env, check=True)
print("HF DONE", total, f"{OUT}/{NAME}_with-sound.mp4")
