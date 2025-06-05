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
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_15_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.insertLiteralOverride
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
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
        versionCheckPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
        )
    )

    execute {
        addResources("youtube", "layout.buttons.navigation.navigationButtonsPatch")

        val preferences = mutableSetOf(
            SwitchPreference("revanced_hide_home_button"),
            SwitchPreference("revanced_hide_shorts_button"),
            SwitchPreference("revanced_hide_create_button"),
            SwitchPreference("revanced_hide_subscriptions_button"),
            SwitchPreference("revanced_hide_notifications_button"),
            SwitchPreference("revanced_switch_create_with_notifications_button"),
            SwitchPreference("revanced_hide_navigation_button_labels"),
        )

        if (is_19_25_or_greater) {
            preferences += SwitchPreference("revanced_disable_translucent_navigation_bar_light")
            preferences += SwitchPreference("revanced_disable_translucent_navigation_bar_dark")

            PreferenceScreen.GENERAL_LAYOUT.addPreferences(
                SwitchPreference("revanced_disable_translucent_status_bar")
            )
        }

        if (is_20_15_or_greater) {
            PreferenceScreen.GENERAL_LAYOUT.addPreferences(
                SwitchPreference("revanced_navigation_bar_animations")
            )
        }

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_navigation_buttons_screen",
                sorting = Sorting.UNSORTED,
                preferences = preferences
            )
        )

        // Switch create with notifications button.
        addCreateButtonViewFingerprint.let {
            it.method.apply {
                val conditionalCheckIndex = it.instructionMatches[1].index
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
        }

        // Hide navigation button labels.
        createPivotBarFingerprint.let {
            it.method.apply {
                val setTextIndex = it.instructionMatches.first().index
                val targetRegister = getInstruction<FiveRegisterInstruction>(setTextIndex).registerC

                addInstruction(
                    setTextIndex,
                    "invoke-static { v$targetRegister }, " +
                            "$EXTENSION_CLASS_DESCRIPTOR->hideNavigationButtonLabels(Landroid/widget/TextView;)V",
                )
            }
        }

        // Hook navigation button created, in order to hide them.
        hookNavigationButtonCreated(EXTENSION_CLASS_DESCRIPTOR)

        // Force on/off translucent effect on status bar and navigation buttons.
        if (is_19_25_or_greater) {
            translucentNavigationStatusBarFeatureFlagFingerprint.let {
                it.method.insertLiteralOverride(
                    it.instructionMatches.first().index,
                    "$EXTENSION_CLASS_DESCRIPTOR->useTranslucentNavigationStatusBar(Z)Z",
                )
            }

            translucentNavigationButtonsFeatureFlagFingerprint.let {
                it.method.insertLiteralOverride(
                    it.instructionMatches.first().index,
                    "$EXTENSION_CLASS_DESCRIPTOR->useTranslucentNavigationButtons(Z)Z",
                )
            }

            translucentNavigationButtonsSystemFeatureFlagFingerprint.let {
                it.method.insertLiteralOverride(
                    it.instructionMatches.first().index,
                    "$EXTENSION_CLASS_DESCRIPTOR->useTranslucentNavigationButtons(Z)Z",
                )
            }
        }

        if (is_20_15_or_greater) {
            animatedNavigationTabsFeatureFlagFingerprint.let {
                it.method.insertLiteralOverride(
                    it.instructionMatches.first().index,
                    "$EXTENSION_CLASS_DESCRIPTOR->useAnimatedNavigationButtons(Z)Z"
                )
            }
        }
    }
}
