package app.revanced.patches.music.misc.settings

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.packagename.setOrGetFallbackPackageName
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.*
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.settingsPatch
import app.revanced.patches.youtube.misc.settings.licenseActivityOnCreateFingerprint
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.util.MethodUtil

private const val BASE_ACTIVITY_HOOK_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/settings/BaseActivityHook;"
private const val GOOGLE_API_ACTIVITY_HOOK_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/music/settings/GoogleApiActivityHook;"

private val preferences = mutableSetOf<BasePreference>()


private val settingsResourcePatch = resourcePatch {
    dependsOn(
        resourceMappingPatch,
        settingsPatch(
            IntentPreference(
                titleKey = "revanced_settings_title",
                summaryKey = null,
                intent = newIntent("revanced_settings_intent"),
            ) to "settings_headers",
            preferences
        )
    )

    execute {

        val targetResource = "values/styles.xml"
        inputStreamFromBundledResource(
            "settings/music",
            targetResource,
        )!!.let { inputStream ->
            "resources".copyXmlNode(
                document(inputStream),
                document("res/$targetResource"),
            ).close()
        }

        // Remove horizontal divider from the settings Preferences.
        val styleFile = get("res/values/styles.xml")
        styleFile.writeText(
            styleFile.readText()
                .replace(
                    "allowDividerAbove\">true",
                    "allowDividerAbove\">false"
                ).replace(
                    "allowDividerBelow\">true",
                    "allowDividerBelow\">false"
                )
        )
    }
}

val settingsPatch = bytecodePatch(
    description = "Adds settings for ReVanced to YouTube Music.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsResourcePatch,
        addResourcesPatch,
    )

    execute {
        addResources("music", "misc.settings.settingsPatch")
        addResources("shared", "misc.debugging.enableDebuggingPatch")

        // Should make a separate debugging patch, but for now include it with all installations.
        PreferenceScreen.MISC.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_debug_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_debug"),
                    NonInteractivePreference(
                        "revanced_debug_export_logs_to_clipboard",
                        tag = "app.revanced.extension.shared.settings.preference.ExportLogToClipboardPreference",
                        selectable = true
                    ),
                    NonInteractivePreference(
                        "revanced_debug_logs_clear_buffer",
                        tag = "app.revanced.extension.shared.settings.preference.ClearLogBufferPreference",
                        selectable = true
                    )
                )
            )
        )

        // Add an "About" preference to the top.
        preferences += NonInteractivePreference(
            key = "revanced_settings_music_screen_0_about",
            summaryKey = null,
            tag = "app.revanced.extension.shared.settings.preference.ReVancedAboutPreference",
            selectable = true,
        )

        // Modify GoogleApiActivity and remove all existing layout code.
        // Must modify an existing activity and cannot add a new activity to the manifest,
        // as that fails for root installations.

        googleApiActivityFingerprint.method.addInstructions(
            1,
            """
                invoke-static { }, $GOOGLE_API_ACTIVITY_HOOK_CLASS_DESCRIPTOR->createInstance()Lapp/revanced/extension/music/settings/GoogleApiActivityHook;
                move-result-object v0
                invoke-static { v0, p0 }, $BASE_ACTIVITY_HOOK_CLASS_DESCRIPTOR->initialize(Lapp/revanced/extension/shared/settings/BaseActivityHook;Landroid/app/Activity;)V
                return-void
            """
        )

        // Remove other methods as they will break as the onCreate method is modified above.
        googleApiActivityFingerprint.classDef.apply {
            methods.removeIf { it.name != "onCreate" && !MethodUtil.isConstructor(it) }
        }

        googleApiActivityFingerprint.classDef.apply {
            // Override finish() to intercept back gesture.
            ImmutableMethod(
                type,
                "finish",
                emptyList(),
                "V",
                AccessFlags.PUBLIC.value,
                null,
                null,
                MutableMethodImplementation(3),
            ).toMutable().apply {
                addInstructions(
                    """
                    invoke-static {}, Lapp/revanced/extension/music/settings/GoogleApiActivityHook;->handleBackPress()Z
                    move-result v0
                    if-nez v0, :search_handled
                    invoke-super { p0 }, Landroid/app/Activity;->finish()V
                    return-void
                    :search_handled
                    return-void
                """
                )
            }.let(methods::add)
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
    targetClass = "com.google.android.gms.common.api.GoogleApiActivity"
) {
    // The package name change has to be reflected in the intent.
    setOrGetFallbackPackageName("com.google.android.apps.youtube.music")
}

object PreferenceScreen : BasePreferenceScreen() {
    val ADS = Screen(
        "revanced_settings_music_screen_1_ads",
        summaryKey = null
    )
    val GENERAL = Screen(
        "revanced_settings_music_screen_2_general",
        summaryKey = null
    )
    val PLAYER = Screen(
        "revanced_settings_music_screen_3_player",
        summaryKey = null
    )
    val MISC = Screen(
        "revanced_settings_music_screen_4_misc",
        summaryKey = null
    )

    override fun commit(screen: PreferenceScreenPreference) {
        preferences += screen
    }
}
