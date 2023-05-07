package app.revanced.patches.spotify.audio.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.spotify.audio.annotation.DisableCaptureRestrictionCompatibility
import app.revanced.patches.spotify.audio.fingerprints.DisableCaptureRestrictionAudioDriverFingerprint
import app.revanced.patches.spotify.audio.resource.patch.DisableCaptureRestrictionResourcePatch
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("disable-capture-restriction")
@DependsOn([DisableCaptureRestrictionResourcePatch::class])
@Description("Allows capturing Spotify's audio output while screen sharing or screen recording.")
@DisableCaptureRestrictionCompatibility
@Version("0.0.2")
class DisableCaptureRestrictionBytecodePatch : BytecodePatch(
    listOf(
        DisableCaptureRestrictionAudioDriverFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val method = DisableCaptureRestrictionAudioDriverFingerprint.result!!.mutableMethod

        method.apply {
            // Replace constant
            val original = instruction(0) as OneRegisterInstruction
            replaceInstruction(
                0,
                "const/4 v${original.registerA}, $ALLOW_CAPTURE_BY_ALL"
            )
        }

        return PatchResultSuccess()
    }

    private companion object {
        const val ALLOW_CAPTURE_BY_ALL = 0x01
    }
}