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
    name = "Enable developer menu", description = """
        Enables the developer menu, which can be found at the bottom of settings menu with name 'Internal Settings'.
        
        It is recommended to use this patch with an alpha/beta Instagram release. Patching a stable release works, but the developer menu shows the developer flags as numbers and does not show a human readable description.
    """.trimIndentMultiline(), use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        clearNotificationReceiverFingerprint.method.apply {
            indexOfFirstInstructionReversedOrThrow(clearNotificationReceiverFingerprint.stringMatches!!.first().index) {
                val reference = getReference<MethodReference>()
                // The obfuscator uses invoke-static or invoke-static/range interchangeably, both options are valid:
                // * invoke-static/range {v16 .. v16}, LX/2zj;->A00(Lcom/instagram/common/session/UserSession;)Z
                // * invoke-static {v9}, LX/3bf;->A00(Lcom/instagram/common/session/UserSession;)Z
                opcode in listOf(Opcode.INVOKE_STATIC, Opcode.INVOKE_STATIC_RANGE) &&
                        reference?.parameterTypes?.size == 1 &&
                        reference.parameterTypes.first() == "Lcom/instagram/common/session/UserSession;" &&
                        reference.returnType == "Z"
            }.let { index ->
                navigate(this).to(index).stop().returnEarly(true)
            }
        }
    }
}

