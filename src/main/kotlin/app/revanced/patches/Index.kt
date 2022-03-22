package app.revanced.patches

import app.revanced.patcher.patch.Patch
import app.revanced.patches.ad.VideoAds
import app.revanced.patches.interaction.EnableSeekbarTapping
import app.revanced.patches.layout.*

/**
 * Index contains all the patches and should be imported when using this library.
 */
@Suppress("Unused")
object Index {
    /**
     * Array of patches.
     * New patches should be added to the array.
     */
    val patches: Array<() -> Patch> = arrayOf(
        ::VideoAds,
        ::MinimizedPlayback,
        ::CreateButtonRemover,
        ::HideReels,
        ::HideSuggestions,
        ::OldQualityLayout,
        ::EnableSeekbarTapping
    )
}