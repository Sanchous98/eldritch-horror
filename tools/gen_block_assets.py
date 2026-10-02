#!/usr/bin/env python3
"""Generate block assets for every registered stub block: a 16x16 texture, a block model +
blockstate, and the item model + 26.3 client item definition so it shows correctly in hand/GUI.

Derived from the block registry classes, deterministic, re-runnable. Artists replace the PNGs.

Usage: python3 tools/gen_block_assets.py
"""
import hashlib
import json
import os
import re

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "src/main/java/com/sanchous98/eldritchhorror/registry/blocks")
ASSETS = os.path.join(ROOT, "src/main/resources/assets/eldritch_horror")
MODELS_BLOCK = os.path.join(ASSETS, "models/block")
MODELS_ITEM = os.path.join(ASSETS, "models/item")
BLOCKSTATES = os.path.join(ASSETS, "blockstates")
ITEM_DEFS = os.path.join(ASSETS, "items")
TEXTURES = os.path.join(ASSETS, "textures/block")

# class -> (base colour, accent colour)
STYLE = {
    "RitualBlocks":   ((60, 50, 70),  (150, 90, 200)),
    "RiftBlocks":     ((25, 22, 40),  (120, 60, 200)),
    "CorruptedBlocks":((45, 60, 40),  (110, 160, 80)),
}


def ids_for(path):
    return re.findall(r'ModBlocks\.add\("([a-z_]+)"', open(path).read())


def registry():
    out = {}
    for fn in sorted(os.listdir(SRC)):
        # PrologueBlocks is bespoke: the city_gate has 24 city= property variants, which this
        # single-variant generator cannot express. Its assets are hand-written; do not touch them.
        if fn.endswith(".java") and fn != "PrologueBlocks.java":
            out[fn[:-5]] = ids_for(os.path.join(SRC, fn))
    return out


def shade(rgb, f):
    return tuple(max(0, min(255, int(c * f))) for c in rgb)


def texture(base, accent, seed):
    img = Image.new("RGBA", (16, 16), base + (255,))
    d = ImageDraw.Draw(img)
    # speckled stone with a few accent veins; deterministic per id
    for y in range(16):
        for x in range(16):
            h = (seed >> ((x * 7 + y * 13) % 29)) & 3
            if h == 0:
                img.putpixel((x, y), shade(base, 0.82) + (255,))
            elif h == 3:
                img.putpixel((x, y), shade(base, 1.14) + (255,))
    for i in range(4):
        vx = (seed >> (i * 3)) % 16
        vy = (seed >> (i * 5)) % 16
        d.line([(vx, vy), ((vx + 5) % 16, (vy + 7) % 16)], fill=accent + (255,))
    d.rectangle([0, 0, 15, 15], outline=shade(base, 0.6) + (255,))
    return img


def main():
    reg = registry()
    for p in (MODELS_BLOCK, MODELS_ITEM, BLOCKSTATES, ITEM_DEFS, TEXTURES):
        os.makedirs(p, exist_ok=True)
    n = 0
    for cls, ids in reg.items():
        base, accent = STYLE.get(cls, ((80, 80, 80), (150, 150, 150)))
        for b in ids:
            seed = int(hashlib.md5(b.encode()).hexdigest(), 16)
            texture(base, accent, seed).save(os.path.join(TEXTURES, b + ".png"))
            # block model (all faces same texture)
            json.dump({"parent": "block/cube_all",
                       "textures": {"all": f"eldritch_horror:block/{b}"}},
                      open(os.path.join(MODELS_BLOCK, b + ".json"), "w"), indent=2)
            open(os.path.join(MODELS_BLOCK, b + ".json"), "a").write("\n")
            # blockstate (single variant)
            json.dump({"variants": {"": {"model": f"eldritch_horror:block/{b}"}}},
                      open(os.path.join(BLOCKSTATES, b + ".json"), "w"), indent=2)
            open(os.path.join(BLOCKSTATES, b + ".json"), "a").write("\n")
            # item model (parent = the block model) + client item definition
            json.dump({"parent": f"eldritch_horror:block/{b}"},
                      open(os.path.join(MODELS_ITEM, b + ".json"), "w"), indent=2)
            open(os.path.join(MODELS_ITEM, b + ".json"), "a").write("\n")
            json.dump({"model": {"type": "minecraft:model", "model": f"eldritch_horror:item/{b}"}},
                      open(os.path.join(ITEM_DEFS, b + ".json"), "w"), indent=2)
            open(os.path.join(ITEM_DEFS, b + ".json"), "a").write("\n")
            n += 1
    print(f"wrote block assets for {n} blocks across {len(reg)} categories")


if __name__ == "__main__":
    main()
