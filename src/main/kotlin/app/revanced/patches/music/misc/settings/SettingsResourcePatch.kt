package app.revanced.patches.music.misc.settings

import app.revanced.patcher.data.ResourceContext
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.BaseSettingsResourcePatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

object SettingsResourcePatch : BaseSettingsResourcePatch(
    IntentPreference(
        "revanced_settings",
        intent = SettingsPatch.newIntent("revanced_settings_intent")
    ) to "settings_headers",
    dependencies = setOf(
        ResourceMappingPatch::class,
        AddResourcesPatch::class,
    )
) {
    override fun execute(context: ResourceContext) {
        super.execute(context)

        AddResourcesPatch(this::class)

        context.copyResources(
            "settings",
            ResourceGroup("layout", "revanced_settings_with_toolbar.xml")
        )
    }
}
