#!/usr/bin/env python3
"""Generate the coastal packet's entity texture.

One 1024x1024 RGB sheet holds every material the ship model samples: tarred hull
planks, a pale deck, painted cabin boards, a slate roof, canvas sails, an iron
funnel, dark rigging, benches, lantern glass and portholes.  Each material lives
in its own rectangle, and every model cube is given a ``texOffs`` at that
rectangle (see ``client/model/CoastalPacketModel``).

The script is deterministic: the RNG is seeded from the texture name, so running
it again always produces byte-identical pixels.

Usage:
    python3 tools/generate_ship_texture.py
    python3 tools/generate_ship_texture.py --out /tmp/preview
"""

from __future__ import annotations

import argparse
import math
import random
import struct
import sys
import zlib
from pathlib import Path

SIZE = 1024
NAME = "coastal_packet"

REPO_ROOT = Path(__file__).resolve().parents[1]
TEXTURE_DIR = (REPO_ROOT / "src" / "main" / "resources" / "assets"
               / "eldritch_horror" / "textures" / "entity")

# Region rectangles (x, y, w, h). The model's texOffs constants mirror these.
HULL_A = (0, 0, 208, 128)
HULL_B = (0, 132, 208, 128)
DECK = (0, 264, 196, 84)
ROOF = (0, 352, 200, 84)
SAIL = (0, 440, 188, 138)
CABIN = (256, 0, 80, 40)
WINDOW = (256, 48, 30, 8)
WINDOW_TALL = (290, 48, 30, 21)
BENCH = (400, 160, 130, 11)
KEEL = (256, 80, 168, 72)
MAST = (440, 0, 40, 100)
YARD = (440, 104, 68, 34)
FUNNEL = (256, 160, 72, 28)
TRIM = (340, 176, 64, 64)
FOREDECK = (256, 240, 192, 80)
LANTERN = (460, 140, 24, 14)
PORTHOLE = (490, 140, 16, 8)


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

    def rect(self, x0, y0, w, h, color, alpha=1.0):
        for y in range(max(0, y0), min(self.h, y0 + h)):
            for x in range(max(0, x0), min(self.w, x0 + w)):
                self.blend(x, y, color, alpha)

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

    def line(self, x0, y0, x1, y1, color, alpha):
        steps = max(1, int(max(abs(x1 - x0), abs(y1 - y0))))
        for i in range(steps + 1):
            t = i / steps
            self.blend(int(round(x0 + (x1 - x0) * t)),
                       int(round(y0 + (y1 - y0) * t)), color, alpha)

    def rows(self):
        for y in range(self.h):
            yield bytes(self.data[y * self.w * 3:(y + 1) * self.w * 3])


def jitter(canvas, rect, rng, amount):
    x0, y0, w, h = rect
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            d = rng.randint(-amount, amount)
            if d:
                i = (y * canvas.w + x) * 3
                for k in range(3):
                    canvas.data[i + k] = max(0, min(255, canvas.data[i + k] + d))


def shade(color, factor):
    return tuple(max(0, min(255, int(c * factor))) for c in color)


