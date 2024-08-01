package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.Match
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val spoofClientPatch = spoofClientPatch(redirectUri = "http://baconreader.com/auth") { clientIdOption ->
    compatibleWith(
        "com.onelouder.baconreader",
        "com.onelouder.baconreader.premium",
    )

    val getAuthorizationUrlMatch by getAuthorizationUrlFingerprint()
    val requestTokenMatch by requestTokenFingerprint()

    val clientId by clientIdOption

    execute {
        fun Match.patch(replacementString: String) {
            val clientIdIndex = stringMatches!!.first().index

            mutableMethod.apply {
                val clientIdRegister = getInstruction<OneRegisterInstruction>(clientIdIndex).registerA
                replaceInstruction(
                    clientIdIndex,
                    "const-string v$clientIdRegister, \"$replacementString\"",
                )
            }
        }

        // Patch client id in authorization url.
        getAuthorizationUrlMatch.patch("client_id=$clientId")

        // Patch client id for access token request.
        requestTokenMatch.patch(clientId!!)
    }
}
