/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.registry;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

// Registration helper mirroring NeoForge's DeferredHolder, backed by a vanilla Holder.Reference.
// Holder is sealed in 26.2, so this cannot implement Holder itself. Use holder() wherever a
// vanilla Holder is required and the delegating methods below for the common Holder queries.
public class DeferredHolder<T, I extends T> implements Supplier<I> {
    final Identifier id;
    final ResourceKey<Registry<T>> registryKey;
    final ResourceKey<T> key;
    private Holder.Reference<T> reference;

    DeferredHolder(Identifier id, ResourceKey<Registry<T>> registryKey) {
        this.id = id;
        this.registryKey = registryKey;
        this.key = ResourceKey.create(registryKey, id);
    }

    // Equivalent of NeoForge's DeferredHolder.create for holders of vanilla entries.
    // The holder resolves lazily from the registry until it is bound.
    public static <T> DeferredHolder<T, T> create(ResourceKey<? extends Registry<T>> registryKey, Identifier id) {
        return new DeferredHolder<>(id, castKey(registryKey));
    }

    @SuppressWarnings("unchecked")
    private static <T> ResourceKey<Registry<T>> castKey(ResourceKey<? extends Registry<T>> registryKey) {
        return (ResourceKey<Registry<T>>) registryKey;
    }

    void bind(Holder.Reference<T> reference) {
        this.reference = reference;
    }

    @SuppressWarnings("unchecked")
    public I get() {
        if (this.reference != null) {
            return (I) this.reference.value();
        }
        return (I) DeferredRegister.getRegistry(this.registryKey).getValueOrThrow(this.key);
    }

    // The underlying vanilla holder. Only null before registration has run.
    public Holder<T> holder() {
        return this.reference;
    }

    public Identifier getId() {
        return this.id;
    }

    @SuppressWarnings("unchecked")
    public ResourceKey<I> getKey() {
        return (ResourceKey<I>) (ResourceKey<?>) this.key;
    }

    public T value() {
        return this.get();
    }

    public boolean isBound() {
        return this.reference != null && this.reference.isBound();
    }

    public boolean areComponentsBound() {
        return this.reference != null && this.reference.areComponentsBound();
    }

    public boolean is(Identifier id) {
        return this.id.equals(id);
    }

    public boolean is(ResourceKey<T> key) {
        return this.key.equals(key);
    }

    public boolean is(Predicate<ResourceKey<T>> predicate) {
        return predicate.test(this.key);
    }

    public boolean is(TagKey<T> tag) {
        return this.reference != null && this.reference.is(tag);
    }

    public boolean is(Holder<T> holder) {
        return holder.is(this.key);
    }

    public Stream<TagKey<T>> tags() {
        return this.reference == null ? Stream.empty() : this.reference.tags();
    }

    public DataComponentMap components() {
        return this.reference == null ? DataComponentMap.EMPTY : this.reference.components();
    }

    public Either<ResourceKey<T>, T> unwrap() {
        return Either.left(this.key);
    }

    public Optional<ResourceKey<T>> unwrapKey() {
        return Optional.of(this.key);
    }

    public Holder.Kind kind() {
        return Holder.Kind.REFERENCE;
    }

    public String getRegisteredName() {
        return this.id.toString();
    }

    public boolean canSerializeIn(HolderOwner<T> owner) {
        return true;
    }
}
