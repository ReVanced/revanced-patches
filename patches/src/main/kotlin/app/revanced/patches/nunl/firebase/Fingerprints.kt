package app.revanced.patches.nunl.firebase

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val getFingerprintHashForPackageFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    parameters()
    returns("Ljava/lang/String;")

    custom { methodDef, classDef ->
        classDef.type.startsWith("Lcom/google/firebase/") && methodDef.name == "getFingerprintHashForPackage"
    }
}
