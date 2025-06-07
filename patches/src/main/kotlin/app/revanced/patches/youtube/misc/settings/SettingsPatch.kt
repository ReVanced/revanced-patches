package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
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
import app.revanced.patches.youtube.misc.fix.playbackspeed.fixPlaybackSpeedWhilePlayingPatch
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.util.MethodUtil

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
            listOf(
                IntentPreference(
                    titleKey = "revanced_settings_title",
                    summaryKey = null,
                    intent = newIntent("revanced_settings_intent"),
                ) to "settings_fragment",
                PreferenceCategory(
                    titleKey = "revanced_settings_title",
                    layout = "@layout/preference_group_title",
                    preferences = setOf(
                        IntentPreference(
                            titleKey = "revanced_settings_submenu_title",
                            summaryKey = null,
                            icon = "@drawable/revanced_settings_icon",
                            layout = "@layout/preference_with_icon",
                            intent = newIntent("revanced_settings_intent"),
                        )
                    )
                ) to "settings_fragment_cairo",
            ),
            preferences
        )
    )

    execute {
        // Used for a fingerprint from SettingsPatch.
        appearanceStringId = resourceMappings["string", "app_theme_appearance_dark"]

        arrayOf(
            ResourceGroup("drawable",
                "revanced_settings_circle_background.xml",
                "revanced_settings_cursor.xml",
                "revanced_settings_custom_checkmark.xml",
                "revanced_settings_icon.xml",
                "revanced_settings_screen_00_about.xml",
                "revanced_settings_screen_01_ads.xml",
                "revanced_settings_screen_02_alt_thumbnails.xml",
                "revanced_settings_screen_03_feed.xml",
                "revanced_settings_screen_04_general.xml",
                "revanced_settings_screen_05_player.xml",
                "revanced_settings_screen_06_shorts.xml",
                "revanced_settings_screen_07_seekbar.xml",
                "revanced_settings_screen_08_swipe_controls.xml",
                "revanced_settings_screen_09_return_youtube_dislike.xml",
                "revanced_settings_screen_10_sponsorblock.xml",
                "revanced_settings_screen_11_misc.xml",
                "revanced_settings_screen_12_video.xml",
            ),
            ResourceGroup("layout",
                "revanced_color_dot_widget.xml",
                "revanced_color_picker.xml",
                "revanced_custom_list_item_checked.xml",
                "revanced_preference_with_icon_no_search_result.xml",
                "revanced_search_suggestion_item.xml",
                "revanced_settings_with_toolbar.xml"),
            ResourceGroup("menu", "revanced_search_menu.xml")
        ).forEach { resourceGroup ->
            copyResources("settings", resourceGroup)
        }

        // Copy style properties used to fix over-sized copy menu that appear in EditTextPreference.
        // For a full explanation of how this fixes the issue, see the comments in this style file
        // and the comments in the extension code.
        val targetResource = "values/styles.xml"
        inputStreamFromBundledResource(
            "settings/host",
            targetResource,
        )!!.let { inputStream ->
            "resources".copyXmlNode(
                document(inputStream),
                document("res/$targetResource"),
            ).close()
        }

        // Remove horizontal divider from the settings Preferences
        // To better match the appearance of the stock YouTube settings.
        document("res/values/styles.xml").use { document ->
            val childNodes = document.childNodes

            arrayOf(
                "Theme.YouTube.Settings",
                "Theme.YouTube.Settings.Dark",
            ).forEach { value ->
                val listDividerNode = document.createElement("item")
                listDividerNode.setAttribute("name", "android:listDivider")
                listDividerNode.appendChild(document.createTextNode("@null"))

                childNodes.findElementByAttributeValueOrThrow(
                    "name",
                    value,
                ).appendChild(listDividerNode)
            }
        }

        // Modify the manifest and add a data intent filter to the LicenseActivity.
        // Some devices freak out if undeclared data is passed to an intent,
        // and this change appears to fix the issue.
        document("AndroidManifest.xml").use { document ->
            val licenseElement = document.childNodes.findElementByAttributeValueOrThrow(
                "android:name",
                "com.google.android.libraries.social.licenses.LicenseActivity",
            )

            val mimeType = document.createElement("data")
            mimeType.setAttribute("android:mimeType", "text/plain")

            val intentFilter = document.createElement("intent-filter")
            intentFilter.appendChild(mimeType)

            licenseElement.appendChild(intentFilter)
        }
    }
}

