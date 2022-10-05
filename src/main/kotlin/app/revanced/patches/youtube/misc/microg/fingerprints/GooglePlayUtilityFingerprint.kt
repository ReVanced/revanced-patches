package app.revanced.patches.youtube.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility
import org.jf.dexlib2.AccessFlags

@Name("google-play-utility-fingerprint")
@MicroGPatchCompatibility
@Version("0.0.1")
object GooglePlayUtilityFingerprint : MethodFingerprint(
    "I",
    AccessFlags.PUBLIC or AccessFlags.STATIC,
    listOf("L", "L"),
    strings = listOf("This should never happen.", "MetadataValueReader", "com.google.android.gms")
)