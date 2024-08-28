package app.revanced.patches.music.ad.video

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideVideoAdsPatch = bytecodePatch(
    name = "Hide music video ads",
    description = "Hides ads that appear while listening to or streaming music videos, podcasts, or songs.",
) {
    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "6.45.54",
            "6.51.53",
            "7.01.53",
            "7.02.52",
            "7.03.52",
        ),
    )

    val showVideoAdsParentMatch by showVideoAdsParentFingerprint()

    execute { context ->
        val showVideoAdsMethod = context
            .navigate(showVideoAdsParentMatch.mutableMethod)
            .at(showVideoAdsParentMatch.patternMatch!!.startIndex + 1).mutable()

        showVideoAdsMethod.addInstruction(0, "const/4 p1, 0x0")
    }
}
