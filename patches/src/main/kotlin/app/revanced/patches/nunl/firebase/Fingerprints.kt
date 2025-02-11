package app.revanced.patches.nunl.firebase

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val getFingerprintHashForPackageFingerprints = arrayOf(
    "Lcom/google/firebase/installations/remote/FirebaseInstallationServiceClient;",
    "Lcom/google/firebase/remoteconfig/internal/ConfigFetchHttpClient;",
    "Lcom/google/firebase/remoteconfig/internal/ConfigRealtimeHttpClient;"
).map { className ->
    fingerprint {
        accessFlags(AccessFlags.PRIVATE)
        parameters()
        returns("Ljava/lang/String;")

        custom { methodDef, classDef ->
            classDef.type == className && methodDef.name == "getFingerprintHashForPackage"
        }
    }
}
