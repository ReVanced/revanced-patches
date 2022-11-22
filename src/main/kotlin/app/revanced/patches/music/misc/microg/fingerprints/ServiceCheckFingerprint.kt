package app.revanced.patches.music.misc.microg.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags


@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
object ServiceCheckFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.STATIC,
    listOf("L", "I"),
    strings = listOf("Google Play Services not available")
)
