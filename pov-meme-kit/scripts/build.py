"""Build a multi-scene Pawspective-style POV video from a JSON story.

usage (from the pov-meme-kit folder): python3 scripts/build.py stories/<story>.json out
Each scene: background + green-screen actors (keyed, auto-cropped) + labels + subtitles.
Audio: each actor's own clip sound while it speaks, over a music bed.
Paths in the story JSON are relative to the folder you run from.
"""
import json, os, subprocess, sys
import numpy as np
from PIL import Image, ImageDraw, ImageFont

W, H, FPS = 1080, 1920, 30
REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
KEY = "chromakey=0x50BB4A:0.10:0.04,despill=type=green"  # default (cat/dog packs)
_KEYS = {}


def key_for(src):
    """Chroma key matched to each video's own green (sampled from a corner), plus its frame size."""
    if src not in _KEYS:
        raw = subprocess.run(["ffmpeg", "-v", "error", "-ss", "0.5", "-i", src, "-frames:v", "1", "-f", "rawvideo",
                              "-pix_fmt", "rgb24", "-"], capture_output=True).stdout
        wh = subprocess.run(["ffprobe", "-v", "error", "-select_streams", "v", "-show_entries", "stream=width,height",
                             "-of", "csv=p=0", src], capture_output=True, text=True).stdout.strip().split(",")
        w, h = int(wh[0]), int(wh[1])
        r, g, b = np.frombuffer(raw, np.uint8).reshape(h, w, 3)[3, 3]
        # pure-green screens: no despill (it turns yellow orange) — chromakey alone is clean there
        key = KEY if g < 230 else f"chromakey=0x{r:02X}{g:02X}{b:02X}:0.18:0.06"
        _KEYS[src] = (key, w, h)
    return _KEYS[src]
story = json.load(open(sys.argv[1]))
OUT = sys.argv[2]
os.makedirs(f"{OUT}/tmp", exist_ok=True)


def run(cmd):
    subprocess.run(cmd, check=True)


def _longest_run(mask1d):
    best, cur, start, bs = 0, 0, 0, 0
    for i, v in enumerate(mask1d):
        if v:
            if cur == 0:
                start = i
            cur += 1
            if cur > best:
                best, bs = cur, start
        else:
            cur = 0
    return bs, bs + best


def main_blob(a):
    """Keep only the longest contiguous band of rows (then columns) — the character, not a watermark."""
    r0, r1 = _longest_run(a.sum(1) > 2)
    b = np.zeros_like(a)
    b[r0:r1] = a[r0:r1]
    c0, c1 = _longest_run(b.sum(0) > 2)
    out = np.zeros_like(a)
    out[r0:r1, c0:c1] = a[r0:r1, c0:c1]
    return out


def bbox(src, ss, dur):
    """Union bounding box of the keyed subject over the clip."""
    d = f"{OUT}/tmp/bb"
    os.makedirs(d, exist_ok=True)
    for f in os.listdir(d):
        os.remove(f"{d}/{f}")
    run(["ffmpeg", "-v", "error", "-ss", str(ss), "-t", str(dur), "-i", src, "-vf",
         f"fps=3,format=yuva420p,{key_for(src)[0]},alphaextract", f"{d}/%03d.png"])
    a = np.max([np.array(Image.open(f"{d}/{f}")) for f in os.listdir(d)], axis=0) > 128
    a = main_blob(a)  # drop corner watermarks / stray specks: keep the biggest block of rows & columns
    ys, xs = np.where(a)
    _, w, h = key_for(src)
    # tight_bottom: crop at the character's feet (portrait clips have empty green below the cat,
    # which made it float); off for older stories so they rebuild identically
    y1 = min(ys.max() + 2, h - 1) if story.get("tight_bottom") else h - 1
    x0, x1, y0 = max(xs.min() - 4, 0), min(xs.max() + 4, w - 1), max(ys.min() - 4, 0)
    return f"{(x1 - x0) // 2 * 2}:{(y1 - y0) // 2 * 2}:{x0}:{y0}"


