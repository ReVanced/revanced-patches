package app.revanced.patches.youtube.layout.hide.infocards.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object InfocardsIncognitoFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf("L", "J"),
    strings = listOf("vibrator")
)