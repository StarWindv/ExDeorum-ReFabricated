/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Player facing drop rate overrides, stored as JSON in the game's own config folder so they
 * survive mod updates and can be inspected or hand edited.
 * <p>
 * The file is sparse: it only lists recipes whose chance differs from what the recipe
 * itself asks for. Anything absent, or listed with the recipe's own value, uses the
 * original number. Because these are explicit player edits they win over datapacks, and a
 * conflict with a datapack is logged rather than silently resolved.
 */
public final class ProbabilityConfig {
    public static final String FILE_NAME = "exdeorum-probabilities.json";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Sparse overrides, keyed by recipe id. Only entries that differ from the recipe default
     * live here.
     * <p>
     * Written from the config screen on the render thread and read from the data pack reload
     * thread, so this must be a concurrent map; a plain LinkedHashMap read while another
     * thread writes can spin forever inside a resize.
     */
    private static final Map<ResourceKey<Recipe<?>>, Float> OVERRIDES = new ConcurrentHashMap<>();

    private ProbabilityConfig() {
    }

    public static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    /** Reloads the overrides from disk. A missing file simply means "no overrides". */
    public static void load() {
        OVERRIDES.clear();
        var file = path();

        if (!Files.isRegularFile(file)) {
            return;
        }

        JsonObject root;

        try {
            root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.error("Could not read {}, keeping the recipes' own drop rates", FILE_NAME, e);
            backup(file);
            return;
        }

        var applied = 0;

        for (var entry : root.entrySet()) {
            var value = entry.getValue();

            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                LOGGER.warn("Ignoring \"{}\" in {}: expected a number", entry.getKey(), FILE_NAME);
                continue;
            }

            var id = Identifier.tryParse(entry.getKey());

            if (id == null) {
                LOGGER.warn("Ignoring \"{}\" in {}: not a valid id", entry.getKey(), FILE_NAME);
                continue;
            }

            var probability = Mth.clamp(value.getAsFloat(), 0.0f, 1.0f);

            if (probability != value.getAsFloat()) {
                LOGGER.warn("Clamping \"{}\" in {} from {} to {}", entry.getKey(), FILE_NAME, value.getAsFloat(), probability);
            }

            OVERRIDES.put(ResourceKey.create(Registries.RECIPE, id), probability);
            applied++;
        }

        LOGGER.info("Loaded {} drop rate override(s) from {}", applied, FILE_NAME);
    }

    /**
     * Writes the overrides back, dropping any entry that now matches the recipe's own value
     * so that "reset to default" leaves no residue in the file.
     *
     * @param defaults the current default chance per recipe, used to prune redundant entries
     */
    public static void save(Map<ResourceKey<Recipe<?>>, Float> defaults) {
        var pruned = new TreeMap<String, Float>();

        for (var entry : OVERRIDES.entrySet()) {
            var value = entry.getValue();
            var defaultValue = defaults.get(entry.getKey());

            if (defaultValue != null && Math.abs(defaultValue - value) < 1.0e-6f) {
                continue;
            }

            pruned.put(entry.getKey().identifier().toString(), value);
        }

        OVERRIDES.keySet().removeIf(id -> !pruned.containsKey(id.identifier().toString()));

        var file = path();
        JsonObject root = new JsonObject();

        for (var entry : pruned.entrySet()) {
            root.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
        }

        try {
            var parent = file.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(file, GSON.toJson(root) + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Could not write {}", FILE_NAME, e);
        }
    }

    public static void set(ResourceKey<Recipe<?>> id, float probability) {
        OVERRIDES.put(id, Mth.clamp(probability, 0.0f, 1.0f));
    }

    public static void clear(ResourceKey<Recipe<?>> id) {
        OVERRIDES.remove(id);
    }

    public static void clearAll() {
        OVERRIDES.clear();
    }

    public static boolean isOverridden(ResourceKey<Recipe<?>> id) {
        return OVERRIDES.containsKey(id);
    }

    public static float get(ResourceKey<Recipe<?>> id, float fallback) {
        return OVERRIDES.getOrDefault(id, fallback);
    }

    public static Map<ResourceKey<Recipe<?>>, Float> overrides() {
        return Map.copyOf(OVERRIDES);
    }

    public static int size() {
        return OVERRIDES.size();
    }

    /** Serialised view of the file for the config screen to show. */
    public static JsonObject toJson() {
        var root = new JsonObject();

        for (var entry : OVERRIDES.entrySet()) {
            root.addProperty(entry.getKey().identifier().toString(), entry.getValue());
        }

        return root;
    }

    private static void backup(Path file) {
        try {
            Files.move(file, file.resolveSibling(FILE_NAME + ".broken"), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.warn("Moved the unreadable {} aside as {}.broken", FILE_NAME, FILE_NAME);
        } catch (IOException e) {
            LOGGER.warn("Could not move the unreadable {} aside", FILE_NAME, e);
        }
    }
}