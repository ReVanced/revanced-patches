package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.offlineVideoEndpointMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes(
        "Ljava/util/Map;",
        "L",
        "Ljava/lang/String", // Video ID
        "L",
    )
    instructions(
        "Object is not an offlineable video: "(),
    )
}
