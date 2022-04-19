package app.revanced.patches

import app.revanced.patcher.patch.Patch
import app.revanced.patches.ad.HomeAdsPatch
import app.revanced.patches.ad.HomePromoPatch
import app.revanced.patches.ad.VideoAdsPatch
import app.revanced.patches.interaction.EnableSeekbarTappingPatch
import app.revanced.patches.layout.CreateButtonRemoverPatch
import app.revanced.patches.layout.ShortsButtonRemoverPatch
import app.revanced.patches.layout.HideReelsPatch
import app.revanced.patches.layout.MinimizedPlaybackPatch
import app.revanced.patches.layout.OldQualityLayoutPatch
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
        ::EnableSeekbarTappingPatch
    )
}