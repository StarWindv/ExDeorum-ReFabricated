/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum;

import top.starwindv.exdeorum.network.NetworkHandler;

// Shim so the network codecs register without touching the client-only handler.
final class NetworkRegistrationHelper {
    static void register() {
        NetworkHandler.register();
    }
}
