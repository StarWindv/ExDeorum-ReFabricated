/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.util;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

// Adapts a simple "reload from the resource manager" callback to the identifiable
// reload listener Fabric API requires for modded reload listeners.
public final class SimpleReloadListener implements IdentifiableResourceReloadListener, ResourceManagerReloadListener {
    private final Identifier id;
    private final Consumer<ResourceManager> action;

    public SimpleReloadListener(Identifier id, Consumer<ResourceManager> action) {
        this.id = id;
        this.action = action;
    }

    @Override
    public Identifier getFabricId() {
        return this.id;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        this.action.accept(manager);
    }
}
