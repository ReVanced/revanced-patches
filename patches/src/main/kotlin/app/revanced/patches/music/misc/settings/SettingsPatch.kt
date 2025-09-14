package app.revanced.patches.music.misc.settings

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.packagename.setOrGetFallbackPackageName
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.*
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.settingsPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.util.MethodUtil

private val screens = mutableSetOf<BasePreference>()


private val settingsResourcePatch = resourcePatch {
    dependsOn(
        resourceMappingPatch,
        settingsPatch(
            IntentPreference(
                titleKey = "revanced_settings_title",
                summaryKey = null,
                intent = newIntent("revanced_settings_intent"),
            ) to "settings_headers",
            screens
        )
    )

    execute {

        copyResources(
            "settings",
            ResourceGroup(
                "layout",
                "revanced_music_settings_with_toolbar.xml"
            )
        )
    }
}

val settingsPatch = bytecodePatch(
    name = "Settings",
    description = "Adds settings for ReVanced to YouTube Music.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsResourcePatch,
        addResourcesPatch,
    )

    val extensionPackage = "app/revanced/extension/music"
    val activityHookClassDescriptor = "L$extensionPackage/settings/GoogleApiActivityHook;"

    execute {
        addResources("music", "misc.settings.settingsPatch")

        // Modify the license activity and remove all existing layout code.
        // Must modify an existing activity and cannot add a new activity to the manifest,
        // as that fails for root installations.

        googleApiActivityFingerprint.method.addInstructions(
            1,
            """
                invoke-static { p0 }, $activityHookClassDescriptor->initialize(Landroid/app/Activity;)V
                return-void
            """,
        )

        // Remove other methods as they will break as the onCreate method is modified above.
        googleApiActivityFingerprint.classDef.apply {
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
    targetClass = "com.google.android.gms.common.api.GoogleApiActivity"
) {
    // The package name change has to be reflected in the intent.
    setOrGetFallbackPackageName("com.google.android.apps.youtube.music")
}

object PreferenceScreen : BasePreferenceScreen() {
    // TODO: For test. Chance this to real.
    val MISC = Screen(
        "revanced_settings_screen_1_misc",
        summaryKey = null
    )

    override fun commit(screen: PreferenceScreenPreference) {
        screens += screen
    }
}
