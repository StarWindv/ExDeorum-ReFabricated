/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.transfer;

import java.lang.reflect.Proxy;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.blockentity.AbstractMachineBlockEntity;
import top.starwindv.exdeorum.blockentity.helper.EnergyHelper;
import top.starwindv.exdeorum.registry.EBlockEntities;

/**
 * Soft hook into the TeamReborn energy API, so cables from tech mods can charge the mechanical
 * sieve and hammer, which is how NeoForge's block energy capability let them.
 *
 * <p>Like the seed compat, nothing here needs the API at compile time: when a loaded mod
 * provides {@code team_reborn_energy}, the sided energy lookup is fetched reflectively and both
 * machines register an adapter backed by their {@link EnergyHelper}; without it this class logs
 * and does nothing, and the machines keep their buffer but nothing can fill it. The adapter is
 * a {@link Proxy} so the API's interface class is never named by compiled code, which is what
 * keeps the game from crashing on installs without any energy mod.
 *
 * <p>Machines only ever receive energy; they have no output.
 */
public final class RebornEnergyInterop {
    /** The mod id of the TeamReborn energy API, shipped by Tech Reborn and other tech mods. */
    public static final String ENERGY_API_MOD_ID = "team_reborn_energy";

    @Nullable
    private static Class<?> storageApi;

    private RebornEnergyInterop() {
    }

    /** Call once during mod init. A no-op unless a loaded mod provides the energy API. */
    @SuppressWarnings("unchecked")
    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(ENERGY_API_MOD_ID)) {
            ExDeorum.LOGGER.info("TeamReborn energy API not present, the machines cannot be charged by cables");
            return;
        }

        try {
            // Fabric resolves classes across mods, so the API class is visible once any mod
            // ships it, whether that is Tech Reborn itself or the standalone API jar.
            storageApi = Class.forName("team.reborn.energy.api.EnergyStorage");
            var sided = (BlockApiLookup<Object, @Nullable Direction>) storageApi.getField("SIDED").get(null);

            sided.registerForBlockEntity(
                    (machine, direction) -> machine.getOrCreateEnergyApi(),
                    EBlockEntities.MECHANICAL_SIEVE.get());
            sided.registerForBlockEntity(
                    (machine, direction) -> machine.getOrCreateEnergyApi(),
                    EBlockEntities.MECHANICAL_HAMMER.get());
        } catch (ReflectiveOperationException e) {
            ExDeorum.LOGGER.warn("Could not hook into the TeamReborn energy API ({}), machine charging disabled", e.toString());
            storageApi = null;
            return;
        }

        ExDeorum.LOGGER.info("Machines registered with the TeamReborn energy API, cables can charge them");
    }

    /**
     * Wraps the machine's buffer in the API interface. Must only be called while the API is
     * present, which the registration above guarantees for all of its callers.
     */
    public static Object createAdapter(EnergyHelper helper) {
        var api = java.util.Objects.requireNonNull(storageApi, "TeamReborn energy API not present");
        var storage = new StagedEnergy(helper);

        return Proxy.newProxyInstance(RebornEnergyInterop.class.getClassLoader(), new Class<?>[]{api},
                (proxy, method, args) -> switch (method.getName()) {
                    case "insert" -> storage.insert((Long) args[0], (TransactionContext) args[1]);
                    case "extract" -> 0L; // machines consume, they never supply
                    case "getAmount" -> (long) storage.current();
                    case "getCapacity" -> (long) helper.getMaxEnergyStored();
                    case "supportsInsertion" -> Boolean.TRUE;
                    case "supportsExtraction" -> Boolean.FALSE;
                    case "toString" -> "ExDeorumMachineEnergy[" + storage.current() + "/" + helper.getMaxEnergyStored() + "]";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.toString());
                });
    }

    // Transaction staging for the adapter: cables work inside Transfer API transactions, while
    // the buffer itself commits directly. Updates are staged per open transaction and applied
    // to the helper only when the outermost transaction commits.
    private static final class StagedEnergy extends SnapshotParticipant<Integer> {
        private final EnergyHelper helper;
        private int staged;

        StagedEnergy(EnergyHelper helper) {
            this.helper = helper;
        }

        long insert(long maxAmount, TransactionContext transaction) {
            var room = (long) this.helper.getMaxEnergyStored() - this.helper.getEnergyStored() - this.staged;
            var inserted = (int) Math.min(maxAmount, Math.max(0, room));

            if (inserted > 0) {
                this.updateSnapshots(transaction);
                this.staged += inserted;
            }

            return inserted;
        }

        int current() {
            return this.helper.getEnergyStored() + this.staged;
        }

        @Override
        protected Integer createSnapshot() {
            return this.staged;
        }

        @Override
        protected void readSnapshot(Integer snapshot) {
            this.staged = snapshot;
        }

        @Override
        protected void onFinalCommit() {
            this.helper.receiveEnergy(this.staged, false);
            this.staged = 0;
        }
    }
}
