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
FUNNY = story.get("style") == "funny"  # funny style: comic font, POV pills, white "quoted" speech, labels below, no song
if FUNNY:
    REG = "assets/fonts/ComicNeue-Bold.ttf"
EMOJI = "/usr/share/fonts/truetype/noto/NotoColorEmoji.ttf"


def emoji_png(ch, h):
    """Colour emoji sticker (Noto renders at 109 px; scaled to h)."""
    f = ImageFont.truetype(EMOJI, 109)
    im = Image.new("RGBA", (160, 160), (0, 0, 0, 0))
    ImageDraw.Draw(im).text((80, 80), ch, font=f, embedded_color=True, anchor="mm")
    im = im.crop(im.getbbox())
    return im.resize((int(im.width * h / im.height), h), Image.LANCZOS)
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
    if FUNNY:  # one white rounded pill per line, hugging the text (ref: funny_ref1.mp4)
        f = ImageFont.truetype(REG, 56)
        y = 430
        for line in ["POV:"] + title.split("\n"):
            w = d.textlength(line, font=f)
            d.rounded_rectangle(((W - w) / 2 - 22, y - 8, (W + w) / 2 + 22, y + 66), radius=18, fill="white")
            d.text(((W - w) / 2, y), line, font=f, fill="black")
            y += 74
        im.save(path)
        return
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


FULL_SOUND = {"C30"}  # clips whose meme sound must always play complete (user request)
try:
    CATALOG = json.load(open(os.path.join(os.path.dirname(story["packs"]["cat"]), "catalog.json")))
except FileNotFoundError:
    CATALOG = {}
try:
    SFX = json.load(open("assets/sfx/sfx.json"))
except FileNotFoundError:
    SFX = {}
