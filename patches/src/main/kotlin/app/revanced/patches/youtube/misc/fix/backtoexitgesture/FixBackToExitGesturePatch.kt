package app.revanced.patches.youtube.misc.fix.backtoexitgesture

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.shared.mainActivityOnBackPressedFingerprint
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/FixBackToExitGesturePatch;"

internal val fixBackToExitGesturePatch = bytecodePatch(
    description = "Fixes the swipe back to exit gesture."
) {

    execute {
        recyclerViewTopScrollingFingerprint.let {
            it.method.addInstructionsAtControlFlowLabel(
                it.instructionMatches.last().index + 1,
                "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->onTopView()V"
            )
        }

        scrollPositionFingerprint.let {
            navigate(it.originalMethod)
                .to(it.patternMatch!!.startIndex + 1)
                .stop().apply {
                    val index = indexOfFirstInstructionOrThrow {
                        opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.definingClass ==
                                "Landroid/support/v7/widget/RecyclerView;"
                    }

                    addInstruction(
                        index,
                        "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->onScrollingViews()V"
                    )
                }
        }

        mainActivityOnBackPressedFingerprint.method.apply {
            val index = indexOfFirstInstructionOrThrow(Opcode.RETURN_VOID)
            addInstruction(
                index,
                "invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->onBackPressed(Landroid/app/Activity;)V"
            )
        }
    }
}
