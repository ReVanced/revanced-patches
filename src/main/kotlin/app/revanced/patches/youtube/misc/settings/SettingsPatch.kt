package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.packagename.ChangePackageNamePatch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen.SortStyle
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.fingerprints.LicenseActivityOnCreateFingerprint
import app.revanced.patches.youtube.misc.settings.fingerprints.SetThemeFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.util.MethodUtil
import java.io.Closeable

@Patch(
    description = "Adds settings for ReVanced to YouTube.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsResourcePatch::class,
        AddResourcesPatch::class
    ]
)
object SettingsPatch : BytecodePatch(
    setOf(LicenseActivityOnCreateFingerprint, SetThemeFingerprint)
), Closeable {
    private const val INTEGRATIONS_PACKAGE = "app/revanced/integrations/youtube"
    private const val ACTIVITY_HOOK_CLASS_DESCRIPTOR = "L$INTEGRATIONS_PACKAGE/settings/LicenseActivityHook;"

    private const val THEME_HELPER_DESCRIPTOR = "L$INTEGRATIONS_PACKAGE/ThemeHelper;"
    private const val SET_THEME_METHOD_NAME: String = "setTheme"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        PreferenceScreen.MISC.addPreferences(
            TextPreference(
                key = null,
                titleKey = "revanced_pref_import_export_title",
                summaryKey = "revanced_pref_import_export_summary",
                inputType = InputType.TEXT_MULTI_LINE,
                tag = "app.revanced.integrations.shared.settings.preference.ImportExportPreference"
            )
        )

        SetThemeFingerprint.result?.mutableMethod?.let { setThemeMethod ->
            setThemeMethod.implementation!!.instructions.mapIndexedNotNull { i, instruction ->
                if (instruction.opcode == Opcode.RETURN_OBJECT) i else null
            }.asReversed().forEach { returnIndex ->
                // The following strategy is to replace the return instruction with the setTheme instruction,
                // then add a return instruction after the setTheme instruction.
                // This is done because the return instruction is a target of another instruction.

                setThemeMethod.apply {
                    // This register is returned by the setTheme method.
                    val register = getInstruction<OneRegisterInstruction>(returnIndex).registerA
                    replaceInstruction(
                        returnIndex,
                        "invoke-static { v$register }, " +
                                "$THEME_HELPER_DESCRIPTOR->$SET_THEME_METHOD_NAME(Ljava/lang/Object;)V"
                    )
                    addInstruction(returnIndex + 1, "return-object v$register")
                }
            }
        } ?: throw SetThemeFingerprint.exception

        // Modify the license activity and remove all existing layout code.
        // Must modify an existing activity and cannot add a new activity to the manifest,
        // as that fails for root installations.
        LicenseActivityOnCreateFingerprint.result?.let { result ->
            result.mutableMethod.addInstructions(
                1,
                """
                    invoke-static { p0 }, $ACTIVITY_HOOK_CLASS_DESCRIPTOR->initialize(Landroid/app/Activity;)V
                    return-void
                """
            )

            // Remove other methods as they will break as the onCreate method is modified above.
            result.mutableClass.apply {
                methods.removeIf { it.name != "onCreate" && !MethodUtil.isConstructor(it) }
            }
        } ?: throw LicenseActivityOnCreateFingerprint.exception
    }

    /**
     * Creates an intent to open ReVanced settings.
     */
    fun newIntent(settingsName: String) = IntentPreference.Intent(
        data = settingsName,
        targetClass = "com.google.android.libraries.social.licenses.LicenseActivity"
    ) {
        // The package name change has to be reflected in the intent.
        ChangePackageNamePatch.setOrGetFallbackPackageName("com.google.android.youtube")
    }

    object PreferenceScreen : BasePreferenceScreen() {
        // Screens are sorted in the UI by the screen key.
        // This is done because sorting by title scatters related items apart,
        // and there is no other way to specify an ordering here without refactoring other code.
        // Each screen is bundled only if one or more preference is added during patching.
        val ADS = Screen("revanced_settings_screen_01",
            "revanced_ads_screen_title", null)
        // Alternative thumbnails is item 2
        val LAYOUT_FEED = Screen("revanced_settings_screen_03",
            "revanced_layout_feed_screen_title", null)
        val LAYOUT_PLAYER = Screen("revanced_settings_screen_04",
            "revanced_layout_player_screen_title", null)
        val LAYOUT_OTHER = Screen("revanced_settings_screen_05",
            "revanced_layout_other_screen_title", null)
        val SHORTS = Screen("revanced_settings_screen_06",
            "revanced_shorts_screen_title", null)
        // Use no sorting for the Seekbar because title sorting scatters the custom color preferences.
        // Can use title sorting when PreferenceCategory support is added.
        val SEEKBAR = Screen("revanced_settings_screen_07",
            "revanced_seekbar_screen_title", null, sortStyle = SortStyle.UNSORTED)
        // Swipe controls is item 8
        // RYD is item 9
        // SB is item 10
        val MISC = Screen("revanced_settings_screen_11", "revanced_misc_screen_title", null)
        val VIDEO = Screen("revanced_settings_screen_12", "revanced_video_screen_title", null)

        override fun commit(screen: app.revanced.patches.shared.misc.settings.preference.PreferenceScreen) {
            SettingsResourcePatch += screen
        }
    }

    override fun close() = PreferenceScreen.close()
}
