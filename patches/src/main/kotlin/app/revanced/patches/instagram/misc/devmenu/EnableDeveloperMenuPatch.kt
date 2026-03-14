package app.revanced.patches.instagram.misc.devmenu

import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode

@Suppress("unused")
val enableDeveloperMenuPatch = bytecodePatch(
    name = "Enable developer menu",
    description = """
        Enables the developer menu, which can be found at the bottom of settings menu with name 'Internal Settings'.
        
        It is recommended to use this patch with an alpha/beta Instagram release. Patching a stable release works, but the developer menu shows the developer flags as numbers and does not show a human readable description.
    """.trimIndentMultiline(),
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        clearNotificationReceiverMethodMatch.let {
            val stringIndex = it[0]

            it.immutableMethod.indexOfFirstInstructionReversedOrThrow(stringIndex) {
                val reference = methodReference
                opcode in listOf(Opcode.INVOKE_STATIC, Opcode.INVOKE_STATIC_RANGE) &&
                    reference?.parameterTypes?.size == 1 &&
                    reference.parameterTypes.first() == "Lcom/instagram/common/session/UserSession;" &&
                    reference.returnType == "Z"
            }.let { index ->
                navigate(it.immutableMethod).to(index).stop().returnEarly(true)
            }
        }
    }
}
