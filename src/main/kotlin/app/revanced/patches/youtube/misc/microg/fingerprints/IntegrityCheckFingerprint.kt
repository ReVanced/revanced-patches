package app.revanced.patches.youtube.misc.microg.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object IntegrityCheckFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L", "L"),
    strings = listOf("This should never happen.", "GooglePlayServicesUtil", "Google Play Store signature invalid.")
)