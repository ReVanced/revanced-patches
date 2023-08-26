package app.revanced.patches.youtube.layout.hide.endscreencards.bytecode.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.hide.endscreencards.annotations.HideEndscreenCardsCompatibility
import app.revanced.patches.youtube.layout.hide.endscreencards.bytecode.fingerprints.LayoutCircleFingerprint
import app.revanced.patches.youtube.layout.hide.endscreencards.bytecode.fingerprints.LayoutIconFingerprint
import app.revanced.patches.youtube.layout.hide.endscreencards.bytecode.fingerprints.LayoutVideoFingerprint
import app.revanced.patches.youtube.layout.hide.endscreencards.resource.patch.HideEndscreenCardsResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c

@Patch
@DependsOn([IntegrationsPatch::class, HideEndscreenCardsResourcePatch::class])
@Name("Hide endscreen cards")
@Description("Hides the suggested video cards at the end of a video in fullscreen.")
@HideEndscreenCardsCompatibility
class HideEndscreenCardsPatch : BytecodePatch(
    listOf(
        LayoutCircleFingerprint,
        LayoutIconFingerprint,
        LayoutVideoFingerprint,
    )
) {
    override fun execute(context: BytecodeContext) {
        fun MethodFingerprint.injectHideCall() {
            val layoutResult = result ?: throw exception
            layoutResult.mutableMethod.apply {
                val insertIndex = layoutResult.scanResult.patternScanResult!!.endIndex + 1
                val viewRegister = getInstruction<Instruction21c>(insertIndex - 1).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static { v$viewRegister }, Lapp/revanced/integrations/patches/HideEndscreenCardsPatch;->hideEndscreen(Landroid/view/View;)V"
                )
            }
        }

        listOf(
            LayoutCircleFingerprint,
            LayoutIconFingerprint,
            LayoutVideoFingerprint
        ).forEach(MethodFingerprint::injectHideCall)
    }
}
