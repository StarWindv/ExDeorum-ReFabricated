/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Sprite pixels are not exposed in 26.2; the debug compost color computation reads them directly.
@Mixin(SpriteContents.class)
public interface SpriteContentsAccessor {
    @Accessor("originalImage")
    NativeImage exdeorum$getOriginalImage();
}
