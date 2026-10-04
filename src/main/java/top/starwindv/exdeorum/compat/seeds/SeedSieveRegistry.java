/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.compat.seeds;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import top.starwindv.exdeorum.ExDeorum;

/**
 * Registry of {@link ModSeeds} providers, resolved once the first datapack is loaded.
 *
 * <p>Split into register and process on purpose:
 *
 * <ul>
 *   <li>{@link #register} only records the provider. It is safe to call during mod init, and
 *       resolving items there would not work: Fabric gives no ordering guarantee between mods'
 *   initializers, so another mod's items are not necessarily in the registry yet.</li>
 *   <li>{@link #process} runs every provider whose mod is loaded. It is called after the first
 *       recipe reload, where the item registry and the tags are both fully populated.</li>
 * </ul>
 *
 * <p>Processing happens once. {@code /reload} rebuilds the recipe caches from what
 * {@link top.starwindv.exdeorum.api.ExDeorumApi} already holds, so the registered drops survive
 * every reload without being re-added.
 */
public final class SeedSieveRegistry {
    private static final List<ModSeeds> PROVIDERS = new CopyOnWriteArrayList<>();
    private static volatile boolean processed;

    private SeedSieveRegistry() {
    }

    /**
     * Adds a provider. Call this during your own mod init; it does no registry lookups.
     *
     * <p>Safe to call after {@link #process} has already run: a late provider is picked up on the
     * next reload rather than being dropped.
     */
    public static void register(ModSeeds provider) {
        PROVIDERS.add(provider);
    }

    /** Registers Ex Deorum's own providers. */
    public static void registerBuiltins() {
        register(new FarmersDelightSeeds());
        register(new RusticDelightSeeds());
        register(new UbesDelightSeeds());
    }

    /**
     * Resolves every registered provider whose mod is loaded and adds its seeds to the sieves.
     *
     * <p>Only does work on the first call. Idempotent afterwards, so a datapack reload or a
     * second world load does not register the same drops twice.
     *
     * @return how many providers were applied
     */
    public static int process() {
        if (processed) {
            return 0;
        }

        synchronized (SeedSieveRegistry.class) {
            if (processed) {
                return 0;
            }

            var collector = new ModSeeds.Collector();
            var applied = new ArrayList<String>(PROVIDERS.size());

            for (var provider : PROVIDERS) {
                var modId = provider.modId();

                if (modId != null && !isLoaded(modId)) {
                    continue;
                }

                var name = modId == null ? provider.getClass().getSimpleName() : modId;

                try {
                    provider.register(collector);
                    applied.add(name);
                } catch (Exception e) {
                    // One misbehaving provider must not take the rest of the sieve drops with it,
                    // and must never stop the game from loading a world.
                    ExDeorum.LOGGER.warn("Seed compat: {} failed to register its seeds ({}), skipping",
                            name, e.toString());
                }
            }

            processed = true;

            ExDeorum.LOGGER.info("Ex Deorum sifting seed compat active for {}", applied);

            return applied.size();
        }
    }

    /** Whether {@link #process} has already run. */
    public static boolean isProcessed() {
        return processed;
    }

    /** How many providers are registered, loaded mods or not. */
    public static int size() {
        return PROVIDERS.size();
    }

    private static boolean isLoaded(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).isPresent();
    }
}