package app.revanced.patches.instagram.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/instagram/misc/links/OpenLinksExternallyPatch;"

@Suppress("unused")
val openLinksExternallyPatch = bytecodePatch(
    name = "Open links externally",
    description = "Changes links to always open in your external browser, instead of the in-app browser.",
    use = false,
) {

    dependsOn(sharedExtensionPatch)

    compatibleWith("com.instagram.android")

    execute {
        inAppBrowserFunctionFingerprint.let {
            val stringMatchIndex = it.stringMatches?.first { match -> match.string == TARGET_STRING }!!.index

            it.method.apply {
                val urlResultObjIndex = indexOfFirstInstructionOrThrow(
                    stringMatchIndex, Opcode.MOVE_OBJECT_FROM16
                )

                // Register that contains the url after moving from a higher register.
                val urlRegister = getInstruction<TwoRegisterInstruction>(urlResultObjIndex).registerA

                addInstructions(
                    urlResultObjIndex + 1,
                    """
                        invoke-static/range { v$urlRegister .. v$urlRegister }, $EXTENSION_CLASS_DESCRIPTOR->openExternally(Ljava/lang/String;)Z
                        move-result v0
                        return v0
                    """
                )
            }
        }
    }
}
