package app.revanced.patches.youtube.misc.fix.backtoexitgesture

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.shared.mainActivityOnBackPressedMethod
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/FixBackToExitGesturePatch;"

internal val fixBackToExitGesturePatch = bytecodePatch(
    description = "Fixes the swipe back to exit gesture.",
) {

    apply {
        recyclerViewTopScrollingMethod.let {
            it.method.addInstructionsAtControlFlowLabel(
                it.indices.last() + 1,
                "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->onTopView()V",
            )
        }

        scrollPositionMethod.let {
            navigate(it.originalMethod)
                .to(it.patternMatch.startIndex + 1)
                .stop().apply {
                    val index = indexOfFirstInstructionOrThrow {
                        opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.definingClass ==
                            "Landroid/support/v7/widget/RecyclerView;"
                    }

                    addInstruction(
                        index,
                        "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->onScrollingViews()V",
                    )
                }
        }

        mainActivityOnBackPressedMethod.apply {
            val index = indexOfFirstInstructionOrThrow(Opcode.RETURN_VOID)
            addInstruction(
                index,
                "invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->onBackPressed(Landroid/app/Activity;)V",
            )
        }
    }
}
