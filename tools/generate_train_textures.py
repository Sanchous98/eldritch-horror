#!/usr/bin/env python3
"""Generate the scheduled train's entity textures.

Two 256x256 RGBA atlases, one for the passenger wagon and one for the locomotive. They share a
single UV layout (the regions below), which is mirrored by the constants at the top of
``client/model/TrainCarModel.java``: keep the two in sync. The window region is fully transparent
apart from a few pale reflection streaks, so ``entityCutout`` discards it and the carriage windows
are genuine openings with glazing bars.

Usage:
    python3 tools/generate_train_textures.py              # write both atlases
    python3 tools/generate_train_textures.py wagon        # only the wagon
    python3 tools/generate_train_textures.py --list

Pillow is used when installed; otherwise a small built-in RGBA PNG encoder writes the same pixels.
The script is deterministic (seed = zlib.crc32(name)), so re-running it produces byte-identical
files.
"""

from __future__ import annotations

import argparse
import math
import random
import struct
import sys
import zlib
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
TEXTURE_DIR = REPO_ROOT / "src" / "main" / "resources" / "assets" / "eldritch_horror" / "textures" / "entity"

SIZE = 256

# name: (u, v, width, height) in the shared 256x256 atlas.
REGIONS = {
    "floor": (0, 0, 112, 35),
    "glass": (116, 0, 56, 52),
    "frame": (0, 40, 68, 37),
    "wall": (72, 56, 66, 46),
    "pillar": (142, 56, 20, 50),
    "door": (166, 56, 60, 50),
    "wheel": (230, 56, 24, 24),
    "upper": (0, 82, 66, 38),
    "roof": (136, 110, 110, 34),
    "brass": (0, 124, 40, 40),
    "nose": (44, 124, 80, 46),
    "lantern": (128, 148, 30, 30),
    "endwall": (0, 174, 40, 50),
    "glow": (44, 174, 20, 16),
    "bench": (76, 180, 60, 42),
    "metal": (140, 180, 102, 46),
}

# variant: material -> base, light, dark, accent
PALETTES = {
    "wagon": {
        "floor": ((120, 84, 52), (156, 114, 70), (74, 50, 30), (190, 160, 112)),
        "frame": ((52, 44, 38), (86, 74, 62), (30, 24, 20), (120, 100, 76)),
        "wall": ((98, 62, 38), (136, 92, 54), (58, 34, 20), (186, 146, 92)),
        "upper": ((186, 172, 140), (216, 204, 172), (136, 122, 96), (196, 164, 90)),
        "pillar": ((76, 48, 30), (112, 74, 44), (42, 26, 16), (188, 152, 88)),
        "roof": ((58, 60, 62), (92, 96, 98), (28, 30, 32), (142, 148, 150)),
        "metal": ((64, 66, 68), (104, 108, 110), (30, 32, 34), (146, 152, 156)),
        "brass": ((166, 124, 50), (224, 186, 96), (96, 66, 22), (255, 224, 148)),
        "bench": ((118, 40, 44), (166, 70, 70), (70, 22, 26), (214, 152, 110)),
        "door": ((92, 58, 34), (130, 88, 50), (54, 32, 18), (184, 144, 90)),
        "endwall": ((88, 56, 34), (124, 84, 48), (50, 30, 18), (176, 136, 86)),
        "wheel": ((48, 48, 50), (92, 92, 94), (20, 20, 22), (168, 120, 48)),
        "nose": ((64, 72, 66), (100, 112, 102), (30, 36, 32), (170, 140, 70)),
        "lantern": ((70, 58, 36), (116, 98, 60), (36, 28, 18), (250, 210, 110)),
        "glass": ((168, 206, 220), (210, 238, 246), (120, 164, 182), (245, 255, 255)),
        "glow": ((255, 206, 92), (255, 238, 168), (206, 132, 36), (255, 255, 232)),
    },
    "locomotive": {
        "floor": ((112, 78, 48), (148, 108, 66), (68, 46, 28), (184, 154, 106)),
        "frame": ((42, 38, 34), (76, 68, 58), (22, 20, 18), (112, 94, 70)),
        "wall": ((44, 58, 48), (72, 92, 76), (24, 34, 28), (152, 124, 64)),
        "upper": ((58, 74, 60), (86, 108, 86), (32, 44, 36), (176, 146, 82)),
        "pillar": ((34, 44, 36), (58, 74, 60), (18, 24, 20), (166, 132, 68)),
        "roof": ((46, 48, 50), (78, 82, 84), (22, 24, 26), (128, 134, 136)),
        "metal": ((54, 56, 58), (92, 96, 98), (26, 28, 30), (140, 146, 150)),
        "brass": ((156, 116, 44), (216, 178, 90), (88, 60, 20), (255, 222, 146)),
        "bench": ((70, 46, 34), (110, 76, 54), (40, 24, 18), (198, 148, 104)),
        "door": ((40, 52, 44), (66, 84, 68), (22, 30, 26), (162, 132, 74)),
        "endwall": ((38, 50, 42), (62, 80, 66), (20, 28, 24), (156, 128, 72)),
        "wheel": ((44, 44, 46), (86, 86, 88), (18, 18, 20), (176, 128, 52)),
        "nose": ((44, 50, 48), (74, 84, 78), (20, 24, 22), (172, 142, 72)),
        "lantern": ((64, 54, 34), (108, 90, 56), (32, 26, 16), (250, 210, 110)),
        "glass": ((150, 190, 206), (198, 228, 240), (108, 150, 170), (240, 252, 255)),
        "glow": ((255, 198, 84), (255, 232, 156), (198, 122, 30), (255, 255, 232)),
    },
}


