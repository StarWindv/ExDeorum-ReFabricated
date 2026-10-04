# Ex Deorum — Fabric port

###### *"From the Gods"*

> **This repository is a Fabric port.** The original NeoForge mod is
> [Ex Deorum](https://github.com/Thedarkcolour/ExDeorum) by
> [thedarkcolour](https://github.com/Thedarkcolour), and that project is the upstream of everything
> here. Report upstream gameplay bugs to them; report Fabric-port bugs here.

*Ex Deorum* is a port of the 1.7 SkyBlock companion mod *Ex Nihilo* by Erasmus_Crowley. It also
borrows ideas from *Ex Nihilo Adscensio* by unascribed, *YUNoMakeGoodMap* by LexManos, and
*Ex Compressum* by BlayTheNinth, although Ex Deorum does not use code from any of those mods.

Unlike the original mod and any of its ports, *Ex Deorum* includes a SkyBlock world type out of the
box. All of this mod's recipes exist in data packs. The default recipes only cover items from Vanilla
Minecraft, and the sieve drops are generous enough that this mod should be playable without
*Ex Compressum*.

## License and attribution

Everything here is **GPL-3-Clause**, like the original. The full license text is in
[`licenses/LICENSE-GPL-3-Clause.md`](licenses/LICENSE-GPL-3-Clause.md); see
[`licenses/README.md`](licenses/README.md) for how the notices are split between the upstream code
and the code written for this port.

The original *Ex Deorum* is © 2024 thedarkcolour and is licensed under the GPL-3-Clause. Porting it
to Fabric creates a derivative work, so this port must stay GPL-3-Clause too, and the original
copyright notices are retained in every file that came from upstream, alongside a statement of what
was changed. Code written for this port is © 2026 StarWindv and is also GPL-3-Clause.

If you redistribute a build of this mod, you must keep those notices, offer the corresponding
source, and license your own changes under the GPL-3-Clause as well.

## What this port changes

Beyond the loader swap, the port is not a byte-for-byte copy of upstream. The notable differences:

- **Networking, recipes and item handlers** are built directly on Fabric API, replacing the NeoForge
  equivalents. That meant writing replacements for the deferred registries, the item and fluid stack
  types, and the transfer capability handlers, since Fabric has no built-in equivalent.
- **No ASM coremodule.** The upstream NeoForge build shipped a coremodule with class transformers;
  those were replaced with Mixin.
- **The SkyBlock world type was removed.** Upstream registers its own chunk generator and world
  presets. Skyblock and void worlds are expected to come from other mods such as SkyBlockBuilder or
  Void Island Control, and the starting-items advancement is granted when such a world is detected.
- **A drop rate editor** with a player-facing config screen, and a `TunableRecipe` interface so any
  Ex Deorum recipe can expose its chance. Upstream's probabilities are data-pack only.
- **An API for other mods** (`top.starwindv.exdeorum.api.ExDeorumApi`) to register their own sieve
  and compressed sieve drops, which upstream has no equivalent of.
- **Seed compat** for mods whose crops ship no seed that vanilla sieves, so those starts are not
  dead on arrival.

## Credits

Herobrine knows all. Thanks to Erasmus_Crowley for the original 1.7 mod.

[Upstream Discord](https://discord.gg/FWrzBRThHu) (thedarkcolour's, for the mod itself)

