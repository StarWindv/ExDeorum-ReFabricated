/*
 * Ex Deorum
 * Copyright (c) 2024 thedarkcolour
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Modifications Copyleft (c) 2026 StarWindv
 * Ported to Fabric
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.recipe;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.*;
import org.slf4j.Logger;
import top.starwindv.exdeorum.fluid.FluidIngredient;
import top.starwindv.exdeorum.fluid.FluidStack;
import org.jetbrains.annotations.Nullable;
import top.starwindv.exdeorum.loot.SummationGenerator;
import top.starwindv.exdeorum.recipe.barrel.BarrelCompostRecipe;
import top.starwindv.exdeorum.recipe.barrel.BarrelFluidMixingRecipe;
import top.starwindv.exdeorum.recipe.barrel.BarrelMixingRecipe;
import top.starwindv.exdeorum.recipe.barrel.FluidTransformationRecipe;
import top.starwindv.exdeorum.recipe.cache.*;
import top.starwindv.exdeorum.recipe.crook.CrookRecipe;
import top.starwindv.exdeorum.recipe.crucible.CrucibleRecipe;
import top.starwindv.exdeorum.recipe.hammer.CompressedHammerRecipe;
import top.starwindv.exdeorum.recipe.hammer.HammerRecipe;
import top.starwindv.exdeorum.recipe.sieve.CompressedSieveRecipe;
import top.starwindv.exdeorum.recipe.sieve.SieveRecipe;
import top.starwindv.exdeorum.api.ExDeorumApi;
import top.starwindv.exdeorum.registry.ERecipeTypes;

import java.util.*;

public final class RecipeUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CONSTANT_TYPE = 1;
    private static final int UNIFORM_TYPE = 2;
    private static final int BINOMIAL_TYPE = 3;
    private static final int SUMMATION_TYPE = 4;
    private static final int UNKNOWN_TYPE = 99;

    private static SingleIngredientRecipeCache<BarrelCompostRecipe> barrelCompostRecipeCache;
    private static SingleIngredientRecipeCache<CrucibleRecipe> lavaCrucibleRecipeCache;
    private static SingleIngredientRecipeCache<CrucibleRecipe> waterCrucibleRecipeCache;
    private static SingleIngredientRecipeCache<HammerRecipe> hammerRecipeCache;
    private static SingleIngredientRecipeCache<CompressedHammerRecipe> compressedHammerRecipeCache;
    private static SieveRecipeCache<SieveRecipe> sieveRecipeCache;
    private static SieveRecipeCache<CompressedSieveRecipe> compressedSieveRecipeCache;
    private static BarrelFluidMixingRecipeCache barrelFluidMixingRecipeCache;
    private static FluidTransformationRecipeCache fluidTransformationRecipeCache;
    private static CrookRecipeCache crookRecipeCache;
    private static CrucibleHeatRecipeCache crucibleHeatRecipeCache;
    private static List<BarrelMixingRecipe> barrelMixingRecipes;
    private static RecipeMap currentRecipeMap;
    // The merged sieve recipes exactly as the caches see them — API ones included, drop rate
    // overrides applied. The JEI plugin reads these so its display matches actual behaviour;
    // the raw recipe map alone would hide every API-registered compat drop.
    private static List<RecipeHolder<SieveRecipe>> allSieveRecipes;
    private static List<RecipeHolder<CompressedSieveRecipe>> allCompressedSieveRecipes;

    public static void reload(RecipeMap recipes) {
        currentRecipeMap = recipes;

        // Player drop rate overrides are applied here so every cache below sees the tuned
        // recipes, and the config screen can enumerate what ended up tunable.
        ProbabilityTuner.beginReload();

        // Recipes other mods registered through Ex Deorum's API, merged on top of the
        // data packs so pack authors can still override them. The compressed drops go to
        // the compressed sieve only: merging them here as well made the config screen list
        // every compressed entry twice.
        var apiSieve = ExDeorumApi.withSieveDrops(recipes.byType(ERecipeTypes.SIEVE.get()),
                ExDeorumApi.sieveDrops(), null);
        var apiCompressedSieve = ExDeorumApi.withSieveDrops(recipes.byType(ERecipeTypes.COMPRESSED_SIEVE.get()),
                null, ExDeorumApi.compressedSieveDrops());
        var barrelMixing = recipes.byType(ERecipeTypes.BARREL_MIXING.get());
        barrelCompostRecipeCache = new SingleIngredientRecipeCache<>(ProbabilityTuner.tune(recipes.byType(ERecipeTypes.BARREL_COMPOST.get())), ERecipeTypes.BARREL_COMPOST);
        lavaCrucibleRecipeCache = new SingleIngredientRecipeCache<>(ProbabilityTuner.tune(recipes.byType(ERecipeTypes.LAVA_CRUCIBLE.get())), ERecipeTypes.LAVA_CRUCIBLE);
        waterCrucibleRecipeCache = new SingleIngredientRecipeCache<>(ProbabilityTuner.tune(recipes.byType(ERecipeTypes.WATER_CRUCIBLE.get())), ERecipeTypes.WATER_CRUCIBLE);
        hammerRecipeCache = new SingleIngredientRecipeCache<>(ProbabilityTuner.tune(recipes.byType(ERecipeTypes.HAMMER.get())), ERecipeTypes.HAMMER).trackAllRecipes();
        compressedHammerRecipeCache = new SingleIngredientRecipeCache<>(ProbabilityTuner.tune(recipes.byType(ERecipeTypes.COMPRESSED_HAMMER.get())), ERecipeTypes.COMPRESSED_HAMMER).trackAllRecipes();
        var tunedSieve = ProbabilityTuner.tune(apiSieve);
        var tunedCompressedSieve = ProbabilityTuner.tune(apiCompressedSieve);
        sieveRecipeCache = new SieveRecipeCache<>(tunedSieve, ERecipeTypes.SIEVE);
        compressedSieveRecipeCache = new SieveRecipeCache<>(tunedCompressedSieve, ERecipeTypes.COMPRESSED_SIEVE);
        barrelFluidMixingRecipeCache = new BarrelFluidMixingRecipeCache(recipes);
        fluidTransformationRecipeCache = new FluidTransformationRecipeCache(recipes);
        crookRecipeCache = new CrookRecipeCache(ProbabilityTuner.tune(recipes.byType(ERecipeTypes.CROOK.get())));
        crucibleHeatRecipeCache = new CrucibleHeatRecipeCache(recipes);
        barrelMixingRecipes = barrelMixing.stream().map(RecipeHolder::value).toList();
        allSieveRecipes = List.copyOf(tunedSieve);
        allCompressedSieveRecipes = List.copyOf(tunedCompressedSieve);
        ProbabilityTuner.endReload();
        ProbabilityTuner.logApplied();
        logDirtSieveDrops(apiSieve);
    }

    // Diagnostic for the seed-compat pipeline: after every reload, print everything the
    // regular sieves can drop from a plain dirt block, grouped by the namespace that owns
    // each result (Vanilla vs. the providing mod). A missing mod group means that mod's
    // seed recipes never got registered, which is exactly what the seed compat should fix.
    private static void logDirtSieveDrops(Collection<RecipeHolder<SieveRecipe>> recipes) {
        var dirt = Blocks.DIRT.asItem();
        var byNamespace = new LinkedHashMap<String, LinkedHashMap<Identifier, List<SieveRecipe>>>();
        var matching = 0;

        for (var holder : recipes) {
            var recipe = holder.value();

            if (recipe.ingredient.items().noneMatch(h -> h.value() == dirt)) {
                continue;
            }

            matching++;

            // Resolve the item through the template's holder rather than create(), which would
            // build an ItemStack and read the not-yet-bound item components at this point.
            var resultId = BuiltInRegistries.ITEM.getKey(recipe.result().item().value());
            byNamespace.computeIfAbsent(resultId.getNamespace(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(resultId, k -> new ArrayList<>())
                    .add(recipe);
        }

        if (byNamespace.isEmpty()) {
            LOGGER.warn("[Dirt sieve dump] no sieve recipe accepts minecraft:dirt, sieve drops are broken");
            return;
        }

        LOGGER.info("[Dirt sieve dump] {} sieve recipe(s) accept minecraft:dirt, from {} source(s):",
                matching, byNamespace.size());

        for (var namespace : byNamespace.entrySet()) {
            LOGGER.info("[Dirt sieve dump]   {} ({}): {} result(s)",
                    sourceLabel(namespace.getKey()), namespace.getKey(), namespace.getValue().size());

            for (var result : namespace.getValue().entrySet()) {
                var chances = result.getValue().stream().map(SieveRecipe::tunableProbability).toList();
                var meshes = result.getValue().stream()
                        .flatMap(recipe -> recipe.mesh().items())
                        .map(h -> BuiltInRegistries.ITEM.getKey(h.value()).getPath())
                        .distinct()
                        .toList();
                LOGGER.info("[Dirt sieve dump]     {} p={} meshes={}", result.getKey(), chanceRange(chances), meshes);
            }
        }
    }

    private static String chanceRange(List<Float> chances) {
        var min = chances.stream().min(Float::compare).orElse(0.0f);
        var max = chances.stream().max(Float::compare).orElse(0.0f);
        return min == max ? String.valueOf(min) : min + ".." + max;
    }

    private static String sourceLabel(String namespace) {
        if (namespace.equals("minecraft")) {
            return "Vanilla";
        }

        return FabricLoader.getInstance().getModContainer(namespace)
                .map(container -> container.getMetadata().getName())
                .orElse(namespace);
    }

    public static void unload() {
        barrelCompostRecipeCache = null;
        lavaCrucibleRecipeCache = null;
        waterCrucibleRecipeCache = null;
        hammerRecipeCache = null;
        compressedHammerRecipeCache = null;
        sieveRecipeCache = null;
        compressedSieveRecipeCache = null;
        barrelFluidMixingRecipeCache = null;
        fluidTransformationRecipeCache = null;
        crookRecipeCache = null;
        crucibleHeatRecipeCache = null;
        barrelMixingRecipes = null;
        currentRecipeMap = null;
        allSieveRecipes = null;
        allCompressedSieveRecipes = null;
    }

    /** Every loaded sieve recipe — API ones included, with drop rate overrides applied. */
    public static List<RecipeHolder<SieveRecipe>> getAllSieveRecipes() {
        return allSieveRecipes == null ? List.of() : allSieveRecipes;
    }

    /** Every loaded compressed sieve recipe, same shape as {@link #getAllSieveRecipes()}. */
    public static List<RecipeHolder<CompressedSieveRecipe>> getAllCompressedSieveRecipes() {
        return allCompressedSieveRecipes == null ? List.of() : allCompressedSieveRecipes;
    }

    public static List<SieveRecipe> getSieveRecipes(Item mesh, ItemStack item) {
        return sieveRecipeCache.getRecipe(mesh, item);
    }

    public static List<CompressedSieveRecipe> getCompressedSieveRecipes(Item mesh, ItemStack item) {
        return compressedSieveRecipeCache.getRecipe(mesh, item);
    }

    @Nullable
    public static CrucibleRecipe getLavaCrucibleRecipe(ItemStack item) {
        return lavaCrucibleRecipeCache.getRecipe(item);
    }

    @Nullable
    public static CrucibleRecipe getWaterCrucibleRecipe(ItemStack item) {
        return waterCrucibleRecipeCache.getRecipe(item);
    }

    @Nullable
    public static BarrelCompostRecipe getBarrelCompostRecipe(ItemStack item) {
        return barrelCompostRecipeCache.getRecipe(item);
    }

    @Nullable
    public static HammerRecipe getHammerRecipe(Item item) {
        return hammerRecipeCache.getRecipe(item);
    }

    public static Collection<RecipeHolder<HammerRecipe>> getCachedHammerRecipes() {
        return hammerRecipeCache.getAllRecipes();
    }

    @Nullable
    public static CompressedHammerRecipe getCompressedHammerRecipe(Item item) {
        return compressedHammerRecipeCache.getRecipe(item);
    }

    public static Collection<RecipeHolder<CompressedHammerRecipe>> getCachedCompressedHammerRecipes() {
        return compressedHammerRecipeCache.getAllRecipes();
    }

    public static void toNetworkNumberProvider(FriendlyByteBuf buffer, NumberProvider provider) {
        if (provider instanceof ConstantValue constant) {
            buffer.writeByte(CONSTANT_TYPE);
            buffer.writeFloat(constant.value());
        } else if (provider instanceof UniformGenerator uniform) {
            buffer.writeByte(UNIFORM_TYPE);
            toNetworkNumberProvider(buffer, uniform.min());
            toNetworkNumberProvider(buffer, uniform.max());
        } else if (provider instanceof BinomialDistributionGenerator binomial) {
            buffer.writeByte(BINOMIAL_TYPE);
            toNetworkNumberProvider(buffer, binomial.n());
            toNetworkNumberProvider(buffer, binomial.p());
        } else if (provider instanceof SummationGenerator summation) {
            var providers = summation.providers();
            int length = providers.size();
            buffer.writeByte(SUMMATION_TYPE);
            buffer.writeByte(length);
            for (int i = 0; i < length; i++) {
                toNetworkNumberProvider(buffer, providers.get(i));
            }
        } else {
            buffer.writeByte(UNKNOWN_TYPE);
        }
    }

    public static NumberProvider fromNetworkNumberProvider(FriendlyByteBuf buffer) {
        return switch (buffer.readByte()) {
            case CONSTANT_TYPE -> ConstantValue.exactly(buffer.readFloat());
            case UNIFORM_TYPE ->
                    new UniformGenerator(fromNetworkNumberProvider(buffer), fromNetworkNumberProvider(buffer));
            case BINOMIAL_TYPE ->
                    new BinomialDistributionGenerator(fromNetworkNumberProvider(buffer), fromNetworkNumberProvider(buffer));
            case SUMMATION_TYPE -> {
                var length = buffer.readByte();
                var providers = new NumberProvider[length];
                for (int i = 0; i < length; i++) {
                    providers[i] = fromNetworkNumberProvider(buffer);
                }
                yield new SummationGenerator(List.of(providers));
            }
            default -> ConstantValue.exactly(1f);
        };
    }

    public static boolean areIngredientsEqual(Ingredient first, Ingredient second) {
        if (first == second) return true;
        return first.equals(second);
    }

    public static boolean isCompostable(ItemStack stack) {
        return barrelCompostRecipeCache != null && barrelCompostRecipeCache.getRecipe(stack) != null;
    }

    @Nullable
    public static BarrelMixingRecipe getBarrelMixingRecipe(ItemStack stack, FluidStack fluid) {
        if (barrelMixingRecipes == null) return null;
        for (var recipe : barrelMixingRecipes) {
            if (recipe.matches(stack, fluid)) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    public static BarrelFluidMixingRecipe getFluidMixingRecipe(FluidStack base, Fluid additive) {
        var recipe = barrelFluidMixingRecipeCache.getRecipe(base.getFluid(), additive);
        if (recipe != null && base.getAmount() >= recipe.baseFluid().amount()) {
            return recipe;
        } else {
            return null;
        }
    }

    @Nullable
    public static FluidTransformationRecipe getFluidTransformationRecipe(Fluid baseFluid, BlockState catalystState) {
        if (baseFluid != Fluids.EMPTY) {
            return fluidTransformationRecipeCache.getRecipe(baseFluid, catalystState);
        } else {
            return null;
        }
    }

    // The ported FluidIngredient doesn't expose its fluid or tag, so the matching fluids are
    // found by testing every registered fluid. This is equivalent to NeoForge's
    // FluidIngredient.fluids() for the single-fluid and tag ingredients Ex Deorum uses.
    public static List<Fluid> getMatchingFluids(FluidIngredient ingredient) {
        var fluids = new ArrayList<Fluid>();

        for (var fluid : BuiltInRegistries.FLUID) {
            if (ingredient.test(fluid)) {
                fluids.add(fluid);
            }
        }

        return fluids;
    }

    @SuppressWarnings("IfCanBeSwitch")
    public static double getExpectedValue(NumberProvider provider) {
        if (provider instanceof ConstantValue constant) {
            return constant.value();
        } else if (provider instanceof UniformGenerator uniform) {
            return getExpectedValue(uniform.min()) + getExpectedValue(uniform.max()) / 2.0;
        } else if (provider instanceof BinomialDistributionGenerator binomial) {
            return getExpectedValue(binomial.n()) * getExpectedValue(binomial.p());
        } else if (provider instanceof SummationGenerator summation) {
            double avgSum = 0.0;

            for (var child : summation.providers()) {
                double expectedValue = getExpectedValue(child);

                if (expectedValue == -1.0f) {
                    return -1.0f;
                } else {
                    avgSum += expectedValue;
                }
            }

            return avgSum;
        } else {
            // no way of knowing beforehand so just put them last
            return -1.0;
        }
    }

    public static boolean isTagEmpty(TagKey<Item> tag) {
        return !BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator().hasNext();
    }

    public static LootContext emptyLootContext(ServerLevel level) {
        return new LootContext.Builder(new LootParams.Builder(level).create(LootContextParamSets.EMPTY)).create(Optional.empty());
    }

    public static List<CrookRecipe> getCrookRecipes(BlockState state) {
        return crookRecipeCache.getRecipes(state);
    }

    public static int getHeatValue(BlockState state) {
        return crucibleHeatRecipeCache.getValue(state);
    }

    public static ObjectSet<Object2IntMap.Entry<BlockState>> getHeatSources() {
        return crucibleHeatRecipeCache.getEntries();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String writeBlockState(BlockState state) {
        var registryKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        Collection<Property> properties = (Collection<Property>) ((Collection)state.getProperties());

        if (properties.isEmpty()) {
            return registryKey.toString();
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append(registryKey);
            builder.append('[');
            for (Iterator<Property> iterator = properties.iterator(); iterator.hasNext(); ) {
                var property = iterator.next();
                builder.append(property.getName());
                builder.append('=');
                builder.append(property.getName(state.getValue(property)));
                if (iterator.hasNext()) {
                    builder.append(',');
                }
            }
            builder.append(']');
            return builder.toString();
        }
    }

    public static BlockState parseBlockState(String stateString) {
        try {
            return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, stateString, false).blockState();
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException("Failed to parse BlockState string \"" + stateString + "\"");
        }
    }

    public static void writeTag(FriendlyByteBuf buffer, TagKey<?> ore) {
        buffer.writeIdentifier(ore.location());
    }

    public static <T> TagKey<T> readTag(FriendlyByteBuf buffer, ResourceKey<Registry<T>> registry) {
        return TagKey.create(registry, buffer.readIdentifier());
    }

    public static boolean isValidResourceLocation(String string) {
        return Identifier.tryParse(string) != null;
    }

    /**
     * @return The global recipe map. {@code null} if recipes have not been loaded yet.
     */
    @Nullable
    public static RecipeMap getRecipeMap() {
        return currentRecipeMap;
    }
}
