package app.revanced.patches.youtube.layout.hideinfocards.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object InfocardsIncognitoFingerprint : MethodFingerprint(
    "Ljava/lang/Boolean;",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("vibrator")
)