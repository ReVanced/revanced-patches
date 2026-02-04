package app.revanced.patches.reddit.customclients.baconreader.fix.redgifs

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.getOkHttpClientMethod by gettingFirstMethodDeclaratively {
    definingClass("Lcom/onelouder/baconreader/media/gfycat/RedGifsManager;")
    name("getOkhttpClient")
    returnType("Lokhttp3/OkHttpClient;")
    parameterTypes()
}
