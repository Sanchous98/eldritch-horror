#!/usr/bin/env python3
"""Bake the Earth landmask for the Eldritch Horror world generator.

Downloads Natural Earth's 1:50m land polygons (public domain) and rasterizes them to
an equirectangular landmask at the map's resolution, plus a small preview.

Output (written under src/main/resources/assets/eldritch_horror/map/):
  landmask_8192x4096.png   1-bit land/ocean bitmap (~4 MB), the only shipped map asset
  landmask_preview.png     small grayscale preview for eyeballing continents

Mapping (equirectangular, whole globe):
  lon -180..180  ->  x 0..W-1 (left..right)
  lat +90..-90   ->  y 0..H-1 (top..bottom)
The mod maps x -> world X = x*2 - 8192, y -> world Z = y*2 - 4096 (2 blocks/pixel).

Usage:
  python3 tools/bake_landmask.py [--width 8192] [--force] [--source PATH_OR_URL]
"""
import argparse
import json
import os
import sys
import urllib.request

DEFAULT_SOURCE = (
    "https://raw.githubusercontent.com/nvkelso/natural-earth-vector/"
    "master/geojson/ne_50m_land.geojson"
)
CACHE = os.path.join(os.path.dirname(__file__), ".cache")
OUT_DIR = os.path.join(
    os.path.dirname(__file__), "..",
    "src", "main", "resources", "assets", "eldritch_horror", "map",
)


def fetch(source):
    os.makedirs(CACHE, exist_ok=True)
    if os.path.exists(source):
        with open(source) as f:
            return json.load(f)
    name = os.path.basename(source) or "source.geojson"
    cached = os.path.join(CACHE, name)
    if not os.path.exists(cached):
        print(f"downloading {source} ...")
        urllib.request.urlretrieve(source, cached)
    with open(cached) as f:
        return json.load(f)


def iter_polygons(geometry):
    t = geometry.get("type")
    coords = geometry.get("coordinates")
    if t == "Polygon":
        yield coords
    elif t == "MultiPolygon":
        yield from coords


def project(lon, lat, w, h):
    x = (lon + 180.0) / 360.0 * w
    y = (90.0 - lat) / 180.0 * h
    return x, y


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--width", type=int, default=8192)
    ap.add_argument("--source", default=DEFAULT_SOURCE)
    ap.add_argument("--force", action="store_true", help="overwrite existing output")
    args = ap.parse_args()

    h = args.width // 2
    w = args.width

    try:
        from PIL import Image, ImageDraw
    except ImportError:
        sys.exit("needs Pillow: pip install --user pillow")

    out_png = os.path.abspath(os.path.join(OUT_DIR, f"landmask_{w}x{h}.png"))
    if os.path.exists(out_png) and not args.force:
        sys.exit(f"{out_png} exists (use --force)")

    data = fetch(args.source)
    features = data["features"]
    print(f"{len(features)} features; rasterizing at {w}x{h} ...")

    # Draw on a 3x-wide canvas so antimeridian-crossing polygons wrap correctly,
    # then crop the centre third.
    canvas = Image.new("1", (w * 3, h), 0)
    draw = ImageDraw.Draw(canvas)

    for feat in features:
        for poly in iter_polygons(feat["geometry"]):
            outer = poly[0]  # holes are lakes/inland seas; ignore for a land mask
            pts = []
            for lon, lat in outer:
                x, y = project(lon, lat, w, h)
                pts.append((x + w, y))  # centre third
            if len(pts) >= 3:
                draw.polygon(pts, fill=1)

    mask = canvas.crop((w, 0, w * 2, h))

    os.makedirs(OUT_DIR, exist_ok=True)
    mask.save(out_png, optimize=True)

    # Small preview: land dark on white, 1024 wide.
    pw = 1024
    ph = pw // 2
    preview = mask.convert("L").resize((pw, ph))
    preview.save(os.path.abspath(os.path.join(OUT_DIR, "landmask_preview.png")))

    land = sum(mask.histogram()[1:]) if mask.mode == "1" else 0
    total = w * h
    print(f"wrote {out_png} ({os.path.getsize(out_png)/1e6:.1f} MB)")
    print(f"land fraction: {land/total*100:.1f}%")


if __name__ == "__main__":
    main()
