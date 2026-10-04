/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.slf4j.Logger;
import top.starwindv.exdeorum.recipe.sieve.CompressedSieveRecipe;
import top.starwindv.exdeorum.recipe.sieve.SieveRecipe;

/**
 * Entry point for other mods that want to add their own drops to Ex Deorum's sieves and
 * compressed sieves, most usefully seeds and ores that no vanilla tag covers.
 *
 * <p>Follows the same shape as other mods' helper APIs: call the static methods during your
 * own mod init, no event subscription or interface implementation needed.
 *
 * <p>Registrations made here are applied on top of whatever the data packs define, so pack
 * authors can still override them. They survive a {@code /reload}.
 *
 * <p>Note that most mods do not need this at all: a data pack can add
 * {@code data/<yourmod>/recipe/exdeorum/sieve/<input>/<mesh>/<name>.json} directly. Reach for
 * this API when the inputs are generated in code, or when you want the registration to be
 * conditional.
 *
 * <pre>{@code
 * ExDeorumApi.addSieveDrops("mymod:wheat_seeds", ...);
 * }</pre>
 */
public final class ExDeorumApi {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier SIEVE_ID = Identifier.fromNamespaceAndPath("exdeorum", "sieve");
    private static final Identifier COMPRESSED_SIEVE_ID = Identifier.fromNamespaceAndPath("exdeorum", "compressed_sieve");

    private static final List<RecipeHolder<SieveRecipe>> SIEVE = new ArrayList<>();
    private static final List<RecipeHolder<CompressedSieveRecipe>> COMPRESSED_SIEVE = new ArrayList<>();

    private ExDeorumApi() {
    }

    /**
     * A sieve (or compressed sieve) recipe waiting to be merged into the recipe map.
     *
     * @param id       a unique id; must not collide with a data pack recipe
     * @param inputs   what can be sifted, e.g. one crushed block
     * @param meshes   the meshes that produce this drop, e.g. string and iron
     * @param result   what drops
     * @param rolls    how many independent rolls are made per sift
     * @param chance   the chance each roll succeeds, in {@code [0, 1]}
     * @param byHandOnly when true the mechanical sieve will not produce this
     */
    public record SieveDrop(Identifier id, Ingredient inputs, Ingredient meshes, ItemStackTemplate result,
                             int rolls, float chance, boolean byHandOnly) {
        public SieveDrop {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(inputs, "inputs");
            Objects.requireNonNull(meshes, "meshes");
            Objects.requireNonNull(result, "result");

            if (rolls <= 0) {
                throw new IllegalArgumentException("rolls must be positive, got " + rolls);
            }

            if (chance < 0.0f || chance > 1.0f) {
                throw new IllegalArgumentException("chance must be between 0 and 1, got " + chance);
            }
        }

        /** One roll with the given chance, the common case. */
        public static SieveDrop once(Identifier id, Ingredient inputs, Ingredient meshes, ItemStack result) {
            return new SieveDrop(id, inputs, meshes, new ItemStackTemplate(result.getItem(), result.getCount()),
                    1, 1.0f, false);
        }

        /** {@code rolls} rolls, each with the given chance. */
        public static SieveDrop chance(Identifier id, Ingredient inputs, Ingredient meshes, ItemStack result,
                                       int rolls, float chance) {
            return new SieveDrop(id, inputs, meshes, new ItemStackTemplate(result.getItem(), result.getCount()),
                    rolls, chance, false);
        }

        /** A flat one in {@code divisor}. */
        public static SieveDrop oneIn(Identifier id, Ingredient inputs, Ingredient meshes, ItemStack result, int divisor) {
            return new SieveDrop(id, inputs, meshes, new ItemStackTemplate(result.getItem(), result.getCount()),
                    1, 1.0f / divisor, false);
        }
    }

    // ------------------------------------------------------------------ sieves

    /**
     * Registers drops for the regular sieves.
     *
     * @return the ids that were registered
     */
    public static Collection<Identifier> addSieveDrops(SieveDrop... drops) {
        return addSieveDrops(List.of(drops));
    }

    /** Registers drops for the regular sieves. */
    public static Collection<Identifier> addSieveDrops(Collection<SieveDrop> drops) {
        var registered = new ArrayList<Identifier>();

        for (var drop : drops) {
            SIEVE.add(new RecipeHolder<>(key(SIEVE_ID, drop.id()),
                    new SieveRecipe(drop.inputs(), drop.result(), amount(drop), drop.meshes(), drop.byHandOnly())));
            registered.add(drop.id());
        }

        if (!registered.isEmpty()) {
            LOGGER.info("Ex Deorum registered {} extra sieve drop(s) from the API: {}", registered.size(), registered);
        }

        return registered;
    }

