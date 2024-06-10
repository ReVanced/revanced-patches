package app.revanced.patches.reddit.customclients.sync.syncforreddit.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.patches.reddit.customclients.sync.detection.piracy.disablePiracyDetectionPatch
import app.revanced.patches.reddit.customclients.sync.syncforreddit.api.fingerprints.getAuthorizationStringFingerprint
import app.revanced.patches.reddit.customclients.sync.syncforreddit.api.fingerprints.getBearerTokenFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import java.util.*

@Suppress("unused")
val spoofClientPatch = spoofClientPatch(
    redirectUri = "http://redditsync/auth",
) { clientIdOption ->
    dependsOn(disablePiracyDetectionPatch)

    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    val getAuthorizationStringResult by getAuthorizationStringFingerprint
    val getBearerTokenResult by getBearerTokenFingerprint

    val clientId by clientIdOption

    execute { context ->
        // region Patch client id.

        getBearerTokenFingerprint.apply {
            resolve(context, getAuthorizationStringResult.classDef)
        }.resultOrThrow().mutableMethod.apply {
            val auth = Base64.getEncoder().encodeToString("$clientId:".toByteArray(Charsets.UTF_8))

            addInstructions(
                0,
                """
                     const-string v0, "Basic $auth"
                     return-object v0
                """,
            )
        }

        getAuthorizationStringResult.mutableMethod.apply {
            val occurrenceIndex = getAuthorizationStringResult.scanResult.stringsScanResult!!.matches.first().index
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

        // endregion

        // region Patch miscellaneous.

        // Use the non-commercial Imgur API endpoint to fix Imgur uploads.
        val apiUrlIndex = getBearerTokenResult.scanResult.stringsScanResult!!.matches.first().index
        getBearerTokenResult.mutableMethod.replaceInstruction(
            apiUrlIndex,
            "const-string v1, \"https://api.imgur.com/3/image\"",
        )

        // endregion
    }
}
