package app.revanced.patches.gamehub.misc.ota

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val disableOtaUpdatesPatch = bytecodePatch(
    name = "Disable OTA updates",
    description = "Blocks OTA update server URL.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))

    execute {
        baseOtaRepositoryFingerprint.method.apply {
            val urlIndex = indexOfFirstInstructionOrThrow {
                (this as? com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction)
                    ?.reference?.let { it is StringReference && (it as StringReference).string.startsWith("https://www.xiaoji.com") } == true
            }
            // Override the URL string with empty string so OTA calls fail silently
            addInstruction(urlIndex + 1, "const-string p1, \"http://127.0.0.1\"")
        }
    }
}