    /**
     * Registers drops for the compressed sieves, which are fed gravel-like input rather than
     * crushed blocks.
     */
    public static Collection<Identifier> addCompressedSieveDrops(SieveDrop... drops) {
        return addCompressedSieveDrops(List.of(drops));
    }

    /** Registers drops for the compressed sieves. */
    public static Collection<Identifier> addCompressedSieveDrops(Collection<SieveDrop> drops) {
        var registered = new ArrayList<Identifier>();

        for (var drop : drops) {
            COMPRESSED_SIEVE.add(new RecipeHolder<>(key(COMPRESSED_SIEVE_ID, drop.id()),
                    new CompressedSieveRecipe(drop.inputs(), drop.result(), amount(drop), drop.meshes(), drop.byHandOnly())));
            registered.add(drop.id());
        }

        if (!registered.isEmpty()) {
            LOGGER.info("Ex Deorum registered {} extra compressed sieve drop(s) from the API: {}", registered.size(), registered);
        }

        return registered;
    }

    /** Registers the same drops for both sieves, for ores that show up in either form. */
    public static Collection<Identifier> addSieveDropsForBoth(SieveDrop... drops) {
        var list = List.of(drops);
        var ids = new ArrayList<Identifier>(addSieveDrops(list));
        ids.addAll(addCompressedSieveDrops(list));
        return ids;
    }

    // ------------------------------------------------------------------ shortcuts

    /** One guaranteed drop from the given input and mesh. */
    public static void addSieveDrop(Identifier id, Ingredient inputs, Ingredient meshes, ItemStack result) {
        addSieveDrops(SieveDrop.once(id, inputs, meshes, result));
    }

    /** One guaranteed drop from the given block and mesh item. */
    public static void addSieveDrop(Identifier id, Block input, Item mesh, ItemStack result) {
        addSieveDrop(id, Ingredient.of(input), Ingredient.of(mesh), result);
    }

    /** A one in {@code divisor} drop, the shape most seed and gravel drops take. */
    public static void addSieveDropOneIn(Identifier id, Ingredient inputs, Ingredient meshes, ItemStack result, int divisor) {
        addSieveDrops(SieveDrop.oneIn(id, inputs, meshes, result, divisor));
    }

    /** A one in {@code divisor} drop from the given block and mesh item. */
    public static void addSieveDropOneIn(Identifier id, Block input, Item mesh, ItemStack result, int divisor) {
        addSieveDropOneIn(id, Ingredient.of(input), Ingredient.of(mesh), result, divisor);
    }

    // ------------------------------------------------------------------ internals

    /**
     * The recipes registered through this API, ready to be merged into the recipe map. Called
     * by {@link top.starwindv.exdeorum.recipe.RecipeUtil} on every reload, so registrations
     * made during mod init are picked up and survive a {@code /reload}.
     */
    public static <T extends Recipe<?>> Collection<RecipeHolder<T>> withSieveDrops(
            Collection<RecipeHolder<T>> loaded,
            @Nullable Collection<RecipeHolder<SieveRecipe>> extra,
            @Nullable Collection<RecipeHolder<CompressedSieveRecipe>> extraCompressed) {
        if ((extra == null || extra.isEmpty()) && (extraCompressed == null || extraCompressed.isEmpty())) {
            return loaded;
        }

        var merged = new ArrayList<RecipeHolder<T>>(loaded.size() + (extra == null ? 0 : extra.size()));

        for (var holder : loaded) {
            merged.add(holder);
        }

        if (extra != null) {
            merged.addAll(castAll(extra));
        }

        if (extraCompressed != null) {
            merged.addAll(castAll(extraCompressed));
        }

        return merged;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> List<RecipeHolder<T>> castAll(Collection<? extends RecipeHolder<?>> holders) {
        var out = new ArrayList<RecipeHolder<T>>(holders.size());
        holders.forEach(holder -> out.add((RecipeHolder<T>) holder));
        return out;
    }

    private static NumberProvider amount(SieveDrop drop) {
        return BinomialDistributionGenerator.binomial(drop.rolls(), drop.chance());
    }

    /**
     * Ids are namespaced under the recipe type, so {@code mymod:wheat_seeds} registered as a
     * sieve drop becomes {@code exdeorum:sieve/mymod/wheat_seeds}.
     */
    /** The sieve recipes registered so far. */
    public static Collection<RecipeHolder<SieveRecipe>> sieveDrops() {
        return List.copyOf(SIEVE);
    }

    /** The compressed sieve recipes registered so far. */
    public static Collection<RecipeHolder<CompressedSieveRecipe>> compressedSieveDrops() {
        return List.copyOf(COMPRESSED_SIEVE);
    }

    private static ResourceKey<Recipe<?>> key(Identifier type, Identifier id) {
        var path = type.getPath() + "/" + id.getNamespace() + "/" + id.getPath();
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(type.getNamespace(), path));
    }
}