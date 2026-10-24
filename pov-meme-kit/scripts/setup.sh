#!/bin/bash
# One-time setup in a fresh cloud session (ffmpeg is already installed there).
# pytorch.org's download index is blocked in Claude cloud sessions, so torch comes from PyPI.
pip install -q numpy pillow torch diffusers transformers accelerate soundfile
