package app.revanced.patches.youtube.player.hapticfeedback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.player.hapticfeedback.fingerprints.MarkerHapticsFingerprint
import app.revanced.patches.youtube.player.hapticfeedback.fingerprints.ScrubbingHapticsFingerprint
import app.revanced.patches.youtube.player.hapticfeedback.fingerprints.SeekHapticsFingerprint
import app.revanced.patches.youtube.player.hapticfeedback.fingerprints.SeekUndoHapticsFingerprint
import app.revanced.patches.youtube.player.hapticfeedback.fingerprints.ZoomHapticsFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.PLAYER
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Disable haptic feedback",
    description = "Adds an option to disable haptic feedback when swiping the video player.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object HapticFeedBackPatch : BytecodePatch(
    setOf(
        MarkerHapticsFingerprint,
        SeekHapticsFingerprint,
        SeekUndoHapticsFingerprint,
        ScrubbingHapticsFingerprint,
        ZoomHapticsFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        arrayOf(
            SeekHapticsFingerprint to "disableSeekVibrate",
            SeekUndoHapticsFingerprint to "disableSeekUndoVibrate",
            ScrubbingHapticsFingerprint to "disableScrubbingVibrate",
            MarkerHapticsFingerprint to "disableChapterVibrate",
            ZoomHapticsFingerprint to "disableZoomVibrate"
        ).map { (fingerprint, name) -> fingerprint.injectHook(name) }

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: PLAYER_SETTINGS",
                "SETTINGS: DISABLE_HAPTIC_FEEDBACK"
            )
        )

        SettingsPatch.updatePatchStatus("Disable haptic feedback")

    }

    private fun MethodFingerprint.injectHook(methodName: String) {
        result?.let {
            it.mutableMethod.apply {
                var index = 0
                var register = 0

                if (this.name == "run") {
                    index = implementation!!.instructions.indexOfFirst { instruction ->
                        instruction.opcode == Opcode.SGET
                    }
                    register = getInstruction<OneRegisterInstruction>(index).registerA
                }

                injectHook(index, register, methodName)
            }
        } ?: throw exception
    }

    private fun MutableMethod.injectHook(
        index: Int,
        register: Int,
        name: String
    ) {
        addInstructionsWithLabels(
            index, """
                    invoke-static {}, $PLAYER->$name()Z
                    move-result v$register
                    if-eqz v$register, :vibrate
                    return-void
                    """, ExternalLabel("vibrate", getInstruction(index))
        )
    }
}

