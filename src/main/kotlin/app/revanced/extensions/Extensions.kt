package app.revanced.extensions

import app.revanced.patcher.smali.toInstruction
import org.jf.dexlib2.builder.MutableMethodImplementation

internal fun MutableMethodImplementation.injectHideCall(
    index: Int,
    register: Int
) {
    this.addInstruction(
        index,
        "invoke-static { v$register }, Lfi/razerman/youtube/XAdRemover;->HideView(Landroid/view/View;)V".toInstruction()
    )
}