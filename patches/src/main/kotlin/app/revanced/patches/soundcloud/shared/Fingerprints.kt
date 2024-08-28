package app.revanced.patches.soundcloud.shared

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val featureConstructorFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("Ljava/lang/String;", "Z", "Ljava/util/List;")
    custom { _, classDef ->
        classDef.sourceFile == "Feature.kt"
    }
}
