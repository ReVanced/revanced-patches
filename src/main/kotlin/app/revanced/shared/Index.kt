package app.revanced.shared

import app.revanced.patcher.patch.Patch
import app.revanced.patches.ad.VideoAdsPatch
import app.revanced.patches.interaction.EnableSeekbarTappingPatch
import app.revanced.patches.layout.*
import app.revanced.patches.misc.IntegrationsPatch
import app.revanced.signatures.SignatureSupplier
import app.revanced.signatures.ad.VideoAdsSignature
import app.revanced.signatures.misc.IntegrationsSignature

/**
 * Index contains all the patches and signatures.
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

    /**
     * Array of signatures.
     * New signatures should be added to the array.
     */
    val signatures: Array<() -> SignatureSupplier> = arrayOf(
        ::IntegrationsSignature,
        ::VideoAdsSignature,
    )
}