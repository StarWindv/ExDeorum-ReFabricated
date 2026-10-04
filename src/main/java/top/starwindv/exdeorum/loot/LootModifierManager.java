/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.loot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import org.slf4j.Logger;
import top.starwindv.exdeorum.ExDeorum;

// Port of NeoForge's global loot modifier system. Reads data/exdeorum/loot_modifiers/*.json
// and applies the loaded modifiers to every generated loot list from LootTableMixin.
//
// The JSON is decoded with a RegistryOps context so that conditions such as
// minecraft:match_tool can resolve item tag references, the same way vanilla's own
// loot table loader does.
public final class LootModifierManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier FOLDER = ExDeorum.loc("loot_modifiers");
    private static final Map<String, MapCodec<? extends LootModifier>> SERIALIZERS = new LinkedHashMap<>();
    private static volatile List<LootModifier> MODIFIERS = List.of();

    public static <M extends LootModifier> MapCodec<M> register(String name, MapCodec<M> codec) {
        SERIALIZERS.put(name, codec);
        return codec;
    }

    public static void reload(ResourceManager manager, HolderLookup.Provider registries, ProfilerFiller profiler) {
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        var modifiers = new ArrayList<LootModifier>();

        profiler.push("exdeorum:loot_modifiers");

        for (var entry : manager.listResources(FOLDER.getPath(), path -> path.getNamespace().equals(ExDeorum.ID) && path.getPath().endsWith(".json")).entrySet()) {
            try (var reader = entry.getValue().openAsReader()) {
                var json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                var type = json.get("type").getAsString();
                var codec = SERIALIZERS.get(type.substring(type.indexOf(':') + 1));

                if (codec == null) {
                    LOGGER.warn("Unknown loot modifier type {} in {}", type, entry.getKey());
                    continue;
                }

                var parsed = codec.codec().parse(ops, json);
                var result = parsed.result();

                if (result.isPresent()) {
                    modifiers.add(result.get());
                } else {
                    LOGGER.error("Failed to parse loot modifier {}: {}", entry.getKey(), parsed.error().map(DataResult.Error::message).orElse("unknown error"));
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load loot modifier {}", entry.getKey(), e);
            }
        }

        profiler.pop();

        modifiers.sort(Comparator.comparingInt(LootModifier::priority));
        MODIFIERS = List.copyOf(modifiers);
        LOGGER.debug("Loaded {} global loot modifiers for Ex Deorum", MODIFIERS.size());
    }

    public static ObjectArrayList<ItemStack> modifyLoot(ObjectArrayList<ItemStack> loot, LootContext context) {
        var modifiers = MODIFIERS;

        if (modifiers.isEmpty()) {
            return loot;
        }

        for (var modifier : modifiers) {
            loot = modifier.apply(loot, context);
        }

        return loot;
    }
}