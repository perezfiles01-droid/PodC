#!/bin/bash
# SessionStart hook (wired in .claude/settings.json): installs the kit's tools in a fresh cloud session
# without blocking startup. Runs scripts/setup.sh in the background; progress/result in /tmp/pov-setup.log.
# Skips if everything is already there (resumed session).
cd "$(dirname "$0")/.." || exit 0
if python3 -c "import torch, diffusers, PIL, numpy" 2>/dev/null && [ -x motion/node_modules/.bin/hyperframes ]; then
  echo "pov-meme-kit: tools already installed"
  exit 0
fi
nohup bash scripts/setup.sh > /tmp/pov-setup.log 2>&1 &
echo "pov-meme-kit: installing tools in the background (see /tmp/pov-setup.log; 'setup OK' + 'motion OK' = done)"
