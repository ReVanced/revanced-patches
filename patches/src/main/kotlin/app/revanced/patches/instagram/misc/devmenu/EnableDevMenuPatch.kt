package app.revanced.patches.instagram.misc.devmenu

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val enableDevMenuPatch = bytecodePatch(
    name = "Enable developer menu",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        with(clearNotificationReceiverFingerprint.method) {
            indexOfFirstInstructionReversedOrThrow {
                val ref = getReference<MethodReference>()
                Opcode.INVOKE_STATIC == opcode &&
                        ref?.parameterTypes?.size == 1 &&
                        ref.parameterTypes.first() == "Lcom/instagram/common/session/UserSession;" &&
                        ref.returnType == "Z"
            }.also { index ->
                navigate(this).to(index).stop().returnEarly(true)
            }
        }
    }
}

