package app.revanced.patches.reddit.customclients.sync.syncforreddit.api

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.`Spoof client`
import app.revanced.patches.reddit.customclients.sync.detection.piracy.`Disable piracy detection`
import app.revanced.patches.shared.misc.string.replaceStringPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import java.util.Base64

@Suppress("unused")
val spoofClientPatch = `Spoof client`(
    redirectUri = "http://redditsync/auth",
) { clientIdOption ->
    dependsOn(
        `Disable piracy detection`,
        // Redirects from SSL to WWW domain are bugged causing auth problems.
        // Manually rewrite the URLs to fix this.
        replaceStringPatch("ssl.reddit.com", "www.reddit.com")
    )

    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    val clientId by clientIdOption

    apply {
        // region Patch client id.

        getBearerTokenMethod.apply {
            val auth = Base64.getEncoder().encodeToString("$clientId:".toByteArray(Charsets.UTF_8))
            returnEarly("Basic $auth")
        }

        getAuthorizationStringMethod.apply {
            val occurrenceIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.CONST_STRING &&
                        getReference<StringReference>()?.string?.contains("client_id=") == true
            }

            val authorizationStringInstruction = getInstruction<OneRegisterInstruction>(occurrenceIndex)
            val targetRegister = authorizationStringInstruction.registerA
            val reference = authorizationStringInstruction.getReference<StringReference>()!!

            val newAuthorizationUrl = reference.string.replace(
                "client_id=.*?&".toRegex(),
                "client_id=$clientId&",
            )

            replaceInstruction(
                occurrenceIndex,
                "const-string v$targetRegister, \"$newAuthorizationUrl\"",
            )
        }

        // endregion

        // region Patch user agent.

        // Use a random user agent.
        val randomName = (0..100000).random()
        val userAgent = "$randomName:app.revanced.$randomName:v1.0.0 (by /u/revanced)"

        getUserAgentMethod.returnEarly(userAgent)

        // endregion

        // region Patch Imgur API URL.

        imgurImageAPIMethod.apply {
            val apiUrlIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.CONST_STRING &&
                        getReference<StringReference>()?.string == "https://api.imgur.com/3/image"
            }

            replaceInstruction(
                apiUrlIndex,
                "const-string v1, \"https://api.imgur.com/3/image\"",
            )
        }

        // endregion
    }
}