def text_png(path, items):
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    for it in items:
        f = ImageFont.truetype(REG, it.get("size", 50))
        y = it["y"]
        for line in it["text"].split("\n"):
            w = d.textlength(line, font=f)
            x = min(max(it["x"] - w / 2, 20), W - 20 - w)
            d.text((x, y), line, font=f, fill=it.get("color", "white"),
                   stroke_width=it.get("stroke", 3), stroke_fill="black")
            y += f.size + 8
    im.save(path)


def pov_png(path, title):
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    f = ImageFont.truetype(REG, 40)
    lines = ["POV:"] + title.split("\n")
    d.rounded_rectangle((50, 190, W - 50, 190 + 40 + len(lines) * 50), radius=40, fill="white")
    y = 210
    for line in lines:
        d.text(((W - d.textlength(line, font=f)) / 2, y), line, font=f, fill="black")
        y += 50
    wm = ImageFont.truetype(REG, 36)
    t = story.get("handle", "")
    d.text(((W - d.textlength(t, font=wm)) / 2, 190 + 40 + len(lines) * 50 + 10), t, font=wm,
           fill=(255, 255, 255, 110))
    im.save(path)


try:
    CATALOG = json.load(open(os.path.join(os.path.dirname(story["packs"]["cat"]), "catalog.json")))
except FileNotFoundError:
    CATALOG = {}
