package app.revanced.patches.youtube.layout.hideinfocards.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hideinfocards.annotations.HideInfocardsCompatibility
import org.jf.dexlib2.AccessFlags

@Name("infocards-incognito-fingerprint")
@HideInfocardsCompatibility
@Version("0.0.1")
object InfocardsIncognitoFingerprint : MethodFingerprint(
    "Ljava/lang/Boolean;",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("vibrator")
)