def paint_wood(canvas, rect, base, seam, light, rng, row_h=8, plank_w=32,
               horizontal=True, nails=True):
    """Planked timber: alternating strakes, caulk seams, grain, knots, nails."""
    x0, y0, w, h = rect
    canvas.rect(x0, y0, w, h, base)
    jitter(canvas, rect, rng, 7)
    length = w if horizontal else h
    rows = (h if horizontal else w) // row_h + 1
    for row in range(rows):
        offset = (row * 13) % max(1, plank_w)
        start = (row * row_h) + (0 if horizontal else 0)
        factor = 0.90 + 0.18 * rng.random()
        tone = shade(base, factor)
        if horizontal:
            canvas.rect(x0, y0 + start, w, row_h, tone, 0.55)
            canvas.line(x0, y0 + start, x0 + w, y0 + start, seam, 0.85)
        else:
            canvas.rect(x0 + start, y0, row_h, h, tone, 0.55)
            canvas.line(x0 + start, y0, x0 + start, y0 + h, seam, 0.85)
        # Butts between planks.
        for butt in range(offset, length, max(8, plank_w)):
            if horizontal:
                canvas.line(x0 + butt, y0 + start, x0 + butt, y0 + start + row_h,
                            seam, 0.8)
                if nails:
                    canvas.ellipse(x0 + butt + 2, y0 + start + 2, 0.9, 0.9, light, 0.7)
                    canvas.ellipse(x0 + butt + 2, y0 + start + row_h - 3, 0.9, 0.9,
                                   light, 0.7)
            else:
                canvas.line(x0 + start, y0 + butt, x0 + start + row_h, y0 + butt,
                            seam, 0.8)
                if nails:
                    canvas.ellipse(x0 + start + 2, y0 + butt + 2, 0.9, 0.9, light, 0.7)
                    canvas.ellipse(x0 + start + row_h - 3, y0 + butt + 2, 0.9, 0.9,
                                   light, 0.7)
    # Grain streaks and knots.
    for _ in range(max(4, w * h // 900)):
        gx = rng.uniform(x0, x0 + w)
        gy = rng.uniform(y0, y0 + h)
        ln = rng.uniform(6, 34)
        if horizontal:
            canvas.line(gx, gy, gx + ln, gy + rng.uniform(-1.2, 1.2), seam, 0.22)
        else:
            canvas.line(gx, gy, gx + rng.uniform(-1.2, 1.2), gy + ln, seam, 0.22)
    for _ in range(max(2, w * h // 5000)):
        kx = rng.uniform(x0 + 3, x0 + w - 3)
        ky = rng.uniform(y0 + 3, y0 + h - 3)
        r = rng.uniform(1.2, 3.0)
        canvas.ellipse(kx, ky, r, r * 0.65, seam, 0.5)
        canvas.ellipse(kx - 0.5, ky - 0.5, r * 0.5, r * 0.35, light, 0.35)


def paint_hull(canvas, rect, base, rng):
    paint_wood(canvas, rect, base, shade(base, 0.52), shade(base, 1.3), rng,
               row_h=7, plank_w=41, horizontal=True)
    x0, y0, w, h = rect
    # Tar streaks running down the topsides.
    for _ in range(18):
        x = rng.uniform(x0, x0 + w)
        canvas.line(x, y0, x + rng.uniform(-2, 2), y0 + h * rng.uniform(0.2, 0.8),
                    shade(base, 0.45), rng.uniform(0.15, 0.35))


def paint_deck(canvas, rect, rng):
    base = (126, 102, 67)
    paint_wood(canvas, rect, base, (52, 39, 26), (176, 150, 106), rng,
               row_h=6, plank_w=48, horizontal=True, nails=False)
    x0, y0, w, h = rect
    for _ in range(w * h // 900):
        canvas.ellipse(rng.uniform(x0, x0 + w), rng.uniform(y0, y0 + h),
                       rng.uniform(2, 7), rng.uniform(1, 3), (92, 72, 46), 0.10)


def paint_roof(canvas, rect, rng):
    x0, y0, w, h = rect
    canvas.rect(x0, y0, w, h, (44, 50, 46))
    jitter(canvas, rect, rng, 6)
    row_h = 7
    for row, y in enumerate(range(y0, y0 + h, row_h)):
        canvas.line(x0, y, x0 + w, y, (20, 24, 22), 0.85)
        step = 12
        offset = (row * step // 2) % step
        for x in range(x0 + offset, x0 + w, step):
            canvas.line(x, y, x, min(y + row_h, y0 + h), (26, 31, 28), 0.55)
            canvas.rect(x + 1, y + 1, step - 2, row_h - 2,
                        shade((44, 50, 46), 0.9 + 0.25 * rng.random()), 0.3)
    # Moss in the joints.
    for _ in range(w * h // 700):
        canvas.ellipse(rng.uniform(x0, x0 + w), rng.uniform(y0, y0 + h),
                       rng.uniform(1, 4), rng.uniform(0.7, 2), (58, 74, 46), 0.22)


def paint_cabin(canvas, rect, rng):
    base = (62, 82, 76)
    paint_wood(canvas, rect, base, (24, 36, 33), (112, 134, 120), rng,
               row_h=6, plank_w=26, horizontal=True, nails=True)
    x0, y0, w, h = rect
    # Cream sheer band and a dark bead below it.
    canvas.rect(x0, y0 + 2, w, 3, (188, 176, 140), 0.85)
    canvas.rect(x0, y0 + 6, w, 1, (30, 42, 38), 0.8)
    for _ in range(10):
        canvas.ellipse(rng.uniform(x0, x0 + w), rng.uniform(y0, y0 + h),
                       rng.uniform(2, 6), rng.uniform(1, 2.5), (40, 54, 50), 0.12)


def paint_sail(canvas, rect, rng):
    x0, y0, w, h = rect
    base = (196, 184, 150)
    canvas.rect(x0, y0, w, h, base)
    jitter(canvas, rect, rng, 5)
    # Canvas panels.
    for x in range(x0, x0 + w, 11):
        canvas.line(x, y0, x, y0 + h, (164, 150, 118), 0.5)
    for y in range(y0, y0 + h, 24):
        canvas.line(x0, y, x0 + w, y, (172, 158, 124), 0.35)
    # Stains and reef patches.
    for _ in range(26):
        canvas.ellipse(rng.uniform(x0, x0 + w), rng.uniform(y0, y0 + h),
                       rng.uniform(4, 16), rng.uniform(2, 9), (146, 128, 92),
                       rng.uniform(0.08, 0.22))
    # Bolt-rope hem.
    canvas.rect(x0, y0, w, 2, (120, 104, 72), 0.9)
    canvas.rect(x0, y0 + h - 2, w, 2, (120, 104, 72), 0.9)
    # A faded sigil, so the packet reads as ours from a distance.
    cx, cy = x0 + w / 2, y0 + h * 0.42
    r = min(w, h) * 0.22
    for i in range(48):
        a0 = (i / 48) * 6.28318
        a1 = ((i + 1) / 48) * 6.28318
        canvas.line(cx + r * math.cos(a0), cy + r * math.sin(a0),
                    cx + r * math.cos(a1), cy + r * math.sin(a1),
                    (142, 48, 40), 0.30)
    for k in range(5):
        a = k * 1.2566 - 1.5708
        canvas.line(cx, cy, cx + r * 0.92 * math.cos(a),
                    cy + r * 0.92 * math.sin(a), (142, 48, 40), 0.22)
    canvas.ellipse(cx, cy, r * 0.16, r * 0.16, (142, 48, 40), 0.30)


def paint_window(canvas, rect, rng):
    x0, y0, w, h = rect
    for y in range(y0, y0 + h):
        t = (y - y0) / max(1, h - 1)
        color = (int(9 + 10 * t), int(18 + 18 * t), int(28 + 24 * t))
        canvas.rect(x0, y, w, 1, color, 1.0)
    # Glare.
    for x in range(x0, x0 + w, 17):
        canvas.line(x, y0, x + 6, y0 + h - 1, (120, 150, 166), 0.18)
    # Mullions.
    canvas.line(x0 + w // 2, y0, x0 + w // 2, y0 + h, (24, 30, 32), 0.75)
    canvas.line(x0, y0 + h // 2, x0 + w, y0 + h // 2, (24, 30, 32), 0.6)
    # Frame.
    for x in range(x0, x0 + w):
        canvas.blend(x, y0, (52, 44, 30), 0.9)
        canvas.blend(x, y0 + h - 1, (52, 44, 30), 0.9)
    for y in range(y0, y0 + h):
        canvas.blend(x0, y, (52, 44, 30), 0.9)
        canvas.blend(x0 + w - 1, y, (52, 44, 30), 0.9)


def paint_funnel(canvas, rect, rng):
    x0, y0, w, h = rect
    for y in range(y0, y0 + h):
        t = (y - y0) / max(1, h - 1)
        color = (int(38 + 12 * t), int(38 + 10 * t), int(42 + 12 * t))
        canvas.rect(x0, y, w, 1, color, 1.0)
    # Vertical plates and rivets.
    for x in range(x0, x0 + w, 12):
        canvas.line(x, y0, x, y0 + h, (22, 22, 26), 0.7)
        for y in range(y0 + 2, y0 + h, 5):
            canvas.blend(x + 2, y, (96, 92, 82), 0.65)
    # Rust streaks.
    for _ in range(14):
        x = rng.uniform(x0, x0 + w)
        canvas.line(x, y0 + rng.uniform(0, 4), x + rng.uniform(-2, 2),
                    y0 + h * rng.uniform(0.4, 1.0), (104, 56, 32),
                    rng.uniform(0.18, 0.42))
    canvas.rect(x0, y0, w, 2, (18, 18, 22), 0.9)


def paint_trim(canvas, rect, rng):
    base = (72, 30, 30)
    paint_wood(canvas, rect, base, (28, 10, 10), (128, 62, 52), rng,
               row_h=6, plank_w=22, horizontal=True, nails=False)
    x0, y0, w, h = rect
    canvas.rect(x0, y0 + 1, w, 2, (126, 110, 72), 0.75)
    for _ in range(12):
        canvas.ellipse(rng.uniform(x0, x0 + w), rng.uniform(y0, y0 + h),
                       rng.uniform(1, 4), rng.uniform(0.7, 2), (22, 16, 14), 0.3)


def paint_lantern(canvas, rect, rng):
    x0, y0, w, h = rect
    cx, cy = x0 + w / 2, y0 + h / 2
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            dx = (x - cx) / (w / 2)
            dy = (y - cy) / (h / 2)
            d = min(1.0, (dx * dx + dy * dy) ** 0.5)
            color = (int(255 - 120 * d), int(206 - 120 * d), int(96 - 70 * d))
            canvas.blend(x, y, color, 1.0)
    # Iron cage.
    for x in (x0, x0 + w - 1, x0 + w // 2):
        canvas.line(x, y0, x, y0 + h - 1, (34, 28, 20), 0.9)
    for y in (y0, y0 + h - 1, y0 + h // 2):
        canvas.line(x0, y, x0 + w - 1, y, (34, 28, 20), 0.8)


def paint_porthole(canvas, rect, rng):
    x0, y0, w, h = rect
    canvas.rect(x0, y0, w, h, (28, 24, 18))
    cx, cy = x0 + w / 2, y0 + h / 2
    canvas.ellipse(cx, cy, w * 0.46, h * 0.46, (142, 112, 56), 1.0)
    canvas.ellipse(cx, cy, w * 0.34, h * 0.34, (12, 20, 28), 1.0)
    canvas.ellipse(cx - w * 0.10, cy - h * 0.12, w * 0.10, h * 0.09,
                   (92, 120, 134), 0.5)
    for _ in range(4):
        canvas.ellipse(rng.uniform(x0 + 2, x0 + w - 2), rng.uniform(y0 + 2, y0 + h - 2),
                       0.7, 0.7, (170, 140, 80), 0.6)


def paint(canvas):
    rng = random.Random(zlib.crc32(NAME.encode("utf-8")))
    canvas.rect(0, 0, SIZE, SIZE, (20, 18, 16))
    paint_hull(canvas, HULL_A, (62, 46, 32), rng)
    paint_hull(canvas, HULL_B, (58, 43, 30), rng)
    paint_deck(canvas, DECK, rng)
    paint_roof(canvas, ROOF, rng)
    paint_sail(canvas, SAIL, rng)
    paint_cabin(canvas, CABIN, rng)
    paint_window(canvas, WINDOW, rng)
    paint_window(canvas, WINDOW_TALL, rng)
    paint_wood(canvas, BENCH, (100, 72, 42), (44, 30, 18), (146, 116, 76), rng,
               row_h=5, plank_w=34)
    paint_wood(canvas, KEEL, (36, 28, 22), (14, 10, 8), (74, 60, 46), rng,
               row_h=9, plank_w=30)
    paint_wood(canvas, MAST, (52, 38, 26), (20, 14, 10), (92, 70, 46), rng,
               row_h=40, plank_w=9, horizontal=False, nails=False)
    paint_wood(canvas, YARD, (50, 37, 26), (20, 14, 10), (90, 68, 46), rng,
               row_h=10, plank_w=13, horizontal=False, nails=False)
    paint_funnel(canvas, FUNNEL, rng)
    paint_trim(canvas, TRIM, rng)
    paint_deck(canvas, FOREDECK, rng)
    paint_lantern(canvas, LANTERN, rng)
    paint_porthole(canvas, PORTHOLE, rng)
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
        raw.append(0)
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
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--out", type=Path, default=TEXTURE_DIR,
                        help="output directory")
    args = parser.parse_args(argv)

    canvas = paint(Canvas((20, 18, 16)))
    target = args.out / f"{NAME}.png"
    encoder = write_png(target, canvas)
    size = target.stat().st_size
    print(f"{target}  {canvas.w}x{canvas.h}  {size} bytes  [{encoder}]")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
