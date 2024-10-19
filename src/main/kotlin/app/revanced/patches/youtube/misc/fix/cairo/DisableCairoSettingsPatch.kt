package app.revanced.patches.youtube.misc.fix.cairo

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.backgroundplayback.BackgroundPlaybackPatch
import app.revanced.patches.youtube.misc.fix.cairo.fingerprints.CarioFragmentConfigFingerprint
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    description = "Disables Cairo Fragment from being used.",
    dependencies = [
        VersionCheckPatch::class
    ]
)
internal object DisableCairoSettingsPatch : BytecodePatch(
    setOf(CarioFragmentConfigFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        if (!VersionCheckPatch.is_19_04_or_greater) {
            return
        }

        /**
         * <pre>
         * Cairo Fragment was added since YouTube v19.04.38.
         *
         * Disable this for the following reasons:
         * 1. [BackgroundPlaybackPatch] does not activate the Minimized playback setting of Cairo Fragment.
         * 2. Some patches do not yet support Cairo Fragments (ie: custom Seekbar color).
         * 3. Settings preferences added by ReVanced are missing.
         *
         * Screenshots of the Cairo Fragment:
         * <a href="https://github.com/qnblackcat/uYouPlus/issues/1468">uYouPlus#1468</a>.
         */
        CarioFragmentConfigFingerprint.resultOrThrow().mutableMethod.apply {
            val literalIndex = indexOfFirstWideLiteralInstructionValueOrThrow(
                CarioFragmentConfigFingerprint.CAIRO_CONFIG_LITERAL_VALUE
            )
            val resultIndex = indexOfFirstInstructionOrThrow(literalIndex, Opcode.MOVE_RESULT)
            val register = getInstruction<OneRegisterInstruction>(resultIndex).registerA

            addInstruction(
                resultIndex + 1,
                "const/16 v$register, 0x0"
            )
        }
    }
}