package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.packagename.ChangePackageNamePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.*
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preferences
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.settings.fingerprints.licenseActivityOnCreateFingerprint
import app.revanced.patches.youtube.misc.settings.fingerprints.setThemeFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.util.MethodUtil

val settingsPatch = bytecodePatch(
    description = "Adds settings for ReVanced to YouTube.",
) {
    dependsOn(
        integrationsPatch,
        settingsResourcePatch,
        addResourcesPatch,
    )

    val setThemeResult by setThemeFingerprint
    val licenseActivityOnCreateResult by licenseActivityOnCreateFingerprint

    val integrationsPackage = "app/revanced/integrations/youtube"
    val activityHookClassDescriptor = "L$integrationsPackage/settings/LicenseActivityHook;"

    val themeHelperDescriptor = "L$integrationsPackage/ThemeHelper;"
    val setThemeMethodName = "setTheme"

    execute {
        addResources(this)

        // Add an "about" preference to the top.
        preferences += NonInteractivePreference(
            key = "revanced_settings_screen_00_about",
            summaryKey = null,
            tag = "app.revanced.integrations.youtube.settings.preference.ReVancedYouTubeAboutPreference",
            selectable = true,
        )

        PreferenceScreen.MISC.addPreferences(
            TextPreference(
                key = null,
                titleKey = "revanced_pref_import_export_title",
                summaryKey = "revanced_pref_import_export_summary",
                inputType = InputType.TEXT_MULTI_LINE,
                tag = "app.revanced.integrations.shared.settings.preference.ImportExportPreference",
            ),
        )

        setThemeResult.mutableMethod.let { setThemeMethod ->
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
                            "$themeHelperDescriptor->$setThemeMethodName(Ljava/lang/Enum;)V",
                    )
                    addInstruction(returnIndex + 1, "return-object v$register")
                }
            }
        }

        // Modify the license activity and remove all existing layout code.
        // Must modify an existing activity and cannot add a new activity to the manifest,
        // as that fails for root installations.
        licenseActivityOnCreateResult.mutableMethod.addInstructions(
            1,
            """
                invoke-static { p0 }, $activityHookClassDescriptor->initialize(Landroid/app/Activity;)V
                return-void
            """,
        )

        // Remove other methods as they will break as the onCreate method is modified above.
        licenseActivityOnCreateResult.mutableClass.apply {
            methods.removeIf { it.name != "onCreate" && !MethodUtil.isConstructor(it) }
        }
    }

    finalize {
        PreferenceScreen.close()
    }
}

/**
 * Creates an intent to open ReVanced settings.
 */
fun newIntent(settingsName: String) = IntentPreference.Intent(
    data = settingsName,
    targetClass = "com.google.android.libraries.social.licenses.LicenseActivity",
) {
    // The package name change has to be reflected in the intent.
    ChangePackageNamePatch.setOrGetFallbackPackageName("com.google.android.youtube")
}

object PreferenceScreen : BasePreferenceScreen() {
    // Sort screens in the root menu by key, to not scatter related items apart
    // (sorting key is set in revanced_prefs.xml).
    // If no preferences are added to a screen, the screen will not be added to the settings.
    val ADS = Screen(
        key = "revanced_settings_screen_01_ads",
        summaryKey = null,
    )
    val ALTERNATIVE_THUMBNAILS = Screen(
        key = "revanced_settings_screen_02_alt_thumbnails",
        summaryKey = null,
        sorting = Sorting.UNSORTED,
    )
    val FEED = Screen(
        key = "revanced_settings_screen_03_feed",
        summaryKey = null,
    )
    val PLAYER = Screen(
        key = "revanced_settings_screen_04_player",
        summaryKey = null,
    )
    val GENERAL_LAYOUT = Screen(
        key = "revanced_settings_screen_05_general",
        summaryKey = null,
    )

    // Don't sort, as related preferences are scattered apart.
    // Can use title sorting after PreferenceCategory support is added.
    val SHORTS = Screen(
        key = "revanced_settings_screen_06_shorts",
        summaryKey = null,
        sorting = Sorting.UNSORTED,
    )

    // Don't sort, because title sorting scatters the custom color preferences.
    val SEEKBAR = Screen(
        key = "revanced_settings_screen_07_seekbar",
        summaryKey = null,
        sorting = Sorting.UNSORTED,
    )
    val SWIPE_CONTROLS = Screen(
        key = "revanced_settings_screen_08_swipe_controls",
        summaryKey = null,
        sorting = Sorting.UNSORTED,
    )

    // RYD and SB are items 9 and 10.
    // Menus are added in their own patch because they use an Intent and not a Screen.

    val MISC = Screen(
        key = "revanced_settings_screen_11_misc",
        summaryKey = null,
    )
    val VIDEO = Screen(
        key = "revanced_settings_screen_12_video",
        summaryKey = null,
    )

    override fun commit(screen: app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference) {
        preferences += screen
    }
}
