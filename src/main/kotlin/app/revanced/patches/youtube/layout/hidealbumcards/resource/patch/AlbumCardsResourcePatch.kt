package app.revanced.patches.youtube.layout.hidealbumcards.resource.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.layout.hidealbumcards.annotations.AlbumCardsCompatibility
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference

@Name("hide-album-cards-resource-patch")
@AlbumCardsCompatibility
@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
@Version("0.0.1")
class AlbumCardsResourcePatch : ResourcePatch {
    companion object {
        internal var albumCardId: Long = -1
    }

    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_album_cards",
                StringResource("revanced_hide_album_cards_title", "Hide the album cards"),
                false,
                StringResource("revanced_hide_album_cards_summary_on", "Album cards is hidden"),
                StringResource("revanced_hide_album_cards_summary_off", "Album cards is visible")
            )
        )

        albumCardId = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "album_card"
        }.id

        return PatchResultSuccess()
    }
}