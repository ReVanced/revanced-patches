package app.revanced.patches.discord.misc.plugin

import app.revanced.patches.discord.misc.plugin.fingerprints.MainActivityOnCreateFingerprint
import app.revanced.patches.shared.misc.react.BaseReactPreloadScriptBootstrapperPatch

@Suppress("unused")
object PluginLoaderPatch : BaseReactPreloadScriptBootstrapperPatch(
    name = "Plugin loader",
    description = "Bootstraps a plugin loader.",
    mainActivityOnCreateFingerprintInsertIndexPair = MainActivityOnCreateFingerprint to 2,
) {
    override val integrationsClassDescriptor = "Lapp/revanced/integrations/discord/plugin/BunnyBootstrapperPatch;"
}
