package app.revanced.patches.youtube.general.loadingscreen

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.general.loadingscreen.fingerprints.GradientLoadingScreenPrimaryFingerprint
import app.revanced.patches.youtube.general.loadingscreen.fingerprints.GradientLoadingScreenSecondaryFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.GENERAL
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable gradient loading screen",
    description = "Adds an option to enable gradient loading screen.",
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
object GradientLoadingScreenPatch : BytecodePatch(
    setOf(
        GradientLoadingScreenPrimaryFingerprint,
        GradientLoadingScreenSecondaryFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        /**
         * YouTube v18.29.38 ~
         */
        GradientLoadingScreenSecondaryFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getWideLiteralInstructionIndex(45418917) + 2
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1, """
                        invoke-static { }, $GENERAL->enableGradientLoadingScreen()Z
                        move-result v$targetRegister
                        """
                )
            }
        }

        GradientLoadingScreenPrimaryFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getWideLiteralInstructionIndex(45412406) + 2
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1, """
                        invoke-static { }, $GENERAL->enableGradientLoadingScreen()Z
                        move-result v$targetRegister
                        """
                )
            }
        } ?: throw GradientLoadingScreenPrimaryFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: GENERAL_SETTINGS",
                "SETTINGS: ENABLE_GRADIENT_LOADING_SCREEN"
            )
        )

        SettingsPatch.updatePatchStatus("Enable gradient loading screen")

    }
}
