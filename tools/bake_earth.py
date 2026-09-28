#!/usr/bin/env python3
"""Bake the Earth map layers for the Eldritch Horror world generator.

Produces, at the map resolution (8192x4096 = 2 blocks/pixel, equirectangular),
under src/main/resources/assets/eldritch_horror/map/:

  landmask_8192x4096.png    1-bit land/ocean bitmap      (~0.1 MB)
  elevation_8192x4096.png   16-bit unsigned, metres+12000 (~tens of MB)
  koppen_8192x4096.png      8-bit Koppen-Geiger class 0-29

Sources (public / open):
  land     Natural Earth 1:50m land polygons (public domain)
  elevation ETOPO1 bedrock grid-registered GeoTIFF (NOAA)
  koppen   Beck et al. 2018 Koppen-Geiger map (CC-BY)

Mapping: lon -180..180 -> x 0..W-1 ; lat +90..-90 -> y 0..H-1.
The mod maps x -> world X = x*2 - 8192, y -> world Z = y*2 - 4096.

Usage:
  python3 tools/bake_earth.py --layers landmask
  python3 tools/bake_earth.py --layers elevation --elevation-src tools/.cache/etopo1.tif.zip
  python3 tools/bake_earth.py --layers koppen    --koppen-src    tools/.cache/koppen.zip
  python3 tools/bake_earth.py --layers all --width 8192
"""
import argparse
import io
import json
import os
import sys
import urllib.request
import zipfile

DEFAULT_LAND = (
    "https://raw.githubusercontent.com/nvkelso/natural-earth-vector/"
    "master/geojson/ne_50m_land.geojson"
)
DEFAULT_ELEVATION = (
    "https://www.ngdc.noaa.gov/mgg/global/relief/ETOPO1/data/bedrock/"
    "grid_registered/georeferenced_tiff/ETOPO1_Bed_g_geotiff.zip"
)
DEFAULT_KOPPEN = "https://figshare.com/ndownloader/files/12407516"

HERE = os.path.dirname(__file__)
CACHE = os.path.join(HERE, ".cache")
OUT_DIR = os.path.abspath(os.path.join(
    HERE, "..", "src", "main", "resources", "assets", "eldritch_horror", "map"))
ELEV_OFFSET = 12000  # store elevation+offset as unsigned 16-bit


def cache_path(source, default_name):
    os.makedirs(CACHE, exist_ok=True)
    if os.path.exists(source):
        return source
    p = os.path.join(CACHE, default_name)
    if not os.path.exists(p):
        print(f"downloading {source} ...")
        urllib.request.urlretrieve(source, p)
    return p


def read_raster(source, default_name, prefer=None):
    """Return a numpy array from a .tif, a .zip containing a .tif, or a URL.

    When the zip holds several rasters, ``prefer`` (a list of name fragments, best
    first) selects one; otherwise the first .tif is used.
    """
    import tifffile
    import numpy as np

    path = cache_path(source, default_name)
    if path.endswith(".zip"):
        with zipfile.ZipFile(path) as z:
            names = [n for n in z.namelist()
                     if n.lower().endswith((".tif", ".tiff"))]
            if not names:
                names = [n for n in z.namelist()
                         if not n.endswith("/") and "legend" not in n.lower()]
            name = None
            if prefer:
                for frag in prefer:
                    name = next((n for n in names if frag in n), None)
                    if name:
                        break
            if name is None:
                name = names[0]
            print(f"  reading {name} from {os.path.basename(path)}")
            with z.open(name) as f:
                return tifffile.imread(io.BytesIO(f.read())).astype(np.float32)
    return tifffile.imread(path).astype(np.float32)


def resize(arr, w, h, mode):
    from PIL import Image
    import numpy as np

    if arr.dtype != np.float32 and mode == "nearest":
        # keep integer classes intact
        im = Image.fromarray(arr.astype(np.uint8), mode="L")
    else:
        im = Image.fromarray(arr)
    im = im.resize((w, h), Image.NEAREST if mode == "nearest" else Image.BILINEAR)
    return np.asarray(im)


