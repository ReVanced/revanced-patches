package app.revanced.patches

import app.revanced.patcher.data.base.Data
import app.revanced.patcher.patch.base.Patch
import app.revanced.patches.music.audio.codecs.patch.CodecsUnlockPatch
import app.revanced.patches.music.audio.exclusiveaudio.patch.ExclusiveAudioPatch
import app.revanced.patches.music.layout.tastebuilder.patch.RemoveTasteBuilderPatch
import app.revanced.patches.music.layout.upgradebutton.patch.RemoveUpgradeButtonPatch
import app.revanced.patches.music.premium.backgroundplay.patch.BackgroundPlayPatch
import app.revanced.patches.youtube.ad.home.patch.PromotionsPatch
import app.revanced.patches.youtube.ad.video.patch.VideoAdsPatch
import app.revanced.patches.youtube.interaction.seekbar.patch.EnableSeekbarTappingPatch
import app.revanced.patches.youtube.layout.createbutton.patch.CreateButtonRemoverPatch
import app.revanced.patches.youtube.layout.minimizedplayback.patch.MinimizedPlaybackPatch
import app.revanced.patches.youtube.layout.oldqualitylayout.patch.OldQualityLayoutPatch
import app.revanced.patches.youtube.layout.reels.patch.HideReelsPatch
import app.revanced.patches.youtube.layout.shorts.button.patch.ShortsButtonRemoverPatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch

/**
 * Index contains all the patches.
 */
@Suppress("Unused")
object Index {
    /**
     * Array of patches.
     * New patches should be added to the array.
     */
    val patches: List<() -> Patch<Data>> = listOf(
        ::IntegrationsPatch,
        ::FixLocaleConfigErrorPatch,
        //::HomeAdsPatch,
        ::VideoAdsPatch,
        ::PromotionsPatch,
        ::MinimizedPlaybackPatch,
        ::CreateButtonRemoverPatch,
        ::ShortsButtonRemoverPatch,
        ::HideReelsPatch,
        ::OldQualityLayoutPatch,
        ::EnableSeekbarTappingPatch,
        ::ExclusiveAudioPatch,
        ::RemoveUpgradeButtonPatch,
        ::RemoveTasteBuilderPatch,
        ::BackgroundPlayPatch,
        ::CodecsUnlockPatch
    )
}
