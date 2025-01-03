package app.revanced.patches.music.ad.video

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideVideoAdsPatch = bytecodePatch(
    name = "Hide music video ads",
    description = "Hides ads that appear while listening to or streaming music videos, podcasts, or songs.",
) {
    compatibleWith("com.google.android.apps.youtube.music")

    execute {
        navigate(showVideoAdsParentFingerprint.originalMethod)
            .to(showVideoAdsParentFingerprint.filterMatches.first().index + 1)
            .stop()
            .addInstruction(0, "const/4 p1, 0x0")
    }
}
