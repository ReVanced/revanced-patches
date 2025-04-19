package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val shareUrlToStringFingerprint = fingerprint {
    strings("ShareUrl", "shareId") // partial matches
    custom { method, _ ->
        method.name == "toString"
    }
}

internal val shareUrlConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}