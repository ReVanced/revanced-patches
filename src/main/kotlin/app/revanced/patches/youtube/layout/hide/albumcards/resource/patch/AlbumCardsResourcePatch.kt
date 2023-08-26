package app.revanced.patches.youtube.layout.hide.albumcards.resource.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
class AlbumCardsResourcePatch : ResourcePatch {
    companion object {
        internal var albumCardId: Long = -1
    }

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_album_cards",
                StringResource("revanced_hide_album_cards_title", "Hide album cards"),
                StringResource("revanced_hide_album_cards_summary_on", "Album cards are hidden"),
                StringResource("revanced_hide_album_cards_summary_off", "Album cards are shown")
            )
        )

        albumCardId = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "album_card"
        }.id
    }
}