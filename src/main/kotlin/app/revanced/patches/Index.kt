package app.revanced.patches

import app.revanced.patcher.patch.Patch
import app.revanced.patches.ad.VideoAdsPatch
import app.revanced.patches.interaction.EnableSeekbarTappingPatch
import app.revanced.patches.layout.*
import app.revanced.patches.misc.IntegrationsPatch

/**
 * Index contains all the patches.
 */
@Suppress("Unused")
object Index {
    /**
     * Array of patches.
     * New patches should be added to the array.
     */
    val patches: Array<() -> Patch> = arrayOf(
        ::IntegrationsPatch,
        ::VideoAdsPatch,
        ::MinimizedPlaybackPatch,
        ::CreateButtonRemoverPatch,
        ::HideReelsPatch,
        ::HideSuggestionsPatch,
        ::OldQualityLayoutPatch,
        ::EnableSeekbarTappingPatch
    )
}