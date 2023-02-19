package app.revanced.patches.youtube.layout.hide.artistcards.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.hide.artistcards.annotations.HideArtistCardCompatibility
import app.revanced.patches.youtube.misc.litho.filter.patch.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([ResourceMappingPatch::class, LithoFilterPatch::class])
@Name("hide-artist-card")
@Description("Hides the artist card below the searchbar.")
@HideArtistCardCompatibility
@Version("0.0.1")
class HideArtistCardsPatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_artist_cards",
                StringResource("revanced_hide_artist_cards_title", "Hide artist cards"),
                false,
                StringResource("revanced_hide_artist_cards_on", "Artist cards is hidden"),
                StringResource("revanced_hide_artist_cards_off", "Artist cards is shown")
            ),
        )
        return PatchResultSuccess()
    }
}
