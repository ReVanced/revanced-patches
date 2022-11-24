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
import app.revanced.patches.shared.settings.BasePreference
import app.revanced.patches.shared.settings.impl.Preference
import app.revanced.patches.shared.settings.impl.PreferenceScreen
import app.revanced.patches.shared.settings.impl.StringResource
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.annotations.SettingsCompatibility
import app.revanced.patches.youtube.misc.settings.bytecode.fingerprints.LicenseActivityFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.fingerprints.ThemeSetterAppFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.fingerprints.ThemeSetterSystemFingerprint
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
    listOf(LicenseActivityFingerprint, ThemeSetterSystemFingerprint, ThemeSetterAppFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        fun buildInvokeInstructionsString(
            registers: String = "v0",
            classDescriptor: String = THEME_HELPER_DESCRIPTOR,
            methodName: String = SET_THEME_METHOD_NAME,
            parameters: String = "Ljava/lang/Object;"
        ) = "invoke-static {$registers}, $classDescriptor->$methodName($parameters)V"

        // apply the current theme of the settings page
        with(ThemeSetterSystemFingerprint.result!!) {
            with(mutableMethod) {
                val call = buildInvokeInstructionsString()

                addInstruction(
                    scanResult.patternScanResult!!.startIndex,
                    call
                )

                addInstruction(
                    mutableMethod.implementation!!.instructions.size - 1,
                    call
                )
            }
        }

        // set the theme based on the preference of the app
        with(ThemeSetterAppFingerprint.result!!) {
            with(mutableMethod) {
                fun buildInstructionsString(theme: Int) = """
                    const/4 v0, 0x$theme
                    ${buildInvokeInstructionsString(parameters = "I")}
                """

                addInstructions(
                    scanResult.patternScanResult!!.endIndex + 1,
                    buildInstructionsString(1)
                )
                addInstructions(
                    scanResult.patternScanResult!!.endIndex - 7,
                    buildInstructionsString(0)
                )

                addInstructions(
                    scanResult.patternScanResult!!.endIndex - 9,
                    buildInstructionsString(1)
                )
                addInstructions(
                    mutableMethod.implementation!!.instructions.size - 2,
                    buildInstructionsString(0)
                )
            }
        }

        // set the theme based on the preference of the device
        with(LicenseActivityFingerprint.result!!) licenseActivity@{
            with(mutableMethod) {
                fun buildSettingsActivityInvokeString(
                    registers: String = "p0",
                    classDescriptor: String = SETTINGS_ACTIVITY_DESCRIPTOR,
                    methodName: String = "initializeSettings",
                    parameters: String = this@licenseActivity.mutableClass.type
                ) = buildInvokeInstructionsString(registers, classDescriptor, methodName, parameters)

                // initialize the settings
                addInstructions(
                    1,
                    """
                        ${buildSettingsActivityInvokeString()}
                        return-void
                    """
                )

                // set the current theme
                addInstruction(0, buildSettingsActivityInvokeString(methodName = "setTheme"))
            }

            // remove method overrides
            with(mutableClass) {
                methods.removeIf { it.name != "onCreate" && !MethodUtil.isConstructor(it) }
            }
        }

        return PatchResultSuccess()
    }

    internal companion object {
        private const val INTEGRATIONS_PACKAGE = "app/revanced/integrations"

        private const val SETTINGS_ACTIVITY_DESCRIPTOR = "L$INTEGRATIONS_PACKAGE/settingsmenu/ReVancedSettingActivity;"

        private const val THEME_HELPER_DESCRIPTOR = "L$INTEGRATIONS_PACKAGE/utils/ThemeHelper;"
        private const val SET_THEME_METHOD_NAME = "setTheme"

        fun addString(identifier: String, value: String, formatted: Boolean = true) =
            SettingsResourcePatch.addString(identifier, value, formatted)

        fun addPreferenceScreen(preferenceScreen: app.revanced.patches.shared.settings.impl.PreferenceScreen) =
            SettingsResourcePatch.addPreferenceScreen(preferenceScreen)

        fun addPreference(preference: Preference) =
            SettingsResourcePatch.addPreference(preference)

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
