package app.revanced.patches.youtube.layout.alternativethumbnails.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object MessageDigestImageUrlParentFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType =  "Ljava/lang/String;",
    parameters = listOf(),
    strings = listOf("@#&=*+-_.,:!?()/~'%;\$"),
)