class Canvas:
    """Small RGBA canvas with alpha-aware source-over blending."""

    def __init__(self):
        self.w = SIZE
        self.h = SIZE
        self.data = bytearray(SIZE * SIZE * 4)

    def blend(self, x, y, rgb, alpha=1.0):
        if not (0 <= x < self.w and 0 <= y < self.h):
            return
        a = max(0.0, min(1.0, alpha))
        if a <= 0.0:
            return
        i = (y * self.w + x) * 4
        d = self.data
        da = d[i + 3] / 255.0
        oa = a + da * (1.0 - a)
        if oa <= 0.0:
            return
        for c in range(3):
            d[i + c] = int((rgb[c] * a + d[i + c] * da * (1.0 - a)) / oa)
        d[i + 3] = int(oa * 255)

    def clear_rect(self, x, y, w, h):
        for yy in range(max(0, y), min(self.h, y + h)):
            start = (yy * self.w + max(0, x)) * 4
            end = (yy * self.w + min(self.w, x + w)) * 4
            self.data[start:end] = b"\x00" * (end - start)

    def rect(self, x, y, w, h, color, alpha=1.0):
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                self.blend(xx, yy, color, alpha)

    def line(self, x0, y0, x1, y1, color, alpha=1.0):
        steps = max(1, int(max(abs(x1 - x0), abs(y1 - y0))))
        for i in range(steps + 1):
            t = i / steps
            self.blend(int(x0 + (x1 - x0) * t), int(y0 + (y1 - y0) * t), color, alpha)

    def disc(self, cx, cy, r, color, alpha=1.0):
        r2 = r * r
        for yy in range(int(cy - r) - 1, int(cy + r) + 2):
            for xx in range(int(cx - r) - 1, int(cx + r) + 2):
                if (xx - cx) ** 2 + (yy - cy) ** 2 <= r2:
                    self.blend(xx, yy, color, alpha)

    def rows(self):
        for y in range(self.h):
            yield bytes(self.data[y * self.w * 4:(y + 1) * self.w * 4])


def region(name):
    return REGIONS[name]


