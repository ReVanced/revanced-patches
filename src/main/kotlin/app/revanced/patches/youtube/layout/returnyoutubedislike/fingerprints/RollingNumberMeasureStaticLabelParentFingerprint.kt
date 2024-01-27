package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object RollingNumberMeasureStaticLabelParentFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf(),
    strings = listOf("RollingNumberFontProperties{paint=")
)