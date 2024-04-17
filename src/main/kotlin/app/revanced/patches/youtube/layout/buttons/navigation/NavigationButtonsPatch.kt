package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.ANDROID_AUTOMOTIVE_STRING
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.AddCreateButtonViewFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.navigation.NavigationBarHookPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Navigation buttons",
    description = "Adds options to hide and change navigation buttons (such as the Shorts button).",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        NavigationBarHookPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                "19.11.43"
            ],
        ),
    ],
)
@Suppress("unused")
object NavigationButtonsPatch : BytecodePatch(
    setOf(AddCreateButtonViewFingerprint),
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/NavigationButtonsPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            PreferenceScreen(
                key = "revanced_navigation_buttons_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_hide_home_button"),
                    SwitchPreference("revanced_hide_shorts_button"),
                    SwitchPreference("revanced_hide_create_button"),
                    SwitchPreference("revanced_hide_subscriptions_button"),
                    SwitchPreference("revanced_switch_create_with_notifications_button"),
                ),
            ),
        )

        // Switch create with notifications button.
        AddCreateButtonViewFingerprint.result?.let {
            it.mutableMethod.apply {
                val stringIndex = it.scanResult.stringsScanResult!!.matches.find { match ->
                    match.string == ANDROID_AUTOMOTIVE_STRING
                }!!.index

                val conditionalCheckIndex = stringIndex - 1
                val conditionRegister =
                    getInstruction<OneRegisterInstruction>(conditionalCheckIndex).registerA

                addInstructions(
                    conditionalCheckIndex,
                    """
                        invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->switchCreateWithNotificationButton()Z
                        move-result v$conditionRegister
                    """,
                )
            }
        } ?: throw AddCreateButtonViewFingerprint.exception

        // Hook navigation button created, in order to hide them.
        NavigationBarHookPatch.hookNavigationButtonCreated(INTEGRATIONS_CLASS_DESCRIPTOR)
    }
}
