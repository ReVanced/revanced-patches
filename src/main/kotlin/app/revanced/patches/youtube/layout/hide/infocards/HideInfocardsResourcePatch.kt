package app.revanced.patches.youtube.layout.hide.infocards

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class
    ],
)
object HideInfocardsResourcePatch : ResourcePatch() {
    internal var drawerResourceId: Long = -1

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_info_cards",
                StringResource("revanced_hide_info_cards_title", "Hide info cards"),
                StringResource("revanced_hide_info_cards_summary_on", "Info cards are hidden"),
                StringResource("revanced_hide_info_cards_summary_off", "Info cards are shown")
            )
        )

        drawerResourceId = ResourceMappingPatch.resourceMappings.single {
            it.type == "id" && it.name == "info_cards_drawer_header"
        }.id
    }
}