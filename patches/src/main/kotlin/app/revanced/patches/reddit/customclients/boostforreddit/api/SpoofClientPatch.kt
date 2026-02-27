package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val spoofClientPatch = spoofClientPatch(redirectUri = "http://rubenmayayo.com") { clientIdOption ->
    compatibleWith("com.rubenmayayo.reddit")

    val clientId by clientIdOption

    apply {
        // region Patch client id.

        getClientIdMethod.returnEarly(clientId!!)

        // endregion

        // region Patch user agent.

        // Use a random user agent.
        val randomName = (0..100000).random()
        val userAgent = "$randomName:app.revanced.$randomName:v1.0.0 (by /u/revanced)"

        val userAgentTemplateIndex = buildUserAgentMethod.indexOfFirstInstructionOrThrow {
            opcode == Opcode.CONST_STRING && getReference<StringReference>()?.string == "%s:%s:%s (by /u/%s)"
        }
        val register = buildUserAgentMethod.getInstruction<OneRegisterInstruction>(userAgentTemplateIndex).registerA

        buildUserAgentMethod.replaceInstruction(userAgentTemplateIndex, "const-string v$register, \"$userAgent\"")

        // endregion
    }
}
