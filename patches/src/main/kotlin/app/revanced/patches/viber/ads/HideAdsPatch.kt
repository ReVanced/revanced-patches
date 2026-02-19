package app.revanced.patches.viber.ads

import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.typeReference
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.returnType
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide Ads",
    description = "Hides ad banners between chats.",
) {
    compatibleWith("com.viber.voip"("25.9.2.0", "26.1.2.0"))

    apply {
        val referenceIndex = findAdStringMethodMatch[0]

        val targetClass =
            findAdStringMethodMatch.immutableMethod.getInstruction<ReferenceInstruction>(referenceIndex).typeReference

        val adFreeMethod = firstMethodDeclaratively {
            definingClass(targetClass!!.type)
            returnType("I")
            parameterTypes()
        }.returnEarly(1)
    }
}
