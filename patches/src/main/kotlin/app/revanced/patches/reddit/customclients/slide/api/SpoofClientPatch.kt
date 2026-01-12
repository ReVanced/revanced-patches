package app.revanced.patches.reddit.customclients.slide.api

import app.revanced.patches.reddit.customclients.`Spoof client`
import app.revanced.util.returnEarly

val spoofClientPatch = `Spoof client`(redirectUri = "http://www.ccrama.me") { clientIdOption ->
    compatibleWith("me.ccrama.redditslide")

    val clientId by clientIdOption

    apply {
        getClientIdMethod.returnEarly(clientId!!)
    }
}
