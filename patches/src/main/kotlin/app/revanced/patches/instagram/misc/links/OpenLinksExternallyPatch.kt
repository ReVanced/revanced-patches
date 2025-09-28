package app.revanced.patches.instagram.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

internal const val TARGET_STRING = "Tracking.ARG_CLICK_SOURCE"

@Suppress("unused")
val openLinksExternallyPatch = bytecodePatch(
    name = "Open links externally",
    description = "Changes links to always open in your external browser, instead of the in-app browser.",
    use = false,
) {

    dependsOn(sharedExtensionPatch)

    compatibleWith("com.instagram.android")

    execute {
        inAppBrowserFunctionFingerprint.apply {

            val stringMatchIndex =
                inAppBrowserFunctionFingerprint.stringMatches?.first { it.string == TARGET_STRING }!!.index

            val urlResultObjIndex = method.indexOfFirstInstructionOrThrow(stringMatchIndex, Opcode.MOVE_OBJECT_FROM16)

            // Register that contains the url after moving from a higher register.
            val urlRegister = method.getInstruction<TwoRegisterInstruction>(urlResultObjIndex).registerA

            method.addInstructions(
                urlResultObjIndex + 1,
                """
                    invoke-static { v$urlRegister }, Lapp/revanced/extension/instagram/misc/links/OpenLinksExternallyPatch;->openExternally(Ljava/lang/String;)Z
                    move-result v$urlRegister
                    return v$urlRegister
                """
            )
        }
    }
}
