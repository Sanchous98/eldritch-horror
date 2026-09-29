package com.sanchous98.eldritchhorror.world;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Loads and samples the baked Earth layers (see {@code tools/bake_earth.py}).
 *
 * <p>Pixels are equirectangular at 8 blocks/pixel: image x 0..W-1 maps to world
 * X -65536..65535 and image y 0..H-1 maps to world Z -32768..32767.
 *
 * <ul>
 *   <li>{@code landmask_<W>x<H>.png} — 1-bit land/ocean.</li>
 *   <li>{@code elevation_<W>x<H>.png} — 8-bit grayscale, {@code round(metres / ELEVATION_METRES_PER_LEVEL) + ELEVATION_OFFSET}.</li>
 *   <li>{@code koppen_<W>x<H>.png} — 8-bit Köppen–Geiger class index.</li>
 * </ul>
 *
 * <p>Read-only and thread-safe once loaded. This class deliberately has no Minecraft
 * dependency so it can be unit-tested without a game.
 */
public final class EarthMap {
    /** World extent (blocks). Map is 2:1: X spans 2 * HALF_WIDTH. */
    public static final int HALF_WIDTH = 65536;
    public static final int HALF_HEIGHT = 32768;
    /** Blocks per map pixel. */
    public static final int BLOCKS_PER_PIXEL = 8;
    /** Stored elevation is {@code round(metres / ELEVATION_METRES_PER_LEVEL) + ELEVATION_OFFSET}. */
    public static final int ELEVATION_OFFSET = 127;
    /** Metres represented by one stored elevation level. */
    public static final double ELEVATION_METRES_PER_LEVEL = 75.0;

    private final int width;
    private final int height;
    private final BufferedImage land;
    private final BufferedImage elevation;
    private final BufferedImage koppen;

    private EarthMap(int width, int height, BufferedImage land,
                     BufferedImage elevation, BufferedImage koppen) {
        this.width = width;
        this.height = height;
        this.land = land;
        this.elevation = elevation;
        this.koppen = koppen;
    }

    /** Loads the layers from the mod resources for the given pixel width. */
    public static EarthMap load(int pixelWidth) throws IOException {
        return load(EarthMap.class.getClassLoader(), pixelWidth);
    }

    /**
     * Loads the layers using an explicit class loader. On a dedicated server the mod jar is
     * loaded by a separate class loader, so callers should pass theirs when possible.
     */
    public static EarthMap load(ClassLoader loader, int pixelWidth) throws IOException {
        int w = pixelWidth;
        int h = pixelWidth / 2;
        return new EarthMap(w, h,
                read(loader, "landmask_" + w + "x" + h + ".png"),
                read(loader, "elevation_" + w + "x" + h + ".png"),
                read(loader, "koppen_" + w + "x" + h + ".png"));
    }

    private static final String MAP_DIR = "assets/eldritch_horror/map/";

    private static BufferedImage read(ClassLoader loader, String name) throws IOException {
        InputStream in = loader != null ? loader.getResourceAsStream(MAP_DIR + name) : null;
        if (in == null) {
            in = EarthMap.class.getResourceAsStream("/" + MAP_DIR + name);
        }
        if (in == null) {
            ClassLoader ctx = Thread.currentThread().getContextClassLoader();
            if (ctx != null) {
                in = ctx.getResourceAsStream(MAP_DIR + name);
            }
        }
        if (in == null) {
            throw new IOException("missing map layer " + MAP_DIR + name);
        }
        try (InputStream stream = in) {
            return ImageIO.read(stream);
        }
    }

    public int pixelWidth() {
        return width;
    }

    public int pixelHeight() {
        return height;
    }

    /** True if the column at world (x,z) is land. Nearest-neighbour. */
    public boolean isLand(int worldX, int worldZ) {
        return sample(land, worldX, worldZ) != 0;
    }

    /** Real elevation in metres at world (x,z). Bilinear. Negative = ocean floor. */
    public double elevationMetres(double worldX, double worldZ) {
        double px = (worldX + HALF_WIDTH) / BLOCKS_PER_PIXEL;
        double py = (worldZ + HALF_HEIGHT) / BLOCKS_PER_PIXEL;
        double v = bilinear(elevation, px, py);
        return (v - ELEVATION_OFFSET) * ELEVATION_METRES_PER_LEVEL;
    }

    /** Köppen–Geiger class index (0 = ocean/unknown, else the legend number). Nearest. */
    public int koppenClass(int worldX, int worldZ) {
        return sample(koppen, worldX, worldZ);
    }

    // ------------------------------------------------------------------ sampling
    private int sample(BufferedImage img, int worldX, int worldZ) {
        int px = clamp((worldX + HALF_WIDTH) / BLOCKS_PER_PIXEL, width - 1);
        int py = clamp((worldZ + HALF_HEIGHT) / BLOCKS_PER_PIXEL, height - 1);
        return img.getRaster().getSample(px, py, 0);
    }

    private double bilinear(BufferedImage img, double px, double py) {
        int x0 = (int) Math.floor(px);
        int y0 = (int) Math.floor(py);
        double fx = px - x0;
        double fy = py - y0;
        double v00 = at(img, x0, y0);
        double v10 = at(img, x0 + 1, y0);
        double v01 = at(img, x0, y0 + 1);
        double v11 = at(img, x0 + 1, y0 + 1);
        return (v00 * (1 - fx) + v10 * fx) * (1 - fy) + (v01 * (1 - fx) + v11 * fx) * fy;
    }

    private double at(BufferedImage img, int px, int py) {
        int x = Math.max(0, Math.min(width - 1, px));
        int y = Math.max(0, Math.min(height - 1, py));
        return img.getRaster().getSample(x, y, 0);
    }

    private static int clamp(int v, int max) {
        return v < 0 ? 0 : (v > max ? max : v);
    }
}
