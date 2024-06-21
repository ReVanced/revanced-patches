package app.revanced.patches.bandcamp.removeplaylimits

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bandcamp.removeplaylimits.fingerprints.playLimitsFingerprint
import app.revanced.util.exception

@Patch(
    name = "Remove Play Limits",
    description = "Disables purchase nagging and playback limits of not purchased tracks",
    compatiblePackages = [CompatiblePackage("com.bandcamp.android")],
)
object removePlayLimitsPatch : BytecodePatch(
    setOf(playLimitsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        playLimitsFingerprint.result?.mutableMethod?.addInstructions(
            0,"return-void"
            ) ?: throw playLimitsFingerprint.exception
    }
}
