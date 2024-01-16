package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.*
import app.revanced.patches.youtube.layout.buttons.navigation.utils.InjectionUtils.REGISTER_TEMPLATE_REPLACEMENT
import app.revanced.patches.youtube.layout.buttons.navigation.utils.InjectionUtils.injectHook
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Navigation buttons",
    description = "Adds options to hide and change navigation buttons (such as the Shorts button).",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        ResolvePivotBarFingerprintsPatch::class
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
                "19.01.34"
            ]
        )
    ]
)
@Suppress("unused")
object NavigationButtonsPatch : BytecodePatch(
    setOf(AddCreateButtonViewFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/NavigationButtonsPatch;"

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_navigation_buttons_preference_screen",
                StringResource("revanced_navigation_buttons_preference_screen_title", "Navigation buttons"),
                listOf(
                    SwitchPreference(
                        "revanced_hide_home_button",
                        StringResource("revanced_hide_home_button_title", "Hide home button"),
                        StringResource("revanced_hide_home_button_summary_on", "Home button is hidden"),
                        StringResource("revanced_hide_home_button_summary_off", "Home button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_button",
                        StringResource("revanced_hide_shorts_button_title", "Hide Shorts button"),
                        StringResource("revanced_hide_shorts_button_summary_on", "Shorts button is hidden"),
                        StringResource("revanced_hide_shorts_button_summary_off", "Shorts button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_subscriptions_button",
                        StringResource("revanced_hide_subscriptions_button_title", "Hide subscriptions button"),
                        StringResource(
                            "revanced_hide_subscriptions_button_summary_on",
                            "Home subscriptions is hidden"
                        ),
                        StringResource("revanced_hide_subscriptions_button_summary_off", "Home subscriptions is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_create_button",
                        StringResource("revanced_hide_create_button_title", "Hide create button"),
                        StringResource("revanced_hide_create_button_summary_on", "Create button is hidden"),
                        StringResource("revanced_hide_create_button_summary_off", "Create button is shown")
                    ),
                    SwitchPreference(
                        "revanced_switch_create_with_notifications_button",
                        StringResource(
                            "revanced_switch_create_with_notifications_button_title",
                            "Switch create with notifications button"
                        ),
                        StringResource(
                            "revanced_switch_create_with_notifications_button_summary_on",
                            "Create button is switched with notifications"
                        ),
                        StringResource(
                            "revanced_switch_create_with_notifications_button_summary_off",
                            "Create button is not switched with notifications"
                        ),
                    ),
                ),
                StringResource(
                    "revanced_navigation_buttons_preference_screen_summary",
                    "Hide or change buttons in the navigation bar"
                )
            )
        )

        /*
         * Resolve fingerprints
         */

        val initializeButtonsResult = InitializeButtonsFingerprint.result!!

        val fingerprintResults =
            arrayOf(PivotBarEnumFingerprint, PivotBarButtonsViewFingerprint)
                .onEach {
                    if (!it.resolve(
                            context,
                            initializeButtonsResult.mutableMethod,
                            initializeButtonsResult.mutableClass
                        )
                    )
                        throw it.exception
                }
                .map { it.result!!.scanResult.patternScanResult!! }


        val enumScanResult = fingerprintResults[0]
        val buttonViewResult = fingerprintResults[1]

        val enumHookInsertIndex = enumScanResult.startIndex + 2
        val buttonHookInsertIndex = buttonViewResult.endIndex

        /*
         * Inject hooks
         */

        val enumHook = "sput-object v$REGISTER_TEMPLATE_REPLACEMENT, " +
                "$INTEGRATIONS_CLASS_DESCRIPTOR->lastNavigationButton:Ljava/lang/Enum;"
        val buttonHook = "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, " +
                "$INTEGRATIONS_CLASS_DESCRIPTOR->hideButton(Landroid/view/View;)V"

        // Inject bottom to top to not mess up the indices
        mapOf(
            buttonHook to buttonHookInsertIndex,
            enumHook to enumHookInsertIndex
        ).forEach { (hook, insertIndex) ->
            initializeButtonsResult.mutableMethod.injectHook(hook, insertIndex)
        }

        /*
         * Hide create or switch it with notifications buttons.
         */

        AddCreateButtonViewFingerprint.result?.let {
            it.mutableMethod.apply {
                val stringIndex = it.scanResult.stringsScanResult!!.matches.find {
                        match -> match.string == ANDROID_AUTOMOTIVE_STRING
                }!!.index

                val conditionalCheckIndex = stringIndex - 1
                val conditionRegister = getInstruction<OneRegisterInstruction>(conditionalCheckIndex).registerA

                addInstructions(
                    conditionalCheckIndex,
                    """
                        invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->switchCreateWithNotificationButton()Z
                        move-result v$conditionRegister
                    """
                )
            }
        } ?: throw AddCreateButtonViewFingerprint.exception

        /*
         * Resolve fingerprints
         */

        InitializeButtonsFingerprint.result!!.let {
            if (!PivotBarCreateButtonViewFingerprint.resolve(context, it.mutableMethod, it.mutableClass))
                throw PivotBarCreateButtonViewFingerprint.exception
        }

        PivotBarCreateButtonViewFingerprint.result!!.apply {
            val insertIndex = scanResult.patternScanResult!!.endIndex

            /*
             * Inject hooks
             */
            val hook = "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, " +
                    "$INTEGRATIONS_CLASS_DESCRIPTOR->hideCreateButton(Landroid/view/View;)V"

            mutableMethod.injectHook(hook, insertIndex)
        }
    }
}