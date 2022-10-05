package app.revanced.patches.youtube.misc.settings.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.toInstruction
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingResourcePatch
import app.revanced.patches.youtube.misc.settings.annotations.SettingsCompatibility
import app.revanced.patches.youtube.misc.settings.bytecode.fingerprints.LicenseActivityFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.fingerprints.ReVancedSettingsActivityFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.fingerprints.ThemeSetterFingerprint
import app.revanced.patches.youtube.misc.settings.framework.components.BasePreference
import app.revanced.patches.youtube.misc.settings.framework.components.impl.ArrayResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.Preference
import app.revanced.patches.youtube.misc.settings.framework.components.impl.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.resource.patch.SettingsResourcePatch
import org.jf.dexlib2.util.MethodUtil
import java.io.Closeable

@Patch
@DependsOn(
    [
        IntegrationsPatch::class,
        SettingsResourcePatch::class,
    ]
)
@Name("settings")
@Description("Adds settings for ReVanced to YouTube.")
@SettingsCompatibility
@Version("0.0.1")
class SettingsPatch : BytecodePatch(
    listOf(LicenseActivityFingerprint, ReVancedSettingsActivityFingerprint, ThemeSetterFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
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
                themeSetterResult.scanResult.patternScanResult!!.startIndex,
                setThemeInstruction
            )

            it.addInstruction(
                it.instructions.size - 1, // add before return
                setThemeInstruction
            )
        }

        // add the setTheme call to the onCreate method to not affect the offsets
        onCreate.addInstructions(
            1,
            """
                invoke-static { p0 }, ${settingsClass.type}->${initializeSettings.name}(${licenseActivityClass.type})V
                return-void
            """
        )

        // add the initializeSettings call to the onCreate method
        onCreate.addInstruction(
            0,
            "invoke-static { p0 }, ${settingsClass.type}->$setThemeMethodName(${licenseActivityClass.type})V"
        )

        // get rid of, now, useless overridden methods
        licenseActivityResult.mutableClass.methods.removeIf { it.name != "onCreate" && !MethodUtil.isConstructor(it) }

        return PatchResultSuccess()
    }

    internal companion object {
        // TODO: hide this somehow
        var appearanceStringId: Long = ResourceMappingResourcePatch.resourceMappings.find {
            it.type == "string" && it.name == "app_theme_appearance_dark"
        }!!.id

        fun addString(identifier: String, value: String, formatted: Boolean = true) =
            SettingsResourcePatch.addString(identifier, value, formatted)

        fun addPreferenceScreen(preferenceScreen: app.revanced.patches.youtube.misc.settings.framework.components.impl.PreferenceScreen) =
            SettingsResourcePatch.addPreferenceScreen(preferenceScreen)

        fun addPreference(preference: Preference) =
            SettingsResourcePatch.addPreference(preference)

        fun addArray(arrayResource: ArrayResource) =
            SettingsResourcePatch.addArray(arrayResource)

        fun renameIntentsTargetPackage(newPackage: String) {
            SettingsResourcePatch.overrideIntentsTargetPackage = newPackage
        }
    }

    /**
     * Preference screens patches should add their settings to.
     */
    internal enum class PreferenceScreen(
        private val key: String,
        private val title: String,
        private val summary: String? = null,
        private val preferences: MutableList<BasePreference> = mutableListOf()
    ) : Closeable {
        ADS("ads", "Ads", "Ad related settings"),
        INTERACTIONS("interactions", "Interaction", "Settings related to interactions"),
        LAYOUT("layout", "Layout", "Settings related to the layout"),
        MISC("misc", "Miscellaneous", "Miscellaneous patches");

        override fun close() {
            if (preferences.size == 0) return

            addPreferenceScreen(
                PreferenceScreen(
                    key,
                    StringResource("${key}_title", title),
                    preferences,
                    summary?.let { summary ->
                        StringResource("${key}_summary", summary)
                    }
                )
            )
        }

        /**
         * Add preferences to the preference screen.
         */
        fun addPreferences(vararg preferences: BasePreference) = this.preferences.addAll(preferences)
    }

    override fun close() = PreferenceScreen.values().forEach(PreferenceScreen::close)

}