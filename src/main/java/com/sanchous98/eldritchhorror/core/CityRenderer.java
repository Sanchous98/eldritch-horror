package com.sanchous98.eldritchhorror.core;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.MapColor;

/**
 * Dev-only, server-side top-down + isometric PNG renderer for the generated cities.
 *
 * <p>There is no GPU client in this environment, so this class walks the already-generated chunks
 * on the server thread and paints each column with the game's own map colours. It is inert unless
 * the {@code eh.renderCities} system property is set (see
 * {@code world.ModWorldGenEvents.onServerStarted}).
 *
 * <p>Output: {@code run/render/<city_id>_top.png} and {@code run/render/<city_id>_iso.png}.
 */
public final class CityRenderer {

    /** Hard clamp on the render radius so a single city cannot try to generate the whole world. */
    private static final int MAX_RENDER_RADIUS = 500;
    /** Fallback colour (opaque grey) when a block reports no map colour. */
    private static final int FALLBACK_ARGB = 0xFF808080;

    private CityRenderer() {
    }

    /**
     * Renders each named city (matched case-insensitively on {@link City#name()}) to
     * {@code run/render}. Must be called on the server thread.
     *
     * @return the ids actually rendered
     */
    public static List<String> render(MinecraftServer server, List<String> cityNames) {
        List<String> rendered = new ArrayList<>();
        File cwd = new File(".").getAbsoluteFile();
        // The dev server's working directory is already the run/ directory; from the project root
        // it is not. Normalise to <project>/run/render either way.
        String cwdName;
        try {
            cwdName = cwd.getCanonicalFile().getName();
        } catch (java.io.IOException e) {
            cwdName = cwd.getName();
        }
        File dir = cwdName.equals("run") ? new File(cwd, "render") : new File(cwd, "run/render");
        if (!dir.exists() && !dir.mkdirs()) {
            EldritchHorror.LOGGER.error("CityRenderer: could not create {}", dir.getAbsolutePath());
            return rendered;
        }

        for (City city : Cities.all()) {
            if (!cityNames.isEmpty() && cityNames.stream().noneMatch(n -> n.equalsIgnoreCase(city.name())
                    || n.equalsIgnoreCase(city.id()))) {
                continue;
            }
            try {
                renderCity(server, city, dir);
                rendered.add(city.id());
            } catch (Exception e) {
                EldritchHorror.LOGGER.error("CityRenderer: failed to render {} ({})",
                        city.name(), city.id(), e);
            }
        }
        EldritchHorror.LOGGER.info("CityRenderer: rendered {} cities into {}",
                rendered.size(), dir.getAbsolutePath());
        return rendered;
    }

    private static void renderCity(MinecraftServer server, City city, File dir) throws Exception {
        ServerLevel level = server.overworld();
        // The city is actually built by CityLocation, whose radius (220..400) is far larger than
        // the legacy City.radius() (10..44); render the real footprint.
        int cityRadius = new com.sanchous98.eldritchhorror.world.loc.city.CityLocation(city).radius();
        int radius = Math.min(cityRadius + 24, MAX_RENDER_RADIUS);
        int diameter = radius * 2 + 1;
        int minX = city.x() - radius;
        int minZ = city.z() - radius;

        // --- 1. Force-generate every chunk covering the render square, synchronously. ---
        int cMinX = (city.x() - radius) >> 4;
        int cMaxX = (city.x() + radius) >> 4;
        int cMinZ = (city.z() - radius) >> 4;
        int cMaxZ = (city.z() + radius) >> 4;
        long total = (long) (cMaxX - cMinX + 1) * (cMaxZ - cMinZ + 1);
        int step = (int) Math.max(1, Math.ceil(Math.sqrt(total / 4096.0))); // keep chunk count sane
        int chunks = 0;
        for (int cx = cMinX; cx <= cMaxX; cx += step) {
            for (int cz = cMinZ; cz <= cMaxZ; cz += step) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
                chunks++;
            }
        }

