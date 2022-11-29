package app.revanced.patches.youtube.layout.pivotbar.createbutton.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.pivotbar.annotations.PivotBarCompatibility
import app.revanced.patches.youtube.layout.pivotbar.createbutton.fingerprints.PivotBarCreateButtonViewFingerprint
import app.revanced.patches.youtube.layout.pivotbar.fingerprints.InitializeButtonsFingerprint
import app.revanced.patches.youtube.layout.pivotbar.resource.patch.ResolvePivotBarFingerprintsPatch
import app.revanced.patches.youtube.layout.pivotbar.utils.InjectionUtils.REGISTER_TEMPLATE_REPLACEMENT
import app.revanced.patches.youtube.layout.pivotbar.utils.InjectionUtils.injectHook
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([IntegrationsPatch::class, ResourceMappingPatch::class, SettingsPatch::class, ResolvePivotBarFingerprintsPatch::class])
@Name("hide-create-button")
@Description("Hides the create button in the navigation bar.")
@PivotBarCompatibility
@Version("0.0.1")
class CreateButtonRemoverPatch : BytecodePatch() {
    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/HideCreateButtonPatch;"
    }

    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_create_button_enabled",
                StringResource("revanced_create_button_enabled_title", "Show create button"),
                false,
                StringResource("revanced_create_button_summary_on", "Create button is shown"),
                StringResource("revanced_create_button_summary_off", "Create button is hidden")
            )
        )

        /*
         * Resolve fingerprints
         */

        InitializeButtonsFingerprint.result!!.let {
            if (!PivotBarCreateButtonViewFingerprint.resolve(context, it.mutableMethod, it.mutableClass))
                return PivotBarCreateButtonViewFingerprint.toErrorResult()
        }

        val createButtonResult = PivotBarCreateButtonViewFingerprint.result!!
        val insertIndex = createButtonResult.scanResult.patternScanResult!!.endIndex

        /*
         * Inject hooks
         */

        val hook = "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, " +
                "$INTEGRATIONS_CLASS_DESCRIPTOR->hideCreateButton(Landroid/view/View;)V"

        createButtonResult.mutableMethod.injectHook(hook, insertIndex)

        return PatchResultSuccess()
    }
}
