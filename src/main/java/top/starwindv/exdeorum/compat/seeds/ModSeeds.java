/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.compat.seeds;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.api.ExDeorumApi;
import top.starwindv.exdeorum.registry.ECompressedBlocks;
import top.starwindv.exdeorum.registry.EItems;

/**
 * One mod's contribution to the sieves: the seeds and other starter drops it adds, and how
 * likely each mesh tier is to yield them.
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
     * the worst and the netherite mesh the best, so seed density tracks the player's progression
     * through the mesh tree rather than being flat.
     */
    final class Collector {
        // Seed chances are deliberately well below the vanilla curve (wheat runs 0.125 -> 0.3):
        // Ex Deorum worlds are skyblocks where a handful of mod seeds go a long way, so a full
        // stack of dirt should yield a few, not dozens.
        private static final float STRING_CHANCE = 0.05f;
        private static final float FLINT_CHANCE = 0.06f;
        private static final float IRON_CHANCE = 0.07f;
        private static final float GOLDEN_CHANCE = 0.08f;
        private static final float DIAMOND_CHANCE = 0.09f;
        private static final float NETHERITE_CHANCE = 0.1f;
        // The compressed sieve makes the same per-roll draws as the regular one, but a
        // compressed dirt block stands for several, so its recipes all roll 7 times.
        private static final int COMPRESSED_ROLLS = 7;
        // Gravel drops reuse the data pack's own ore chunk chances, so they sit at the same
        // place in the progression as the chunks every player already knows.
        private static final float GRAVEL_IRON_CHANCE = 0.09f;
        private static final float GRAVEL_GOLDEN_CHANCE = 0.07f;
        private static final float GRAVEL_DIAMOND_CHANCE = 0.11f;
        private static final float GRAVEL_NETHERITE_CHANCE = 0.12f;

        private static final Ingredient DIRT = Ingredient.of(Blocks.DIRT);
        private static final Ingredient COMPRESSED_DIRT = Ingredient.of(ECompressedBlocks.COMPRESSED_DIRT.getBlock());
        private static final Ingredient GRAVEL = Ingredient.of(Blocks.GRAVEL);
        private static final Ingredient COMPRESSED_GRAVEL = Ingredient.of(ECompressedBlocks.COMPRESSED_GRAVEL.getBlock());

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
                fromDirt(owner, path, own);
                return;
            }

            var vanilla = item("minecraft", path);

            if (vanilla != null) {
                ExDeorum.LOGGER.debug("Seed compat: {}/{} is published under minecraft:{}, sifting it",
                        owner, path, path);
                fromDirt(owner, path, vanilla);
                return;
            }

            // Neither namespace has it, e.g. the mod renamed the seed or this compat entry is
            // out of date. Say so instead of silently dropping the seed from the sieves.
            ExDeorum.LOGGER.warn("Seed compat: no item {}/{} or minecraft:{} is registered, seed not added",
                    owner, path, path);
        }

        /**
         * Sifts gravel with the iron mesh and above — the same window the data pack uses for
         * its ore chunks, whose chances these reuse. Gravel is where sieved goods start
         * looking processed, so mod items that are not plants belong here rather than in dirt.
         *
         * <p>Registers the compressed sieve variants too, which make the same draws with seven
         * rolls like the compressed dirt ones.
         */
        public void dropFromGravel(String owner, String path) {
            var item = item(owner, path);

            if (item == null) {
                ExDeorum.LOGGER.warn("Seed compat: no item {}/{} or minecraft:{} is registered, drop not added",
                        owner, path, path);
                return;
            }

            var result = new ItemStackTemplate(item, 1);

            for (var tier : new Tier[]{new Tier(EItems.IRON_MESH.get(), GRAVEL_IRON_CHANCE),
                    new Tier(EItems.GOLDEN_MESH.get(), GRAVEL_GOLDEN_CHANCE),
                    new Tier(EItems.DIAMOND_MESH.get(), GRAVEL_DIAMOND_CHANCE),
                    new Tier(EItems.NETHERITE_MESH.get(), GRAVEL_NETHERITE_CHANCE)}) {
                var mesh = Ingredient.of(tier.mesh());
                var suffix = path + "/" + BuiltInRegistries.ITEM.getKey(tier.mesh()).getPath();

                ExDeorumApi.addSieveDrops(ExDeorumApi.SieveDrop.chance(
                        Identifier.fromNamespaceAndPath(owner, suffix), GRAVEL, mesh, result, 1, tier.chance()));
                ExDeorumApi.addCompressedSieveDrops(ExDeorumApi.SieveDrop.chance(
                        Identifier.fromNamespaceAndPath("exdeorum_compressed_sieve", suffix), COMPRESSED_GRAVEL, mesh,
                        result, COMPRESSED_ROLLS, tier.chance()));
            }
        }

        private void fromDirt(String namespace, String path, Item result) {
            // The result must stay a template: this runs from the recipe reload hook, which 26.2
            // schedules before the item components are bound, so building an ItemStack here would
            // throw "Components not bound yet". The sieves only unbox a real stack while actually
            // dropping, by which point the components are long since bound.
            var template = new ItemStackTemplate(result, 1);

            for (var tier : new Tier[]{new Tier(EItems.STRING_MESH.get(), STRING_CHANCE),
                    new Tier(EItems.FLINT_MESH.get(), FLINT_CHANCE),
                    new Tier(EItems.IRON_MESH.get(), IRON_CHANCE),
                    new Tier(EItems.GOLDEN_MESH.get(), GOLDEN_CHANCE),
                    new Tier(EItems.DIAMOND_MESH.get(), DIAMOND_CHANCE),
                    new Tier(EItems.NETHERITE_MESH.get(), NETHERITE_CHANCE)}) {
                // Per-mesh ids, mirroring how the data pack names its sieve recipes, so every tier
                // is a separately tunable entry in the probability config instead of four holders
                // sharing one key.
                var meshId = BuiltInRegistries.ITEM.getKey(tier.mesh()).getPath();
                var regular = Identifier.fromNamespaceAndPath(namespace, path + "/" + meshId);
                // The compressed sieve is a separate recipe type with its own ids, so it needs its
                // own namespace or both recipes would share a ResourceKey and collapse into one
                // entry in the tunable probability config.
                var compressed = Identifier.fromNamespaceAndPath("exdeorum_compressed_sieve", path + "/" + meshId);
                var mesh = Ingredient.of(tier.mesh());

                ExDeorumApi.addSieveDrops(ExDeorumApi.SieveDrop.chance(regular, DIRT, mesh, template, 1, tier.chance()));
                ExDeorumApi.addCompressedSieveDrops(ExDeorumApi.SieveDrop.chance(compressed, COMPRESSED_DIRT, mesh, template,
                        COMPRESSED_ROLLS, tier.chance()));
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