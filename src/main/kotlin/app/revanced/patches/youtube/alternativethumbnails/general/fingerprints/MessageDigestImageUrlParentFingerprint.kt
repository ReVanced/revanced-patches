package app.revanced.patches.youtube.alternativethumbnails.general.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object MessageDigestImageUrlParentFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType =  "Ljava/lang/String;",
    parameters = emptyList(),
    strings = listOf("@#&=*+-_.,:!?()/~'%;\$"),
)