package app.revanced.patches.youtube.layout.shorts.button.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.layout.shorts.button.annotations.ShortsButtonCompatibility
import app.revanced.patches.youtube.layout.shorts.button.fingerprints.PivotBarButtonTabEnumFingerprint
import app.revanced.patches.youtube.layout.shorts.button.fingerprints.PivotBarButtonsViewFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class])
@Name("hide-shorts-button")
@Description("Hides the shorts button on the navigation bar.")
@ShortsButtonCompatibility
@Version("0.0.1")
class ShortsButtonRemoverPatch : BytecodePatch(
    listOf(
        PivotBarButtonTabEnumFingerprint, PivotBarButtonsViewFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val tabEnumResult = PivotBarButtonTabEnumFingerprint.result!!
        val tabEnumImplementation = tabEnumResult.mutableMethod.implementation!!
        val moveEnumInstruction = tabEnumImplementation.instructions[tabEnumResult.patternScanResult!!.endIndex]
        val enumRegister = (moveEnumInstruction as OneRegisterInstruction).registerA

        val buttonsViewResult = PivotBarButtonsViewFingerprint.result!!
        val buttonsViewImplementation = buttonsViewResult.mutableMethod.implementation!!
        val moveViewInstruction = buttonsViewImplementation.instructions[buttonsViewResult.patternScanResult!!.startIndex + 1]
        val viewRegister = (moveViewInstruction as OneRegisterInstruction).registerA


        // Save the tab enum in XGlobals to avoid smali/register workarounds
        tabEnumResult.mutableMethod.addInstruction(
            tabEnumResult.patternScanResult!!.endIndex + 1,
            "sput-object v$enumRegister, Lapp/revanced/integrations/patches/HideShortsButtonPatch;->lastPivotTab:Ljava/lang/Enum;"
        )

        // Hide the button view via proxy by passing it to the hideShortsButton method
        // It only hides it if the last tab name is "TAB_SHORTS"
        buttonsViewResult.mutableMethod.addInstruction(
            buttonsViewResult.patternScanResult!!.startIndex + 3,
            "invoke-static { v$viewRegister }, Lapp/revanced/integrations/patches/HideShortsButtonPatch;->hideShortsButton(Landroid/view/View;)V"
        )

        return PatchResultSuccess()
    }
}
