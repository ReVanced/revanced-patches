package app.revanced.patches.reddit.customclients.slide.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patches.reddit.customclients.spoofClientPatch

val spoofClientPatch = spoofClientPatch(redirectUri = "http://www.ccrama.me") { clientIdOption ->
    compatibleWith("me.ccrama.redditslide")

    val clientId by clientIdOption

    execute {
        getClientIdFingerprint.method().addInstructions(
            0,
            """
                 const-string v0, "$clientId"
                 return-object v0
            """,
        )
    }
}
