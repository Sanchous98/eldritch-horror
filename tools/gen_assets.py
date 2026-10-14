#!/usr/bin/env python3
"""Procedural asset generator for Eldritch Horror (Pillow).

Everything here is deterministic (seeded per file) and palette-driven, so the whole look can be
retuned in one place and regenerated with one command:

    python3 tools/gen_assets.py

What it writes (paths relative to the repo root):
- src/main/resources/logo.png / pack.png        128x128 mod logo and pack icon
- .../textures/item/<id>.png                     16x16 icons for content missing one
- .../textures/entity/<id>.png                   an organic "flesh" skin per entity, in that
                                                 entity's palette, at the size the old file used
- .../items/<id>.json                            the item model definition a new icon needs

The entity skins are deliberately UV-layout agnostic: layered bands, wrapped tendrils and a few
watching eyes read as an organic mass no matter how the model's islands are mapped. That is a real
upgrade over the flat noise/dither placeholders the entities ship with now, but it is still
programmer art - a human pass is welcome for hero assets.
"""
from __future__ import annotations

import hashlib
import math
import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/eldritch_horror"
TEX_ENTITY = ASSETS / "textures/entity"
TEX_ITEM = ASSETS / "textures/item"
ITEM_DEFS = ASSETS / "items"

VOID = (9, 6, 16)
BONE = (214, 204, 222)
INK = (4, 3, 8)

# --- entity palettes: base (darkest), mid, accent ------------------------------------------------
PALETTES: dict[str, tuple[tuple[int, int, int], tuple[int, int, int], tuple[int, int, int]]] = {
    # Ancient Ones (design/28)
    "cthulhu": ((5, 22, 30), (28, 82, 92), (126, 214, 200)),
    "dunwich_horror": ((16, 26, 10), (62, 92, 32), (176, 200, 96)),
    "shub_niggurath": ((12, 24, 14), (44, 96, 40), (150, 200, 90)),
    "azathoth": ((14, 10, 30), (52, 36, 96), (170, 130, 230)),
    "yog_sothoth": ((22, 16, 12), (92, 70, 40), (230, 200, 120)),
    "ithaqua": ((18, 26, 34), (86, 116, 140), (206, 236, 250)),
    "yig": ((18, 24, 10), (86, 108, 30), (196, 214, 80)),
    "atlach_nacha": ((12, 8, 18), (66, 40, 80), (190, 140, 210)),
    "nyarlathotep": ((10, 9, 14), (60, 48, 30), (226, 190, 90)),
    "cthugha": ((30, 8, 4), (140, 44, 12), (255, 160, 60)),
    "glaaki": ((10, 24, 20), (40, 100, 84), (140, 220, 170)),
    "hydra": ((12, 22, 14), (48, 96, 56), (150, 220, 120)),
    "nyogtha": ((14, 8, 16), (58, 30, 60), (180, 110, 160)),
    "rhan_tegoth": ((20, 16, 12), (90, 70, 50), (220, 190, 140)),
    "hastur": ((26, 22, 6), (120, 104, 20), (240, 226, 110)),
    "nephren_ka": ((18, 14, 10), (86, 66, 42), (214, 186, 130)),
    "abhoth": ((22, 18, 22), (98, 80, 92), (226, 190, 200)),
    "chaugnar_faugn": ((20, 12, 10), (94, 52, 40), (226, 150, 120)),
    "tulzscha": ((12, 26, 10), (60, 120, 40), (180, 240, 120)),
    "zstylzhemghi": ((12, 10, 20), (56, 48, 92), (170, 156, 230)),
    "the_horror": ((8, 5, 16), (48, 26, 78), (170, 110, 210)),
    # cultists
    "worshipper": ((14, 12, 18), (60, 52, 76), (170, 158, 200)),
    "cult_zealot": ((20, 10, 10), (92, 40, 40), (220, 120, 110)),
    "cult_raider": ((20, 16, 10), (92, 72, 40), (220, 190, 120)),
    "rite_binder": ((10, 14, 20), (44, 66, 92), (140, 180, 230)),
    "plague_crone": ((14, 18, 10), (66, 84, 40), (170, 200, 110)),
}


