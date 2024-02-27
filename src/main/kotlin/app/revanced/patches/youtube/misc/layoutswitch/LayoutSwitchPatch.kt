package app.revanced.patches.youtube.misc.layoutswitch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.layoutswitch.fingerprints.GetFormFactorFingerprint
import app.revanced.patches.youtube.utils.fingerprints.LayoutSwitchFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.MISC_PATH
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Layout switch",
    description = "Adds an option to trick dpi to use tablet or phone layout.",
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
object LayoutSwitchPatch : BytecodePatch(
    setOf(
        GetFormFactorFingerprint,
        LayoutSwitchFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        GetFormFactorFingerprint.result?.let {
            it.mutableMethod.apply {
                val jumpIndex = it.scanResult.patternScanResult!!.endIndex - 4

                addInstructionsWithLabels(
                    0, """
                        invoke-static { }, $MISC_PATH/LayoutOverridePatch;->enableTabletLayout()Z
                        move-result v0 # Free register
                        if-nez v0, :is_large_form_factor
                        """,
                    ExternalLabel(
                        "is_large_form_factor",
                        getInstruction(jumpIndex)
                    )
                )
            }
        } ?: GetFormFactorFingerprint.exception

        LayoutSwitchFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    4, """
                        invoke-static {p0}, $MISC_PATH/LayoutOverridePatch;->getLayoutOverride(I)I
                        move-result p0
                        """
                )
            }
        } ?: throw LayoutSwitchFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "SETTINGS: EXPERIMENTAL_FLAGS",
                "SETTINGS: LAYOUT_SWITCH"
            )
        )

        SettingsPatch.updatePatchStatus("Layout switch")

    }
}
