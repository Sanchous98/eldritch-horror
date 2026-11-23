#!/usr/bin/env python3
"""Generate the Ancient One entity textures.

Every texture is a 128x128 RGB sheet: a seeded, palette-driven mottled organic surface. The
rectangle (0,0)-(16,16) is reserved for the pale "eye" region and (16,0)-(28,12) for the near-black
"pupil" region, so models can sample readable eyes from a flat noise sheet (see
``BossParts.EYE_U``/``PUPIL_U``).

Usage:
    python3 tools/generate_boss_textures.py            # write every texture
    python3 tools/generate_boss_textures.py dunwich_horror glaaki
    python3 tools/generate_boss_textures.py --list

Pillow is used when installed; otherwise a small built-in PNG encoder writes the same pixels.

The script is deterministic: the RNG seed is derived from the texture name with zlib.crc32, so
re-running it always produces byte-identical pixels.
"""

from __future__ import annotations

import argparse
import random
import struct
import sys
import zlib
from pathlib import Path

SIZE = 128
EYE_ZONE = (0, 0, 16, 16)
PUPIL_ZONE = (16, 0, 12, 12)

REPO_ROOT = Path(__file__).resolve().parents[1]
TEXTURE_DIR = REPO_ROOT / "src" / "main" / "resources" / "assets" / "eldritch_horror" / "textures" / "entity"


# name: base, light, dark, accent, eye, pupil
PALETTES = {
    # --- pilot trio -------------------------------------------------------------------------
    "dunwich_horror": ((120, 130, 104), (162, 174, 138), (74, 84, 64), (196, 210, 164), (222, 230, 188), (16, 18, 12)),
    "glaaki":         ((42, 62, 48),    (82, 108, 78),    (18, 28, 22),   (118, 152, 108), (198, 220, 178), (8, 12, 8)),
    "chaugnar_faugn": ((116, 108, 92),  (156, 146, 122),  (72, 66, 54),   (188, 174, 134), (226, 214, 164), (24, 18, 12)),
    # --- batch 2: major presences -----------------------------------------------------------
    "cthulhu":        ((44, 86, 74),    (78, 132, 110),   (18, 42, 36),   (146, 176, 130), (212, 226, 172), (10, 16, 12)),
    "azathoth":       ((46, 34, 58),    (82, 62, 102),    (20, 14, 26),   (126, 190, 120), (170, 232, 142), (8, 6, 12)),
    "yog_sothoth":    ((96, 64, 34),    (164, 118, 62),   (48, 30, 18),   (226, 176, 92),  (250, 232, 180), (18, 10, 8)),
    "hastur":         ((138, 118, 44),  (188, 168, 76),   (78, 66, 22),   (230, 212, 122), (238, 232, 206), (30, 24, 10)),
    "nyarlathotep":   ((38, 30, 34),    (72, 52, 58),     (16, 12, 14),   (146, 38, 42),   (230, 226, 210), (12, 8, 8)),
    "shub_niggurath": ((52, 38, 28),    (90, 66, 44),     (24, 16, 12),   (128, 54, 34),   (214, 200, 158), (14, 10, 8)),
    "the_horror":     ((32, 24, 38),    (62, 46, 72),     (12, 8, 16),    (104, 62, 138),  (196, 164, 220), (6, 4, 8)),
    # --- batch 3 ----------------------------------------------------------------------------
    "atlach_nacha":   ((30, 28, 34),    (62, 56, 66),     (10, 10, 14),   (158, 40, 52),   (206, 190, 164), (8, 6, 8)),
    "ithaqua":        ((156, 178, 192), (206, 224, 232),  (88, 108, 124), (232, 244, 250), (214, 240, 252), (40, 58, 72)),
    "yig":            ((34, 76, 46),    (62, 118, 70),    (14, 38, 22),   (172, 196, 92),  (206, 222, 122), (10, 20, 10)),
    "cthugha":        ((152, 46, 22),   (220, 92, 30),    (74, 18, 10),   (250, 176, 58),  (255, 222, 120), (40, 8, 4)),
    "hydra":          ((54, 82, 54),    (92, 128, 78),    (26, 42, 26),   (140, 164, 84),  (192, 214, 128), (12, 20, 12)),
    "nyogtha":        ((24, 20, 26),    (52, 42, 52),     (8, 6, 10),     (86, 60, 102),   (154, 132, 172), (4, 2, 6)),
    "rhan_tegoth":    ((92, 82, 62),    (132, 118, 88),   (52, 44, 32),   (176, 148, 82),  (220, 198, 132), (24, 16, 8)),
    # --- batch 4 ----------------------------------------------------------------------------
    "tulzscha":       ((36, 96, 42),    (76, 168, 66),    (12, 40, 16),   (156, 236, 110), (198, 255, 158), (8, 28, 10)),
    "zstylzhemghi":   ((52, 44, 30),    (94, 80, 52),     (22, 18, 12),   (164, 120, 44),  (210, 186, 112), (12, 10, 6)),
    "abhoth":         ((112, 100, 92),  (150, 136, 124),  (66, 58, 52),   (178, 138, 128), (212, 196, 178), (28, 20, 18)),
    "nephren_ka":     ((162, 148, 112), (204, 190, 148),  (94, 84, 58),   (216, 186, 84),  (236, 224, 176), (32, 24, 12)),
    "choir_leviathan": ((42, 58, 68),   (74, 98, 108),    (18, 26, 32),   (108, 140, 138), (208, 226, 216), (10, 14, 16)),
    "ithaqual_shade": ((146, 162, 178), (192, 208, 220),  (78, 90, 104),  (216, 234, 242), (220, 240, 248), (48, 58, 70)),
}

