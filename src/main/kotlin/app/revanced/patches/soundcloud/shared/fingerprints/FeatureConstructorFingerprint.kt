package app.revanced.patches.soundcloud.shared.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object FeatureConstructorFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    parameters = listOf("Ljava/lang/String;", "Z", "Ljava/util/List;"),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "Feature.kt"
    },
)