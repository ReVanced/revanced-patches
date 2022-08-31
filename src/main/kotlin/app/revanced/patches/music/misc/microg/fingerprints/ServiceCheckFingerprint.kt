package app.revanced.patches.music.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.music.misc.microg.annotations.MusicMicroGPatchCompatibility
import org.jf.dexlib2.AccessFlags

@Name("google-play-service-checker-fingerprint")
@MatchingMethod(
    "Lnuv;", "d"
)
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@MusicMicroGPatchCompatibility
@Version("0.0.1")
object ServiceCheckFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.STATIC,
    listOf("L", "I"),
    strings = listOf("Google Play Services not available")
)
