package app.revanced.patches.music.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.music.misc.microg.annotations.MusicMicroGPatchCompatibility
import org.jf.dexlib2.AccessFlags

@Name("google-play-utility-fingerprint")

@MusicMicroGPatchCompatibility
@Version("0.0.1")
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