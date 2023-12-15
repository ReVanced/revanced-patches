package app.revanced.patches.youtube.misc.settings

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.AbstractSettingsResourcePatch.Companion.addPreference
import app.revanced.patches.shared.settings.preference.impl.Preference
import app.revanced.patches.shared.settings.util.AbstractPreferenceScreen
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.fingerprints.LicenseActivityFingerprint
import app.revanced.patches.youtube.misc.settings.fingerprints.SetThemeFingerprint
import app.revanced.patches.youtube.misc.strings.StringsPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.util.MethodUtil
import java.io.Closeable

@Patch(
    description = "Adds settings for ReVanced to YouTube.",
    dependencies = [IntegrationsPatch::class, SettingsResourcePatch::class, StringsPatch::class]
)
object SettingsPatch : BytecodePatch(
    setOf(LicenseActivityFingerprint, SetThemeFingerprint)
), Closeable {
    private const val INTEGRATIONS_PACKAGE = "app/revanced/integrations"
    private const val SETTINGS_ACTIVITY_DESCRIPTOR = "L$INTEGRATIONS_PACKAGE/settingsmenu/YouTubeSettingActivity;"
    private const val THEME_HELPER_DESCRIPTOR = "L$INTEGRATIONS_PACKAGE/utils/ThemeHelper;"
    private const val SET_THEME_METHOD_NAME = "setTheme"

    /**
     * To add ReVanced settings to YouTube, it must be thru an Activity,
     * and all Activities must be declared in the app manifest.
     *
     * But root installs cannot use a modified manifest,
     * so an existing Activity is hooked and the selected
     * ReVanced Preference screen is passed thru as an Activity intent.
     */
    internal const val HOOKED_SETTINGS_ACTIVITY_NAME = "com.google.android.libraries.social.licenses.LicenseActivity"

    override fun execute(context: BytecodeContext) {
        // TODO: Remove this when it is only required at one place.
        fun getSetThemeInstructionString(
                registers: String = "v0",
                classDescriptor: String = THEME_HELPER_DESCRIPTOR,
                methodName: String = SET_THEME_METHOD_NAME,
                parameters: String = "Ljava/lang/Object;"
        ) = "invoke-static { $registers }, $classDescriptor->$methodName($parameters)V"

        SetThemeFingerprint.result?.mutableMethod?.let { setThemeMethod ->
            setThemeMethod.implementation!!.instructions.mapIndexedNotNull { i, instruction ->
                    if (instruction.opcode == Opcode.RETURN_OBJECT) i else null
                }
                .asReversed() // Prevent index shifting.
                .forEach { returnIndex ->
                    // The following strategy is to replace the return instruction with the setTheme instruction,
                    // then add a return instruction after the setTheme instruction.
                    // This is done because the return instruction is a target of another instruction.

                    setThemeMethod.apply {
                        // This register is returned by the setTheme method.
                        val register = getInstruction<OneRegisterInstruction>(returnIndex).registerA

                        val setThemeInstruction = getSetThemeInstructionString("v$register")
                        replaceInstruction(returnIndex, setThemeInstruction)
                        addInstruction(returnIndex + 1, "return-object v0")
                    }
                }
        } ?: throw SetThemeFingerprint.exception


        // Modify the license activity and remove all existing layout code.
        // Must modify an existing activity and cannot add a new activity to the manifest,
        // as that fails for root installations.
        LicenseActivityFingerprint.result!!.apply licenseActivity@{
            mutableMethod.apply {
                fun buildSettingsActivityInvokeString(
                        registers: String = "p0",
                        classDescriptor: String = SETTINGS_ACTIVITY_DESCRIPTOR,
                        methodName: String = "initializeSettings",
                        parameters: String = "Landroid/app/Activity;"
                ) = getSetThemeInstructionString(registers, classDescriptor, methodName, parameters)

                // initialize the settings
                addInstructions(
                    1,
                    """
                        ${buildSettingsActivityInvokeString()}
                        return-void
                    """
                )
            }

            // remove method overrides
            mutableClass.apply {
                methods.removeIf { it.name != "onCreate" && !MethodUtil.isConstructor(it) }
            }
        }

    }

    fun addPreference(preference: Preference) = SettingsResourcePatch.addPreference(preference)

    fun renameIntentsTargetPackage(newPackage: String) {
        SettingsResourcePatch.overrideIntentsTargetPackage = newPackage
    }

    /**
     * Creates an intent to open ReVanced settings of the given name
     */
    fun createReVancedSettingsIntent(settingsName: String) = Preference.Intent(
        "com.google.android.youtube",
        settingsName,
        HOOKED_SETTINGS_ACTIVITY_NAME
    )

    /**
     * Preference screens patches should add their settings to.
     */
    object PreferenceScreen : AbstractPreferenceScreen() {
        val ADS = Screen(
            "revanced_ads_screen",
            "revanced_ads_screen_title",
            "revanced_ads_screen_summary"
        )
        val INTERACTIONS = Screen(
            "revanced_interaction_screen",
            "revanced_interaction_screen_title",
            "revanced_interaction_screen_summary"
        )
        val LAYOUT = Screen(
            "revanced_layout_screen",
            "revanced_layout_screen_title",
            "revanced_layout_screen_summary"
        )
        val MISC = Screen(
            "revanced_misc_screen",
            "revanced_misc_screen_title",
            "revanced_misc_screen_summary"
        )
        val VIDEO = Screen(
            "revanced_video_screen",
            "revanced_video_screen_title",
            "revanced_video_screen_summary"
        )

        override fun commit(screen: app.revanced.patches.shared.settings.preference.impl.PreferenceScreen) {
            addPreference(screen)
        }
    }

    override fun close() = PreferenceScreen.close()
}
