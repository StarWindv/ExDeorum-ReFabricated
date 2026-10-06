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

package top.starwindv.exdeorum.compat.jei;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.fabricmc.loader.api.FabricLoader;
import top.starwindv.exdeorum.registry.DeferredHolder;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.client.screen.MechanicalHammerScreen;
import top.starwindv.exdeorum.client.screen.MechanicalSieveScreen;
import top.starwindv.exdeorum.compat.CompatUtil;
import top.starwindv.exdeorum.compat.ModIds;
import top.starwindv.exdeorum.compat.XeiSieveRecipe;
import top.starwindv.exdeorum.util.TranslationKeys;
import top.starwindv.exdeorum.item.WateringCanItem;
import top.starwindv.exdeorum.recipe.RecipeUtil;
import top.starwindv.exdeorum.recipe.barrel.BarrelCompostRecipe;
import top.starwindv.exdeorum.recipe.barrel.BarrelFluidMixingRecipe;
import top.starwindv.exdeorum.recipe.barrel.BarrelMixingRecipe;
import top.starwindv.exdeorum.recipe.crucible.CrucibleRecipe;
import top.starwindv.exdeorum.recipe.hammer.CompressedHammerRecipe;
import top.starwindv.exdeorum.recipe.hammer.HammerRecipe;
import top.starwindv.exdeorum.registry.EFluids;
import top.starwindv.exdeorum.registry.EItems;
import top.starwindv.exdeorum.registry.ERecipeTypes;
import top.starwindv.exdeorum.tag.EItemTags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@JeiPlugin
public class ExDeorumJeiPlugin implements IModPlugin {
    public static final Identifier EX_DEORUM_JEI_TEXTURE = ExDeorum.loc("textures/gui/jei/enr_jei.png");

    static final IRecipeType<BarrelCompostRecipe> BARREL_COMPOST = recipeType("barrel_compost", BarrelCompostRecipe.class);
    static final IRecipeType<BarrelMixingRecipe> BARREL_MIXING = recipeType("barrel_mixing", BarrelMixingRecipe.class);
    static final IRecipeType<BarrelFluidMixingRecipe> BARREL_FLUID_MIXING = recipeType("barrel_fluid_mixing", BarrelFluidMixingRecipe.class);
    static final IRecipeType<CrucibleRecipe> LAVA_CRUCIBLE = recipeType("lava_crucible", CrucibleRecipe.class);
    static final IRecipeType<CrucibleRecipe> WATER_CRUCIBLE = recipeType("water_crucible", CrucibleRecipe.class);
    static final IRecipeType<CrucibleHeatSourceRecipe> CRUCIBLE_HEAT_SOURCES = recipeType("crucible_heat_sources", CrucibleHeatSourceRecipe.class);
    static final IRecipeType<XeiSieveRecipe> SIEVE = recipeType("sieve", XeiSieveRecipe.class);
    static final IRecipeType<XeiSieveRecipe> COMPRESSED_SIEVE = recipeType("compressed_sieve", XeiSieveRecipe.class);
    static final IRecipeType<HammerRecipe> HAMMER = recipeType("hammer", HammerRecipe.class);
    static final IRecipeType<HammerRecipe> COMPRESSED_HAMMER = recipeType("compressed_hammer", CompressedHammerRecipe.class);
    static final IRecipeType<CrookJeiRecipe> CROOK = recipeType("crook", CrookJeiRecipe.class);

    private static <T> IRecipeType<T> recipeType(String path, Class<? extends T> type) {
        // use alternative namespace so that EMI doesn't skip JEI compatibility
        String namespace = FabricLoader.getInstance().isModLoaded(ModIds.EMI) ? ExDeorum.ID + "_" + ModIds.EMI : ExDeorum.ID;
        return IRecipeType.create(namespace, path, type);
    }

