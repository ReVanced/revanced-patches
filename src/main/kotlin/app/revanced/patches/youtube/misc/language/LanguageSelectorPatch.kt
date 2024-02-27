package app.revanced.patches.youtube.misc.language

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.language.fingerprints.GeneralPrefsFingerprint
import app.revanced.patches.youtube.misc.language.fingerprints.GeneralPrefsLegacyFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.MISC_PATH
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable language switch",
    description = "Adds an option to enable or disable language switching toggle.",
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
object LanguageSelectorPatch : BytecodePatch(
    setOf(
        GeneralPrefsFingerprint,
        GeneralPrefsLegacyFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        val result = GeneralPrefsFingerprint.result // YouTube v18.33.xx ~
            ?: GeneralPrefsLegacyFingerprint.result // ~ YouTube v18.32.xx
            ?: throw GeneralPrefsFingerprint.exception

        result.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex - 2
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {}, $MISC_PATH/LanguageSelectorPatch;->enableLanguageSwitch()Z
                        move-result v$insertRegister
                        """
                )
            }
        }

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "SETTINGS: ENABLE_LANGUAGE_SWITCH"
            )
        )

        SettingsPatch.updatePatchStatus("Enable language switch")

    }
}
