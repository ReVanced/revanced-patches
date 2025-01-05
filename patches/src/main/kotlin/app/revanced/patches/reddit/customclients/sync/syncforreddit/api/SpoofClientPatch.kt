package app.revanced.patches.reddit.customclients.sync.syncforreddit.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.patches.reddit.customclients.sync.detection.piracy.disablePiracyDetectionPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import java.util.*

val spoofClientPatch = spoofClientPatch(
    redirectUri = "http://redditsync/auth",
) { clientIdOption ->
    dependsOn(disablePiracyDetectionPatch)

    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    val clientId by clientIdOption

    execute {
        // region Patch client id.

        getBearerTokenFingerprint.match(getAuthorizationStringFingerprint.originalClassDef).method.apply {
            val auth = Base64.getEncoder().encodeToString("$clientId:".toByteArray(Charsets.UTF_8))
            addInstructions(
                0,
                """
                     const-string v0, "Basic $auth"
                     return-object v0
                """,
            )
            val occurrenceIndex =
                getAuthorizationStringFingerprint.stringMatches.first().index

            getAuthorizationStringFingerprint.method.apply {
                val authorizationStringInstruction = getInstruction<ReferenceInstruction>(occurrenceIndex)
                val targetRegister = (authorizationStringInstruction as OneRegisterInstruction).registerA
                val reference = authorizationStringInstruction.reference as StringReference

                val newAuthorizationUrl = reference.string.replace(
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

        getUserAgentFingerprint.method.replaceInstruction(
            0,
            """
                const-string v0, "$userAgent"
                return-object v0
            """,
        )

        // endregion

        // region Patch Imgur API URL.

        val apiUrlIndex = imgurImageAPIFingerprint.stringMatches.first().index
        imgurImageAPIFingerprint.method.replaceInstruction(
            apiUrlIndex,
            "const-string v1, \"https://api.imgur.com/3/image\"",
        )

        // endregion
    }
}
