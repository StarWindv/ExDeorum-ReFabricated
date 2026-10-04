/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.client;

import java.lang.reflect.Method;

import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

// Iris is the only shader mod that publishes an API for this, and it is optional, so it is
// called reflectively. The NeoForge version used IClientItemExtensions to notice shaders and
// fall back to the vanilla block model for infested leaves; that hook has no equivalent here,
// so the same decision is made from this class instead.
public final class ShaderCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String IRIS_API = "net.irisshaders.iris.api.v0.IrisApi";

    private static final Method GET_INSTANCE = findIrisMethod("getInstance");
    private static final Method SHADERPACK_IN_USE = findIrisMethod("isShaderPackInUse");
    private static boolean loggedFailure;

    private ShaderCompat() {
    }

    private static Method findIrisMethod(String name) {
        if (!FabricLoader.getInstance().isModLoaded("iris")) {
            return null;
        }

        try {
            return Class.forName(IRIS_API).getMethod(name);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            LOGGER.warn("Iris is loaded but {}.{} is missing, shader detection stays off", IRIS_API, name);
            return null;
        }
    }

    public static boolean isShaderPackInUse() {
        if (GET_INSTANCE == null || SHADERPACK_IN_USE == null) {
            return false;
        }

        try {
            return (Boolean) SHADERPACK_IN_USE.invoke(GET_INSTANCE.invoke(null));
        } catch (ReflectiveOperationException e) {
            if (!loggedFailure) {
                loggedFailure = true;
                LOGGER.warn("Failed to ask Iris whether a shader pack is in use", e);
            }

            return false;
        }
    }
}