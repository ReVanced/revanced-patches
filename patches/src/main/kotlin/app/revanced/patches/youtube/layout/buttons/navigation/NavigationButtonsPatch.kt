package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.navigation.hookNavigationButtonCreated
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.sun.org.apache.bcel.internal.generic.InstructionConst.getInstruction

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/NavigationButtonsPatch;"

val navigationButtonsPatch = bytecodePatch(
    name = "Navigation buttons",
    description = "Adds options to hide and change navigation buttons (such as the Shorts button).",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        navigationBarHookPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
        ),
    )

    execute {
        addResources("youtube", "layout.buttons.navigation.navigationButtonsPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_navigation_buttons_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_hide_home_button"),
                    SwitchPreference("revanced_hide_shorts_button"),
                    SwitchPreference("revanced_hide_create_button"),
                    SwitchPreference("revanced_hide_subscriptions_button"),
                    SwitchPreference("revanced_switch_create_with_notifications_button"),
                    SwitchPreference("revanced_hide_navigation_button_labels"),
                ),
            ),
        )

        // Switch create with notifications button.
        addCreateButtonViewFingerprint.method().apply {
            val stringIndex = addCreateButtonViewFingerprint.stringMatches()!!.find { match ->
                match.string == ANDROID_AUTOMOTIVE_STRING
            }!!.index

            val conditionalCheckIndex = stringIndex - 1
            val conditionRegister =
                getInstruction<OneRegisterInstruction>(conditionalCheckIndex).registerA

            addInstructions(
                conditionalCheckIndex,
                """
                    invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->switchCreateWithNotificationButton()Z
                    move-result v$conditionRegister
                """,
            )
        }

        // Hide navigation button labels.
        createPivotBarFingerprint.method().apply {
            val setTextIndex = indexOfFirstInstructionOrThrow {
                getReference<MethodReference>()?.name == "setText"
            }

            val targetRegister = getInstruction<FiveRegisterInstruction>(setTextIndex).registerC

            addInstruction(
                setTextIndex,
                "invoke-static { v$targetRegister }, " +
                    "$EXTENSION_CLASS_DESCRIPTOR->hideNavigationButtonLabels(Landroid/widget/TextView;)V",
            )
        }

        // Hook navigation button created, in order to hide them.
        hookNavigationButtonCreated(EXTENSION_CLASS_DESCRIPTOR)
    }
}
