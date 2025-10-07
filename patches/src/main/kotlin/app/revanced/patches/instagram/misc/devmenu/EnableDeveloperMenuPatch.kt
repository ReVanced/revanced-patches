package app.revanced.patches.instagram.misc.devmenu

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val enableDeveloperMenuPatch = bytecodePatch(
    name = "Enable developer menu",
    description = """
        Enables the developer menu, which can be found at the bottom of settings menu with name "Internal Settings".
        
        This patch is suggested to be applied on Alpha/Beta builds. Applying the patch on a Stable build will still
        enable the menu, however the flags will be displayed as numbers without a human readable description.
    """.trimIndentMultiline(),
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        with(clearNotificationReceiverFingerprint.method) {
            indexOfFirstInstructionReversedOrThrow(clearNotificationReceiverFingerprint.stringMatches!!.first().index) {
                val reference = getReference<MethodReference>()
                Opcode.INVOKE_STATIC == opcode &&
                        reference?.parameterTypes?.size == 1 &&
                        reference.parameterTypes.first() == "Lcom/instagram/common/session/UserSession;" &&
                        reference.returnType == "Z"
            }.let { index ->
                navigate(this).to(index).stop().returnEarly(true)
            }
        }
    }
}

