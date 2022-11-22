package app.revanced.patches.youtube.misc.microg.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object ServiceCheckFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L", "I"),
    strings = listOf("Google Play Services not available", "GooglePlayServices not available due to error ")
)