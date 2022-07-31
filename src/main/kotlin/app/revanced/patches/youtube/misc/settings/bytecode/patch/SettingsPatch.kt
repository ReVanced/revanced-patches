package app.revanced.patches.youtube.misc.settings.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.annotations.SettingsCompatibility
import app.revanced.patches.youtube.misc.settings.bytecode.fingerprints.LicenseActivityFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.fingerprints.ReVancedSettingsActivityFingerprint
import app.revanced.patches.youtube.misc.settings.resource.patch.SettingsResourcePatch

@Patch
@Dependencies([IntegrationsPatch::class, SettingsResourcePatch::class])
@Name("settings")
@Description("Adds settings for ReVanced to YouTube.")
@SettingsCompatibility
@Version("0.0.1")
class SettingsPatch : BytecodePatch(
    listOf(LicenseActivityFingerprint, ReVancedSettingsActivityFingerprint)
) {
    override fun execute(data: BytecodeData): PatchResult {
        val licenseActivityResult = LicenseActivityFingerprint.result!!
        val settingsResult = ReVancedSettingsActivityFingerprint.result!!

        val licenseActivityClass = licenseActivityResult.mutableClass
        val settingsClass = settingsResult.mutableClass

        val onCreate = licenseActivityResult.mutableMethod
        val setThemeMethodName = "setTheme"
        val initializeSettings = settingsResult.mutableMethod

        // First add the setTheme call to the onCreate method to not affect the offsets.
        onCreate.addInstructions(
            1,
            """
                invoke-static { p0 }, ${settingsClass.type}->${initializeSettings.name}(${licenseActivityClass.type})V
                return-void
            """
        )

        // Add the initializeSettings call to the onCreate method.
        onCreate.addInstruction(
            0,
            "invoke-static { p0 }, ${settingsClass.type}->$setThemeMethodName(${licenseActivityClass.type})V"
        )
        return PatchResultSuccess()
    }
}