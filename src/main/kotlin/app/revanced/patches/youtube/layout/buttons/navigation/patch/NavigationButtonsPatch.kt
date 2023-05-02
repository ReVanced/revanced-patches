package app.revanced.patches.youtube.layout.buttons.navigation.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.buttons.navigation.annotations.NavigationButtonsCompatibility
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.*
import app.revanced.patches.youtube.layout.buttons.navigation.utils.InjectionUtils.REGISTER_TEMPLATE_REPLACEMENT
import app.revanced.patches.youtube.layout.buttons.navigation.utils.InjectionUtils.injectHook
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn(
    [
        IntegrationsPatch::class,
        SettingsPatch::class,
        ResolvePivotBarFingerprintsPatch::class,
    ]
)
@Name("navigation-buttons")
@Description("Adds options to hide or change navigation buttons.")
@NavigationButtonsCompatibility
@Version("0.0.1")
class NavigationButtonsPatch : BytecodePatch(listOf(AddCreateButtonViewFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_navigation_buttons",
                StringResource("revanced_navigation_buttons", "Navigation button settings"),
                listOf(
                    SwitchPreference(
                        "revanced_hide_home_button",
                        StringResource("revanced_hide_home_button_title", "Hide home button"),
                        false,
                        StringResource("revanced_hide_home_button_summary_on", "Home button is hidden"),
                        StringResource("revanced_hide_home_button_summary_off", "Home button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_button",
                        StringResource("revanced_hide_shorts_button_title", "Hide shorts button"),
                        true,
                        StringResource("revanced_hide_shorts_button_summary_on", "Shorts button is hidden"),
                        StringResource("revanced_hide_shorts_button_summary_off", "Shorts button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_subscriptions_button",
                        StringResource("revanced_hide_subscriptions_button_title", "Hide subscriptions button"),
                        false,
                        StringResource(
                            "revanced_hide_subscriptions_button_summary_on",
                            "Home subscriptions is hidden"
                        ),
                        StringResource("revanced_hide_subscriptions_button_summary_off", "Home subscriptions is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_create_button",
                        StringResource("revanced_hide_create_button_title", "Hide create button"),
                        true,
                        StringResource("revanced_hide_create_button_summary_on", "Create button is hidden"),
                        StringResource("revanced_hide_create_button_summary_off", "Create button is shown")
                    ),
                    SwitchPreference(
                        "revanced_switch_create_with_notifications_button",
                        StringResource(
                            "revanced_switch_create_with_notifications_button_title",
                            "Switch create with notifications button"
                        ),
                        true,
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
                        return it.toErrorResult()
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
                val conditionRegister = (instruction(conditionalCheckIndex) as OneRegisterInstruction).registerA

                addInstructions(
                    conditionalCheckIndex,
                    """
                        invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->switchCreateWithNotificationButton()Z
                        move-result v$conditionRegister
                    """
                )
            }
        } ?: return AddCreateButtonViewFingerprint.toErrorResult()

        /*
         * Resolve fingerprints
         */

        InitializeButtonsFingerprint.result!!.let {
            if (!PivotBarCreateButtonViewFingerprint.resolve(context, it.mutableMethod, it.mutableClass))
                return PivotBarCreateButtonViewFingerprint.toErrorResult()
        }

        PivotBarCreateButtonViewFingerprint.result!!.apply {
            val insertIndex = mutableMethod.implementation!!.instructions.let {
                val scanStart = scanResult.patternScanResult!!.endIndex

                scanStart + it.subList(scanStart, it.size - 1).indexOfFirst { instruction ->
                    instruction.opcode == Opcode.INVOKE_STATIC
                }
            }

            /*
             * Inject hooks
             */
            val hook = "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, " +
                    "$INTEGRATIONS_CLASS_DESCRIPTOR->hideCreateButton(Landroid/view/View;)V"

            mutableMethod.injectHook(hook, insertIndex)
        }
        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/NavigationButtonsPatch;"
    }
}