/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

// Port of NeoForge's FluidInteractionRegistry, scoped to the interactions Ex Deorum registers.
// The registration API mirrors the original so the ported call sites keep the same shape.
// Interactions are checked from LiquidBlockMixin in place of NeoForge's patched neighborChanged.
public final class FluidInteractions {
    private static final List<InteractionInformation> INTERACTIONS = new ArrayList<>();

    public static synchronized void addInteraction(Predicate<FluidState> selfFluid, InteractionInformation interaction) {
        INTERACTIONS.add(interaction.self(selfFluid));
    }

    public static boolean canInteract(Level level, BlockPos pos) {
        FluidState state = level.getFluidState(pos);

        for (var direction : LiquidBlock.POSSIBLE_FLOW_DIRECTIONS) {
            BlockPos relativePos = pos.relative(direction.getOpposite());

            for (var interaction : INTERACTIONS) {
                if (interaction.self().test(state) && interaction.predicate().test(level, pos, relativePos, state)) {
                    interaction.interaction().interact(level, pos, relativePos, state);
                    return true;
                }
            }
        }

        return false;
    }

    public interface HasFluidInteraction {
        boolean test(Level level, BlockPos currentPos, BlockPos relativePos, FluidState currentState);
    }

    public interface FluidInteraction {
        void interact(Level level, BlockPos currentPos, BlockPos relativePos, FluidState currentState);
    }

    public static final class InteractionInformation {
        private final Predicate<FluidState> self;
        private final HasFluidInteraction predicate;
        private final FluidInteraction interaction;

        private InteractionInformation(Predicate<FluidState> self, HasFluidInteraction predicate, FluidInteraction interaction) {
            this.self = self;
            this.predicate = predicate;
            this.interaction = interaction;
        }

        public InteractionInformation(HasFluidInteraction predicate, FluidInteraction interaction) {
            this(s -> false, predicate, interaction);
        }

        public InteractionInformation self(Predicate<FluidState> self) {
            return new InteractionInformation(self, this.predicate, this.interaction);
        }

        public Predicate<FluidState> self() {
            return this.self;
        }

        public HasFluidInteraction predicate() {
            return this.predicate;
        }

        public FluidInteraction interaction() {
            return this.interaction;
        }

        public InteractionInformation(HasFluidInteraction predicate, Function<FluidState, BlockState> getState) {
            this(predicate, (level, currentPos, relativePos, currentState) -> {
                level.setBlockAndUpdate(currentPos, getState.apply(currentState));
                level.levelEvent(1501, currentPos, 0);
            });
        }
    }
}
