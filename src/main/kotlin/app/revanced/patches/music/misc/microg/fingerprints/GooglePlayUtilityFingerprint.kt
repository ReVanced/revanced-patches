package app.revanced.patches.music.misc.microg.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object GooglePlayUtilityFingerprint : MethodFingerprint(
    "I",
    AccessFlags.PUBLIC or AccessFlags.STATIC,
    listOf("L", "I"),
    strings = listOf(
        "This should never happen.",
        "MetadataValueReader",
        "GooglePlayServicesUtil",
        "com.android.vending",
        "android.hardware.type.embedded"
    )
)