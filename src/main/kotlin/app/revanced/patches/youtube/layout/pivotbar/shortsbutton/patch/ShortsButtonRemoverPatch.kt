package app.revanced.patches.youtube.layout.pivotbar.shortsbutton.patch

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
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.pivotbar.annotations.PivotBarCompatibility
import app.revanced.patches.youtube.layout.pivotbar.fingerprints.InitializeButtonsFingerprint
import app.revanced.patches.youtube.layout.pivotbar.resource.patch.ResolvePivotBarFingerprintsPatch
import app.revanced.patches.youtube.layout.pivotbar.shortsbutton.fingerprints.PivotBarEnumFingerprint
import app.revanced.patches.youtube.layout.pivotbar.shortsbutton.fingerprints.PivotBarShortsButtonViewFingerprint
import app.revanced.patches.youtube.layout.pivotbar.utils.InjectionUtils.REGISTER_TEMPLATE_REPLACEMENT
import app.revanced.patches.youtube.layout.pivotbar.utils.InjectionUtils.injectHook
import app.revanced.patches.youtube.layout.pivotbar.utils.InjectionUtils.toErrorResult
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class, ResolvePivotBarFingerprintsPatch::class])
@Name("hide-shorts-button")
@Description("Hides the shorts button on the navigation bar.")
@PivotBarCompatibility
@Version("0.0.1")
class ShortsButtonRemoverPatch : BytecodePatch() {
    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/HideShortsButtonPatch;"
    }

    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_shorts_button_enabled",
                StringResource("revanced_shorts_button_enabled_title", "Show shorts button"),
                false,
                StringResource("revanced_shorts_button_summary_on", "Shorts button is shown"),
                StringResource("revanced_shorts_button_summary_off", "Shorts button is hidden")
            )
        )

        /*
         * Resolve fingerprints
         */

        val initializeButtonsResult = InitializeButtonsFingerprint.result!!

        val fingerprintResults =
            arrayOf(PivotBarEnumFingerprint, PivotBarShortsButtonViewFingerprint)
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
                "$INTEGRATIONS_CLASS_DESCRIPTOR->lastPivotTab:Ljava/lang/Enum;"
        val buttonHook = "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, " +
                "$INTEGRATIONS_CLASS_DESCRIPTOR->hideShortsButton(Landroid/view/View;)V"

        // Inject bottom to top to not mess up the indices
        mapOf(
            buttonHook to buttonHookInsertIndex,
            enumHook to enumHookInsertIndex
        ).forEach { (hook, insertIndex) ->
            initializeButtonsResult.mutableMethod.injectHook(hook, insertIndex)
        }

        return PatchResultSuccess()
    }
}