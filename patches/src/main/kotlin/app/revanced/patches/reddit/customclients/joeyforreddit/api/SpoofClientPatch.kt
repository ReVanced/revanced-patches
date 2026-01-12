package app.revanced.patches.reddit.customclients.joeyforreddit.api

import app.revanced.patches.reddit.customclients.infinity.api.authUtilityUserAgentMethod
import app.revanced.patches.reddit.customclients.infinity.api.getClientIdMethod
import app.revanced.patches.reddit.customclients.`Spoof client`
import app.revanced.patches.reddit.customclients.sync.detection.piracy.disablePiracyDetectionPatch
import app.revanced.util.returnEarly

val spoofClientPatch = `Spoof client`(redirectUri = "https://127.0.0.1:65023/authorize_callback") { clientIdOption ->
    dependsOn(disablePiracyDetectionPatch)

    compatibleWith(
        "o.o.joey",
        "o.o.joey.pro",
        "o.o.joey.dev",
    )

    val clientId by clientIdOption

    apply {
        // region Patch client id.

        getClientIdMethod.returnEarly(clientId!!)

        // endregion

        // region Patch user agent.

        // Use a random user agent.
        val randomName = (0..100000).random()
        val userAgent = "$randomName:app.revanced.$randomName:v1.0.0 (by /u/revanced)"

        authUtilityUserAgentMethod.returnEarly(userAgent)

        // endregion
    }
}
