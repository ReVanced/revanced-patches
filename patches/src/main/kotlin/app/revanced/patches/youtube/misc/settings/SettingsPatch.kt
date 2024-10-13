package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.packagename.setOrGetFallbackPackageName
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.*
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.settingsPatch
import app.revanced.patches.youtube.misc.check.checkEnvironmentPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.asSequence
import app.revanced.util.copyResources
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.util.MethodUtil
import org.w3c.dom.Element

// Used by a fingerprint() from SettingsPatch.
internal var appearanceStringId = -1L
    private set

private val preferences = mutableSetOf<BasePreference>()

fun addSettingPreference(screen: BasePreference) {
    preferences += screen
}

private val settingsResourcePatch = resourcePatch {
    dependsOn(
        resourceMappingPatch,
        settingsPatch(
            rootPreference = IntentPreference(
                titleKey = "revanced_settings_title",
                summaryKey = null,
                intent = newIntent("revanced_settings_intent"),
            ) to "settings_fragment",
            preferences,
        ),
    )

    execute { context ->
        // Used for a fingerprint from SettingsPatch.
        appearanceStringId = resourceMappings["string", "app_theme_appearance_dark"]

        arrayOf(
            ResourceGroup("layout", "revanced_settings_with_toolbar.xml"),
        ).forEach { resourceGroup ->
            context.copyResources("settings", resourceGroup)
        }

        // Remove horizontal divider from the settings Preferences
        // To better match the appearance of the stock YouTube settings.
        context.document["res/values/styles.xml"].use { document ->
            val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

            resourcesNode.childNodes.asSequence().forEach {
                val node = it as? Element ?: return@forEach

                val name = node.getAttribute("name")
                if (name == "Theme.YouTube.Settings" || name == "Theme.YouTube.Settings.Dark") {
                    val listDividerNode = document.createElement("item")
                    listDividerNode.setAttribute("name", "android:listDivider")
                    listDividerNode.appendChild(document.createTextNode("@null"))
                    node.appendChild(listDividerNode)
                }
            }
        }

        // Modify the manifest and add a data intent filter to the LicenseActivity.
        // Some devices freak out if undeclared data is passed to an intent,
        // and this change appears to fix the issue.
        var modifiedIntent = false
        context.document["AndroidManifest.xml"].use { document ->
            // A xml regular-expression would probably work better than this manual searching.
            val manifestNodes = document.getElementsByTagName("manifest").item(0).childNodes
            for (i in 0..manifestNodes.length) {
                val node = manifestNodes.item(i)
                if (node != null && node.nodeName == "application") {
                    val applicationNodes = node.childNodes
                    for (j in 0..applicationNodes.length) {
                        val applicationChild = applicationNodes.item(j)
                        if (applicationChild is Element &&
                            applicationChild.nodeName == "activity" &&
                            applicationChild.getAttribute("android:name") ==
                            "com.google.android.libraries.social.licenses.LicenseActivity"
                        ) {
                            val intentFilter = document.createElement("intent-filter")
                            val mimeType = document.createElement("data")
                            mimeType.setAttribute("android:mimeType", "text/plain")
                            intentFilter.appendChild(mimeType)
                            applicationChild.appendChild(intentFilter)
                            modifiedIntent = true
                            break
                        }
                    }
                }
            }
        }

        if (!modifiedIntent) throw PatchException("Could not modify activity intent")
    }
}

val settingsPatch = bytecodePatch(
    description = "Adds settings for ReVanced to YouTube.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsResourcePatch,
        addResourcesPatch,
        // Currently there is no easy way to make a mandatory patch,
        // so for now this is a dependent of this patch.
        checkEnvironmentPatch,
    )

    val setThemeMatch by setThemeFingerprint()
    val licenseActivityOnCreateMatch by licenseActivityOnCreateFingerprint()

    val extensionPackage = "app/revanced/extension/youtube"
    val activityHookClassDescriptor = "L$extensionPackage/settings/LicenseActivityHook;"

    val themeHelperDescriptor = "L$extensionPackage/ThemeHelper;"
    val setThemeMethodName = "setTheme"

    execute {
        addResources("youtube", "misc.settings.settingsPatch")

        // Add an "about" preference to the top.
        preferences += NonInteractivePreference(
            key = "revanced_settings_screen_00_about",
            summaryKey = null,
            tag = "app.revanced.extension.youtube.settings.preference.ReVancedYouTubeAboutPreference",
            selectable = true,
        )

        PreferenceScreen.MISC.addPreferences(
            TextPreference(
                key = null,
                titleKey = "revanced_pref_import_export_title",
                summaryKey = "revanced_pref_import_export_summary",
                inputType = InputType.TEXT_MULTI_LINE,
                tag = "app.revanced.extension.shared.settings.preference.ImportExportPreference",
            ),
        )

        setThemeMatch.mutableMethod.let { setThemeMethod ->
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
        licenseActivityOnCreateMatch.mutableMethod.addInstructions(
            1,
            """
                invoke-static { p0 }, $activityHookClassDescriptor->initialize(Landroid/app/Activity;)V
                return-void
            """,
        )

        // Remove other methods as they will break as the onCreate method is modified above.
        licenseActivityOnCreateMatch.mutableClass.apply {
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
    setOrGetFallbackPackageName("com.google.android.youtube")
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

    override fun commit(screen: PreferenceScreenPreference) {
        preferences += screen
    }
}
