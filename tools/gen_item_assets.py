#!/usr/bin/env python3
"""Generate item assets for every registered stub item: a 16x16 placeholder texture plus a
Minecraft item model JSON. Deterministic (seeded per id) and derived from the item registry, so
it stays in sync when items are added.

Artists replace the PNGs under assets/eldritch_horror/textures/item/ in place; the JSON and paths
never change.

Usage:
    python3 tools/gen_item_assets.py            # write models + textures
    python3 tools/gen_item_assets.py --sheet    # also write a contact sheet for review
"""
import hashlib
import json
import os
import re
import sys

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ITEMS_SRC = os.path.join(ROOT, "src/main/java/com/sanchous98/eldritchhorror/registry/items")
ASSETS = os.path.join(ROOT, "src/main/resources/assets/eldritch_horror")
MODELS = os.path.join(ASSETS, "models/item")
TEXTURES = os.path.join(ASSETS, "textures/item")
# Minecraft 1.21.4+/26.3: models are only used if referenced by a client item definition under
# assets/<ns>/items/<id>.json. Without this, every item renders as the missing-model placeholder.
ITEM_DEFS = os.path.join(ASSETS, "items")

# True weapons use the handheld model (pointing away from the hand); everything else is a flat
# "generated" icon.
HANDHELD = {
    "occult_tool", "silver_blade", "hollow_pact_blade", "ritual_knife", "wardens_maul",
    "bone_club", "star_iron_sword", "tidal_trident", "sacrificial_dagger", "censer_mace",
    "eye_scepter", "veil_bow",
}

# category class -> (draw kind, base colour, accent colour)
STYLE = {
    "Reagents":    ("flask",  (150, 200, 210), (110, 70, 40)),
    "Consumables": ("potion", (120, 180, 230), (150, 100, 60)),
    "Tomes":       ("book",   (90, 60, 40),    (210, 195, 160)),
    "Artifacts":   ("gem",    (120, 200, 220), (230, 210, 120)),
    "Weapons":     ("blade",  (200, 205, 210), (120, 80, 50)),
    "Conditions":  ("talisman",(90, 40, 60),   (220, 200, 160)),
    "Currency":    ("coin",   (215, 185, 90),  (140, 110, 50)),
    "Navigation":  ("map",    (235, 225, 190), (150, 90, 60)),
    "Factions":    ("badge",  (150, 60, 60),   (225, 210, 180)),
    "Keys":        ("key",    (205, 185, 95),  (120, 95, 50)),
    "Utility":     ("tool",   (150, 150, 155), (110, 80, 55)),
}


def ids_for(path):
    src = open(path).read()
    return re.findall(r'ModItems\.add\("([a-z_]+)"', src)


def registry():
    out = {}
    for fn in sorted(os.listdir(ITEMS_SRC)):
        if not fn.endswith(".java"):
            continue
        cls = fn[:-5]
        out[cls] = ids_for(os.path.join(ITEMS_SRC, fn))
    return out


def shade(rgb, f):
    return tuple(max(0, min(255, int(c * f))) for c in rgb)


