package app.revanced.patches.youtube.interaction.swipecontrols

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_43_or_greater
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityFingerprint
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/swipecontrols/SwipeControlsHostActivity;"

private val swipeControlsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "interaction.swipecontrols.swipeControlsResourcePatch")

        if (is_19_25_or_greater) {
            PreferenceScreen.SWIPE_CONTROLS.addPreferences(
                SwitchPreference("revanced_swipe_change_video")
            )
        }

        PreferenceScreen.SWIPE_CONTROLS.addPreferences(
            SwitchPreference("revanced_swipe_brightness"),
            SwitchPreference("revanced_swipe_volume"),
            SwitchPreference("revanced_swipe_press_to_engage"),
            SwitchPreference("revanced_swipe_haptic_feedback"),
            SwitchPreference("revanced_swipe_save_and_restore_brightness"),
            SwitchPreference("revanced_swipe_lowest_value_enable_auto_brightness"),
            TextPreference("revanced_swipe_overlay_timeout", inputType = InputType.NUMBER),
            TextPreference("revanced_swipe_text_overlay_size", inputType = InputType.NUMBER),
            TextPreference("revanced_swipe_overlay_background_alpha", inputType = InputType.NUMBER),
            TextPreference("revanced_swipe_threshold", inputType = InputType.NUMBER),
        )

        copyResources(
            "swipecontrols",
            ResourceGroup(
                "drawable",
                "revanced_ic_sc_brightness_auto.xml",
                "revanced_ic_sc_brightness_manual.xml",
                "revanced_ic_sc_volume_mute.xml",
                "revanced_ic_sc_volume_normal.xml",
            ),
        )
    }
}

@Suppress("unused")
val swipeControlsPatch = bytecodePatch(
    name = "Swipe controls",
    description = "Adds options to enable and configure volume and brightness swipe controls.",
) {
    dependsOn(
        sharedExtensionPatch,
        playerTypeHookPatch,
        swipeControlsResourcePatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        val wrapperClass = swipeControlsHostActivityFingerprint.classDef
        val targetClass = mainActivityFingerprint.classDef

        // Inject the wrapper class from the extension into the class hierarchy of MainActivity.
        wrapperClass.setSuperClass(targetClass.superclass)
        targetClass.setSuperClass(wrapperClass.type)

        // Ensure all classes and methods in the hierarchy are non-final, so we can override them in the extension.
        traverseClassHierarchy(targetClass) {
            accessFlags = accessFlags and AccessFlags.FINAL.value.inv()
            transformMethods {
                ImmutableMethod(
                    definingClass,
                    name,
                    parameters,
                    returnType,
                    accessFlags and AccessFlags.FINAL.value.inv(),
                    annotations,
                    hiddenApiRestrictions,
                    implementation,
                ).toMutable()
            }
        }

        // region patch to enable/disable swipe to change video.

        if (is_19_43_or_greater) {
            swipeChangeVideoFingerprint.method.insertFeatureFlagBooleanOverride(
                SWIPE_CHANGE_VIDEO_FEATURE_FLAG,
                "$EXTENSION_CLASS_DESCRIPTOR->allowSwipeChangeVideo(Z)Z"
            )
        }

        // endregion
    }
}