LOGICAL_NAMES = set(PALETTES)


def palette_for(name: str):
    if name in PALETTES:
        return PALETTES[name]
    # Stable fallback for a boss added before its palette: a grey-green drowned thing.
    seed = zlib.crc32(name.encode("utf-8"))
    rng = random.Random(seed)
    base = (rng.randint(40, 150), rng.randint(40, 150), rng.randint(40, 150))
    light = tuple(min(255, c + 36) for c in base)
    dark = tuple(max(0, c - 40) for c in base)
    accent = tuple(min(255, c + 60) for c in base)
    return (base, light, dark, accent, (226, 226, 200), (18, 16, 16))


class Canvas:
    def __init__(self, color):
        self.w = SIZE
        self.h = SIZE
        self.data = bytearray(color * (SIZE * SIZE))

    def blend(self, x, y, color, alpha=0.5):
        if 0 <= x < self.w and 0 <= y < self.h and alpha > 0.0:
            i = (y * self.w + x) * 3
            a = max(0.0, min(1.0, alpha))
            d = self.data
            d[i] = int(d[i] + (color[0] - d[i]) * a)
            d[i + 1] = int(d[i + 1] + (color[1] - d[i + 1]) * a)
            d[i + 2] = int(d[i + 2] + (color[2] - d[i + 2]) * a)

    def ellipse(self, cx, cy, rx, ry, color, alpha):
        if rx <= 0 or ry <= 0:
            return
        x0 = max(0, int(cx - rx))
        x1 = min(self.w - 1, int(cx + rx))
        y0 = max(0, int(cy - ry))
        y1 = min(self.h - 1, int(cy + ry))
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                dx = (x - cx) / rx
                dy = (y - cy) / ry
                if dx * dx + dy * dy <= 1.0:
                    self.blend(x, y, color, alpha)

    def rect(self, x0, y0, w, h, color, alpha=1.0):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                self.blend(x, y, color, alpha)

    def line(self, x0, y0, x1, y1, color, alpha):
        steps = max(1, int(max(abs(x1 - x0), abs(y1 - y0))))
        for i in range(steps + 1):
            t = i / steps
            x = int(x0 + (x1 - x0) * t)
            y = int(y0 + (y1 - y0) * t)
            self.blend(x, y, color, alpha)

    def rows(self):
        for y in range(self.h):
            yield bytes(self.data[y * self.w * 3:(y + 1) * self.w * 3])


