package app.revanced.patches.nunl.firebase

import app.revanced.patcher.*
import app.revanced.patcher.BytecodePatchContextMethodMatching.firstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal fun BytecodePatchContext.getFingerprintHashForPackageMethods() = arrayOf(
    "Lcom/google/firebase/installations/remote/FirebaseInstallationServiceClient;",
    "Lcom/google/firebase/remoteconfig/internal/ConfigFetchHttpClient;",
    "Lcom/google/firebase/remoteconfig/internal/ConfigRealtimeHttpClient;"
).map {
    firstMutableMethodDeclaratively {
        name("getFingerprintHashForPackage")
        definingClass(it)
        accessFlags(AccessFlags.PRIVATE)
        returnType("Ljava/lang/String;")
        parameterTypes()
    }
}
