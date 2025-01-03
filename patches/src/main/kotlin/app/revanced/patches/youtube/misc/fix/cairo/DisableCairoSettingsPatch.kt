package app.revanced.patches.youtube.misc.fix.cairo

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.backgroundplayback.backgroundPlaybackPatch
import app.revanced.patches.youtube.misc.playservice.is_19_04_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal val disableCairoSettingsPatch = bytecodePatch(
    description = "Disables Cairo Fragment from being used.",
) {
    dependsOn(versionCheckPatch)

    execute {
        if (!is_19_04_or_greater) {
            return@execute
        }

        /**
         * <pre>
         * Cairo Fragment was added since YouTube v19.04.38.
         *
         * Disable this for the following reasons:
         * 1. [backgroundPlaybackPatch] does not activate the Minimized playback setting of Cairo Fragment.
         * 2. Some patches do not yet support Cairo Fragments (ie: custom Seekbar color).
         * 3. Settings preferences added by ReVanced are missing.
         *
         * Screenshots of the Cairo Fragment:
         * <a href="https://github.com/qnblackcat/uYouPlus/issues/1468">uYouPlus#1468</a>.
         */
        cairoFragmentConfigFingerprint.let{
            it.method.apply {
                val resultIndex = it.filterMatches.last().index
                val register = getInstruction<OneRegisterInstruction>(resultIndex).registerA

                addInstruction(
                    resultIndex + 1,
                    "const/16 v$register, 0x0",
                )
            }
        }
    }
}
