package app.revanced.patches.instagram.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22x

internal val inAppBrowserFunctionFingerprint = fingerprint {
    returns("Z")
    strings("TrackingInfo.ARG_MODULE_NAME","Tracking.ARG_CLICK_SOURCE")
}

@Suppress("unused")
val OpenLinksWithExternalBrowser = bytecodePatch(
    name = "Open links with external browser",
    description = "This patch lets you to open links in external browser, instead of in app browser",
    use = false,
) {
    dependsOn(sharedExtensionPatch)
    compatibleWith("com.instagram.android")

    execute {

        val methodReference =
            "Lapp/revanced/extension/instagram/misc/links/OpenLinksWithExternalBrowser;->" +
                "openExternalBrowser(Ljava/lang/String;)Z"

        val method = inAppBrowserFunctionFingerprint.method

        val stringMatchIndex = inAppBrowserFunctionFingerprint.stringMatches?.first { it.string == "Tracking.ARG_CLICK_SOURCE" }!!.index

        val urlResultObjIndex = method.instructions.first { it.opcode == Opcode.MOVE_OBJECT_FROM16 && it.location.index > stringMatchIndex }.location.index

        // Register that contains the url after moving from a higher register.
        val urlRegister = method.getInstruction<BuilderInstruction22x>(urlResultObjIndex).registerA

        method.addInstructions(
            urlResultObjIndex +1,
            """
                invoke-static { v$urlRegister }, $methodReference
                move-result v$urlRegister
                return v$urlRegister
            """,
        )
    }
}
