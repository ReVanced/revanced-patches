package app.revanced.patches.music.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patches.music.misc.microg.annotations.MusicMicroGPatchCompatibility
import org.jf.dexlib2.AccessFlags

@Name("google-play-utility-fingerprint")
@MatchingMethod(
    "Lnuv;", "b"
)
@DirectPatternScanMethod
@MusicMicroGPatchCompatibility
@Version("0.0.1")
object GooglePlayUtilityFingerprint : MethodFingerprint(
    "I", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L", "I"), null, listOf("This should never happen.", "MetadataValueReader", "GooglePlayServicesUtil", "com.android.vending", "android.hardware.type.embedded")
)