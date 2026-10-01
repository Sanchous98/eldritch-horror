#!/usr/bin/env python3
"""One-off: generate placeholder assets for the three Ancient Ones (boss layer).
Deterministic, matches the repository's existing placeholder style. Art is the user's job."""
import json
import os
import random
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src/main/resources/assets/eldritch_horror")
DATA = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src/main/resources/data/eldritch_horror")
ENTITY_TEX = os.path.join(ROOT, "textures/entity")
ITEM_TEX = os.path.join(ROOT, "textures/item")
MODELS = os.path.join(ROOT, "models/item")
ITEM_DEFS = os.path.join(ROOT, "items")

# id -> (base colour, accent colour) for the entity sheet (128x128, ravager layout).
ENTITIES = {
    "cthulhu": ((40, 70, 90), (60, 140, 150)),
    "dunwich_horror": ((90, 80, 60), (140, 60, 50)),
    "shub_niggurath": ((50, 70, 40), (110, 140, 60)),
}
# id -> (shell, spot) for the 16x16 spawn egg icon.
EGGS = {
    "cthulhu": ((40, 70, 90), (60, 140, 150)),
    "dunwich_horror": ((90, 80, 60), (140, 60, 50)),
    "shub_niggurath": ((50, 70, 40), (110, 140, 60)),
}

os.makedirs(ENTITY_TEX, exist_ok=True)
os.makedirs(ITEM_TEX, exist_ok=True)
os.makedirs(MODELS, exist_ok=True)
os.makedirs(ITEM_DEFS, exist_ok=True)

for name, (base, accent) in ENTITIES.items():
    rng = random.Random(name)
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # A coarse, banded placeholder: base field with darker seams and accent blotches, so it is
    # obviously not final but legible in-world.
    for y in range(128):
        for x in range(128):
            if rng.random() < 0.06:
                img.putpixel((x, y), (*accent, 255))
            elif rng.random() < 0.10:
                img.putpixel((x, y), (max(0, base[0] - 25), max(0, base[1] - 25), max(0, base[2] - 25), 255))
            elif rng.random() < 0.35:
                img.putpixel((x, y), (*base, 255))
    for i in range(0, 128, 16):
        d.line([(0, i), (127, i)], fill=(max(0, base[0] - 35), max(0, base[1] - 35), max(0, base[2] - 35), 255))
    img.save(os.path.join(ENTITY_TEX, name + ".png"))

for name, (shell, spot) in EGGS.items():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([2, 1, 13, 14], fill=(*shell, 255))
    rng = random.Random(name)
    for _ in range(14):
        x = rng.randint(3, 12)
        y = rng.randint(2, 13)
        if 2 <= x <= 13 and 1 <= y <= 14:
            d.point((x, y), fill=(*spot, 255))
    img.save(os.path.join(ITEM_TEX, name + "_spawn_egg.png"))
    with open(os.path.join(MODELS, name + "_spawn_egg.json"), "w") as f:
        json.dump({"parent": "item/generated",
                   "textures": {"layer0": f"eldritch_horror:item/{name}_spawn_egg"}}, f, indent=2)
        f.write("\n")
    with open(os.path.join(ITEM_DEFS, name + "_spawn_egg.json"), "w") as f:
        json.dump({"model": {"type": "minecraft:model",
                             "model": f"eldritch_horror:item/{name}_spawn_egg"}}, f, indent=2)
        f.write("\n")


def item_pool(entries):
    return {"pools": [{"rolls": 1, "entries": entries}]}


def drop(name, lo, hi):
    entry = {"type": "minecraft:item", "name": name}
    if lo != hi:
        entry["modifier"] = {"type": "minecraft:set_count",
                             "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}
    return entry


LOOT = {
    "cthulhu": [drop("eldritch_horror:pearl_reagent", 2, 4),
                drop("eldritch_horror:ichor_vial", 1, 1),
                drop("eldritch_horror:void_reagent", 1, 2)],
    "dunwich_horror": [drop("eldritch_horror:bone_reagent", 1, 3),
                       drop("eldritch_horror:grave_dust", 1, 2),
                       drop("eldritch_horror:veil_dust", 1, 1)],
    "shub_niggurath": [drop("eldritch_horror:ichor_vial", 1, 2),
                       drop("eldritch_horror:void_reagent", 1, 3),
                       drop("eldritch_horror:veil_dust", 2, 4)],
}
loot_dir = os.path.join(DATA, "loot_table/entities")
os.makedirs(loot_dir, exist_ok=True)
for name, entries in LOOT.items():
    with open(os.path.join(loot_dir, name + ".json"), "w") as f:
        json.dump({"type": "minecraft:entity", **item_pool(entries)}, f, indent=2)
        f.write("\n")

print("wrote 3 entity textures, 3 egg icons/models/defs, 3 loot tables")
