"""Generate NEW backgrounds in one go (model loads once). Every scene gets its own image.

usage: python3 scripts/bg_batch.py plan.json
plan = {"assets/backgrounds/<story>/scene1.png": "prompt", ...}   or   {"...png": ["prompt", seed]}
Renders 1024x576 (landscape) and crops the centre to portrait: tall 576x1024 renders often come out
tiled/split or top-down. Look at every result (contact sheet); re-run a bad one with another seed.
Prompts — SAD: "simple plain ..., minimal decor, soft natural light, eye-level, realistic photo, empty,
no people, no animals".  FUNNY: "..., bright sunny daylight, sharp, colorful, realistic photo, eye-level,
wide floor visible, no people, no animals".
"""
import json, os, sys, torch
from diffusers import AutoPipelineForText2Image

plan = json.load(open(sys.argv[1]))
p = AutoPipelineForText2Image.from_pretrained("stabilityai/sd-turbo", torch_dtype=torch.float32)
for i, (out, v) in enumerate(plan.items()):
    prompt, seed = (v, 1000 + i * 7) if isinstance(v, str) else v
    os.makedirs(os.path.dirname(out), exist_ok=True)
    im = p(prompt=prompt, num_inference_steps=4, guidance_scale=0.0, width=1024, height=576,
           generator=torch.Generator().manual_seed(seed)).images[0]
    im.crop((350, 0, 674, 576)).resize((576, 1024)).save(out)
    print("ok", out, flush=True)
