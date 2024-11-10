package app.revanced.patches.photomath.misc.unlock.plus

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val isPlusUnlockedFingerprint = fingerprint{
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    strings("genius")
    custom { _, classDef ->
        classDef.endsWith("/User;")
    }
}