# ------------------------------------------------------------------ layers
def bake_landmask(w, h, force):
    from PIL import Image, ImageDraw

    out = os.path.join(OUT_DIR, f"landmask_{w}x{h}.png")
    if os.path.exists(out) and not force:
        print(f"skip (exists): {out}")
        return
    data = fetch_geojson(DEFAULT_LAND)
    canvas = Image.new("1", (w * 3, h), 0)
    draw = ImageDraw.Draw(canvas)
    for feat in data["features"]:
        for poly in iter_polygons(feat["geometry"]):
            pts = [((lon + 180) / 360 * w + w, (90 - lat) / 180 * h) for lon, lat in poly[0]]
            if len(pts) >= 3:
                draw.polygon(pts, fill=1)
    mask = canvas.crop((w, 0, w * 2, h))
    os.makedirs(OUT_DIR, exist_ok=True)
    mask.save(out, optimize=True)
    print(f"wrote {out} ({os.path.getsize(out)/1e6:.1f} MB)")


def bake_elevation(w, h, force, source):
    from PIL import Image
    import numpy as np

    out = os.path.join(OUT_DIR, f"elevation_{w}x{h}.png")
    if os.path.exists(out) and not force:
        print(f"skip (exists): {out}")
        return
    z = read_raster(source, "etopo1.tif.zip")
    print(f"  source raster {z.shape}, range {z.min():.0f}..{z.max():.0f} m")
    z = resize(z, w, h, "bilinear")
    z = np.clip(z + ELEV_OFFSET, 0, 65535).astype(np.uint16)
    os.makedirs(OUT_DIR, exist_ok=True)
    Image.fromarray(z, mode="I;16").save(out, optimize=True)
    print(f"wrote {out} ({os.path.getsize(out)/1e6:.1f} MB)")


def bake_koppen(w, h, force, source):
    from PIL import Image
    import numpy as np

    out = os.path.join(OUT_DIR, f"koppen_{w}x{h}.png")
    if os.path.exists(out) and not force:
        print(f"skip (exists): {out}")
        return
    k = read_raster(source, "koppen.zip",
                    prefer=["present_0p0083", "present_0p083", "present_0p5", "present"])
    print(f"  source raster {k.shape}, classes {k.min():.0f}..{k.max():.0f}")
    k = np.clip(resize(k, w, h, "nearest"), 0, 255).astype(np.uint8)
    os.makedirs(OUT_DIR, exist_ok=True)
    Image.fromarray(k, mode="L").save(out, optimize=True)
    print(f"wrote {out} ({os.path.getsize(out)/1e6:.1f} MB)")


# ------------------------------------------------------------------ helpers
def fetch_geojson(source):
    if os.path.exists(source):
        with open(source) as f:
            return json.load(f)
    p = cache_path(source, os.path.basename(source) or "land.geojson")
    with open(p) as f:
        return json.load(f)


def iter_polygons(geometry):
    t = geometry.get("type")
    if t == "Polygon":
        yield geometry["coordinates"]
    elif t == "MultiPolygon":
        yield from geometry["coordinates"]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--layers", default="all",
                    help="comma list of landmask,elevation,koppen (or 'all')")
    ap.add_argument("--width", type=int, default=8192)
    ap.add_argument("--force", action="store_true")
    ap.add_argument("--elevation-src", default=DEFAULT_ELEVATION)
    ap.add_argument("--koppen-src", default=DEFAULT_KOPPEN)
    args = ap.parse_args()

    h = args.width // 2
    layers = ["landmask", "elevation", "koppen"] if args.layers == "all" \
        else [s.strip() for s in args.layers.split(",")]

    for layer in layers:
        print(f"[{layer}] {args.width}x{h}")
        if layer == "landmask":
            bake_landmask(args.width, h, args.force)
        elif layer == "elevation":
            bake_elevation(args.width, h, args.force, args.elevation_src)
        elif layer == "koppen":
            bake_koppen(args.width, h, args.force, args.koppen_src)
        else:
            sys.exit(f"unknown layer: {layer}")


if __name__ == "__main__":
    main()
