package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patches.reddit.customclients.spoofClientPatch

@Suppress("unused")
val spoofClientPatch = spoofClientPatch(redirectUri = "http://rubenmayayo.com") { clientIdOption ->
    compatibleWith("com.rubenmayayo.reddit")

    val getClientIdFingerprintResult by getClientIdFingerprint
    val buildUserAgentFingerprintResult by buildUserAgentFingerprint

    val clientId by clientIdOption

    execute {
        // region Patch client id.

        getClientIdFingerprintResult.mutableMethod.addInstructions(
            0,
            """
                 const-string v0, "$clientId"
                 return-object v0
            """,
        )

        // endregion

        // region Patch user agent.

        // Use a random number as the platform in the user agent string.
        val platformName = (0..100000).random()
        val platformParameter = 0

        buildUserAgentFingerprintResult.mutableMethod.addInstructions(
            0,
            "const-string p$platformParameter, \"$platformName\"",
        )

        // endregion
    }
}
