/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import top.starwindv.exdeorum.config.ProbabilityConfig;

/**
 * Discovers every {@link TunableRecipe} the game loaded and applies the player's drop rate
 * overrides to them.
 * <p>
 * Nothing is keyed on a specific item or recipe id: the set of tunable entries is whatever
 * the loaded recipes declare, so datapack added recipes show up in the config screen for
 * free.
 */
public final class ProbabilityTuner {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * One tunable drop rate, as shown by the config screen.
     *
     * @param id      the recipe id, which is also the key in the config file
     * @param recipe  the recipe as loaded, before any override
     * @param defaultValue the chance the recipe itself asks for
     */
    public record Entry(ResourceKey<Recipe<?>> id, TunableRecipe recipe, float defaultValue) {
        public int rolls() {
            return this.recipe.tunableRolls();
        }

        public boolean editable() {
            return this.recipe.tunableEditable();
        }

        public float current() {
            return ProbabilityConfig.get(this.id, this.defaultValue);
        }

        public boolean modified() {
            return ProbabilityConfig.isOverridden(this.id);
        }

        /** The expected number of items produced per use, for display. */
        public double expected() {
            return this.current() * this.rolls();
        }
    }

    /**
     * Immutable snapshot of everything the last reload discovered.
     * <p>
     * Discovery happens on the data pack reload thread while the config screen reads it from
     * the render thread, so the state is built up locally and published as one immutable
     * object rather than mutated in place.
     */
    private record Snapshot(List<Entry> entries, Map<ResourceKey<Recipe<?>>, Float> defaults,
                            List<ResourceKey<Recipe<?>>> applied) {
    }

    private static volatile Snapshot state = new Snapshot(List.of(), Map.of(), List.of());
    private static final ThreadLocal<Builder> BUILDING = ThreadLocal.withInitial(Builder::new);

    private static final class Builder {
        final List<Entry> entries = new ArrayList<>();
        final Map<ResourceKey<Recipe<?>>, Float> defaults = new LinkedHashMap<>();
        final List<ResourceKey<Recipe<?>>> applied = new ArrayList<>();
    }

    private ProbabilityTuner() {
    }

    /**
     * Clears the discovered state. Call once before feeding a fresh set of recipe types
     * through {@link #tune}, which accumulates.
     */
    public static void beginReload() {
        BUILDING.get().entries.clear();
        BUILDING.get().defaults.clear();
        BUILDING.get().applied.clear();
    }

    /**
     * Records the tunable recipes found in one recipe type and returns the same recipes with
     * any player override already applied, so callers can feed the result straight into the
     * recipe caches.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Recipe<?>> Collection<RecipeHolder<T>> tune(Collection<RecipeHolder<T>> holders) {
        var builder = BUILDING.get();
        var tuned = new ArrayList<RecipeHolder<T>>(holders.size());

        for (var holder : holders) {
            if (!(holder.value() instanceof TunableRecipe tunable) || !tunable.tunableEditable()) {
                tuned.add(holder);
                continue;
            }

            var id = holder.id();
            var defaultValue = tunable.tunableProbability();
            builder.defaults.put(id, defaultValue);
            builder.entries.add(new Entry(id, tunable, defaultValue));

            var override = ProbabilityConfig.get(id, Float.NaN);

            if (!Float.isNaN(override) && Math.abs(override - defaultValue) > 1.0e-6f) {
                tuned.add(new RecipeHolder<>(id, (T) tunable.withTunableProbability(override)));
                builder.applied.add(id);
            } else {
                tuned.add(holder);
            }
        }

        return tuned;
    }

    /**
     * Publishes what the reload found. Must be called once after the last {@link #tune}, which
     * is also what keeps the per-thread builder from being reused by the next reload.
     */
    public static void endReload() {
        var builder = BUILDING.get();

        try {
            state = new Snapshot(List.copyOf(builder.entries), Map.copyOf(builder.defaults), List.copyOf(builder.applied));
        } finally {
            BUILDING.remove();
        }
    }

    /** Logs how many of the configured overrides matched a loaded recipe. */
    public static void logApplied() {
        var applied = state.applied();

        if (applied.isEmpty()) {
            return;
        }

        var unmatched = new ArrayList<String>();

        for (var id : ProbabilityConfig.overrides().keySet()) {
            if (!applied.contains(id)) {
                unmatched.add(id.identifier().toString());
            }
        }

        LOGGER.info("Applied {} drop rate override(s)", applied.size());

        if (!unmatched.isEmpty()) {
            LOGGER.warn("These drop rate overrides did not match any loaded recipe: {}", String.join(", ", unmatched));
        }
    }

    /** All tunable entries discovered by the most recent reload. */
    public static List<Entry> entries() {
        return state.entries();
    }

    /** Default chance per recipe id, used to prune redundant entries when saving. */
    public static Map<ResourceKey<Recipe<?>>, Float> defaults() {
        return state.defaults();
    }

    /**
     * Persists the current overrides and rebuilds every cache, so edits apply without a
     * world reload. Only reachable from the client config screen on an integrated server.
     */
    public static void saveAndReloadRecipes() {
        ProbabilityConfig.save(defaults());

        var recipes = RecipeUtil.getRecipeMap();

        if (recipes == null) {
            return;
        }

        RecipeUtil.reload(recipes);
        LOGGER.info("Reloaded recipes with {} drop rate override(s)", ProbabilityConfig.size());
    }
}