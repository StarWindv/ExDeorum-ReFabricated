/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.compat.seeds;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.api.ExDeorumApi;
import top.starwindv.exdeorum.registry.ECompressedBlocks;
import top.starwindv.exdeorum.registry.EItems;

/**
 * One mod's contribution to the sieves: the seeds it adds and how likely each mesh tier is to
 * yield them.
 *
 * <p>Implementations are only consulted when their mod is loaded, and every item lookup is
 * optional, so a provider for an absent mod, or for a mod that renamed an item in a newer
 * version, contributes nothing instead of breaking the game.
 *
 * <p>Nothing here is keyed on a specific recipe id: the tunable probability config screen
 * enumerates whatever recipes end up registered, so a provider only has to describe its seeds.
 */
public interface ModSeeds {
    /**
     * The mod that has to be present for this provider to apply, or {@code null} to always run.
     */
    @Nullable String modId();

    /** Adds this mod's seeds to the sieves. */
    void register(Collector collector);

    /**
     * Builds sieve drops for the collected seeds.
     *
     * <p>The mesh tiers mirror the vanilla seed drops in {@code SieveRecipes}: the string mesh is
     * the worst and the golden mesh the best, so seed density tracks the player's progression
     * through the mesh tree rather than being flat.
     */
    final class Collector {
        private static final float STRING_CHANCE = 0.1f;
        private static final float FLINT_CHANCE = 0.12f;
        private static final float IRON_CHANCE = 0.15f;
        private static final float GOLDEN_CHANCE = 0.165f;

        private static final Ingredient DIRT = Ingredient.of(Blocks.DIRT);
        private static final Ingredient COMPRESSED_DIRT = Ingredient.of(ECompressedBlocks.COMPRESSED_DIRT.getBlock());

        /**
         * Sifts dirt with every mesh, dropping the seed of {@code owner} at {@code path}.
         *
         * <p>Looks in the owning namespace first, then in {@code minecraft}. Some mods register a
         * crop's seed under the vanilla namespace on purpose rather than by mistake, and this is
         * the only place Ex Deorum would otherwise cover them. The owning mod still has to be
         * loaded, otherwise this would re-register vanilla seeds the data pack already sifts.
         *
         * @param owner the mod being compat'd, also the recipe id namespace
         * @param path  the item path, e.g. {@code cabbage_seeds}
         */
        public void seedFromDirt(String owner, String path) {
            var own = item(owner, path);

            if (own != null) {
                fromDirt(owner, path, new ItemStack(own));
                return;
            }

            var vanilla = item("minecraft", path);

            if (vanilla != null) {
                ExDeorum.LOGGER.debug("Seed compat: {}/{} is published under minecraft:{}, sifting it",
                        owner, path, path);
                fromDirt(owner, path, new ItemStack(vanilla));
            }
        }

        private void fromDirt(String namespace, String path, ItemStack result) {
            var regular = Identifier.fromNamespaceAndPath(namespace, path);
            // The compressed sieve is a separate recipe type with its own ids, so it needs its own
            // namespace or both recipes would share a ResourceKey and collapse into one entry in
            // the tunable probability config.
            var compressed = Identifier.fromNamespaceAndPath("exdeorum_compressed_sieve", path);

            for (var tier : new Tier[]{new Tier(EItems.STRING_MESH.get(), STRING_CHANCE),
                    new Tier(EItems.FLINT_MESH.get(), FLINT_CHANCE),
                    new Tier(EItems.IRON_MESH.get(), IRON_CHANCE),
                    new Tier(EItems.GOLDEN_MESH.get(), GOLDEN_CHANCE)}) {
                var mesh = Ingredient.of(tier.mesh());

                ExDeorumApi.addSieveDrops(ExDeorumApi.SieveDrop.chance(regular, DIRT, mesh, result, 1, tier.chance()));
                ExDeorumApi.addCompressedSieveDrops(ExDeorumApi.SieveDrop.chance(compressed, COMPRESSED_DIRT, mesh, result, 1, tier.chance()));
            }
        }

        private record Tier(Item mesh, float chance) {
        }

        /**
         * @return the item, or {@code null} when it is not registered. Never throws, so a mod that
         * drops or renames a seed cannot stop the game from starting.
         */
        private static @Nullable Item item(String namespace, String path) {
            try {
                return BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath(namespace, path))
                        .map(reference -> reference.value())
                        .orElse(null);
            } catch (Exception e) {
                ExDeorum.LOGGER.warn("Seed compat: could not resolve {}:{} ({}), skipping",
                        namespace, path, e.toString());
                return null;
            }
        }
    }
}