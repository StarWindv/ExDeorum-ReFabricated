/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum;

import top.starwindv.exdeorum.client.CompostColors;

// Small shim to load compost colors at init without exposing the client class
// to serverside loading paths.
final class CompostColorsLoader {
    static void loadColors() {
        CompostColors.loadColors();
    }
}
