package app.revanced.extensions

import app.revanced.patcher.util.smali.toInstruction
import org.jf.dexlib2.builder.MutableMethodImplementation
import org.w3c.dom.Node

internal fun MutableMethodImplementation.injectHideCall(
    index: Int,
    register: Int
) {
    this.addInstruction(
        index,
        "invoke-static { v$register }, Lfi/razerman/youtube/XAdRemover;->HideView(Landroid/view/View;)V".toInstruction()
    )
}

internal fun Node.doRecursively(action: (Node) -> Unit) {
    action(this)
    for (i in 0 until this.childNodes.length) this.childNodes.item(i).doRecursively(action)
}

internal fun String.startsWithAny(vararg prefixes: String): Boolean {
    for (prefix in prefixes)
        if (this.startsWith(prefix))
            return true

    return false
}

internal fun String.equalsAny(vararg other: String): Boolean {
    for (_other in other)
        if (this == _other)
            return true

    return false
}
