/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.compat.thirst;

import java.lang.reflect.Field;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import top.starwindv.exdeorum.registry.EItems;

/**
 * Soft hook into Thirst Was Taken (TWT-U), so water moved through Ex Deorum's barrels and
 * crucibles carries the same water purity the thirst mod keys its effects on.
 *
 * <p>Like the other compat hooks here, nothing needs the mod at compile time. The purity
 * itself lives in an item's {@link DataComponents#CUSTOM_DATA} as an int named {@code Purity}
 * — a format stable enough to read and write directly, with a single reflective read of
 * TWT-U's configurable {@code DEFAULT_PURITY} so untagged water is judged the way the thirst
 * mod would judge it.
 *
 * <p>Purity levels: 0 dirty, 1 slightly dirty, 2 acceptable, 3 purified.
 */
public final class ThirstCompat {
    public static final String MOD_ID = "twt-u";
    public static final int DIRTY = 0;
    public static final int SLIGHTLY_DIRTY = 1;
    public static final int ACCEPTABLE = 2;
    public static final int PURIFIED = 3;

    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded(MOD_ID);
    @Nullable
    private static volatile Field defaultPurity;

    private ThirstCompat() {
    }

    public static boolean loaded() {
        return LOADED;
    }

    /** The purity untagged water counts as, honoring TWT-U's own config when present. */
    public static int defaultPurity() {
        if (!LOADED) {
            return ACCEPTABLE;
        }

        try {
            var field = defaultPurityField();
            return field == null ? ACCEPTABLE : field.getInt(null);
        } catch (ReflectiveOperationException e) {
            return ACCEPTABLE;
        }
    }

    /**
     * Reads an item's water purity, honoring TWT-U's configured default for untagged water.
     * Meaningless on items that hold no water; callers decide what counts as a water container.
     */
    public static int getPurity(ItemStack stack) {
        if (!LOADED) {
            return ACCEPTABLE;
        }

        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return defaultPurity();
        }

        return customData.copyTag().getIntOr("Purity", defaultPurity());
    }

    public static boolean hasPurity(ItemStack stack) {
        if (!LOADED) {
            return false;
        }

        var customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.copyTag().contains("Purity");
    }

    /**
     * Writes an item's water purity, mirroring TWT-U's own writer: the tag is stripped again
     * when the purity equals the configured default, so untagged and default stay the same.
     */
    public static void setPurity(ItemStack stack, int purity) {
        if (!LOADED) {
            return;
        }

        if (purity == defaultPurity()) {
            var existing = stack.get(DataComponents.CUSTOM_DATA);
            if (existing != null) {
                var tag = existing.copyTag();
                tag.remove("Purity");
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        } else {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("Purity", purity));
        }
    }

    /**
     * Whether the item is a container of standing water whose purity should follow barrel
     * transfers: the vanilla water bucket, our porcelain water bucket, a water bottle or
     * TWT-U's terracotta water bowl.
     */
    public static boolean isWaterContainer(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET) || stack.is(EItems.PORCELAIN_WATER_BUCKET.get())) {
            return true;
        }

        if (stack.is(Items.POTION)) {
            return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
        }

        return isTerracottaWaterBowl(stack);
    }

    public static boolean isTerracottaWaterBowl(ItemStack stack) {
        var bowl = terracottaWaterBowl();
        return bowl != null && stack.is(bowl);
    }

    /** Whether the item is TWT-U's empty terracotta bowl, ready to be filled from a tank. */
    public static boolean isTerracottaBowl(ItemStack stack) {
        var bowl = terracottaBowl();
        return bowl != null && stack.is(bowl);
    }

    /** The empty bowl a filled terracotta water bowl hands back, or null without TWT-U. */
    @Nullable
    public static Item terracottaBowl() {
        return registryItem("terracotta_bowl");
    }

    /** The filled terracotta water bowl, or null without TWT-U. */
    @Nullable
    public static Item terracottaWaterBowl() {
        return registryItem("terracotta_water_bowl");
    }

    @Nullable
    private static Item registryItem(String path) {
        if (!LOADED) {
            return null;
        }

        return BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath(MOD_ID, path))
                .map(reference -> reference.value())
                .orElse(null);
    }

    @Nullable
    private static Field defaultPurityField() {
        var field = defaultPurity;
        if (field != null) {
            return field;
        }

        try {
            field = Class.forName("twtu.foundation.config.CommonConfig").getField("DEFAULT_PURITY");
        } catch (ReflectiveOperationException e) {
            field = null;
        }

        defaultPurity = field;
        return field;
    }
}
