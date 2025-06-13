package app.revanced.patches.reddit.customclients.joeyforreddit.api

import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.disablePiracyDetectionPatch
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.returnEarly

val spoofClientPatch = spoofClientPatch(redirectUri = "https://127.0.0.1:65023/authorize_callback") { clientIdOption ->
    dependsOn(disablePiracyDetectionPatch)

    compatibleWith(
        "o.o.joey",
        "o.o.joey.pro",
        "o.o.joey.dev",
    )

    val clientId by clientIdOption

    execute {
        // region Patch client id.

        getClientIdFingerprint.method.returnEarly(clientId!!)

        // endregion

        // region Patch user agent.

        // Use a random user agent.
        val randomName = (0..100000).random()
        val userAgent = "$randomName:app.revanced.$randomName:v1.0.0 (by /u/revanced)"

        authUtilityUserAgentFingerprint.method.returnEarly(userAgent)

        // endregion
    }
}
