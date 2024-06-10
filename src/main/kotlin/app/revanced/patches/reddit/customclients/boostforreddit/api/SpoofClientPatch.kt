package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints.getClientIdFingerprint
import app.revanced.patches.reddit.customclients.spoofClientPatch

@Suppress("unused")
val spoofClientPatch = spoofClientPatch(redirectUri = "http://rubenmayayo.com") { clientIdOption ->
    compatibleWith("com.rubenmayayo.reddit")

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
