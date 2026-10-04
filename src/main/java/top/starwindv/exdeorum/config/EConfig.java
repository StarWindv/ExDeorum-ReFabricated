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

package top.starwindv.exdeorum.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import top.starwindv.exdeorum.compat.ModIds;
import top.starwindv.exdeorum.recipe.RecipeUtil;

import java.nio.file.Path;
import java.util.List;

public class EConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec SERVER_SPEC;
    public static final Client CLIENT;
    public static final Common COMMON;
    public static final Server SERVER;

    public static class Client {
        public final ModConfigSpec.BooleanValue useFastInfestedLeaves;
        public final ModConfigSpec.BooleanValue rainbowCompostDuringJune;

        public Client(ModConfigSpec.Builder builder) {
            builder.comment("Client configuration for Ex Deorum").push("client");

            this.useFastInfestedLeaves = builder
                    .comment("Whether to use a simplified renderer for infested leaves (reduces FPS lag with lots of infested trees)")
                    .define("use_fast_infested_leaves", false);
            this.rainbowCompostDuringJune = builder
                    .comment("Whether compost in barrels appears as rainbow colored during the month of June")
                    .define("rainbow_compost_during_june", true);

            builder.pop();
        }
    }

    // Needed because these configs are needed before Tags are loaded
    public static class Common {
        public final ModConfigSpec.ConfigValue<String> preferredAluminumOre;
        public final ModConfigSpec.ConfigValue<String> preferredCobaltOre;
        public final ModConfigSpec.ConfigValue<String> preferredSilverOre;
        public final ModConfigSpec.ConfigValue<String> preferredLeadOre;
        public final ModConfigSpec.ConfigValue<String> preferredPlatinumOre;
        public final ModConfigSpec.ConfigValue<String> preferredNickelOre;
        public final ModConfigSpec.ConfigValue<String> preferredUraniumOre;
        public final ModConfigSpec.ConfigValue<String> preferredOsmiumOre;
        public final ModConfigSpec.ConfigValue<String> preferredTinOre;
        public final ModConfigSpec.ConfigValue<String> preferredZincOre;
        public final ModConfigSpec.ConfigValue<String> preferredIridiumOre;
        public final ModConfigSpec.ConfigValue<String> preferredThoriumOre;
        public final ModConfigSpec.ConfigValue<String> preferredMagnesiumOre;
        public final ModConfigSpec.ConfigValue<String> preferredLithiumOre;
        public final ModConfigSpec.ConfigValue<String> preferredBoronOre;

        public Common(ModConfigSpec.Builder builder) {
            // Preferred items
            builder.comment("Common configuration for Ex Deorum").push("common");

            builder.comment("For recipes automatically added by Ex Deorum for other mods, some mods may add two of the same item (ex. Tin Ore). When Ex Deorum adds a recipe for those kinds of items, you may choose which item of the two (or more) is chosen as the crafting result.").push("preferred_tag_items");

            var airId = ModIds.MINECRAFT + ":air";

            this.preferredAluminumOre = preferredOreConfig(builder, "aluminum_ore", airId);
            this.preferredCobaltOre = preferredOreConfig(builder, "cobalt_ore", ModIds.TINKERS_CONSTRUCT + ":cobalt_ore");
            this.preferredSilverOre = preferredOreConfig(builder, "silver_ore", airId);
            this.preferredLeadOre = preferredOreConfig(builder, "lead_ore", airId);
            this.preferredPlatinumOre = preferredOreConfig(builder, "platinum_ore", airId);
            this.preferredNickelOre = preferredOreConfig(builder, "nickel_ore", airId);
            this.preferredUraniumOre = preferredOreConfig(builder, "uranium_ore", airId);
            this.preferredOsmiumOre = preferredOreConfig(builder, "osmium_ore", airId);
            this.preferredTinOre = preferredOreConfig(builder, "tin_ore", airId);
            this.preferredZincOre = preferredOreConfig(builder, "zinc_ore", airId);
            this.preferredIridiumOre = preferredOreConfig(builder, "iridium_ore", airId);
            this.preferredThoriumOre = preferredOreConfig(builder, "thorium_ore", airId);
            this.preferredMagnesiumOre = preferredOreConfig(builder, "magnesium_ore", airId);
            this.preferredLithiumOre = preferredOreConfig(builder, "lithium_ore", airId);
            this.preferredBoronOre = preferredOreConfig(builder, "boron_ore", airId);

            builder.pop();

            builder.pop();
        }
    }

    public static class Server {
        public final ModConfigSpec.BooleanValue startingTorch;
        public final ModConfigSpec.BooleanValue startingWateringCan;
        public final ModConfigSpec.BooleanValue simultaneousSieveUsage;
        public final ModConfigSpec.IntValue simultaneousSieveUsageRange;
        public final ModConfigSpec.BooleanValue automatedSieves;
        public final ModConfigSpec.BooleanValue nerfAutomatedSieves;
        public final ModConfigSpec.BooleanValue simultaneousCompressedSieveUsage;
        public final ModConfigSpec.DoubleValue barrelProgressStep;
        public final ModConfigSpec.BooleanValue witchWaterDirtGenerator;
        public final ModConfigSpec.BooleanValue witchWaterNetherrackGenerator;
        public final ModConfigSpec.BooleanValue limitMossSieveDrops;
        public final ModConfigSpec.BooleanValue allowWaterBottleTransfer;
        public final ModConfigSpec.BooleanValue allowWitchWaterEntityConversion;
        public final ModConfigSpec.IntValue mechanicalSieveEnergyStorage;
        public final ModConfigSpec.IntValue mechanicalSieveEnergyConsumption;
        public final ModConfigSpec.IntValue mechanicalHammerEnergyStorage;
        public final ModConfigSpec.IntValue mechanicalHammerEnergyConsumption;
        public final ModConfigSpec.IntValue sieveIntervalTicks;
        public final ModConfigSpec.BooleanValue barrelsCollectRainWater;
        public final ModConfigSpec.BooleanValue cruciblesCollectRainWater;

        public Server(ModConfigSpec.Builder builder) {
            builder.comment("Server configuration for Ex Deorum").push("server");

            this.startingTorch = builder
                    .comment("Whether players joining a skyblock world start out with a torch or not.")
                    .define("starting_torch", true);
            this.startingWateringCan = builder
                    .comment("Whether players joining a skyblock world start out with a full wooden watering can.")
                    .define("starting_watering_can", true);
            this.simultaneousSieveUsage = builder
                    .comment("Whether players can use multiple sieves in a 3x3 or larger area at once.")
                    .define("simultaneous_sieve_usage", true);
            this.simultaneousSieveUsageRange = builder
                    .comment("The range from which simultaneous sieve usage can reach. 1 means a maximum of 3x3 sieves at once, 2 means a maximum of 5x5, 3 means maximum of 7x7 simultaneous sieves, and so on.")
                    .defineInRange("simultaneous_sieve_range", 2, 0, 6);
            this.automatedSieves = builder
                    .comment("Whether machines/fake players can interact with the Sieve. Keep in mind, the intended automation method is to use the Mechanical Sieve.")
                    .define("automated_sieves", false);
            this.nerfAutomatedSieves = builder
                    .comment("Whether machines/fake players that interact with the Sieve can sieve in a 3x3 or larger. This option does nothing if automated_sieves is set to false.")
                    .define("nerf_automated_sieves", true);
            this.simultaneousCompressedSieveUsage = builder
                    .comment("Whether players can use multiple compressed sieves in a 3x3 or larger area at once.")
                    .define("simultaneous_compressed_sieve_usage", true);
            this.barrelProgressStep = builder
                    .comment("The progress to increment by each tick for barrel composting.")
                    .defineInRange("barrel_progress_step", 0.004, 0.0f, 1.0f);
            this.witchWaterDirtGenerator = builder
                    .comment("Whether Witch Water forms dirt when water flows into it, allowing for a dirt version of a cobblestone generator.")
                    .define("witch_water_dirt_generator", false);
            this.witchWaterNetherrackGenerator = builder
                    .comment("Whether Witch Water forms netherrack when lava flows into it, allowing for a netherrack version of a cobblestone generator.")
                    .define("witch_water_netherrack_generator", true);
            this.limitMossSieveDrops = builder
                    .comment("Whether to restrict Moss Block sieve drops to 1-2 items when sieving. May be useful when lots of mods add saplings and the sieve drops become spammy.")
                    .define("limit_moss_sieve_drops", true);
            this.allowWaterBottleTransfer = builder
                    .comment("Whether glass bottles can be used to transfer water between water crucibles and barrels.")
                    .define("allow_water_bottle_transfer", true);
            this.allowWitchWaterEntityConversion = builder
                    .comment("Whether the entity conversion mechanic of Witch Water is enabled. If enabled, when an entity steps into Witch Water, the following conversions may happen: Villager -> Zombie Villager, Cleric Villager -> Witch, Skeleton -> Wither Skeleton, Creeper -> Charged Creeper, Spider -> Cave Spider, Pig & Piglin -> Zombified Piglin, Squid -> Ghast, Mooshroom -> Brown Mooshroom, Axolotl -> Blue Axolotl, Rabbit -> Killer Rabbit, Pufferfish -> Guardian, Horse -> Skeleton/Zombie Horse")
                    .define("allow_witch_water_entity_conversion", true);
            this.mechanicalSieveEnergyStorage = builder
                    .comment("The maximum amount of FE the mechanical sieve can have in its energy storage.")
                    .defineInRange("mechanical_sieve_energy_storage", 40_000, 0, Integer.MAX_VALUE);
            this.mechanicalSieveEnergyConsumption = builder
                    .comment("The amount of FE/t a tick consumed by the mechanical sieve when sifting a block.")
                    .defineInRange("mechanical_sieve_energy_consumption", 40, 0, Integer.MAX_VALUE);
            this.mechanicalHammerEnergyStorage = builder
                    .comment("The maximum amount of FE the mechanical hammer can have in its energy storage.")
                    .defineInRange("mechanical_hammer_energy_storage", 40_000, 0, Integer.MAX_VALUE);
            this.mechanicalHammerEnergyConsumption = builder
                    .comment("The amount of FE/t a tick consumed by the mechanical hammer when crushing a block.")
                    .defineInRange("mechanical_hammer_energy_consumption", 20, 0, Integer.MAX_VALUE);
            this.sieveIntervalTicks = builder
                    .comment("The minimum number of ticks a player must wait between two sifting operations. Only affects sifting by hand. 0 means no limit.")
                    .defineInRange("sieve_interval", 1, 0, Integer.MAX_VALUE);
            this.barrelsCollectRainWater = builder
                    .comment("Whether barrels fill up with rain water while it is raining and the barrel is exposed to the sky.")
                    .define("barrels_collect_rain_water", true);
            this.cruciblesCollectRainWater = builder
                    .comment("Whether wooden crucibles fill up with rain water while it is raining and the barrel is exposed to the sky.")
                    .define("crucibles_collect_rain_water", true);
            builder.pop();
        }
    }

    private static String capitalizeWords(String name) {
        var words = name.replace('_', ' ').split(" ");
        var sb = new StringBuilder();

        for (var word : words) {
            if (!word.isEmpty()) {
                if (sb.length() != 0) {
                    sb.append(' ');
                }
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return sb.toString();
    }

    private static ModConfigSpec.ConfigValue<String> preferredOreConfig(ModConfigSpec.Builder builder, String name, String defaultId) {
        return builder
                .comment("The ID of the item to use for Ex Deorum recipes that craft into " + capitalizeWords(name) + ". Leave as air for default preference, which chooses alphabetically by mod name.")
                .define(List.of("preferred_" + name), defaultId, o -> o != null && o.getClass() == String.class && RecipeUtil.isValidResourceLocation((String) o));
    }

    // Loads the client and common configs from the game config folder
    public static void loadConfigs() {
        var configDir = FabricLoader.getInstance().getConfigDir();
        CLIENT_SPEC.load(configDir.resolve("exdeorum-client.toml"));
        COMMON_SPEC.load(configDir.resolve("exdeorum-common.toml"));
    }

    // Loads the per-world server config like NeoForge's SERVER config type
    public static void loadServerConfig(MinecraftServer server) {
        var worldDir = server.getWorldPath(LevelResource.LEVEL_DATA_FILE).getParent();
        SERVER_SPEC.load(worldDir.resolve("serverconfig").resolve("exdeorum-server.toml"));
    }

    static {
        {
            var specPair = ModConfigSpec.configure(Client::new);
            CLIENT = specPair.getLeft();
            CLIENT_SPEC = specPair.getRight();
        }
        {
            var specPair = ModConfigSpec.configure(Common::new);
            COMMON = specPair.getLeft();
            COMMON_SPEC = specPair.getRight();
        }
        {
            var specPair = ModConfigSpec.configure(Server::new);
            SERVER = specPair.getLeft();
            SERVER_SPEC = specPair.getRight();
        }
    }
}
