package app.revanced.patches

import app.revanced.patcher.patch.Patch
import app.revanced.patches.music.audio.EnableAudioOnlyPatch
import app.revanced.patches.music.layout.RemoveUpgradeTabPatch
import app.revanced.patches.music.layout.RemoveTasteBuilderPatch
import app.revanced.patches.music.premium.BackgroundPlayPatch
import app.revanced.patches.music.audio.CodecsUnlockPatch
import app.revanced.patches.youtube.ad.HomeAdsPatch
import app.revanced.patches.youtube.ad.HomePromoPatch
import app.revanced.patches.youtube.ad.VideoAdsPatch
import app.revanced.patches.youtube.interaction.EnableSeekbarTappingPatch
import app.revanced.patches.youtube.layout.*
import app.revanced.patches.youtube.misc.IntegrationsPatch

/**
 * Index contains all the patches.
 */
@Suppress("Unused")
object Index {
    /**
     * Array of patches.
     * New patches should be added to the array.
     */
    val patches: List<() -> Patch> = listOf(
        ::IntegrationsPatch,
        ::HomeAdsPatch,
        ::VideoAdsPatch,
        ::HomePromoPatch,
        ::MinimizedPlaybackPatch,
        ::CreateButtonRemoverPatch,
        ::ShortsButtonRemoverPatch,
        ::HideReelsPatch,
        ::OldQualityLayoutPatch,
        ::EnableSeekbarTappingPatch,
        ::EnableAudioOnlyPatch,
        ::RemoveUpgradeTabPatch,
        ::RemoveTasteBuilderPatch,
        ::BackgroundPlayPatch,
        ::CodecsUnlockPatch
    )
}
