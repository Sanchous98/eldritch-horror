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
DEFAULT_PLACES = (
    "https://raw.githubusercontent.com/nvkelso/natural-earth-vector/"
    "master/geojson/ne_10m_populated_places_simple.geojson"
)

# --- Curated cities: the ~24 real cities the game actually features (see design/21). ---
CURATED_CITIES = [
    "London", "Paris", "Rome", "Istanbul", "Cairo", "Lagos", "Nairobi", "Cape Town",
    "Moscow", "Delhi", "Mumbai", "Shanghai", "Beijing", "Tokyo", "Seoul", "Bangkok",
    "Jakarta", "Sydney", "New York", "Los Angeles", "Mexico City", "Rio de Janeiro",
    "Buenos Aires", "Lima",
]
CURATED_MARKER = "curated"

HERE = os.path.dirname(__file__)
CACHE = os.path.join(HERE, ".cache")
OUT_DIR = os.path.abspath(os.path.join(
    HERE, "..", "src", "main", "resources", "assets", "eldritch_horror", "map"))
ELEV_OFFSET = 12000  # store elevation+offset as unsigned 16-bit

# World extent, matching EarthMap: 2:1, 2 blocks/pixel, 16384 x 8192.
BLOCKS_PER_DEGREE = 16384 / 360.0


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
def world_x(lon):
    return round(lon * BLOCKS_PER_DEGREE)


def world_z(lat):
    return round(-lat * BLOCKS_PER_DEGREE)


def bake_places(force, source, min_pop):
    """Bake real settlements into a JSON list (world coords, tier by population)."""
    import math

    out = os.path.join(OUT_DIR, "settlements.json")
    if os.path.exists(out) and not force:
        print(f"skip (exists): {out}")
        return
    data = fetch_geojson(source)
    places = []
    for feat in data["features"]:
        p = feat["properties"]
        name = p.get("name")
        if not name:
            continue
        lon = p.get("longitude", feat["geometry"]["coordinates"][0])
        lat = p.get("latitude", feat["geometry"]["coordinates"][1])
        if abs(lat) > 75:      # poles: skip (map is compressed at the edges)
            continue
        try:
            pop = int(p.get("pop_max") or 0)
        except (TypeError, ValueError):
            pop = 0
        # Natural Earth has duplicate rows; dedupe by name+coords.
        slug = "".join(c if c.isalnum() else "_" for c in name.lower()).strip("_")[:40]
        wx, wz = world_x(float(lon)), world_z(float(lat))
        places.append({
            "id": f"{slug}_{wx}_{wz}",   # coordinates guarantee uniqueness
            "name": name,
            "lat": round(float(lat), 4),
            "lon": round(float(lon), 4),
            "x": wx,
            "z": wz,
            "population": pop,
        })

    # De-duplicate on rounded coordinates, and keep only places above the floor.
    seen = set()
    unique = []
    for p in places:
        if p["population"] < min_pop:
            continue
        k = (p["x"], p["z"])
        if k in seen:
            continue
        seen.add(k)
        unique.append(p)

    # For each curated name keep only the largest-population entry; demote the rest.
    curated_best = {}
    for p in unique:
        if p["name"] in CURATED_CITIES:
            if p["name"] not in curated_best or p["population"] > curated_best[p["name"]]["population"]:
                curated_best[p["name"]] = p
    for p in unique:
        if p["name"] in CURATED_CITIES and curated_best.get(p["name"]) is not p:
            p["tier"] = "town"
        elif p["name"] in CURATED_CITIES:
            p["tier"] = CURATED_MARKER
        else:
            p["tier"] = "town"

    # Ensure every curated city resolved (fail loudly if a name is wrong/missing).
    found = {p["name"] for p in unique if p["tier"] == CURATED_MARKER}
    missing = [c for c in CURATED_CITIES if c not in found]
    if missing:
        print(f"WARNING: curated cities not found in source: {missing}")

    unique.sort(key=lambda p: (p["tier"] != CURATED_MARKER, -p["population"]))
    os.makedirs(OUT_DIR, exist_ok=True)
    with open(out, "w") as f:
        json.dump({"settlements": unique,
                   "curated": CURATED_CITIES,
                   "blocks_per_degree": BLOCKS_PER_DEGREE}, f, indent=1)
    print(f"wrote {out} ({len(unique)} places; {len(found)}/{len(CURATED_CITIES)} curated "
          f"cities; floor {min_pop})")


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
    ap.add_argument("--places-src", default=DEFAULT_PLACES)
    ap.add_argument("--min-pop", type=int, default=100000,
                    help="population at/above which a place is a city")
    args = ap.parse_args()

    h = args.width // 2
    layers = ["landmask", "elevation", "koppen", "places"] if args.layers == "all" \
        else [s.strip() for s in args.layers.split(",")]

    for layer in layers:
        print(f"[{layer}] {args.width}x{h}")
        if layer == "landmask":
            bake_landmask(args.width, h, args.force)
        elif layer == "elevation":
            bake_elevation(args.width, h, args.force, args.elevation_src)
        elif layer == "koppen":
            bake_koppen(args.width, h, args.force, args.koppen_src)
        elif layer == "places":
            bake_places(args.force, args.places_src, args.min_pop)
        else:
            sys.exit(f"unknown layer: {layer}")


if __name__ == "__main__":
    main()