def palette_for(name: str) -> tuple[tuple[int, int, int], tuple[int, int, int], tuple[int, int, int]]:
    """A palette from the table, or a deterministic one derived from the id."""
    if name in PALETTES:
        return PALETTES[name]
    digest = hashlib.sha256(name.encode()).digest()
    hue = digest[0] / 255.0
    sat = 0.25 + digest[1] / 255.0 * 0.35
    import colorsys

    dark = colorsys.hsv_to_rgb(hue, sat * 0.8, 0.10)
    mid = colorsys.hsv_to_rgb(hue, sat, 0.34)
    accent = colorsys.hsv_to_rgb(hue, sat * 0.7, 0.80)
    return (tuple(round(c * 255) for c in dark),
            tuple(round(c * 255) for c in mid),
            tuple(round(c * 255) for c in accent))


def _seed(name: str) -> int:
    return int.from_bytes(hashlib.sha256(name.encode()).digest()[:4], "big")


def _radial(size: int, inner: tuple[int, int, int], outer: tuple[int, int, int]) -> Image.Image:
    img = Image.new("RGB", (size, size), outer)
    draw = ImageDraw.Draw(img)
    for i in range(size, 0, -1):
        t = i / size
        colour = tuple(round(outer[c] + (inner[c] - outer[c]) * (1.0 - t)) for c in range(3))
        r = size * t / 2
        draw.ellipse([size / 2 - r, size / 2 - r, size / 2 + r, size / 2 + r], fill=colour)
    return img


