"""Cinematic finish for a SAD story + a real clip appended after it.

usage: python3 scripts/cinematic.py stories/x.json out/x_with-sound.mp4 real.mp4 out.mp4 [real_ss real_to]
- warm grade + soft vignette on the cat part
- 0.6 s cross-dissolves (picture + sound) instead of hard scene cuts
- dip to black into the real clip; the song's climax keeps playing quietly under it
- real clip cropped to its footage band (crop W:H:X:Y via REAL_CROP env, default the upload1 band)
  over a blurred copy of itself
- ending: last real frame freezes, dims, white closing line + share line
"""
import json, os, subprocess, sys

story, meme, real, out = sys.argv[1:5]
rss, rto = (float(sys.argv[5]), float(sys.argv[6])) if len(sys.argv) > 6 else (12.0, 53.0)
crop = os.environ.get("REAL_CROP", "720:738:0:271")
s = json.load(open(story))
durs = [sc["dur"] for sc in s["scenes"]]
X = 0.6                      # dissolve length
HOLD = 4.0                   # freeze at the end
FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
m = s["music"]
song_pos = m["end_ss"] + (sum(durs) - m["switch"])   # where the climax is when the cat part ends
rlen = rto - rss

fc, t = [], 0.0
for i, d in enumerate(durs):
    fc.append(f"[0:v]trim={t}:{t+d},setpts=PTS-STARTPTS,"
              f"eq=saturation=0.92:gamma_r=1.04:gamma_b=0.95,vignette=PI/5,format=yuv420p,setsar=1[v{i}]")
    fc.append(f"[0:a]atrim={t}:{t+d},asetpts=PTS-STARTPTS[a{i}]")
    t += d
lv, la, off = "v0", "a0", durs[0]
for i in range(1, len(durs)):
    off -= X
    fc.append(f"[{lv}][v{i}]xfade=transition=fade:duration={X}:offset={off}[xv{i}]")
    fc.append(f"[{la}][a{i}]acrossfade=d={X}[xa{i}]")
    lv, la, off = f"xv{i}", f"xa{i}", off + durs[i]
mlen = off
fc.append(f"[{lv}]fade=t=out:st={mlen-1.0}:d=1.0[mv]")
fc.append(f"[{la}]afade=t=out:st={mlen-1.2}:d=1.2,aresample=44100,aformat=channel_layouts=stereo[ma]")
# real clip
fc.append(f"[1:v]trim={rss}:{rto},setpts=PTS-STARTPTS,crop={crop},split[ra][rb];"
          f"[ra]scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920,boxblur=30:2,eq=brightness=-0.15[rbg];"
          f"[rb]scale=1080:-2[rfg];[rbg][rfg]overlay=0:(H-h)/2,fps=30,format=yuv420p,setsar=1,"
          f"tpad=stop_mode=clone:stop_duration={HOLD},"
          f"fade=t=in:d=0.8,"
          f"drawbox=enable='gte(t,{rlen})':x=0:y=0:w=iw:h=ih:color=black@0.55:t=fill,"
          f"drawtext=fontfile={FONT}:text='Some voices never leave us.':fontcolor=white:fontsize=60:"
          f"x=(w-tw)/2:y=760:enable='gte(t,{rlen+0.4})',"
          f"drawtext=fontfile={FONT}:text='Share this with someone who needs to see it':fontcolor=#FFE600:"
          f"fontsize=40:x=(w-tw)/2:y=1080:enable='gte(t,{rlen+1.6})'[rv]")
fc.append(f"[1:a]atrim={rss}:{rto},asetpts=PTS-STARTPTS,aresample=44100,aformat=channel_layouts=stereo,"
          f"afade=t=in:d=0.8,apad=pad_dur={HOLD}[raw]")
fc.append(f"[2:a]atrim=0:{rlen+HOLD},asetpts=PTS-STARTPTS,aresample=44100,aformat=channel_layouts=stereo,"
          f"volume=0.16,afade=t=in:d=2.5,volume=enable='gte(t,{rlen})':volume=3.5,"
          f"afade=t=out:st={rlen+HOLD-1.5}:d=1.5[song]")
fc.append("[raw][song]amix=inputs=2:duration=first:normalize=0[ra2]")
fc.append("[mv][ma][rv][ra2]concat=n=2:v=1:a=1[v][a]")
cmd = ["ffmpeg", "-v", "error", "-y", "-i", meme, "-i", real, "-ss", str(song_pos), "-i", m["end"],
       "-filter_complex", ";".join(fc), "-map", "[v]", "-map", "[a]",
       "-c:v", "libx264", "-crf", "18", "-r", "30", "-c:a", "aac", "-b:a", "192k", out]
subprocess.run(cmd, check=True)
print("CINEMATIC DONE", round(mlen + rlen + HOLD, 2))
