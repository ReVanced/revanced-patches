package app.revanced.patches.youtube.layout.hide.albumcards

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class
    ],
)
internal object AlbumCardsResourcePatch : ResourcePatch() {
    internal var albumCardId: Long = -1

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("AlbumCards")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_album_cards",
                "revanced_hide_album_cards_title",
                "revanced_hide_album_cards_summary_on",
                "revanced_hide_album_cards_summary_off"
            )
        )

        albumCardId = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "album_card"
        }.id
    }
}