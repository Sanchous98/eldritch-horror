#!/usr/bin/env python3
"""One-off: placeholder assets for the cultist faction batch (five mobs + eggs + loot).
Deterministic, matches the repository's existing placeholder style. Art is the user's job."""
import json
import os
import random

from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src/main/resources")
ASSETS = os.path.join(ROOT, "assets/eldritch_horror")
DATA = os.path.join(ROOT, "data/eldritch_horror")
ENTITY = os.path.join(ASSETS, "textures/entity")
ITEMTEX = os.path.join(ASSETS, "textures/item")
MODELS = os.path.join(ASSETS, "models/item")
ITEMDEFS = os.path.join(ASSETS, "items")
LOOT = os.path.join(DATA, "loot_table/entities")

# id -> (sheet size, base, accent)
ENT = {
    "worshipper":   ((64, 64), (90, 70, 60), (150, 40, 40)),
    "cult_zealot":  ((64, 64), (60, 40, 55), (170, 60, 50)),
    "cult_raider":  ((64, 64), (55, 50, 40), (140, 110, 60)),
    "rite_binder":  ((64, 64), (45, 45, 60), (120, 90, 170)),
    "plague_crone": ((64, 128), (60, 80, 50), (150, 130, 60)),
}
EGG = {
    "worshipper":   ((90, 70, 60), (150, 40, 40)),
    "cult_zealot":  ((60, 40, 55), (170, 60, 50)),
    "cult_raider":  ((55, 50, 40), (140, 110, 60)),
    "rite_binder":  ((45, 45, 60), (120, 90, 170)),
    "plague_crone": ((60, 80, 50), (150, 130, 60)),
}

for name, (size, base, accent) in ENT.items():
    rng = random.Random(name)
    img = Image.new("RGBA", size, (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    w, hgt = size
    for y in range(hgt):
        for x in range(w):
            r = rng.random()
            if r < 0.05:
                img.putpixel((x, y), (*accent, 255))
            elif r < 0.10:
                img.putpixel((x, y), (max(0, base[0] - 25), max(0, base[1] - 25), max(0, base[2] - 25), 255))
            elif r < 0.40:
                img.putpixel((x, y), (*base, 255))
    for i in range(0, hgt, 16):
        d.line([(0, i), (w - 1, i)],
               fill=(max(0, base[0] - 35), max(0, base[1] - 35), max(0, base[2] - 35), 255))
    img.save(os.path.join(ENTITY, name + ".png"))

for name, (shell, spot) in EGG.items():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([2, 1, 13, 14], fill=(*shell, 255))
    rng = random.Random(name)
    for _ in range(14):
        x = rng.randint(3, 12)
        y = rng.randint(2, 13)
        if 2 <= x <= 13 and 1 <= y <= 14:
            d.point((x, y), fill=(*spot, 255))
    img.save(os.path.join(ITEMTEX, name + "_spawn_egg.png"))
    with open(os.path.join(MODELS, name + "_spawn_egg.json"), "w") as f:
        json.dump({"parent": "item/generated",
                   "textures": {"layer0": f"eldritch_horror:item/{name}_spawn_egg"}}, f, indent=2)
        f.write("\n")
    with open(os.path.join(ITEMDEFS, name + "_spawn_egg.json"), "w") as f:
        json.dump({"model": {"type": "minecraft:model",
                             "model": f"eldritch_horror:item/{name}_spawn_egg"}}, f, indent=2)
        f.write("\n")


def drop(name, lo, hi):
    entry = {"type": "minecraft:item", "name": name}
    if lo != hi:
        entry["modifier"] = {"type": "minecraft:set_count",
                             "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}
    return entry


LOOTABLE = {
    "worshipper": [drop("minecraft:emerald", 0, 1), drop("minecraft:bread", 0, 1)],
    "cult_zealot": [drop("eldritch_horror:bone_reagent", 1, 2), drop("minecraft:bone", 0, 2)],
    "cult_raider": [drop("eldritch_horror:silver_reagent", 0, 1), drop("minecraft:iron_nugget", 0, 2)],
    "rite_binder": [drop("eldritch_horror:chalk_reagent", 1, 2), drop("eldritch_horror:spirit_ash", 0, 1)],
    "plague_crone": [drop("eldritch_horror:mandrake_root", 1, 2), drop("eldritch_horror:veil_dust", 0, 1)],
}
for name, entries in LOOTABLE.items():
    with open(os.path.join(LOOT, name + ".json"), "w") as f:
        json.dump({"type": "minecraft:entity", "pools": [{"rolls": 1, "entries": entries}]}, f, indent=2)
        f.write("\n")

print("wrote", len(ENT), "entity textures,", len(EGG), "egg icons/models/defs,", len(LOOTABLE), "loot tables")
