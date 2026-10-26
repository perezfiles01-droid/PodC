"""QA: contact sheet of frames at given seconds, to LOOK at before sending.
usage: python3 scripts/contact.py out.mp4 sheet.png 0 3 9.5 16 ..."""
import sys, subprocess
sel = "+".join(f"eq(n\\,{int(float(t) * 30)})" for t in sys.argv[3:])
subprocess.run(["ffmpeg", "-v", "error", "-y", "-i", sys.argv[1], "-vf",
                f"select='{sel}',scale=270:480,tile={len(sys.argv) - 3}x1", "-frames:v", "1", sys.argv[2]], check=True)
print("saved", sys.argv[2])
