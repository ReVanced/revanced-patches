package app.revanced.patches.reddit.customclients.slide.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patches.reddit.customclients.spoofClientPatch

@Suppress("unused")
val spoofClientPatch = spoofClientPatch(redirectUri = "http://www.ccrama.me") { clientIdOption ->
    compatibleWith("me.ccrama.redditslide")

    val getClientIdMatch by getClientIdFingerprint()

    val clientId by clientIdOption

    execute {
        getClientIdMatch.mutableMethod.addInstructions(
            0,
            """
                 const-string v0, "$clientId"
                 return-object v0
            """,
        )
    }
}
