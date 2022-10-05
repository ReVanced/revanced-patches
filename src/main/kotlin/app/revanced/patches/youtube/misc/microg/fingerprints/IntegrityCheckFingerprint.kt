package app.revanced.patches.youtube.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility
import org.jf.dexlib2.AccessFlags

@Name("google-play-sig-check-fingerprint")
@MicroGPatchCompatibility
@Version("0.0.1")
object IntegrityCheckFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L", "L"),
    strings = listOf("This should never happen.", "GooglePlayServicesUtil", "Google Play Store signature invalid.")
)