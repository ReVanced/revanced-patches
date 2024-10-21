package app.revanced.patches.youtube.layout.buttons.player.flyout

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.buttons.player.flyout.fingerprints.SleepTimerFeatureFingerprint
import app.revanced.util.returnEarly

@Patch(
    name = "Remove sleep timer menu restrictions",
    description = "Show sleep timer options in player flyout menu.",
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "19.34.42", // Only enable this patch on 19.30+ for now.
            ]
        )
    ]
)
@Suppress("unused")
object SleepTimerMenuPatch : BytecodePatch(
    setOf(SleepTimerFeatureFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SleepTimerFeatureFingerprint.returnEarly(true)
    }
}