package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.MatchBuilder
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.patches.shared.misc.string.replaceStringPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

val spoofClientPatch = spoofClientPatch(redirectUri = "http://baconreader.com/auth") { clientIdOption ->
    dependsOn(
        // Redirects from SSL to WWW domain are bugged causing auth problems.
        // Manually rewrite the URLs to fix this.
        replaceStringPatch("ssl.reddit.com", "www.reddit.com")
    )

    compatibleWith(
        "com.onelouder.baconreader",
        "com.onelouder.baconreader.premium",
    )

    val clientId by clientIdOption

    apply {
        fun MatchBuilder.patch(replacementString: String) {
            val clientIdIndex = indices.first()

            val clientIdRegister = method.getInstruction<OneRegisterInstruction>(clientIdIndex).registerA
            method.replaceInstruction(
                clientIdIndex,
                "const-string v$clientIdRegister, \"$replacementString\"",
            )
        }

        // Patch client id in authorization url.
        getAuthorizationUrlMethodMatch.patch("client_id=$clientId")

        // Patch client id for access token request.
        requestTokenMethodMatch.patch(clientId!!)
    }
}