val settingsPatch = bytecodePatch(
    description = "Adds settings for ReVanced to YouTube.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsResourcePatch,
        addResourcesPatch,
        versionCheckPatch,
        fixPlaybackSpeedWhilePlayingPatch,
        // Currently there is no easy way to make a mandatory patch,
        // so for now this is a dependent of this patch.
        checkEnvironmentPatch,
    )

    val extensionPackage = "app/revanced/extension/youtube"
    val activityHookClassDescriptor = "L$extensionPackage/settings/LicenseActivityHook;"

    val themeHelperDescriptor = "L$extensionPackage/ThemeHelper;"
    val setThemeMethodName = "setTheme"

    execute {
        addResources("youtube", "misc.settings.settingsPatch")

        // Add an "about" preference to the top.
        preferences += NonInteractivePreference(
            key = "revanced_settings_screen_00_about",
            icon = "@drawable/revanced_settings_screen_00_about",
            layout = "@layout/preference_with_icon",
            summaryKey = null,
            tag = "app.revanced.extension.youtube.settings.preference.ReVancedYouTubeAboutPreference",
            selectable = true,
        )

        if (is_19_34_or_greater) {
            PreferenceScreen.GENERAL_LAYOUT.addPreferences(
                SwitchPreference("revanced_restore_old_settings_menus")
            )
        }

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_settings_search_history"),
            SwitchPreference("revanced_show_menu_icons")
        )

        PreferenceScreen.MISC.addPreferences(
            TextPreference(
                key = null,
                titleKey = "revanced_pref_import_export_title",
                summaryKey = "revanced_pref_import_export_summary",
                inputType = InputType.TEXT_MULTI_LINE,
                tag = "app.revanced.extension.shared.settings.preference.ImportExportPreference",
            ),
            ListPreference(
                key = "revanced_language",
                summaryKey = null,
                tag = "app.revanced.extension.shared.settings.preference.SortedListPreference"
            )
        )

        setThemeFingerprint.method.let { setThemeMethod ->
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

        licenseActivityOnCreateFingerprint.method.addInstructions(
            1,
            """
                invoke-static { p0 }, $activityHookClassDescriptor->initialize(Landroid/app/Activity;)V
                return-void
            """,
        )

        // Remove other methods as they will break as the onCreate method is modified above.
        licenseActivityOnCreateFingerprint.classDef.apply {
            methods.removeIf { it.name != "onCreate" && !MethodUtil.isConstructor(it) }
        }

        // Add context override to force a specific settings language.
        licenseActivityOnCreateFingerprint.classDef.apply {
            val attachBaseContext = ImmutableMethod(
                type,
                "attachBaseContext",
                listOf(ImmutableMethodParameter("Landroid/content/Context;", null, null)),
                "V",
                AccessFlags.PROTECTED.value,
                null,
                null,
                MutableMethodImplementation(3),
            ).toMutable().apply {
                addInstructions(
                    """
                        invoke-static { p1 }, $activityHookClassDescriptor->getAttachBaseContext(Landroid/content/Context;)Landroid/content/Context;
                        move-result-object p1
                        invoke-super { p0, p1 }, $superclass->attachBaseContext(Landroid/content/Context;)V
                        return-void
                    """
                )
            }

            methods.add(attachBaseContext)
        }

        // Add setting to force cairo settings fragment on/off.
        cairoFragmentConfigFingerprint.method.insertLiteralOverride(
            CAIRO_CONFIG_LITERAL_VALUE,
            "$activityHookClassDescriptor->useCairoSettingsFragment(Z)Z"
        )
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
        icon = "@drawable/revanced_settings_screen_01_ads",
        layout = "@layout/preference_with_icon",
    )
    val ALTERNATIVE_THUMBNAILS = Screen(
        key = "revanced_settings_screen_02_alt_thumbnails",
        summaryKey = null,
        icon = "@drawable/revanced_settings_screen_02_alt_thumbnails",
        layout = "@layout/preference_with_icon",
        sorting = Sorting.UNSORTED,
    )
    val FEED = Screen(
        key = "revanced_settings_screen_03_feed",
        summaryKey = null,
        icon = "@drawable/revanced_settings_screen_03_feed",
        layout = "@layout/preference_with_icon",
    )
    val GENERAL_LAYOUT = Screen(
        key = "revanced_settings_screen_04_general",
        summaryKey = null,
        icon = "@drawable/revanced_settings_screen_04_general",
        layout = "@layout/preference_with_icon",
    )
    val PLAYER = Screen(
        key = "revanced_settings_screen_05_player",
        summaryKey = null,
        icon = "@drawable/revanced_settings_screen_05_player",
        layout = "@layout/preference_with_icon",
    )

    val SHORTS = Screen(
        key = "revanced_settings_screen_06_shorts",
        summaryKey = null,
        icon = "@drawable/revanced_settings_screen_06_shorts",
        layout = "@layout/preference_with_icon",
    )

    val SEEKBAR = Screen(
        key = "revanced_settings_screen_07_seekbar",
        summaryKey = null,
        icon = "@drawable/revanced_settings_screen_07_seekbar",
        layout = "@layout/preference_with_icon",
        )
    val SWIPE_CONTROLS = Screen(
        key = "revanced_settings_screen_08_swipe_controls",
        summaryKey = null,
        icon = "@drawable/revanced_settings_screen_08_swipe_controls",
        layout = "@layout/preference_with_icon",
        sorting = Sorting.UNSORTED,
    )
    val RETURN_YOUTUBE_DISLIKE = Screen(
        key = "revanced_settings_screen_09_return_youtube_dislike",
        summaryKey = null,
        icon = "@drawable/revanced_settings_screen_09_return_youtube_dislike",
        layout = "@layout/preference_with_icon",
        sorting = Sorting.UNSORTED,
    )
    val SPONSORBLOCK = Screen(
        key = "revanced_settings_screen_10_sponsorblock",
        summaryKey = null,
        icon = "@drawable/revanced_settings_screen_10_sponsorblock",
        layout = "@layout/preference_with_icon",
        sorting = Sorting.UNSORTED,
    )
    val MISC = Screen(
        key = "revanced_settings_screen_11_misc",
        summaryKey = null,
        icon = "@drawable/revanced_settings_screen_11_misc",
        layout = "@layout/preference_with_icon",
    )
    val VIDEO = Screen(
        key = "revanced_settings_screen_12_video",
        summaryKey = null,
        icon = "@drawable/revanced_settings_screen_12_video",
        layout = "@layout/preference_with_icon",
        sorting = Sorting.BY_KEY,
    )

    override fun commit(screen: PreferenceScreenPreference) {
        preferences += screen
    }
}