    @Override
    public Identifier getPluginUid() {
        return ExDeorum.loc("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var helper = registration.getJeiHelpers().getGuiHelper();
        var arrow = helper.createDrawable(ExDeorumJeiPlugin.EX_DEORUM_JEI_TEXTURE, 0, 18, 22, 15);
        var plus = helper.createDrawable(ExDeorumJeiPlugin.EX_DEORUM_JEI_TEXTURE, 22, 18, 8, 8);

        registration.addRecipeCategories(new BarrelCompostCategory(helper));
        registration.addRecipeCategories(new BarrelMixingCategory.Items(helper, plus, arrow));
        registration.addRecipeCategories(new BarrelMixingCategory.Fluids(helper, plus, arrow));
        registration.addRecipeCategories(new CrucibleCategory.LavaCrucible(helper, arrow));
        registration.addRecipeCategories(new CrucibleCategory.WaterCrucible(helper, arrow));
        registration.addRecipeCategories(new CrucibleHeatSourcesCategory(registration.getJeiHelpers()));
        registration.addRecipeCategories(new SieveCategory(helper));
        registration.addRecipeCategories(new CompressedSieveCategory(helper));
        registration.addRecipeCategories(new HammerCategory(helper, arrow, EItems.DIAMOND_HAMMER, Component.translatable(TranslationKeys.HAMMER_CATEGORY_TITLE), HAMMER));
        registration.addRecipeCategories(new HammerCategory(helper, arrow, EItems.COMPRESSED_DIAMOND_HAMMER, Component.translatable(TranslationKeys.COMPRESSED_HAMMER_CATEGORY_TITLE), COMPRESSED_HAMMER));
        registration.addRecipeCategories(new CrookCategory(registration.getJeiHelpers(), arrow));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        var barrels = CompatUtil.getAvailableBarrels(true);
        var sieves = CompatUtil.getAvailableSieves(true, true);
        var lavaCrucibles = CompatUtil.getAvailableLavaCrucibles(true);
        var waterCrucibles = CompatUtil.getAvailableWaterCrucibles(true);
        var compressedSieves = CompatUtil.getAvailableCompressedSieves(true);

        for (var barrel : barrels) {
            var stack = new ItemStack(barrel);
            registration.addCraftingStation(BARREL_COMPOST, stack);
            registration.addCraftingStation(BARREL_MIXING, stack);
            registration.addCraftingStation(BARREL_FLUID_MIXING, stack);
        }
        for (var lavaCrucible : lavaCrucibles) {
            var stack = new ItemStack(lavaCrucible);
            registration.addCraftingStation(LAVA_CRUCIBLE, stack);
            registration.addCraftingStation(CRUCIBLE_HEAT_SOURCES, stack);
        }
        for (var waterCrucible : waterCrucibles) {
            registration.addCraftingStation(WATER_CRUCIBLE, new ItemStack(waterCrucible));
        }
        for (var sieve : sieves) {
            registration.addCraftingStation(SIEVE, new ItemStack(sieve));
        }
        for (var compressedSieve : compressedSieves) {
            registration.addCraftingStation(COMPRESSED_SIEVE, new ItemStack(compressedSieve));
        }

        registration.addCraftingStation(HAMMER, new ItemStack(EItems.WOODEN_HAMMER.get()));
        registration.addCraftingStation(HAMMER, new ItemStack(EItems.STONE_HAMMER.get()));
        registration.addCraftingStation(HAMMER, new ItemStack(EItems.GOLDEN_HAMMER.get()));
        registration.addCraftingStation(HAMMER, new ItemStack(EItems.IRON_HAMMER.get()));
        registration.addCraftingStation(HAMMER, new ItemStack(EItems.DIAMOND_HAMMER.get()));
        registration.addCraftingStation(HAMMER, new ItemStack(EItems.NETHERITE_HAMMER.get()));
        registration.addCraftingStation(HAMMER, new ItemStack(EItems.MECHANICAL_HAMMER.get()));

        registration.addCraftingStation(COMPRESSED_HAMMER, new ItemStack(EItems.COMPRESSED_WOODEN_HAMMER.get()));
        registration.addCraftingStation(COMPRESSED_HAMMER, new ItemStack(EItems.COMPRESSED_STONE_HAMMER.get()));
        registration.addCraftingStation(COMPRESSED_HAMMER, new ItemStack(EItems.COMPRESSED_GOLDEN_HAMMER.get()));
        registration.addCraftingStation(COMPRESSED_HAMMER, new ItemStack(EItems.COMPRESSED_IRON_HAMMER.get()));
        registration.addCraftingStation(COMPRESSED_HAMMER, new ItemStack(EItems.COMPRESSED_DIAMOND_HAMMER.get()));
        registration.addCraftingStation(COMPRESSED_HAMMER, new ItemStack(EItems.COMPRESSED_NETHERITE_HAMMER.get()));

        registration.addCraftingStation(CROOK, new ItemStack(EItems.CROOK.get()));
        registration.addCraftingStation(CROOK, new ItemStack(EItems.BONE_CROOK.get()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addItemStackInfo(List.of(new ItemStack(EItems.INFESTED_LEAVES.get()), new ItemStack(EItems.SILKWORM.get())), Component.translatable(TranslationKeys.SILK_WORM_JEI_INFO));
        registration.addItemStackInfo(CompatUtil.getAvailableSieves(true, false).stream().map(ItemStack::new).toList(), Component.translatable(TranslationKeys.SIEVE_JEI_INFO));
        registration.addItemStackInfo(List.of(new ItemStack(EItems.STRING_MESH.get()), new ItemStack(EItems.STRING_MESH.get()), new ItemStack(EItems.FLINT_MESH.get()), new ItemStack(EItems.IRON_MESH.get()), new ItemStack(EItems.GOLDEN_MESH.get()), new ItemStack(EItems.DIAMOND_MESH.get()), new ItemStack(EItems.NETHERITE_MESH.get())), Component.translatable(TranslationKeys.SIEVE_MESH_JEI_INFO));
        registration.addItemStackInfo(EItems.WATERING_CANS.stream().map(WateringCanItem::getFull).toList(), Component.translatable(TranslationKeys.WATERING_CAN_JEI_INFO));
        var witchWaterInfo = Component.translatable(TranslationKeys.WITCH_WATER_JEI_INFO);
        registration.addItemStackInfo(List.of(new ItemStack(EItems.WITCH_WATER_BUCKET.get()), new ItemStack(EItems.PORCELAIN_WITCH_WATER_BUCKET.get())), witchWaterInfo);
        addFluidIngredientInfo(registration, registration.getJeiHelpers().getPlatformFluidHelper(), EFluids.WITCH_WATER.get(), 1000, witchWaterInfo);
        registration.addItemStackInfo(new ItemStack(EItems.GRASS_SEEDS.get()), Component.translatable(TranslationKeys.GRASS_SEEDS_JEI_INFO));
        registration.addItemStackInfo(new ItemStack(EItems.MYCELIUM_SPORES.get()), Component.translatable(TranslationKeys.MYCELIUM_SPORES_JEI_INFO));
        registration.addItemStackInfo(new ItemStack(EItems.WARPED_NYLIUM_SPORES.get()), Component.translatable(TranslationKeys.WARPED_NYLIUM_SPORES_JEI_INFO));
        registration.addItemStackInfo(new ItemStack(EItems.CRIMSON_NYLIUM_SPORES.get()), Component.translatable(TranslationKeys.CRIMSON_NYLIUM_SPORES_JEI_INFO));
        registration.addItemStackInfo(new ItemStack(EItems.SCULK_CORE.get()), Component.translatable(TranslationKeys.SCULK_CORE_JEI_INFO));
        registration.addItemStackInfo(new ItemStack(EItems.CREAKING_CORE.get()), Component.translatable(TranslationKeys.CREAKING_CORE_JEI_INFO));
        registration.addItemStackInfo(new ItemStack(EItems.MECHANICAL_SIEVE.get()), Component.translatable(TranslationKeys.MECHANICAL_SIEVE_JEI_INFO));
        registration.addItemStackInfo(new ItemStack(EItems.MECHANICAL_HAMMER.get()), Component.translatable(TranslationKeys.MECHANICAL_HAMMER_JEI_INFO));

        var toRemove = new ArrayList<ItemStack>();

        if (RecipeUtil.isTagEmpty(EItemTags.ORES_ALUMINUM))
            toRemove.add(new ItemStack(EItems.ALUMINUM_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_COBALT)) toRemove.add(new ItemStack(EItems.COBALT_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_SILVER)) toRemove.add(new ItemStack(EItems.SILVER_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_LEAD)) toRemove.add(new ItemStack(EItems.LEAD_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_PLATINUM))
            toRemove.add(new ItemStack(EItems.PLATINUM_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_NICKEL)) toRemove.add(new ItemStack(EItems.NICKEL_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_URANIUM)) toRemove.add(new ItemStack(EItems.URANIUM_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_OSMIUM)) toRemove.add(new ItemStack(EItems.OSMIUM_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_TIN)) toRemove.add(new ItemStack(EItems.TIN_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_ZINC)) toRemove.add(new ItemStack(EItems.ZINC_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_IRIDIUM)) toRemove.add(new ItemStack(EItems.IRIDIUM_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_THORIUM)) toRemove.add(new ItemStack(EItems.THORIUM_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_MAGNESIUM))
            toRemove.add(new ItemStack(EItems.MAGNESIUM_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_LITHIUM)) toRemove.add(new ItemStack(EItems.LITHIUM_ORE_CHUNK.get()));
        if (RecipeUtil.isTagEmpty(EItemTags.ORES_BORON)) toRemove.add(new ItemStack(EItems.BORON_ORE_CHUNK.get()));

        if (!toRemove.isEmpty()) {
            registration.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, toRemove);
        }

        addRecipes(registration, BARREL_COMPOST, ERecipeTypes.BARREL_COMPOST);
        addRecipes(registration, BARREL_MIXING, ERecipeTypes.BARREL_MIXING);
        addRecipes(registration, BARREL_FLUID_MIXING, ERecipeTypes.BARREL_FLUID_MIXING);
        addRecipes(registration, LAVA_CRUCIBLE, ERecipeTypes.LAVA_CRUCIBLE);
        addRecipes(registration, WATER_CRUCIBLE, ERecipeTypes.WATER_CRUCIBLE);
        addRecipes(registration, HAMMER, ERecipeTypes.HAMMER);
        //noinspection rawtypes,unchecked
        addRecipes(registration, COMPRESSED_HAMMER, ((DeferredHolder) ERecipeTypes.COMPRESSED_HAMMER));
        registration.addRecipes(CROOK, CompatUtil.collectAllRecipes(ERecipeTypes.CROOK.get(), CrookJeiRecipe::create));
        // The merged lists, not the raw recipe map: the raw map hides every API-registered
        // compat drop, and applying the drop rate overrides keeps JEI matching the caches.
        registration.addRecipes(SIEVE, XeiSieveRecipe.getAllRecipesGrouped(RecipeUtil.getAllSieveRecipes(), XeiSieveRecipe.SIEVE_ROWS));
        registration.addRecipes(COMPRESSED_SIEVE, XeiSieveRecipe.getAllRecipesGrouped(RecipeUtil.getAllCompressedSieveRecipes(), XeiSieveRecipe.COMPRESSED_SIEVE_ROWS));

        addCrucibleHeatSources(registration);
    }

    private static void addCrucibleHeatSources(IRecipeRegistration registration) {
        var values = new Object2IntOpenHashMap<Block>();
        for (var entry : RecipeUtil.getHeatSources()) {
            var state = entry.getKey();
            var block = state.getBlock();

            if (block instanceof WallTorchBlock) continue;

            if (block != Blocks.AIR) {
                final int newValue = entry.getIntValue();

                values.computeInt(block, (key, value) -> {
                    if (value != null) {
                        return Math.max(value, newValue);
                    } else {
                        return newValue == 0 ? null : newValue;
                    }
                });
            }
        }
        var fluidHelper = registration.getJeiHelpers().getPlatformFluidHelper();
        var fluidIngredientType = fluidHelper.getFluidIngredientType();
        var recipes = new ArrayList<CrucibleHeatSourceRecipe>();

        for (var entry : values.object2IntEntrySet()) {
            if (entry.getKey() instanceof LiquidBlock liquid) {
                // LiquidBlock.fluid is protected on Fabric, but the default state's fluid is the same still fluid
                var state = entry.getKey().defaultBlockState();
                recipes.add(new CrucibleHeatSourceRecipe(entry.getIntValue(), state, fluidIngredientType, fluidHelper.create(Holder.direct(state.getFluidState().getType()), JeiUtil.toDroplets(1000))));
            } else {
                var itemForm = entry.getKey().asItem();

                if (itemForm != Items.AIR) {
                    recipes.add(new CrucibleHeatSourceRecipe(entry.getIntValue(), entry.getKey().defaultBlockState(), VanillaTypes.ITEM_STACK, new ItemStack(itemForm)));
                } else {
                    recipes.add(new CrucibleHeatSourceRecipe(entry.getIntValue(), entry.getKey().defaultBlockState(), null, null));
                }
            }
        }
        registration.addRecipes(CRUCIBLE_HEAT_SOURCES, recipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // see addRecipeClickArea for reference
        registration.addGuiContainerHandler(MechanicalSieveScreen.class, new IGuiContainerHandler<>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(MechanicalSieveScreen containerScreen, double mouseX, double mouseY) {
                IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(MechanicalSieveScreen.RECIPE_CLICK_AREA_POS_X, MechanicalSieveScreen.RECIPE_CLICK_AREA_POS_Y, MechanicalSieveScreen.RECIPE_CLICK_AREA_WIDTH, MechanicalSieveScreen.RECIPE_CLICK_AREA_HEIGHT, SIEVE);
                return FabricLoader.getInstance().isModLoaded(ModIds.EMI) ? List.of() : List.of(clickableArea);
            }

            @Override
            public List<Rect2i> getGuiExtraAreas(MechanicalSieveScreen containerScreen) {
                var widget = containerScreen.getRedstoneControlWidget();
                if (widget != null) {
                    return widget.getJeiBounds();
                }
                return List.of();
            }
        });
        registration.addGuiContainerHandler(MechanicalHammerScreen.class, new IGuiContainerHandler<>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(MechanicalHammerScreen containerScreen, double mouseX, double mouseY) {
                IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(MechanicalHammerScreen.RECIPE_CLICK_AREA_POS_X, MechanicalHammerScreen.RECIPE_CLICK_AREA_POS_Y, MechanicalHammerScreen.RECIPE_CLICK_AREA_WIDTH, MechanicalHammerScreen.RECIPE_CLICK_AREA_HEIGHT, HAMMER);
                return FabricLoader.getInstance().isModLoaded(ModIds.EMI) ? List.of() : List.of(clickableArea);
            }

            @Override
            public List<Rect2i> getGuiExtraAreas(MechanicalHammerScreen containerScreen) {
                var widget = containerScreen.getRedstoneControlWidget();
                if (widget != null) {
                    return widget.getJeiBounds();
                }
                return List.of();
            }
        });
    }

    private static <C extends RecipeInput, T extends Recipe<C>> void addRecipes(IRecipeRegistration registration, IRecipeType<T> category, Supplier<net.minecraft.world.item.crafting.RecipeType<T>> type) {
        registration.addRecipes(category, CompatUtil.collectAllRecipes(type.get(), Function.identity()));
    }

    // generic helper keeps the fluid ingredient and its type from the same helper instance
    private static <T> void addFluidIngredientInfo(IRecipeRegistration registration, IPlatformFluidHelper<T> fluidHelper, net.minecraft.world.level.material.Fluid fluid, int mB, Component info) {
        registration.addIngredientInfo(fluidHelper.create(Holder.direct(fluid), JeiUtil.toDroplets(mB)), fluidHelper.getFluidIngredientType(), info);
    }
}
