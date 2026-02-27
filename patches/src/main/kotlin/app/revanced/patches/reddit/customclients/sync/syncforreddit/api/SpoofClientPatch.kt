package app.revanced.patches.reddit.customclients.sync.syncforreddit.api

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.extensions.stringReference
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.patches.reddit.customclients.sync.detection.piracy.disablePiracyDetectionPatch
import app.revanced.patches.shared.misc.string.replaceStringPatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import java.util.*

@Suppress("unused")
val spoofClientPatch = spoofClientPatch(
    redirectUri = "http://redditsync/auth",
) { clientIdOption ->
    dependsOn(
        disablePiracyDetectionPatch,
        // Redirects from SSL to WWW domain are bugged causing auth problems.
        // Manually rewrite the URLs to fix this.
        replaceStringPatch("ssl.reddit.com", "www.reddit.com"),
    )

    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    val clientId by clientIdOption

    apply {
        // region Patch client id.

        getAuthorizationStringMethodMatch.immutableClassDef.getBearerTokenMethodMatch.method.apply {
            val auth = Base64.getEncoder().encodeToString("$clientId:".toByteArray(Charsets.UTF_8))
            returnEarly("Basic $auth")

            val occurrenceIndex = getAuthorizationStringMethodMatch[0]

            getAuthorizationStringMethodMatch.method.apply {
                val authorizationStringInstruction = getInstruction<OneRegisterInstruction>(occurrenceIndex)
                val targetRegister = authorizationStringInstruction.registerA

                val newAuthorizationUrl = authorizationStringInstruction.stringReference!!.string.replace(
                    "client_id=.*?&".toRegex(),
                    "client_id=$clientId&",
                )

                replaceInstruction(
                    occurrenceIndex,
                    "const-string v$targetRegister, \"$newAuthorizationUrl\"",
                )
            }
        }

        // endregion

        // region Patch user agent.

        // Use a random user agent.
        val randomName = (0..100000).random()
        val userAgent = "$randomName:app.revanced.$randomName:v1.0.0 (by /u/revanced)"

        getUserAgentMethod.returnEarly(userAgent)

        // endregion

        // region Patch Imgur API URL.

        val apiUrlIndex = imgurImageAPIMethodMatch[0]
        imgurImageAPIMethodMatch.method.replaceInstruction(
            apiUrlIndex,
            "const-string v1, \"https://api.imgur.com/3/image\"",
        )

        // endregion
    }
}
