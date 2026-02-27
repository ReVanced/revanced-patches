package app.revanced.patches.twitch.ad.embedded

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.createsUsherClientMethod by gettingFirstMethodDeclaratively {
    name("buildOkHttpClient")
    definingClass("Ltv/twitch/android/network/OkHttpClientFactory;")
}
