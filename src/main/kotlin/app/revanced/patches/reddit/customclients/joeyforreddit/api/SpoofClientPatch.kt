package app.revanced.patches.reddit.customclients.joeyforreddit.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patches.reddit.customclients.joeyforreddit.api.fingerprints.getClientIdFingerprint
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.disablePiracyDetectionPatch
import app.revanced.patches.reddit.customclients.spoofClientPatch

@Suppress("unused")
val spoofClientPatch = spoofClientPatch(redirectUri = "https://127.0.0.1:65023/authorize_callback") { clientIdOption ->
    dependsOn(disablePiracyDetectionPatch)

    compatibleWith(
        "o.o.joey",
        "o.o.joey.pro",
        "o.o.joey.dev",
    )

    val getClientIdResult by getClientIdFingerprint

    val clientId by clientIdOption

    execute {
        getClientIdResult.mutableMethod.addInstructions(
            0,
            """
                 const-string v0, "$clientId"
                 return-object v0
            """,
        )
    }
}
