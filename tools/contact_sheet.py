#!/usr/bin/env python3
"""Build a labelled contact sheet from the CityRenderer's isometric PNGs.

Usage:
    python3 tools/contact_sheet.py <render-dir> <out.png> [cols] [thumb-width]

Reads <city_id>_iso.png files, downscales each to a thumbnail, lays them out in a
grid and labels every tile with its city name. Requires Pillow.
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFont

FONT_CANDIDATES = [
    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
    "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
]


def load_font(size):
    for path in FONT_CANDIDATES:
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def city_label(filename):
    """<id>_iso.png where id == name_x_z -> 'Name' (strip trailing coordinates)."""
    base = filename[: -len("_iso.png")]
    parts = base.split("_")
    while len(parts) > 1 and parts[-1].lstrip("-").isdigit():
        parts.pop()
    return " ".join(parts)


def main():
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(2)
    src, dst = sys.argv[1], sys.argv[2]
    cols = int(sys.argv[3]) if len(sys.argv) > 3 else 4
    thumb_w = int(sys.argv[4]) if len(sys.argv) > 4 else 340

    files = sorted(f for f in os.listdir(src) if f.endswith("_iso.png"))
    if not files:
        print("no *_iso.png in", src)
        sys.exit(1)

    tiles = []
    for f in files:
        im = Image.open(os.path.join(src, f)).convert("RGBA")
        th = max(1, im.height * thumb_w // im.width)
        tiles.append((city_label(f), im.resize((thumb_w, th), Image.LANCZOS)))

    pad = 12
    label_h = 30
    cell_w = thumb_w
    cell_h = max(t[1].height for t in tiles) + label_h
    rows = (len(tiles) + cols - 1) // cols
    W = cols * cell_w + (cols + 1) * pad
    H = rows * cell_h + (rows + 1) * pad

    sheet = Image.new("RGBA", (W, H), (22, 22, 28, 255))
    draw = ImageDraw.Draw(sheet)
    font = load_font(20)

    for i, (name, im) in enumerate(tiles):
        c, r = i % cols, i // cols
        x0 = pad + c * (cell_w + pad)
        y0 = pad + r * (cell_h + pad)
        draw.rectangle([x0, y0, x0 + cell_w, y0 + label_h], fill=(44, 44, 56, 255))
        tw = draw.textlength(name, font=font)
        draw.text((x0 + max(0, (cell_w - tw) / 2), y0 + 5), name, font=font,
                  fill=(238, 238, 238, 255))
        sheet.alpha_composite(im, (x0, y0 + label_h))

    sheet.convert("RGB").save(dst)
    print("wrote %s (%dx%d, %d tiles)" % (dst, W, H, len(tiles)))


if __name__ == "__main__":
    main()
