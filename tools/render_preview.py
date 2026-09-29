#!/usr/bin/env python3
"""Render a preview of the Earth map: hypsometric tint + curated city markers.

Reads the baked layers and writes docs/world-preview.png.
"""
import json
import numpy as np
from PIL import Image
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.patheffects as pe

Image.MAX_IMAGE_PIXELS = None
BASE = "src/main/resources/assets/eldritch_horror/map/"
W, H = 2048, 1024

land = np.asarray(Image.open(BASE + "landmask_16384x8192.png").convert("L").resize((W, H))) > 127
elev = np.asarray(Image.open(BASE + "elevation_16384x8192.png").resize((W, H))).astype(np.float32) - 12000.0

ocean_nodes = [-11000, -6000, -2000, -200, 0]
ocean_cols = [[6, 18, 55], [15, 50, 110], [35, 100, 160], [70, 150, 195], [95, 170, 205]]
land_nodes = [0, 200, 800, 2500, 4500, 6500, 8800]
land_cols = [[70, 130, 70], [110, 160, 80], [180, 175, 110], [170, 130, 80],
             [140, 110, 90], [200, 200, 205], [255, 255, 255]]


def colorize(v, nodes, cols):
    cols = np.array(cols, np.float32)
    return np.stack([np.interp(v, nodes, cols[:, ch]) for ch in range(3)], axis=-1)


rgb = np.where(land[..., None],
               colorize(elev, land_nodes, land_cols),
               colorize(elev, ocean_nodes, ocean_cols))
img = np.clip(rgb, 0, 255).astype(np.uint8)

fig, ax = plt.subplots(figsize=(20, 10), dpi=110)
ax.imshow(img, extent=[-65536, 65536, -32768, 32768], origin="upper")

sett = json.load(open(BASE + "settlements.json"))
cur = [p for p in sett["settlements"] if p["tier"] == "curated"]
for p in cur:
    ax.plot(p["x"], p["z"], "o", ms=7, mec="black", mew=0.8, mfc="#ff2d2d", zorder=5)
    ax.annotate(p["name"], (p["x"], p["z"]), xytext=(6, 4), textcoords="offset points",
                fontsize=9, color="white", weight="bold", zorder=6,
                path_effects=[pe.withStroke(linewidth=2.5, foreground="black")])

ax.set_title("Eldritch Horror — Earth map  (131,072 × 65,536 blocks, 1 block ≈ 0.61 km)  •  24 curated cities",
             fontsize=15)
ax.set_xlabel("x  (west ← → east)")
ax.set_ylabel("z  (north ↑ / south ↓)")
ax.set_xlim(-65536, 65536)
ax.set_ylim(32768, -32768)
ax.grid(color="white", alpha=0.15, linewidth=0.5)
fig.tight_layout()
fig.savefig("docs/world-preview.png", bbox_inches="tight")
print("wrote docs/world-preview.png")