pov_png(f"{OUT}/tmp/pov.png", story["title"])
parts = []
for si, sc in enumerate(story["scenes"]):
    T = sc["dur"]
    ins = ["-loop", "1", "-t", str(T), "-i", sc["bg"]]
    fc = [f"[0:v]scale={W}:{H}:force_original_aspect_ratio=increase,crop={W}:{H},setsar=1,format=rgba[s0]"]
    if sc.get("dark"):
        fc.append(f"[s0]eq=brightness=-0.25:saturation=0.6[s0]")
    cur, idx, aud, pending = "s0", 1, [], []
    for ai, a in enumerate(sc["actors"]):
        a["h"] = int(a["h"] * story.get("actor_scale", 1.0))  # one knob to make every character bigger
        if a.get("clip"):  # catalog id like "C7" / "D28" (assets/packs/catalog.json, CLIP_CATALOG.docx)
            c = CATALOG[a["clip"]]
            a["pack"] = c["pack"]
            dur = c["end"] - c["start"]
            a.setdefault("ss", round(c["start"] + min(1.0, 0.15 * dur), 2))  # skip the zoom-in intro (less on short clips)
            a.setdefault("len", round(c["end"] - a["ss"] - 0.15, 2))  # stay inside the clip (no flicker)
            if a["len"] < 3.0:  # a short clip ping-ponging in a long scene looks fast-forwarded: slow it down
                a.setdefault("speed", round(max(0.6, a["len"] / 3.0), 2))
        src = CATALOG[a["clip"]]["file"] if a.get("clip") else story["packs"][a.get("pack", "cat")]
        crop = bbox(src, a["ss"], a["len"])
        ins += ["-stream_loop", "-1", "-ss", str(a["ss"]), "-t", str(a["len"]), "-i", src]
        sp = a.get("speed", 1.0)  # <1 slows a twitchy meme clip down so it doesn't look fast-forwarded
        n = int(a["len"] / sp * FPS)
        flip = ",hflip" if a.get("flip") else ""
        # ping-pong loop (forward then backward) so a looping clip never jumps back to its start
        fc.append(f"[{idx}:v]trim=0:{a['len']},setpts=(PTS-STARTPTS)/{sp},fps={FPS},crop={crop},format=yuva420p,{key_for(src)[0]},"
                  f"scale=-2:{a['h']}{flip},split[f{ai}][r{ai}];[r{ai}]reverse[rv{ai}];"
                  f"[f{ai}][rv{ai}]concat=n=2:v=1:a=0,loop=-1:size={2 * n},trim=0:{T},setpts=N/{FPS}/TB[a{ai}]")
        en = f":enable='between(t,{a.get('t0', 0)},{a.get('t1', T)})'"
        fc.append(f"[{cur}][a{ai}]overlay=x={a['x']}-overlay_w/2:y={a['y']}-overlay_h{en}[c{ai}]")
        cur = f"c{ai}"
        # label + speech follow the actor: same x, stacked just above its head, only while visible
        top = max(a["y"] - a["h"], 470)
        lab_y = top - 60
        if a.get("label"):
            lp = f"{OUT}/tmp/lab{si}_{ai}.png"
            text_png(lp, [{"text": a["label"], "x": a["x"] + a.get("lx", 0), "y": lab_y}])
            pending.append((lp, a.get("t0", 0), a.get("t1", T)))
        for k, say in enumerate(a.get("say", [])):
            n_lines = say["text"].count("\n") + 1
            say_png = f"{OUT}/tmp/say{si}_{ai}_{k}.png"
            text_png(say_png, [{"text": say["text"], "x": a["x"] + a.get("lx", 0),
                           "y": lab_y - n_lines * 58 - 4, "color": say.get("color", (255, 230, 0))}])
            pending.append((say_png, say["t0"], say["t1"]))
            a.setdefault("speak", []).append([say["t0"], say["t1"]]) if say.get("sound", True) else None
        for k, (t0, t1) in enumerate(a.get("speak", [])):
            lbl = f"v{ai}_{k}"
            fc.append(f"[{idx}:a]atempo={max(sp, 0.5)},atrim=0:{t1 - t0},asetpts=PTS-STARTPTS,aformat=sample_rates=44100:channel_layouts=stereo,"
                      f"afade=t=in:d=0.1,afade=t=out:st={max(t1 - t0 - 0.3, 0)}:d=0.3,"
                      f"adelay={int(t0 * 1000)}|{int(t0 * 1000)},volume={a.get('vol', 1.0)}[{lbl}]")
            aud.append(lbl)
        idx += 1
    if sc.get("dim_at") is not None:
        fc.append(f"[{cur}]split[d1][d2];[d2]drawbox=c=black@0.6:t=fill[d3];"
                  f"[d1][d3]overlay=enable='gte(t,{sc['dim_at']})'[dm]")
        cur = "dm"
    labels = f"{OUT}/tmp/lab{si}.png"
    text_png(labels, sc.get("labels", []))
    ins += ["-i", labels]
    fc.append(f"[{cur}][{idx}:v]overlay[l]")
    cur, idx = "l", idx + 1
    ins += ["-i", f"{OUT}/tmp/pov.png"]
    fc.append(f"[{cur}][{idx}:v]overlay[p]")
    cur, idx = "p", idx + 1
    for k, (pth, t0, t1) in enumerate(pending):
        ins += ["-i", pth]
        fc.append(f"[{cur}][{idx}:v]overlay=enable='between(t,{t0},{t1})'[q{k}]")
        cur, idx = f"q{k}", idx + 1
    for k, s in enumerate(sc.get("subs", [])):
        p = f"{OUT}/tmp/sub{si}_{k}.png"
        text_png(p, [dict(s, color=s.get("color", (255, 230, 0)))])
        ins += ["-i", p]
        fc.append(f"[{cur}][{idx}:v]overlay=enable='between(t,{s['t0']},{s['t1']})'[t{k}]")
        cur, idx = f"t{k}", idx + 1
    # exactly T*FPS frames, so picture and sound stay locked scene after scene (no drift)
    fc.append(f"[{cur}]format=yuv420p,fps={FPS},tpad=stop_mode=clone:stop_duration=1,"
              f"trim=end_frame={int(round(T * FPS))}[vout]")
    ins += ["-f", "lavfi", "-t", str(T), "-i", "anullsrc=r=44100:cl=stereo"]
    # silence goes FIRST so the mix starts at t=0; otherwise the audio track starts at the first
    # cat sound and phone players "fast-forward" the opening video to catch up
    aud.insert(0, f"{idx}:a")
    fc.append("".join(f"[{x}]" for x in aud) + f"amix=inputs={len(aud)}:normalize=0:duration=first,"
              f"atrim=0:{T},asetpts=N/SR/TB[aout]")
    out = f"{OUT}/tmp/scene{si}.mp4"
    run(["ffmpeg", "-v", "error", "-y", *ins, "-filter_complex", ";".join(fc), "-map", "[vout]",
         "-map", "[aout]", "-t", str(T), "-c:v", "libx264", "-crf", "20", "-preset", "medium",
         "-c:a", "pcm_s16le", out])
    parts.append(out)
    print("scene", si, "ok", flush=True)

