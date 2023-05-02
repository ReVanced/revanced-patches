package app.revanced.patches.youtube.layout.buttons.pivotbar.create.patch

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
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.buttons.pivotbar.create.fingerprints.ANDROID_AUTOMOTIVE_STRING
import app.revanced.patches.youtube.layout.buttons.pivotbar.create.fingerprints.AddCreateButtonViewFingerprint
import app.revanced.patches.youtube.layout.buttons.pivotbar.create.fingerprints.PivotBarCreateButtonViewFingerprint
import app.revanced.patches.youtube.layout.buttons.pivotbar.shared.annotations.PivotBarCompatibility
import app.revanced.patches.youtube.layout.buttons.pivotbar.shared.fingerprints.InitializeButtonsFingerprint
import app.revanced.patches.youtube.layout.buttons.pivotbar.shared.patch.ResolvePivotBarFingerprintsPatch
import app.revanced.patches.youtube.layout.buttons.pivotbar.utils.InjectionUtils.REGISTER_TEMPLATE_REPLACEMENT
import app.revanced.patches.youtube.layout.buttons.pivotbar.utils.InjectionUtils.injectHook
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class, ResourceMappingPatch::class, SettingsPatch::class, ResolvePivotBarFingerprintsPatch::class])
@Name("create-button")
@Description("Hide the create button in the navigation bar or switch it with notifications.")
@PivotBarCompatibility
@Version("0.0.1")
class CreateButtonPatch : BytecodePatch(listOf(AddCreateButtonViewFingerprint)) {
    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/CreateButtonPatch;"
    }

    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
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
        )

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
}
