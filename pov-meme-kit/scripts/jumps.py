import sys, subprocess, numpy as np
raw = subprocess.run(["ffmpeg","-v","error","-i",sys.argv[1],"-vf","scale=90:160,format=gray","-f","rawvideo","-"],capture_output=True).stdout
f = np.frombuffer(raw,np.uint8).reshape(-1,160,90).astype(float)
d = np.abs(np.diff(f,axis=0)).mean((1,2))
med = np.median(d)
spikes = [(round((i+1)/30,2), round(v,1)) for i,v in enumerate(d) if v > max(6*med, 4)]
print("median", round(med,2), "spikes", spikes)
