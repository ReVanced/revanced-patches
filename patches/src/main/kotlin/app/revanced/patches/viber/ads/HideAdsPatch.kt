package app.revanced.patches.viber.ads

import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.typeReference
import app.revanced.patcher.firstMutableMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patcher.returnType
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Suppress("unused", "ObjectPropertyName")
val `Hide Ads` by creatingBytecodePatch(
    description = "Hides ad banners between chats.",
) {
    compatibleWith("com.viber.voip"("25.9.2.0", "26.1.2.0"))

    apply {
        val referenceIndex = findAdStringMethodMatch.indices.first()

        val targetClass =
            findAdStringMethodMatch.immutableMethod.getInstruction<ReferenceInstruction>(referenceIndex).typeReference

        val adFreeFingerprint = firstMutableMethodDeclaratively {
            definingClass(targetClass!!.type)
            returnType("I")
            parameterTypes()
        }.returnEarly(1)
    }
}
