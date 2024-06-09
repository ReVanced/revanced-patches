package app.revanced.patches.music.audio.codecs

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.toInstruction
import app.revanced.patches.music.audio.codecs.fingerprints.allCodecsReferenceFingerprint
import app.revanced.patches.music.audio.codecs.fingerprints.codecsLockFingerprint
import com.android.tools.smali.dexlib2.Opcode

@Suppress("unused")
@Deprecated("This patch is no longer needed as the feature is now enabled by default.")
val codecsUnlockPatch = bytecodePatch(
    description = "Adds more audio codec options. The new audio codecs usually result in better audio quality.",
) {
    compatibleWith("com.google.android.apps.youtube.music")

    val codecsLockResult by codecsLockFingerprint
    val allCodecsResult by allCodecsReferenceFingerprint

    execute { context ->
        val implementation = codecsLockResult.mutableMethod.implementation!!

        val scanResultStartIndex = codecsLockResult.scanResult.patternScanResult!!.startIndex
        val instructionIndex = scanResultStartIndex +
            if (implementation.instructions[scanResultStartIndex - 1].opcode == Opcode.CHECK_CAST) {
                // for 5.16.xx and lower
                -3
            } else {
                // since 5.17.xx
                -2
            }

        val allCodecsMethod = context.navigate(allCodecsResult.method)
            .at(allCodecsResult.scanResult.patternScanResult!!.startIndex).immutable()

        implementation.replaceInstruction(
            instructionIndex,
            "invoke-static {}, ${allCodecsMethod.definingClass}->${allCodecsMethod.name}()Ljava/util/Set;".toInstruction(),
        )
    }
}