def draw_icon(kind, base, accent, seed):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # per-id slight tint variation
    f = 0.9 + (seed % 5) * 0.04
    base = shade(base, f)
    a = accent
    if kind == "flask":
        d.rectangle([7, 2, 8, 5], fill=a)                 # neck (cork)
        d.polygon([(6, 5), (9, 5), (11, 13), (4, 13)], fill=base)
        d.rectangle([5, 8, 10, 9], fill=shade(base, 1.15))
    elif kind == "potion":
        d.rectangle([7, 2, 8, 4], fill=a)
        d.polygon([(6, 4), (9, 4), (10, 6), (10, 13), (5, 13), (5, 6)], fill=base)
        d.rectangle([6, 9, 9, 12], fill=shade(base, 1.2))
    elif kind == "book":
        d.rectangle([3, 3, 12, 13], fill=base)
        d.rectangle([4, 4, 11, 12], fill=shade(base, 1.25))
        d.rectangle([7, 4, 8, 12], fill=shade(base, 0.7))  # spine
        d.line([4, 5, 11, 5], fill=a)
    elif kind == "gem":
        d.polygon([(8, 2), (12, 7), (8, 14), (4, 7)], fill=base)
        d.line([(8, 2), (8, 14)], fill=shade(base, 1.3))
        d.polygon([(8, 2), (12, 7), (8, 7)], fill=shade(base, 1.2))
    elif kind == "blade":
        d.polygon([(4, 13), (12, 3), (13, 4), (5, 14)], fill=base)
        d.rectangle([3, 12, 6, 14], fill=a)                # guard/handle
        d.line([(11, 4), (12, 3)], fill=(240, 240, 240))
    elif kind == "talisman":
        d.polygon([(8, 3), (12, 8), (8, 13), (4, 8)], fill=base)
        d.ellipse([6, 6, 10, 10], fill=a)
        d.line([(8, 1), (8, 3)], fill=a)
    elif kind == "coin":
        d.ellipse([3, 3, 13, 13], fill=base)
        d.ellipse([5, 5, 11, 11], fill=shade(base, 1.15))
        d.line([(8, 5), (8, 11)], fill=a)
    elif kind == "map":
        d.rectangle([2, 4, 14, 12], fill=base)
        d.line([(6, 4), (6, 12)], fill=a)
        d.line([(10, 4), (10, 12)], fill=a)
        d.line([(3, 8), (13, 8)], fill=a)
    elif kind == "badge":
        d.polygon([(3, 3), (13, 3), (13, 9), (8, 14), (3, 9)], fill=base)
        d.ellipse([6, 5, 10, 9], fill=a)
    elif kind == "key":
        d.ellipse([4, 3, 9, 8], fill=base)
        d.ellipse([6, 5, 7, 6], fill=(0, 0, 0, 0))
        d.line([(8, 7), (12, 12)], fill=base, width=2)
        d.line([(11, 11), (13, 13)], fill=a)
        d.line([(9, 12), (11, 14)], fill=a)
    elif kind == "tool":
        d.line([(4, 13), (11, 5)], fill=a, width=2)        # haft
        d.polygon([(10, 3), (14, 3), (13, 7), (10, 7)], fill=base)
    else:
        d.rectangle([3, 3, 12, 12], fill=base)
    return img


def model_json(item_id, handheld):
    parent = "item/handheld" if handheld else "item/generated"
    return {
        "parent": parent,
        "textures": {"layer0": f"eldritch_horror:item/{item_id}"},
    }


def main():
    reg = registry()
    os.makedirs(MODELS, exist_ok=True)
    os.makedirs(TEXTURES, exist_ok=True)
    os.makedirs(ITEM_DEFS, exist_ok=True)
    written_models = written_tex = written_defs = 0
    for cls, ids in reg.items():
        kind, base, accent = STYLE.get(cls, ("tool", (150, 150, 150), (100, 100, 100)))
        for item_id in ids:
            seed = int(hashlib.md5(item_id.encode()).hexdigest(), 16)
            img = draw_icon(kind, base, accent, seed)
            img.save(os.path.join(TEXTURES, item_id + ".png"))
            written_tex += 1
            handheld = item_id in HANDHELD
            with open(os.path.join(MODELS, item_id + ".json"), "w") as f:
                json.dump(model_json(item_id, handheld), f, indent=2)
                f.write("\n")
            written_models += 1
            # client item definition (required in 26.3)
            with open(os.path.join(ITEM_DEFS, item_id + ".json"), "w") as f:
                json.dump({"model": {"type": "minecraft:model",
                                     "model": f"eldritch_horror:item/{item_id}"}}, f, indent=2)
                f.write("\n")
            written_defs += 1
    print(f"wrote {written_tex} textures, {written_models} models, {written_defs} item defs "
          f"across {len(reg)} categories")

    if "--sheet" in sys.argv:
        all_ids = [i for ids in reg.values() for i in ids]
        cols = 16
        cell = 32
        rows = (len(all_ids) + cols - 1) // cols
        sheet = Image.new("RGBA", (cols * cell, rows * cell), (30, 30, 36, 255))
        for n, item_id in enumerate(all_ids):
            im = Image.open(os.path.join(TEXTURES, item_id + ".png")).resize((cell, cell), Image.NEAREST)
            sheet.alpha_composite(im, ((n % cols) * cell, (n // cols) * cell))
        out = "/tmp/eh_item_sheet.png"
        sheet.convert("RGB").save(out)
        print("sheet:", out, len(all_ids), "icons")


if __name__ == "__main__":
    main()
