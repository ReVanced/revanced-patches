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
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityFingerprint
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.transformMethods
import app.revanced.util.traverseClassHierarchy
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

private val swipeControlsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        addResourcesPatch,
    )

    execute { context ->
        addResources("youtube", "interaction.swipecontrols.swipeControlsResourcePatch")

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

        context.copyResources(
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
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val mainActivityMatch by mainActivityFingerprint()
    val swipeControlsHostActivityMatch by swipeControlsHostActivityFingerprint()

    execute { context ->
        val wrapperClass = swipeControlsHostActivityMatch.mutableClass
        val targetClass = mainActivityMatch.mutableClass

        // Inject the wrapper class from the extension into the class hierarchy of MainActivity.
        wrapperClass.setSuperClass(targetClass.superclass)
        targetClass.setSuperClass(wrapperClass.type)

        // Ensure all classes and methods in the hierarchy are non-final, so we can override them in the extension.
        context.traverseClassHierarchy(targetClass) {
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
    }
}
