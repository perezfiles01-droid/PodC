"""Generate many NEW backgrounds in one go (model loads once). Every scene gets its own image.

usage: python3 scripts/bg_batch.py plan.json      plan = {"assets/backgrounds/<story>/scene1.png": "prompt", ...}
Prompts: keep them simple and plain ("simple plain ..., minimal decor, soft natural light, eye-level,
realistic photo, empty, no people, no animals").
"""
import json, os, sys, torch
from diffusers import AutoPipelineForText2Image

plan = json.load(open(sys.argv[1]))
p = AutoPipelineForText2Image.from_pretrained("stabilityai/sd-turbo", torch_dtype=torch.float32)
for i, (out, prompt) in enumerate(plan.items()):
    os.makedirs(os.path.dirname(out), exist_ok=True)
    g = torch.Generator().manual_seed(1000 + i * 7)
    p(prompt=prompt, num_inference_steps=4, guidance_scale=0.0, width=576, height=1024,
      generator=g).images[0].save(out)
    print("ok", out, flush=True)
