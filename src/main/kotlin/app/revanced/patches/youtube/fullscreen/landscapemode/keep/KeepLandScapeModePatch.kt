package app.revanced.patches.youtube.fullscreen.landscapemode.keep

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.fullscreen.landscapemode.keep.fingerprints.BroadcastReceiverFingerprint
import app.revanced.patches.youtube.fullscreen.landscapemode.keep.fingerprints.LandScapeModeConfigFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.FULLSCREEN
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getStringInstructionIndex
import app.revanced.util.getTargetIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Keep landscape mode",
    description = "Adds an option to keep landscape mode when turning the screen off and on in fullscreen.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
object KeepLandScapeModePatch : BytecodePatch(
    setOf(
        BroadcastReceiverFingerprint,
        LandScapeModeConfigFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        LandScapeModeConfigFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = implementation!!.instructions.size - 1
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {v$insertRegister}, $FULLSCREEN->keepFullscreen(Z)Z
                        move-result v$insertRegister
                        """
                )
            }
        } ?: throw PatchException("This version is not supported. Please use YouTube 18.42.41 or later.")

        BroadcastReceiverFingerprint.result?.let { result ->
            result.mutableMethod.apply {
                val stringIndex = getStringInstructionIndex("android.intent.action.SCREEN_ON")
                val insertIndex = getTargetIndex(stringIndex, Opcode.IF_EQZ) + 1

                addInstruction(
                    insertIndex,
                    "invoke-static {}, $FULLSCREEN->setScreenStatus()V"
                )
            }
        } ?: throw BroadcastReceiverFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: FULLSCREEN_SETTINGS",
                "SETTINGS: FULLSCREEN_EXPERIMENTAL_FLAGS",
                "SETTINGS: KEEP_LANDSCAPE_MODE"
            )
        )

        SettingsPatch.updatePatchStatus("Keep landscape mode")

    }
}
