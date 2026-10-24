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
KEY = "chromakey=0x50BB4A:0.10:0.04,despill=type=green"
story = json.load(open(sys.argv[1]))
OUT = sys.argv[2]
os.makedirs(f"{OUT}/tmp", exist_ok=True)


def run(cmd):
    subprocess.run(cmd, check=True)


def bbox(src, ss, dur):
    """Union bounding box of the keyed subject over the clip."""
    d = f"{OUT}/tmp/bb"
    os.makedirs(d, exist_ok=True)
    for f in os.listdir(d):
        os.remove(f"{d}/{f}")
    run(["ffmpeg", "-v", "error", "-ss", str(ss), "-t", str(dur), "-i", src, "-vf",
         f"fps=3,format=yuva420p,{KEY},alphaextract", f"{d}/%03d.png"])
    a = np.max([np.array(Image.open(f"{d}/{f}")) for f in os.listdir(d)], axis=0) > 128
    ys, xs = np.where(a)
    x0, x1, y0, y1 = max(xs.min() - 4, 0), min(xs.max() + 4, 639), max(ys.min() - 4, 0), 359
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


# per-pack notes (assets/packs/<pack>.notes.json): audio stretches that must always be muted
NOTES = {}
for k, src in story["packs"].items():
    try:
        NOTES[src] = json.load(open(os.path.splitext(src)[0] + ".notes.json"))
    except FileNotFoundError:
        NOTES[src] = {}


def mute_filter(src, ss, sp):
    """volume=0 for every flagged stretch, in the clip's local (speed-adjusted) time."""
    out = ""
    for m0, m1, *_ in NOTES.get(src, {}).get("mute", []):
        a, b = (m0 - ss) / sp, (m1 - ss) / sp
        if b > 0:
            out += f"volume=0:enable='between(t,{max(a, 0):.2f},{b:.2f})',"
    return out


pov_png(f"{OUT}/tmp/pov.png", story["title"])
parts = []
for si, sc in enumerate(story["scenes"]):
    T = sc["dur"]
    ins = ["-loop", "1", "-t", str(T), "-i", sc["bg"]]
    fc = [f"[0:v]scale={W}:{H}:force_original_aspect_ratio=increase,crop={W}:{H},setsar=1,format=rgba[s0]"]
    if sc.get("dark"):
        fc.append(f"[s0]eq=brightness=-0.25:saturation=0.6[s0]")
    cur, idx, aud, pending, sfx_lines = "s0", 1, [], [], []
    for ai, a in enumerate(sc["actors"]):
        src = story["packs"][a.get("pack", "cat")]
        crop = bbox(src, a["ss"], a["len"])
        if a.get("keep_top"):  # keep only the top part of the cut-out (e.g. hide hands holding the cat)
            cw, chh, cx, cy = map(int, crop.split(":"))
            crop = f"{cw}:{int(chh * a['keep_top']) // 2 * 2}:{cx}:{cy}"
        ins += ["-stream_loop", "-1", "-ss", str(a["ss"]), "-t", str(a["len"]), "-i", src]
        sp = a.get("speed", 1.0)  # <1 slows a twitchy meme clip down so it doesn't look fast-forwarded
        n = int(a["len"] / sp * FPS)
        flip = ",hflip" if a.get("flip") else ""
        # ping-pong loop (forward then backward) so a looping clip never jumps back to its start
        fc.append(f"[{idx}:v]trim=0:{a['len']},setpts=(PTS-STARTPTS)/{sp},fps={FPS},crop={crop},format=yuva420p,{KEY},"
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
            if say.get("sfx"):  # generated voice (scripts/sfx.py) instead of the clip's own sound
                sfx_lines.append((say["sfx"], say["t0"], say["t1"], a.get("vol", 1.0)))
                continue
            a.setdefault("speak", []).append([say["t0"], say["t1"]]) if say.get("sound", True) else None
        first_t0 = min([t0 for t0, _ in a.get("speak", [])] or [0])
        for k, (t0, t1) in enumerate(a.get("speak", [])):
            off = t0 - first_t0  # each later line continues further into the sound, never repeats it
            lbl = f"v{ai}_{k}"
            a_in, a_ss, a_sp = (idx + 1, a["sound_ss"], 1.0) if a.get("sound_ss") is not None else (idx, a["ss"], sp)
            fc.append(f"[{a_in}:a]{mute_filter(src, a_ss, a_sp)}atempo={max(a_sp, 0.5)},atrim={off}:{off + t1 - t0},asetpts=PTS-STARTPTS,aformat=sample_rates=44100:channel_layouts=stereo,"
                      f"afade=t=in:d=0.1,afade=t=out:st={max(t1 - t0 - 0.3, 0)}:d=0.3,"
                      f"adelay={int(t0 * 1000)}|{int(t0 * 1000)},volume={a.get('vol', 1.0)}[{lbl}]")
            aud.append(lbl)
        if a.get("sound_ss") is not None:  # borrow a clean meow from elsewhere in the pack
            ins += ["-ss", str(a["sound_ss"]), "-i", src]
            idx += 1
        idx += 1
    for k, (f, t0, t1, vol) in enumerate(sfx_lines):
        ins += ["-i", f]
        fc.append(f"[{idx}:a]aformat=sample_rates=44100:channel_layouts=stereo,atrim=0:{t1 - t0},"
                  f"afade=t=in:d=0.05,afade=t=out:st={max(t1 - t0 - 0.3, 0)}:d=0.3,"
                  f"adelay={int(t0 * 1000)}|{int(t0 * 1000)},volume={1.6 * vol}[x{k}]")
        aud.append(f"x{k}")
        idx += 1
    if sc.get("ambience_file"):  # quiet room sound so the voices sit in the scene
        ins += ["-stream_loop", "-1", "-i", sc["ambience_file"]]
        fc.append(f"[{idx}:a]aformat=sample_rates=44100:channel_layouts=stereo,atrim=0:{T},"
                  f"afade=t=in:d=0.3,afade=t=out:st={T - 0.3}:d=0.3,volume={sc.get('ambience_vol', 0.35)}[amb]")
        aud.append("amb")
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
    fc.append(f"[{cur}]format=yuv420p,fps={FPS}[vout]")
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
print("DONE", total)
