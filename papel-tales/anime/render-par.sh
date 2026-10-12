#!/bin/sh
# Split render: same frames, rendered in parallel chunks, joined without re-encoding.
# usage: sh render-par.sh page.html out.mp4 [workers]
PAGE="$1"; OUT="$2"; NW="${3:-4}"
N=$(python3 -c "import json,math;print(math.ceil(json.load(open('timeline.json'))['T']*30))")
STEP=$(( (N + NW - 1) / NW )); rm -f seg_*.mp4 segs.txt
i=0; pids=""
while [ $i -lt $NW ]; do
  S=$((i*STEP)); E=$(((i+1)*STEP)); [ $E -gt $N ] && E=$N
  node render1440-beats.js "$PAGE" "" "seg_$i.mp4" $S $E > seg_$i.log 2>&1 &
  pids="$pids $!"; echo "file 'seg_$i.mp4'" >> segs.txt; i=$((i+1))
done
for p in $pids; do wait $p || { echo "chunk failed"; exit 1; }; done
ffmpeg -y -v error -f concat -safe 0 -i segs.txt -c copy video-only.mp4 &&
ffmpeg -y -v error -i video-only.mp4 -i mix.wav -c:v copy -af apad -c:a aac -b:a 160k -shortest -movflags +faststart "$OUT" &&
echo "frames $N, chunks $NW -> $OUT"
