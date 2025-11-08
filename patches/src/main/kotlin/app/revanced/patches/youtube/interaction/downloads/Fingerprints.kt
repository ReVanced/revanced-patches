package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal val offlineVideoEndpointFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters(
        "Ljava/util/Map;",
        "L",
        "Ljava/lang/String", // VideoId
        "L",
    )
    instructions(
        string("Object is not an offlineable video: ")
    )
}
