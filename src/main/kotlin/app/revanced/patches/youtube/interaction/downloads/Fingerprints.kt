package app.revanced.patches.youtube.interaction.downloads

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val offlineVideoEndpointFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters(
        "Ljava/util/Map;",
        "L",
        "Ljava/lang/String", // VideoId
        "L",
    )
    strings("Object is not an offlineable video: ")
}