        // --- 2. Sample every column once: top non-air block + map colour. ---
        int maxY = level.getMaxY();
        int[] topY = new int[diameter * diameter];
        int[] argb = new int[diameter * diameter];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minSurface = Integer.MAX_VALUE;
        int maxSurface = Integer.MIN_VALUE;

        for (int ix = 0; ix < diameter; ix++) {
            int x = minX + ix;
            for (int iz = 0; iz < diameter; iz++) {
                int z = minZ + iz;
                int surface = level.getMinY();
                BlockState top = null;
                for (int y = maxY; y >= level.getMinY(); y--) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir()) {
                        surface = y;
                        top = state;
                        break;
                    }
                }
                int idx = ix * diameter + iz;
                topY[idx] = surface;
                argb[idx] = top == null ? FALLBACK_ARGB : colourOf(top, level, x, surface, z);
                minSurface = Math.min(minSurface, surface);
                maxSurface = Math.max(maxSurface, surface);
            }
        }
        if (maxSurface <= minSurface) {
            maxSurface = minSurface + 1; // avoid divide-by-zero on a perfectly flat tile
        }
        EldritchHorror.LOGGER.info(
                "CityRenderer: {} generated {} chunks (step {}), sampled {}x{} columns, y {}..{}, radius {}",
                city.id(), chunks, step, diameter, diameter, minSurface, maxSurface, radius);

        // --- 3. Top-down map (north at top: x -> horizontal, z -> vertical). ---
        BufferedImage top = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        for (int ix = 0; ix < diameter; ix++) {
            for (int iz = 0; iz < diameter; iz++) {
                top.setRGB(ix, iz, argb[ix * diameter + iz]);
            }
        }
        write(top, dir, city.id() + "_top.png");

        // --- 4. Isometric projection, columns painted back-to-front. ---
        // sx = (x - minX) - (z - minZ); sy = ((x - minX) + (z - minZ)) / 2 - (topY - minSurface).
        int spanX = diameter;
        int spanZ = diameter;
        int isoW = spanX + spanZ;
        int groundSpan = (spanX + spanZ) / 2;
        int minDrawY = -(maxSurface - minSurface);
        int maxDrawY = groundSpan;
        int isoH = maxDrawY - minDrawY + 1;
        int offY = -minDrawY;

        BufferedImage iso = new BufferedImage(isoW, isoH, BufferedImage.TYPE_INT_ARGB);
        // Back-to-front: far columns (small x+z) first, so nearer ones overwrite them.
        for (int s = 0; s <= (spanX - 1) + (spanZ - 1); s++) {
            for (int ix = 0; ix < spanX; ix++) {
                int iz = s - ix;
                if (iz < 0 || iz >= spanZ) {
                    continue;
                }
                int idx = ix * diameter + iz;
                int sx = ix - iz + (spanZ - 1);
                int sy = (ix + iz) / 2 - (topY[idx] - minSurface) + offY;
                if (sx >= 0 && sx < isoW && sy >= 0 && sy < isoH) {
                    iso.setRGB(sx, sy, argb[idx]);
                }
            }
        }
        write(iso, dir, city.id() + "_iso.png");
    }

    /** Map colour of a state, as an opaque ARGB int, falling back to grey. */
    private static int colourOf(BlockState state, ServerLevel level, int x, int y, int z) {
        try {
            MapColor mapColor = state.getMapColor(level, new BlockPos(x, y, z));
            if (mapColor != null && mapColor != MapColor.NONE && mapColor.col != 0) {
                return mapColor.calculateARGBColor(MapColor.Brightness.HIGH);
            }
        } catch (Exception ignored) {
            // fall through to grey
        }
        return FALLBACK_ARGB;
    }

    private static void write(BufferedImage image, File dir, String name) throws Exception {
        File out = new File(dir, name);
        ImageIO.write(image, "png", out);
        EldritchHorror.LOGGER.info("CityRenderer: wrote {} ({}x{})",
                out.getAbsolutePath(), image.getWidth(), image.getHeight());
    }
}