def paint_planks(canvas, name, pal, rng, seam=8, vertical=True):
    u, v, w, h = region(name)
    base, light, dark, accent = pal
    canvas.rect(u, v, w, h, base, 1.0)
    step = seam
    if vertical:
        for x in range(u, u + w, step):
            canvas.line(x, v, x, v + h - 1, dark, 0.55)
            canvas.line(x + 1, v, x + 1, v + h - 1, light, 0.22)
        for _ in range(w * h // 12):
            x = rng.randrange(u, u + w)
            y = rng.randrange(v, v + h)
            canvas.line(x, y, x + rng.randint(-6, 6), y + rng.randint(2, 12), dark, 0.10)
    else:
        for y in range(v, v + h, step):
            canvas.line(u, y, u + w - 1, y, dark, 0.55)
            canvas.line(u, y + 1, u + w - 1, y + 1, light, 0.22)
    for _ in range(max(4, w * h // 90)):
        x = rng.randrange(u, u + w)
        y = rng.randrange(v, v + h)
        canvas.disc(x, y, 0.8, accent, 0.5)


def paint_panels(canvas, name, pal, rng):
    u, v, w, h = region(name)
    base, light, dark, accent = pal
    canvas.rect(u, v, w, h, base, 1.0)
    # Recessed panel rectangles with a highlight along the top edge.
    for y in range(v + 4, v + h - 4, 16):
        canvas.rect(u + 3, y, w - 6, 12, dark, 0.20)
        canvas.line(u + 3, y, u + w - 4, y, light, 0.55)
        canvas.line(u + 3, y + 12, u + w - 4, y + 12, dark, 0.45)
    for _ in range(w * h // 140):
        canvas.disc(rng.randrange(u, u + w), rng.randrange(v, v + h), 0.7, dark, 0.35)


def paint_metal(canvas, name, pal, rng):
    u, v, w, h = region(name)
    base, light, dark, accent = pal
    canvas.rect(u, v, w, h, base, 1.0)
    for y in range(v, v + h, 9):
        canvas.line(u, y, u + w - 1, y, dark, 0.30)
        canvas.line(u, y + 1, u + w - 1, y + 1, light, 0.18)
    for _ in range(w * h // 80):
        x = rng.randrange(u + 1, u + w - 1)
        y = rng.randrange(v + 1, v + h - 1)
        canvas.disc(x, y, 0.9, accent, 0.55)
        canvas.disc(x, y, 0.35, dark, 0.55)


def paint_brass(canvas, name, pal, rng):
    u, v, w, h = region(name)
    base, light, dark, accent = pal
    canvas.rect(u, v, w, h, base, 1.0)
    for x in range(u, u + w, 6):
        canvas.line(x, v, x, v + h - 1, dark, 0.35)
        canvas.line(x + 1, v, x + 1, v + h - 1, light, 0.30)
    for _ in range(w * h // 120):
        canvas.disc(rng.randrange(u, u + w), rng.randrange(v, v + h), 0.7, accent, 0.45)


def paint_roof(canvas, pal, rng):
    u, v, w, h = region("roof")
    base, light, dark, accent = pal
    canvas.rect(u, v, w, h, base, 1.0)
    for y in range(v, v + h, 7):
        canvas.line(u, y, u + w - 1, y, dark, 0.55)
        canvas.line(u, y + 1, u + w - 1, y + 1, light, 0.28)
    for _ in range(w * h // 70):
        canvas.disc(rng.randrange(u, u + w), rng.randrange(v, v + h), 1.2, accent, 0.16)


def paint_glass(canvas, pal, rng):
    u, v, w, h = region("glass")
    canvas.clear_rect(u, v, w, h)
    base, light, dark, accent = pal
    # Sparse reflection streaks; everything else stays transparent (cutout).
    for _ in range(5):
        x = rng.randrange(u + 3, max(u + 4, u + w - 10))
        y = rng.randrange(v + 3, v + h - 14)
        canvas.line(x, y, x + rng.randint(3, 9), y + rng.randint(8, 18), accent, 0.42)
        canvas.line(x + 1, y, x + rng.randint(4, 10) + 1, y + rng.randint(8, 18), light, 0.20)
    for _ in range(8):
        x = rng.randrange(u, u + w)
        y = rng.randrange(v, v + h)
        canvas.blend(x, y, accent, 0.55)


def paint_wheel(canvas, pal, rng):
    u, v, w, h = region("wheel")
    base, light, dark, accent = pal
    canvas.rect(u, v, w, h, base, 1.0)
    cx, cy = u + w / 2, v + h / 2
    canvas.disc(cx, cy, w * 0.44, dark, 0.9)
    canvas.disc(cx, cy, w * 0.38, base, 1.0)
    canvas.disc(cx, cy, w * 0.17, accent, 0.9)
    canvas.disc(cx, cy, w * 0.09, dark, 0.8)
    for i in range(8):
        angle = i * math.pi / 4
        canvas.line(cx + w * 0.12 * math.cos(angle),
                    cy + w * 0.12 * math.sin(angle),
                    cx + w * 0.33 * math.cos(angle),
                    cy + w * 0.33 * math.sin(angle), light, 0.35)


def paint_bench(canvas, pal, rng):
    u, v, w, h = region("bench")
    base, light, dark, accent = pal
    canvas.rect(u, v, w, h, base, 1.0)
    for y in range(v + 3, v + h, 8):
        for x in range(u + 4, u + w, 10):
            canvas.disc(x, y, 1.2, dark, 0.55)
            canvas.disc(x, y - 1, 0.8, light, 0.55)
    for _ in range(w * h // 100):
        canvas.disc(rng.randrange(u, u + w), rng.randrange(v, v + h), 0.6, accent, 0.20)


def paint_nose(canvas, pal, rng):
    u, v, w, h = region("nose")
    base, light, dark, accent = pal
    canvas.rect(u, v, w, h, base, 1.0)
    for y in range(v, v + h, 6):
        canvas.line(u, y, u + w - 1, y, dark, 0.28)
        canvas.line(u, y + 1, u + w - 1, y + 1, light, 0.16)
    for _ in range(w * h // 60):
        x = rng.randrange(u, u + w)
        y = rng.randrange(v, v + h)
        canvas.blend(x, y, accent, rng.uniform(0.05, 0.22))


def paint_lantern(canvas, pal, rng):
    u, v, w, h = region("lantern")
    base, light, dark, accent = pal
    canvas.rect(u, v, w, h, base, 1.0)
    canvas.rect(u + 2, v + 2, w - 4, h - 4, dark, 0.55)
    canvas.rect(u + 4, v + 4, w - 8, h - 8, accent, 0.75)
    for _ in range(6):
        canvas.blend(rng.randrange(u + 4, u + w - 4), rng.randrange(v + 4, v + h - 4),
                     (255, 246, 200), 0.55)


def paint_glow(canvas, pal, rng):
    u, v, w, h = region("glow")
    base, light, dark, accent = pal
    canvas.rect(u, v, w, h, base, 1.0)
    cx, cy = u + w / 2, v + h / 2
    canvas.disc(cx, cy, w * 0.42, accent, 0.55)
    canvas.disc(cx, cy, w * 0.28, light, 0.75)
    canvas.disc(cx, cy, w * 0.13, (255, 255, 240), 0.95)


def paint(name: str) -> Canvas:
    palette = PALETTES[name]
    rng = random.Random(zlib.crc32(name.encode("utf-8")))
    canvas = Canvas()
    canvas.rect(0, 0, SIZE, SIZE, (0, 0, 0), 0.0)

    paint_planks(canvas, "floor", palette["floor"], rng, seam=9)
    paint_metal(canvas, "frame", palette["frame"], rng)
    paint_planks(canvas, "wall", palette["wall"], rng, seam=8)
    paint_planks(canvas, "pillar", palette["pillar"], rng, seam=6)
    paint_panels(canvas, "upper", palette["upper"], rng)
    paint_planks(canvas, "door", palette["door"], rng, seam=7)
    paint_planks(canvas, "endwall", palette["endwall"], rng, seam=7)
    paint_bench(canvas, palette["bench"], rng)
    paint_roof(canvas, palette["roof"], rng)
    paint_metal(canvas, "metal", palette["metal"], rng)
    paint_nose(canvas, palette["nose"], rng)
    paint_brass(canvas, "brass", palette["brass"], rng)
    paint_wheel(canvas, palette["wheel"], rng)
    paint_lantern(canvas, palette["lantern"], rng)
    paint_glow(canvas, palette["glow"], rng)
    paint_glass(canvas, palette["glass"], rng)
    return canvas


def write_png_pillow(path: Path, canvas: Canvas) -> bool:
    try:
        from PIL import Image  # type: ignore
    except Exception:
        return False
    image = Image.frombytes("RGBA", (canvas.w, canvas.h), bytes(canvas.data))
    image.save(path, format="PNG", optimize=True)
    return True


def _chunk(tag: bytes, payload: bytes) -> bytes:
    return (struct.pack(">I", len(payload)) + tag + payload
            + struct.pack(">I", zlib.crc32(tag + payload) & 0xFFFFFFFF))


def write_png_fallback(path: Path, canvas: Canvas) -> bool:
    raw = bytearray()
    for row in canvas.rows():
        raw.append(0)  # filter type: none
        raw.extend(row)
    png = (b"\x89PNG\r\n\x1a\n"
           + _chunk(b"IHDR", struct.pack(">IIBBBBB", canvas.w, canvas.h, 8, 6, 0, 0, 0))
           + _chunk(b"IDAT", zlib.compress(bytes(raw), 9))
           + _chunk(b"IEND", b""))
    path.write_bytes(png)
    return True


def write_png(path: Path, canvas: Canvas) -> str:
    path.parent.mkdir(parents=True, exist_ok=True)
    if write_png_pillow(path, canvas):
        return "Pillow"
    write_png_fallback(path, canvas)
    return "builtin"


def main(argv):
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("names", nargs="*", help="variants to write (default: both)")
    parser.add_argument("--list", action="store_true", help="list known variants")
    parser.add_argument("--out", type=Path, default=TEXTURE_DIR, help="output directory")
    args = parser.parse_args(argv)

    if args.list:
        for name in sorted(PALETTES):
            print(name)
        return 0

    names = args.names or sorted(PALETTES)
    unknown = [n for n in names if n not in PALETTES]
    if unknown:
        print(f"unknown variant(s): {', '.join(unknown)}", file=sys.stderr)
        return 2

    encoder = None
    for name in names:
        canvas = paint(name)
        target = args.out / f"train_{name}.png"
        used = write_png(target, canvas)
        encoder = encoder or used
        print(f"{target.relative_to(REPO_ROOT) if target.is_relative_to(REPO_ROOT) else target}  "
              f"{canvas.w}x{canvas.h}  [{used}]")
    print(f"wrote {len(names)} texture(s) with {encoder or 'builtin'} encoder")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
