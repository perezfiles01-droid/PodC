#!/bin/bash
# Setup for a fresh cloud session. Building videos only needs ffmpeg (preinstalled) + these:
pip install -q numpy pillow python-docx
# OPTIONAL — only to generate NEW backgrounds (scripts/bg.py). Large download (~3 GB):
#   pip install -q torch diffusers transformers accelerate
# (pytorch.org's index is blocked in Claude cloud sessions; install torch from PyPI as above.)
