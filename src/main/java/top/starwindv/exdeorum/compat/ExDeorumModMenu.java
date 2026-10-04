/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import top.starwindv.exdeorum.client.screen.ProbabilityConfigScreen;

/**
 * Adds Ex Deorum's drop rate screen to ModMenu's mod list. ModMenu is an optional
 * dependency; Fabric only loads this class when ModMenu asks for it, so the mod still runs
 * without ModMenu installed.
 */
public class ExDeorumModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ProbabilityConfigScreen::new;
    }
}