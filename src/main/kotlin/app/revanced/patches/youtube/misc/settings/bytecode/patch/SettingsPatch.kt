package app.revanced.patches.youtube.misc.settings.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.util.smali.toInstruction
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceIdMappingProviderResourcePatch
import app.revanced.patches.youtube.misc.settings.annotations.SettingsCompatibility
import app.revanced.patches.youtube.misc.settings.bytecode.fingerprints.LicenseActivityFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.fingerprints.ReVancedSettingsActivityFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.fingerprints.ThemeSetterFingerprint
import app.revanced.patches.youtube.misc.settings.resource.patch.SettingsResourcePatch

@Patch
@DependsOn(
    [
        IntegrationsPatch::class,
        SettingsResourcePatch::class,
        ResourceIdMappingProviderResourcePatch::class
    ]
)
@Name("settings")
@Description("Adds settings for ReVanced to YouTube.")
@SettingsCompatibility
@Version("0.0.1")
class SettingsPatch : BytecodePatch(
    listOf(LicenseActivityFingerprint, ReVancedSettingsActivityFingerprint, ThemeSetterFingerprint)
) {
    override fun execute(data: BytecodeData): PatchResult {
        val licenseActivityResult = LicenseActivityFingerprint.result!!
        val settingsResult = ReVancedSettingsActivityFingerprint.result!!
        val themeSetterResult = ThemeSetterFingerprint.result!!

        val licenseActivityClass = licenseActivityResult.mutableClass
        val settingsClass = settingsResult.mutableClass

        val onCreate = licenseActivityResult.mutableMethod
        val setThemeMethodName = "setTheme"
        val initializeSettings = settingsResult.mutableMethod

        val setThemeInstruction =
            "invoke-static {v0}, Lapp/revanced/integrations/utils/ThemeHelper;->setTheme(Ljava/lang/Object;)V".toInstruction(
                themeSetterResult.mutableMethod
            )

        // add instructions to set the theme of the settings activity
        themeSetterResult.mutableMethod.implementation!!.let {
            it.addInstruction(
                themeSetterResult.patternScanResult!!.startIndex,
                setThemeInstruction
            )

            it.addInstruction(
                it.instructions.size - 1, // add before return
                setThemeInstruction
            )
        }

        // add the setTheme call to the onCreate method to not affect the offsets.
        onCreate.addInstructions(
            1,
            """
                invoke-static { p0 }, ${settingsClass.type}->${initializeSettings.name}(${licenseActivityClass.type})V
                return-void
            """
        )

        // add the initializeSettings call to the onCreate method.
        onCreate.addInstruction(
            0,
            "invoke-static { p0 }, ${settingsClass.type}->$setThemeMethodName(${licenseActivityClass.type})V"
        )

        return PatchResultSuccess()
    }

    internal companion object {
        val appearanceStringId = ResourceIdMappingProviderResourcePatch.resourceMappings.find {
            it.type == "string" && it.name == "app_theme_appearance_dark"
        }!!.id
    }
}