package app.revanced.patches.youtube.layout.hide.albumcards

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
object AlbumCardsResourcePatch : ResourcePatch() {
    internal var albumCardId: Long = -1

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