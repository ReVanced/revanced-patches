package app.revanced.patches.youtube.layout.hideartistcard.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.litho.filter.patch.LithoFilterPatch
import app.revanced.patches.youtube.layout.buttons.annotations.HideArtistCardCompatibility
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference

@Patch
@DependsOn([ResourceMappingResourcePatch::class, LithoFilterPatch::class])
@Name("hide-artist-card")
@Description("Hides the artist card below the searchbar.")
@HideArtistCardCompatibility
@Version("0.0.1")
class HideArtistCardPatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_artist_card",
                StringResource("revanced_hide_hide_artist_card_title", "Hide artist card"),
                false,
                StringResource("revanced_hide_hide_artist_card_on", "Artist card is hidden"),
                StringResource("revanced_hide_hide_artist_card_off", "Artist card is shown")
            ),
        )
        return PatchResultSuccess()
    }
}