# concat scenes, then lay music under the cat sounds
lst = f"{OUT}/tmp/list.txt"
open(lst, "w").write("".join(f"file '{os.path.abspath(p)}'\n" for p in parts))
run(["ffmpeg", "-v", "error", "-y", "-f", "concat", "-safe", "0", "-i", lst, "-c", "copy", f"{OUT}/tmp/joined.mkv"])
total = sum(s["dur"] for s in story["scenes"])
m = story["music"]
run(["ffmpeg", "-v", "error", "-y", "-i", f"{OUT}/tmp/joined.mkv",
     "-ss", str(m.get("bed_ss", 0)), "-i", m["bed"], "-ss", str(m.get("end_ss", 0)), "-i", m["end"], "-filter_complex",
     f"[1:a]aformat=sample_rates=44100:channel_layouts=stereo,atrim=0:{m['switch']},volume={m['vol']},"
     f"afade=t=out:st={m['switch'] - 1.5}:d=1.5[b];"
     f"[2:a]aformat=sample_rates=44100:channel_layouts=stereo,atrim=0:{total - m['switch'] + 1.5},volume={m['vol'] * 1.4},"
     f"afade=t=in:d=1.5,adelay={int((m['switch'] - 1.5) * 1000)}|{int((m['switch'] - 1.5) * 1000)}[e];"
     f"[0:a][b][e]amix=inputs=3:normalize=0:duration=first,asetpts=N/SR/TB,afade=t=out:st={total - 1}:d=1[a];"
     f"[0:v]fade=t=in:d=0.4,fade=t=out:st={total - 0.8}:d=0.8[v]",
     "-map", "[v]", "-map", "[a]", "-c:v", "libx264", "-crf", "20", "-preset", "medium",
     "-c:a", "aac", "-b:a", "160k", "-t", str(total), f"{OUT}/{story['name']}_with-sound.mp4"])
run(["ffmpeg", "-v", "error", "-y", "-i", f"{OUT}/{story['name']}_with-sound.mp4", "-an", "-c:v", "copy",
     f"{OUT}/{story['name']}_silent.mp4"])
# clip list per scene (catalog ids) — send this to the user with every video
def clip_id(a):
    if a.get("clip"):
        return a["clip"]
    for cid, c in CATALOG.items():
        if c["pack"] == a.get("pack", "cat") and c["start"] <= a["ss"] < c["end"]:
            return cid
    return "?"


def mmss(t):
    return f"{int(t // 60)}:{t % 60:04.1f}"


lines = ["| Scene | Time in video | Place | Who = clip (on screen) |", "|---|---|---|---|"]
t_scene = 0.0
for si, sc in enumerate(story["scenes"], 1):
    who = "<br>".join(
        f"{a.get('name') or a.get('label', '?').rstrip(':')} = **{clip_id(a)}** "
        f"({mmss(t_scene + a.get('t0', 0))}–{mmss(t_scene + a.get('t1', sc['dur']))})" for a in sc["actors"])
    place = os.path.splitext(os.path.basename(sc["bg"]))[0]
    lines.append(f"| {si} | {mmss(t_scene)}–{mmss(t_scene + sc['dur'])} | {place} | {who} |")
    t_scene += sc["dur"]
open(f"{OUT}/{story['name']}_clips.md", "w").write("\n".join(lines) + "\n")
print("\n".join(lines))
print("DONE", total)
