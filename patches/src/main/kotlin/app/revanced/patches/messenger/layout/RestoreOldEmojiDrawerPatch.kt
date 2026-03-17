package app.revanced.patches.messenger.layout

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.firstMethod
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

@Suppress("unused")
val restoreOldEmojiDrawerPatch = bytecodePatch(
    name = "Restore old emoji drawer",
    description = "Disables the new redesigned emoji drawer.",
) {
    compatibleWith("com.facebook.orca")

    apply {
        val isRedesignedDrawerEnabledMethodReference = renderRedesignedDrawerMethodMatch.let {
            it.method.getInstruction(it[0]).getReference<MethodReference>()!!
        }
        val isRedesignedDrawerEnabledMethod = firstMethod {
            definingClass == isRedesignedDrawerEnabledMethodReference.definingClass
                    && MethodUtil.methodSignaturesMatch(this, isRedesignedDrawerEnabledMethodReference)
        }
        isRedesignedDrawerEnabledMethod.returnEarly(false)
    }
}