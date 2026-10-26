#!/bin/bash
# Setup for a fresh cloud session. ffmpeg is preinstalled. Building needs numpy/pillow; every new video
# also needs NEW backgrounds (scripts/bg_batch.py), which needs torch + diffusers (~3 GB, a few minutes).
# pytorch.org's index is blocked in Claude cloud sessions — torch comes from PyPI.
pip install -q numpy pillow python-docx
pip install -q torch diffusers transformers accelerate
python3 -c "import torch, diffusers; print('setup OK')"
# Animation layer (optional): Remotion + HyperFrames + GSAP, pinned in motion/package.json.
(cd motion && npm install --no-audit --no-fund >/dev/null 2>&1) && echo "motion OK (remotion + hyperframes)"
