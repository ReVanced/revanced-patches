package app.revanced.patches.reddit.customclients.slide.api

import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.returnEarly

val spoofClientPatch = spoofClientPatch(redirectUri = "http://www.ccrama.me") { clientIdOption ->
    compatibleWith("me.ccrama.redditslide")

    val clientId by clientIdOption

    execute {
        getClientIdFingerprint.method.returnEarly(clientId!!)
    }
}
