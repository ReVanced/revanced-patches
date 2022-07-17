package app.revanced.patches.youtube.misc.forcevp9.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.forcevp9.annotations.ForceVP9Compatibility
import org.jf.dexlib2.AccessFlags

@Name("replace-device-info-parent-fingerprint")
@MatchingMethod(
    "Lvjb;", "b"
)
@DirectPatternScanMethod
@ForceVP9Compatibility
@Version("0.0.1")
object ReplaceDeviceInfoParentFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf(), null,
    listOf(
        "Failed to read the client side experiments map from the disk"
    )
)