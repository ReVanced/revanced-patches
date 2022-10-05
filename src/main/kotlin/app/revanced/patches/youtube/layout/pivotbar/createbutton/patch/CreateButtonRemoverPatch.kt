package app.revanced.patches.youtube.layout.pivotbar.createbutton.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.MethodFingerprintExtensions.name
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.pivotbar.createbutton.annotations.CreateButtonCompatibility
import app.revanced.patches.youtube.layout.pivotbar.createbutton.fingerprints.PivotBarCreateButtonViewFingerprint
import app.revanced.patches.youtube.layout.pivotbar.fingerprints.PivotBarFingerprint
import app.revanced.patches.youtube.layout.pivotbar.utils.InjectionUtils.REGISTER_TEMPLATE_REPLACEMENT
import app.revanced.patches.youtube.layout.pivotbar.utils.InjectionUtils.injectHook
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference

@Patch
@DependsOn([IntegrationsPatch::class, ResourceMappingResourcePatch::class, SettingsPatch::class])
@Name("hide-create-button")
@Description("Hides the create button in the navigation bar.")
@CreateButtonCompatibility
@Version("0.0.1")
class CreateButtonRemoverPatch : BytecodePatch(
    listOf(
        PivotBarFingerprint
    )
) {
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

        val pivotBarResult = PivotBarFingerprint.result ?: return PatchResultError("PivotBarFingerprint failed")

        if (!PivotBarCreateButtonViewFingerprint.resolve(context, pivotBarResult.method, pivotBarResult.classDef))
            return PatchResultError("${PivotBarCreateButtonViewFingerprint.name} failed")

        val createButtonResult = PivotBarCreateButtonViewFingerprint.result!!
        val insertIndex = createButtonResult.scanResult.patternScanResult!!.endIndex

        /*
         * Inject hooks
         */

        val integrationsClass = "Lapp/revanced/integrations/patches/HideCreateButtonPatch;"
        val hook =
            "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, $integrationsClass->hideCreateButton(Landroid/view/View;)V"

        createButtonResult.mutableMethod.injectHook(hook, insertIndex)

        return PatchResultSuccess()
    }
}