pov_png(f"{OUT}/tmp/pov.png", story["title"])
parts = []
for si, sc in enumerate(story["scenes"]):
    T = sc["dur"]
    MOTION = story.get("motion")  # camera moves, entrances, text pops, props (off = old behaviour)
    ins = ["-loop", "1", "-framerate", str(FPS), "-t", str(T), "-i", sc["bg"]]
    fc = [f"[0:v]scale={W}:{H}:force_original_aspect_ratio=increase,crop={W}:{H},setsar=1,format=rgba[s0]"]
    if MOTION:  # slow zoom / pan so no scene is a still picture
        cam, Z = sc.get("cam", "in"), 0.10
        if cam in ("in", "out"):
            z = f"(1+{Z}*t/{T})" if cam == "in" else f"(1+{Z}-{Z}*t/{T})"
            fc.append(f"[s0]scale=w='{W}*{z}':h='{H}*{z}':eval=frame,crop={W}:{H}:(iw-{W})/2:(ih-{H})/2[s0]")
        else:  # pan left / right across a slightly enlarged background
            px = f"(iw-{W})*t/{T}" if cam == "right" else f"(iw-{W})*(1-t/{T})"
            fc.append(f"[s0]scale={int(W * 1.15)}:-2,crop={W}:{H}:'{px}':(ih-{H})/2[s0]")
    if story.get("bg_soft"):  # calm the background so the characters stand out (still realistic)
        fc.append(f"[s0]gblur=sigma=6,eq=saturation=0.55:brightness=-0.07:contrast=0.88[s0]")
    if sc.get("dark"):
        fc.append(f"[s0]eq=brightness=-0.25:saturation=0.6[s0]")
    if sc.get("grey"):
        fc.append("[s0]hue=s=0[s0]")
    cur, idx, aud, pending = "s0", 1, [], []
    for ai, a in enumerate(sc["actors"]):
        a["h"] = int(a["h"] * story.get("actor_scale", 1.0))  # one knob to make every character bigger
        if a.get("clip"):  # catalog id like "C7" / "D28" (assets/packs/catalog.json, CLIP_CATALOG.docx)
            c = CATALOG[a["clip"]]
            a["pack"] = c["pack"]
            if a["clip"] in FULL_SOUND or a.get("full_sound"):  # play the whole clip, start to end, real speed
                a.setdefault("ss", c["start"]); a.setdefault("len", round(c["end"] - c["start"], 2)); a["full_sound"] = True
            dur = c["end"] - c["start"]
            a.setdefault("ss", round(c["start"] + min(1.0, 0.15 * dur), 2))  # skip the zoom-in intro (less on short clips)
            a.setdefault("len", round(c["end"] - a["ss"] - 0.15, 2))  # stay inside the clip (no flicker)
            if a["len"] < 3.0 and not FUNNY:  # a short clip ping-ponging in a long scene looks fast-forwarded: slow it down
                a.setdefault("speed", round(max(0.6, a["len"] / 3.0), 2))
        src = CATALOG[a["clip"]]["file"] if a.get("clip") else story["packs"][a.get("pack", "cat")]
        crop = bbox(src, a["ss"], a["len"])
        if a.get("keep_top"):  # keep only the top part of the cut-out (e.g. hide hands holding the cat)
            cw, chh, cx, cy = map(int, crop.split(":"))
            crop = f"{cw}:{int(chh * a['keep_top']) // 2 * 2}:{cx}:{cy}"
        ins += ["-stream_loop", "-1", "-ss", str(a["ss"]), "-t", str(a["len"]), "-i", src]
        t0a = a.get("t0", 0)
        # smooth clips (funny style): each clip starts from ITS OWN beginning when it appears and only plays
        # forward (no ping-pong reversing, no starting mid-loop); too short → slowed a little, then holds
        SMOOTH = FUNNY or story.get("smooth_clips")
        D = a.get("t1", T) - t0a
        # (no automatic slow-down in smooth mode: slowing by repeating frames looks like stutter — hold the last frame)
        sp = a.get("speed", 1.0)  # <1 slows a twitchy meme clip down so it doesn't look fast-forwarded
        n = int(a["len"] / sp * FPS)
        flip = ",hflip" if a.get("flip") else ""
        base = (f"[{idx}:v]trim=0:{a['len']},setpts=(PTS-STARTPTS)/{sp},fps={FPS},crop={crop},format=yuva420p,"
                f"{key_for(src)[0]},scale=-2:{a['h']}{flip}")
        # funny swaps cross-fade (0.2 s): the new clip fades in, the old one fades out over the same moment
        XF = 0.2 if FUNNY and a.get("swap") else 0.0
        if SMOOTH:
            fades = ""
            if XF:
                if t0a > 0:
                    fades += f",fade=t=in:st=0:d={XF}:alpha=1"
                if a.get("t1", T) < T:
                    fades += f",fade=t=out:st={D}:d={XF}:alpha=1"
            fc.append(f"{base},tpad=stop_mode=clone:stop_duration={D + 1},trim=0:{D + XF}{fades},"
                      f"setpts=N/{FPS}/TB+{t0a}/TB[a{ai}]")
        else:  # ping-pong loop (forward then backward) so a looping clip never jumps back to its start
            fc.append(f"{base},split[f{ai}][r{ai}];[r{ai}]reverse[rv{ai}];"
                      f"[f{ai}][rv{ai}]concat=n=2:v=1:a=0,loop=-1:size={2 * n},trim=0:{T},setpts=N/{FPS}/TB[a{ai}]")
        en = f":enable='between(t,{t0a},{a.get('t1', T) + (0.2 if FUNNY and a.get('swap') and a.get('t1', T) < T else 0)})'"
        ox, oy = f"{a['x']}-overlay_w/2", f"{a['y']}-overlay_h"
        ent = a.get("enter", "up" if MOTION and not (si == 0 and t0a == 0 and story.get("hook_first_frame")) else None)
        if ent and not a.get("swap"):  # slide in over 0.4 s (eased); a mid-scene emotion swap stays put
            k = f"pow(max(0,1-(t-{t0a})/0.4),2)"
            if ent == "up":
                oy += f"+260*{k}"
            else:
                ox += f"{'-' if ent == 'left' else '+'}600*{k}"
        if a.get("slide_from"):  # e.g. falling down the stairs / running in
            sx0, sy0 = a["slide_from"]
            p = f"min(1,max(0,(t-{t0a})/{a.get('slide_dur', 1.0)}))"
            ox += f"+({sx0}-{a['x']})*(1-{p})"
            oy += f"+({sy0}-{a['y']})*(1-{p})"
        if a.get("slide_to"):  # leaves: moves from x/y to slide_to, starting at slide_at (default t0)
            tx, ty = a["slide_to"]
            p = f"pow(min(1,max(0,(t-{a.get('slide_at', t0a)})/{a.get('slide_dur', 0.8)})),2)"
            ox += f"+({tx}-{a['x']})*{p}"
            oy += f"+({ty}-{a['y']})*{p}"
        if FUNNY and not a.get("no_bob"):  # gentle idle bob so nobody looks frozen
            oy += f"+7*sin(2*PI*0.9*t+{ai})"
        fc.append(f"[{cur}][a{ai}]overlay=x='{ox}':y='{oy}'{en}[c{ai}]")
        cur = f"c{ai}"
        # label + speech follow the actor: same x, stacked just above its head, only while visible
        top = max(a["y"] - a["h"], 470)
        lab_y = top - 60
        if FUNNY:  # speech sits right above the head; the ":Me" label goes BELOW the character
            lab_y = top + 6
        if a.get("label"):
            lp = f"{OUT}/tmp/lab{si}_{ai}.png"
            if FUNNY:
                text_png(lp, [{"text": a["label"], "x": a["x"] + a.get("lx", 90), "y": a["y"] + 4, "size": 52, "stroke": 4}])
            else:
                text_png(lp, [{"text": a["label"], "x": a["x"] + a.get("lx", 0), "y": lab_y}])
            pending.append((lp, a.get("t0", 0), a.get("t1", T), "pop"))
        for k, say in enumerate(a.get("say", [])):
            n_lines = say["text"].count("\n") + 1
            say_png = f"{OUT}/tmp/say{si}_{ai}_{k}.png"
            if FUNNY:
                text_png(say_png, [{"text": say["text"], "x": a["x"] + a.get("sx", 0), "y": lab_y - n_lines * 62 - 10,
                                    "color": say.get("color", "white"), "size": 52, "stroke": 4}])
            else:
                text_png(say_png, [{"text": say["text"], "x": a["x"] + a.get("lx", 0),
                               "y": lab_y - n_lines * 58 - 4, "color": say.get("color", (255, 230, 0))}])
            pending.append((say_png, say["t0"], say["t1"], "shake" if say.get("shake") else "pop"))
            a.setdefault("speak", []).append([say["t0"], say["t1"]]) if say.get("sound", True) else None
        if a.get("full_sound"):  # never cut this clip's sound: each line plays the whole clip audio
            a["speak"] = [[t0, max(t1, t0 + a["len"])] for t0, t1 in a.get("speak", [])]
        for k, (t0, t1) in enumerate(a.get("speak", [])):
            lbl = f"v{ai}_{k}"
            o = max(0.0, t0 - t0a) if SMOOTH else 0.0  # smooth: sound matches the picture (clip started at t0a)
            e = min(o + t1 - t0, a["len"] / sp) if SMOOTH else t1 - t0
            if e - o < 0.2:
                continue
            fc.append(f"[{idx}:a]atempo={max(sp, 0.5)},atrim={o}:{e},asetpts=PTS-STARTPTS,aformat=sample_rates=44100:channel_layouts=stereo,"
                      f"afade=t=in:d=0.1,afade=t=out:st={max(t1 - t0 - 0.3, 0)}:d=0.3,"
                      f"adelay={int(t0 * 1000)}|{int(t0 * 1000)},volume={a.get('vol', 1.0)}[{lbl}]")
            aud.append(lbl)
        idx += 1
    for k, fx in enumerate(sc.get("sfx", [])):  # meme sound effects (assets/sfx/sfx.json ids S1..), funny style
        e = SFX[fx["id"]]
        ln = min(fx.get("len", e["dur"]), T - fx["t"])
        ins += ["-i", e["file"]]
        fc.append(f"[{idx}:a]atrim=0:{ln},asetpts=PTS-STARTPTS,aformat=sample_rates=44100:channel_layouts=stereo,"
                  f"afade=t=out:st={max(ln - 0.15, 0)}:d=0.15,adelay={int(fx['t'] * 1000)}|{int(fx['t'] * 1000)},"
                  f"volume={fx.get('vol', 0.8)}[fx{k}]")
        aud.append(f"fx{k}")
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
    def move(t0, style):  # overlay x/y for a full-canvas text/prop layer
        if isinstance(style, tuple):  # ("slide", dx, dy, dur): prop travels from (x+dx, y+dy) to (x, y)
            _, dx, dy, du = style
            p = f"min(1,max(0,(t-{t0})/{du}))"
            return f":x='{dx}*(1-{p})':y='{dy}*(1-{p})'"
        if not MOTION or not style or (si == 0 and t0 == 0 and story.get("hook_first_frame")):
            return ""
        x = f"if(lt(t-{t0},0.5),14*sin(70*t),0)" if style == "shake" else "0"
        return f":x='{x}':y='30*pow(max(0,1-(t-{t0})/0.25),2)'"
    for pr in sc.get("props", []):  # story objects (cut-out PNGs) that pop in on cue
        if pr.get("emoji"):
            pi = emoji_png(pr["emoji"], pr["h"])
        else:
            pi = Image.open(pr["img"]).convert("RGBA")
            pi = pi.resize((int(pi.width * pr["h"] / pi.height), pr["h"]))
        cv = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        cv.paste(pi, (pr["x"] - pi.width // 2, pr["y"] - pi.height), pi)
        pp = f"{OUT}/tmp/prop{si}_{len(pending)}.png"
        cv.save(pp)
        st = "pop"
        if pr.get("slide_from"):  # e.g. a gas station / cow "driving past" the car window
            st = ("slide", pr["slide_from"][0] - pr["x"], pr["slide_from"][1] - pr["y"],
                  pr.get("slide_dur", pr.get("t1", T) - pr.get("t0", 0)))
        pending.insert(0, (pp, pr.get("t0", 0), pr.get("t1", T), st))
    for k, (pth, t0, t1, style) in enumerate(pending):
        ins += ["-i", pth]
        fc.append(f"[{cur}][{idx}:v]overlay=enable='between(t,{t0},{t1})'{move(t0, style)}[q{k}]")
        cur, idx = f"q{k}", idx + 1
    for k, s in enumerate(sc.get("subs", [])):
        p = f"{OUT}/tmp/sub{si}_{k}.png"
        text_png(p, [dict(s, color=s.get("color", (255, 230, 0)))])
        ins += ["-i", p]
        fc.append(f"[{cur}][{idx}:v]overlay=enable='between(t,{s['t0']},{s['t1']})'{move(s['t0'], 'pop')}[t{k}]")
        cur, idx = f"t{k}", idx + 1
    if MOTION and si > 0:
        fc.append(f"[{cur}]fade=t=in:d=0.3[fi]")
        cur = "fi"
    for k, (a0, a1) in enumerate(sc.get("shake", [])):  # screen shake on a scream (zoom 4 % so no edges show)
        fc.append(f"[{cur}]scale={int(W * 1.04)}:{int(H * 1.04)},crop={W}:{H}:"
                  f"x='{int(W * 0.02)}+if(between(t,{a0},{a1}),18*sin(95*t),0)':"
                  f"y='{int(H * 0.02)}+if(between(t,{a0},{a1}),12*cos(80*t),0)'[sk{k}]")
        cur = f"sk{k}"
    if sc.get("transition") == "slide" and parts:  # whip-slide in over the previous scene's last frame
        last = f"{OUT}/tmp/last{si}.png"
        run(["ffmpeg", "-v", "error", "-y", "-sseof", "-0.1", "-i", parts[-1], "-frames:v", "1", last])
        ins += ["-loop", "1", "-framerate", str(FPS), "-t", str(T), "-i", last]
        fc.append(f"[{idx}:v]format=rgba,setsar=1[prev];[prev][{cur}]overlay=x='{W}*pow(max(0,1-t/0.35),2)':y=0[tr]")
        cur, idx = "tr", idx + 1
    if sc.get("flash_at") is not None:
        fc.append(f"[{cur}]eq=brightness='0.45*max(0,1-abs(t-{sc['flash_at']})/0.12)':eval=frame[fl]")
        cur = "fl"
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
m = story.get("music")
# hook_first_frame: no fade-in, so the very first frame (the preview) already shows the scene
VFADE = (f"[0:v]fade=t=out:st={total - 0.8}:d=0.8[v]" if story.get("hook_first_frame") else
         f"[0:v]fade=t=in:d=0.4,fade=t=out:st={total - 0.8}:d=0.8[v]")
if not m:  # funny style: only the clips' own sounds + meme sound effects, hard ending
    run(["ffmpeg", "-v", "error", "-y", "-i", f"{OUT}/tmp/joined.mkv", "-c:v", "libx264", "-crf", "20", "-preset", "medium",
         "-af", "alimiter=limit=0.95", "-c:a", "aac", "-b:a", "160k", "-t", str(total), f"{OUT}/{story['name']}_with-sound.mp4"])
else:
  run(["ffmpeg", "-v", "error", "-y", "-i", f"{OUT}/tmp/joined.mkv",
     "-ss", str(m.get("bed_ss", 0)), "-i", m["bed"], "-ss", str(m.get("end_ss", 0)), "-i", m["end"], "-filter_complex",
     f"[1:a]aformat=sample_rates=44100:channel_layouts=stereo,atrim=0:{m['switch']},volume={m['vol']},"
     f"afade=t=out:st={m['switch'] - 1.5}:d=1.5[b];"
     f"[2:a]aformat=sample_rates=44100:channel_layouts=stereo,atrim=0:{total - m['switch'] + 1.5},volume={m['vol'] * 1.4},"
     f"afade=t=in:d=1.5,adelay={int((m['switch'] - 1.5) * 1000)}|{int((m['switch'] - 1.5) * 1000)}[e];"
     f"[0:a][b][e]amix=inputs=3:normalize=0:duration=first,asetpts=N/SR/TB,afade=t=out:st={total - 1}:d=1[a];"
     + VFADE,
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
