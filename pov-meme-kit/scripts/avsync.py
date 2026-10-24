"""QA: audio and video must both start at 0 and have the same length (else players 'fast-forward')."""
import json, subprocess, sys
info = json.loads(subprocess.run(["ffprobe", "-v", "error", "-show_entries", "stream=codec_type,start_time,duration",
                                  "-of", "json", sys.argv[1]], capture_output=True, text=True).stdout)
st = {s["codec_type"]: (float(s["start_time"]), float(s["duration"])) for s in info["streams"]}
(vs, vd), (as_, ad) = st["video"], st["audio"]
ok = abs(vs) < 0.05 and abs(as_) < 0.05 and abs(vd - ad) < 0.2
print(f"video start {vs:.2f} len {vd:.2f} | audio start {as_:.2f} len {ad:.2f} ->", "OK" if ok else "FAIL")
sys.exit(0 if ok else 1)