def paint(name: str) -> Canvas:
    base, light, dark, accent, eye, pupil = palette_for(name)
    rng = random.Random(zlib.crc32(name.encode("utf-8")))
    canvas = Canvas(base)

    # Soft vertical shading: lighter crown, darker underside.
    for y in range(SIZE):
        t = y / (SIZE - 1)
        factor = 1.16 - 0.38 * t
        row_color = tuple(max(0, min(255, int(c * factor))) for c in base)
        for x in range(SIZE):
            canvas.blend(x, y, row_color, 1.0)

    # Large mottling: overlapping organic stains in every palette shade.
    for _ in range(120):
        cx = rng.uniform(-8, SIZE + 8)
        cy = rng.uniform(-8, SIZE + 8)
        rx = rng.uniform(3.0, 20.0)
        ry = rng.uniform(3.0, 16.0)
        color = rng.choice((light, dark, accent, base, dark, light))
        canvas.ellipse(cx, cy, rx, ry, color, rng.uniform(0.10, 0.30))

    # Veins / cracks / wet folds.
    for _ in range(22):
        x = rng.uniform(0, SIZE)
        y = rng.uniform(0, SIZE)
        color = rng.choice((dark, dark, accent))
        for _ in range(rng.randint(2, 5)):
            nx = x + rng.uniform(-26, 26)
            ny = y + rng.uniform(-20, 20)
            canvas.line(x, y, nx, ny, color, rng.uniform(0.18, 0.42))
            x, y = nx, ny

    # Bubbles / pustules.
    for _ in range(28):
        cx = rng.uniform(0, SIZE)
        cy = rng.uniform(0, SIZE)
        r = rng.uniform(1.2, 4.2)
        canvas.ellipse(cx, cy, r, r, light, 0.55)
        canvas.ellipse(cx - r * 0.25, cy - r * 0.25, r * 0.45, r * 0.45, accent, 0.45)
        canvas.ellipse(cx + r * 0.3, cy + r * 0.35, r * 0.4, r * 0.4, dark, 0.35)

    # Fine grain.
    for y in range(SIZE):
        for x in range(SIZE):
            delta = rng.randint(-7, 7)
            if delta:
                i = (y * canvas.w + x) * 3
                for c in range(3):
                    canvas.data[i + c] = max(0, min(255, canvas.data[i + c] + delta))

    # Speckles.
    for _ in range(420):
        x = rng.randrange(SIZE)
        y = rng.randrange(SIZE)
        canvas.blend(x, y, accent if rng.random() < 0.45 else dark, rng.uniform(0.3, 0.7))

    # Reserved eye + pupil regions. Kept crisp and un-mottled so eyes read at distance.
    canvas.rect(*EYE_ZONE, eye, 1.0)
    for _ in range(18):
        canvas.ellipse(rng.uniform(0, 16), rng.uniform(0, 16), rng.uniform(1.0, 3.0),
                       rng.uniform(1.0, 3.0), light if rng.random() < 0.5 else dark, 0.20)
    canvas.rect(*PUPIL_ZONE, pupil, 1.0)
    canvas.ellipse(16 + 6, 6, 3.5, 5.0, (0, 0, 0), 0.55)
    return canvas


def write_png_pillow(path: Path, canvas: Canvas) -> bool:
    try:
        from PIL import Image  # type: ignore
    except Exception:
        return False
    image = Image.frombytes("RGB", (canvas.w, canvas.h), bytes(canvas.data))
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
           + _chunk(b"IHDR", struct.pack(">IIBBBBB", canvas.w, canvas.h, 8, 2, 0, 0, 0))
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
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("names", nargs="*", help="texture names (default: every palette)")
    parser.add_argument("--list", action="store_true", help="list known texture names")
    parser.add_argument("--out", type=Path, default=TEXTURE_DIR, help="output directory")
    args = parser.parse_args(argv)

    if args.list:
        for name in sorted(PALETTES):
            print(name)
        return 0

    names = args.names or sorted(PALETTES)
    unknown = [n for n in names if n not in PALETTES]
    if unknown:
        print(f"note: no palette for {', '.join(unknown)}; using deterministic fallback", file=sys.stderr)

    encoder = None
    for name in names:
        canvas = paint(name)
        target = args.out / f"{name}.png"
        used = write_png(target, canvas)
        encoder = encoder or used
        print(f"{target.relative_to(REPO_ROOT) if target.is_relative_to(REPO_ROOT) else target}  "
              f"{canvas.w}x{canvas.h}  {len(canvas.data)} bytes pixels  [{used}]")
    print(f"wrote {len(names)} texture(s) with {encoder or 'builtin'} encoder")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
