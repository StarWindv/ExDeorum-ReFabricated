/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

// Registration helper mirroring the DeferredRegister API the mod was written against,
// backed by vanilla Registry.registerForHolder calls performed at mod init time.
public class DeferredRegister<T> {
    protected final Registry<T> registry;
    protected final ResourceKey<Registry<T>> registryKey;
    protected final String namespace;
    protected final List<Runnable> pending = new ArrayList<>();

    private DeferredRegister(Registry<T> registry, ResourceKey<Registry<T>> registryKey, String namespace) {
        this.registry = registry;
        this.registryKey = registryKey;
        this.namespace = namespace;
    }

    @SuppressWarnings("unchecked")
    static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> registryKey) {
        return (Registry<T>) BuiltInRegistries.REGISTRY.getValueOrThrow((ResourceKey) registryKey);
    }

    @SuppressWarnings("unchecked")
    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        var key = (ResourceKey<Registry<T>>) registryKey;
        return new DeferredRegister<>(getRegistry(key), key, namespace);
    }

    public static Blocks createBlocks(String namespace) {
        return new Blocks(namespace);
    }

    public static Items createItems(String namespace) {
        return new Items(namespace);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Function<Identifier, I> factory) {
        var id = Identifier.fromNamespaceAndPath(this.namespace, name);
        var holder = new DeferredHolder<T, I>(id, this.registryKey);
        this.pending.add(() -> holder.bind(Registry.registerForHolder(this.registry, id, factory.apply(id))));
        return holder;
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> factory) {
        var id = Identifier.fromNamespaceAndPath(this.namespace, name);
        var holder = new DeferredHolder<T, I>(id, this.registryKey);
        this.pending.add(() -> holder.bind(Registry.registerForHolder(this.registry, id, factory.get())));
        return holder;
    }

    // Runs all queued registrations. Call once from the mod entrypoint,
    // after any late registrations such as custom materials.
    public void register() {
        for (var task : this.pending) {
            task.run();
        }
        this.pending.clear();
    }

    public static final class Blocks extends DeferredRegister<Block> {
        private Blocks(String namespace) {
            super(BuiltInRegistries.BLOCK, Registries.BLOCK, namespace);
        }

        @Override
        public <I extends Block> DeferredBlock<I> register(String name, Function<Identifier, I> factory) {
            var id = Identifier.fromNamespaceAndPath(this.namespace, name);
            var holder = new DeferredBlock<I>(id);
            this.pending.add(() -> holder.bind(Registry.registerForHolder(this.registry, id, factory.apply(id))));
            return holder;
        }

        @Override
        public <I extends Block> DeferredBlock<I> register(String name, Supplier<? extends I> factory) {
            var id = Identifier.fromNamespaceAndPath(this.namespace, name);
            var holder = new DeferredBlock<I>(id);
            this.pending.add(() -> holder.bind(Registry.registerForHolder(this.registry, id, factory.get())));
            return holder;
        }
    }

    public static final class Items extends DeferredRegister<Item> {
        private Items(String namespace) {
            super(BuiltInRegistries.ITEM, Registries.ITEM, namespace);
        }

        @Override
        public <I extends Item> DeferredItem<I> register(String name, Function<Identifier, I> factory) {
            var id = Identifier.fromNamespaceAndPath(this.namespace, name);
            var holder = new DeferredItem<I>(id);
            this.pending.add(() -> holder.bind(Registry.registerForHolder(this.registry, id, factory.apply(id))));
            return holder;
        }

        @Override
        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> factory) {
            var id = Identifier.fromNamespaceAndPath(this.namespace, name);
            var holder = new DeferredItem<I>(id);
            this.pending.add(() -> holder.bind(Registry.registerForHolder(this.registry, id, factory.get())));
            return holder;
        }
    }

}
