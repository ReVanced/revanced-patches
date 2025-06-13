package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

val spoofClientPatch = spoofClientPatch(redirectUri = "http://rubenmayayo.com") { clientIdOption ->
    compatibleWith("com.rubenmayayo.reddit")

    val clientId by clientIdOption

    execute {
        // region Patch client id.

        getClientIdFingerprint.method.returnEarly(clientId!!)

        // endregion

        // region Patch user agent.

        // Use a random user agent.
        val randomName = (0..100000).random()
        val userAgent = "$randomName:app.revanced.$randomName:v1.0.0 (by /u/revanced)"
        buildUserAgentFingerprint.let {
            val userAgentTemplateIndex = it.stringMatches!!.first().index
            val register = it.method.getInstruction<OneRegisterInstruction>(userAgentTemplateIndex).registerA
            
            it.method.replaceInstruction(userAgentTemplateIndex, "const-string v$register, \"$userAgent\"")
        }

        // endregion
    }
}
