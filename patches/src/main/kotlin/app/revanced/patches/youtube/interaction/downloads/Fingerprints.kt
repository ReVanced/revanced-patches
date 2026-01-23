package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.addString
import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val offlineVideoEndpointFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes(
        "Ljava/util/Map;",
        "L",
        "Ljava/lang/String", // VideoId
        "L",
    )
    instructions(
        addString("Object is not an offlineable video: "),
    )
}
