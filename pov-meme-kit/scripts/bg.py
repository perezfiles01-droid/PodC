"""Generate photoreal 9:16 scene backgrounds with SD-Turbo (free, runs on CPU).

usage: python3 scripts/bg.py assets/backgrounds/<name> "photo of ... , realistic interior photography, empty, no people, wide angle" [seed]
Writes <name>_0.png and <name>_1.png; pick the better one and rename it to <name>.png.
Keep prompts SIMPLE and plain: "simple plain ..., minimal decor, soft natural light, eye-level".
Reject top-down food shots or images with people/animals and regenerate with another seed.
"""
import sys, torch
from diffusers import AutoPipelineForText2Image

p = AutoPipelineForText2Image.from_pretrained("stabilityai/sd-turbo", torch_dtype=torch.float32)
seed = int(sys.argv[3]) if len(sys.argv) > 3 else 10  # try another seed if the shots come out odd
for i in range(2):
    g = torch.Generator().manual_seed(seed + i)
    p(prompt=sys.argv[2], num_inference_steps=4, guidance_scale=0.0, width=576, height=1024,
      generator=g).images[0].save(f"{sys.argv[1]}_{i}.png")