def logo(size: int = 128) -> Image.Image:
    """The mod's sigil: a void disc, a chalk ring, eight tentacles and one open eye."""
    random.seed(7)
    scale = 4
    s = size * scale
    img = _radial(s, (18, 12, 30), VOID)
    draw = ImageDraw.Draw(img, "RGBA")
    c = s / 2
    for i in range(8):
        base = math.radians(i * 45 + 22)
        pts = []
        for step in range(26):
            t = step / 25
            radius = s * (0.18 + 0.30 * t)
            wiggle = math.sin(t * math.pi * 2.2 + i) * s * 0.035 * t
            angle = base + wiggle / s * 6
            pts.append((c + math.cos(angle) * radius, c + math.sin(angle) * radius))
        draw.line(pts, fill=(*PALETTES["the_horror"][1], 235), width=int(s * 0.072), joint="curve")
    ring = s * 0.34
    draw.ellipse([c - ring, c - ring, c + ring, c + ring],
                 outline=(*PALETTES["the_horror"][2], 150), width=int(s * 0.006))
    for i in range(24):
        angle = math.radians(i * 15)
        long = i % 3 == 0
        r0, r1 = ring + s * 0.012, ring + s * (0.045 if long else 0.028)
        draw.line([c + math.cos(angle) * r0, c + math.sin(angle) * r0,
                   c + math.cos(angle) * r1, c + math.sin(angle) * r1],
                  fill=(*BONE, 220), width=int(s * 0.007))
    eye_w, eye_h = s * 0.30, s * 0.17
    draw.ellipse([c - eye_w, c - eye_h, c + eye_w, c + eye_h], fill=(*BONE, 255))
    iris = s * 0.115
    draw.ellipse([c - iris, c - iris * 1.25, c + iris, c + iris * 1.25], fill=(232, 96, 120, 255))
    draw.ellipse([c - iris * 0.45, c - iris * 0.9, c + iris * 0.45, c + iris * 0.9], fill=(*INK, 255))
    for i in range(9):
        angle = math.radians(random.uniform(0, 360))
        r0, r1 = iris * 1.15, iris * 1.75
        draw.line([c + math.cos(angle) * r0, c + math.sin(angle) * r0 * 1.2,
                   c + math.cos(angle) * r1, c + math.sin(angle) * r1 * 1.2],
                  fill=(232, 96, 120, 140), width=int(s * 0.004))
    draw.ellipse([c - eye_w, c - eye_h, c + eye_w, c + eye_h], outline=(*INK, 255), width=int(s * 0.01))
    img = img.resize((size, size), Image.LANCZOS)
    grain = Image.effect_noise((size, size), 18).convert("L")
    return Image.composite(img, Image.new("RGB", (size, size), VOID),
                           grain.point(lambda v: 235 + v // 8))


def mob_skin(name: str, size: tuple[int, int]) -> Image.Image:
    """An organic skin for {@code name}: layered flesh, wrapped tendrils, a few watching eyes."""
    dark, mid, accent = palette_for(name)
    rng = random.Random(_seed(name))
    w, h = size
    img = Image.new("RGB", (w, h), dark)
    px = img.load()
    # Layered flesh: wrapped bands with a slow warp and per-pixel grain.
    phase = rng.uniform(0, math.tau)
    for y in range(h):
        for x in range(w):
            wobble = math.sin((x / w) * math.tau * 2.0 + phase + math.sin(y / h * math.tau * 2) * 1.5)
            depth = 0.5 + 0.5 * math.sin((y / h) * math.tau * 3.0 + x * 0.12)
            t = (wobble * 0.5 + 0.5) * 0.65 + depth * 0.35
            base = tuple(round(dark[i] + (mid[i] - dark[i]) * t) for i in range(3))
            jitter = rng.randint(-5, 5)
            px[x, y] = tuple(max(0, min(255, base[i] + jitter)) for i in range(3))
    draw = ImageDraw.Draw(img, "RGBA")
    # Tendrils, wrapped on both axes so no seam shows.
    for strand in range(max(6, w // 4)):
        x0 = rng.uniform(0, w)
        strand_phase = rng.uniform(0, math.tau)
        amp = rng.uniform(0.6, 2.6)
        freq = rng.uniform(1.0, 2.4)
        colour = accent if strand % 4 == 0 else mid
        lit = tuple(min(255, c + 30) for c in colour)
        for y in range(h):
            x = (x0 + math.sin(y / h * math.tau * freq + strand_phase) * amp) % w
            draw.point((x, y), fill=(*lit, 210))
            if rng.random() < 0.02:
                draw.point(((x + 1) % w, y), fill=(*accent, 120))
    # Eyes: a few wrapped clusters (never more than a handful - a mass, not a peacock).
    for _ in range(rng.randint(3, 7)):
        ex, ey = rng.uniform(0, w), rng.uniform(0, h)
        rx = rng.uniform(1.2, 2.4)
        for dx in (-w, 0, w):
            for dy in (-h, 0, h):
                draw.ellipse([ex + dx - rx, ey + dy - rx * 0.7, ex + dx + rx, ey + dy + rx * 0.7],
                             fill=(*BONE, 235))
                draw.ellipse([ex + dx - rx * 0.4, ey + dy - rx * 0.4,
                              ex + dx + rx * 0.4, ey + dy + rx * 0.4], fill=(*INK, 255))
    return img


def spawn_egg_icon(colour: tuple[int, int, int] = PALETTES["the_horror"][2]) -> Image.Image:
    """A 16x16 spawn-egg icon (base + spots), matching the mod's egg look."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    body = (238, 236, 232, 255)
    draw.ellipse([3, 4, 12, 14], fill=body)
    draw.ellipse([3.5, 3.5, 11.5, 8.5], fill=(250, 250, 248, 255))
    for spot in ((6, 8), (9, 9), (7, 11), (10, 12), (5, 12)):
        draw.ellipse([spot[0] - 1.3, spot[1] - 1.3, spot[0] + 1.3, spot[1] + 1.3],
                     fill=(*colour, 235))
    draw.arc([3, 4, 12, 14], 200, 330, fill=(150, 140, 150, 255))
    return img


def main() -> None:
    # 1. Logo and pack icon.
    (ROOT / "src/main/resources").mkdir(parents=True, exist_ok=True)
    sigil = logo()
    sigil.save(ROOT / "src/main/resources/logo.png")
    sigil.save(ROOT / "src/main/resources/pack.png")

    # 2. Entity skins: same size as the placeholder they replace.
    TEX_ENTITY.mkdir(parents=True, exist_ok=True)
    count = 0
    for path in sorted(TEX_ENTITY.glob("*.png")):
        with Image.open(path) as old:
            size = old.size
        mob_skin(path.stem, size).save(path)
        count += 1

    # 3. Icons for content that has none: texture, item definition and per-item model.
    TEX_ITEM.mkdir(parents=True, exist_ok=True)
    missing = {"the_horror_spawn_egg": PALETTES["the_horror"]}
    for item, palette in missing.items():
        spawn_egg_icon(palette[2]).save(TEX_ITEM / f"{item}.png")
        ITEM_DEFS.mkdir(parents=True, exist_ok=True)
        for folder in (ASSETS / "items", ASSETS / "models/item"):
            src = folder / "abhoth_spawn_egg.json"
            (folder / f"{item}.json").write_text(src.read_text().replace("abhoth_spawn_egg", item))

    print(f"wrote logo.png, pack.png, {count} entity skins, {len(missing)} item icon(s) + model(s)")


if __name__ == "__main__":
    main()
