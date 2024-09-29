package app.revanced.patches.soundcloud.ad.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object UserConsumerPlanConstructorFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    parameters = listOf("Ljava/lang/String;", "Z", "Ljava/lang/String;", "Ljava/util/List;", "Ljava/lang/String;", "Ljava/lang/String;"),
)