package app.revanced.patches.nunl.firebase

import app.revanced.patcher.BytecodePatchContextMethodMatching.firstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val firebaseClasses = arrayOf(
    "Lcom/google/firebase/installations/remote/FirebaseInstallationServiceClient;",
    "Lcom/google/firebase/remoteconfig/internal/ConfigFetchHttpClient;",
    "Lcom/google/firebase/remoteconfig/internal/ConfigRealtimeHttpClient;"
)

internal fun BytecodePatchContext.getFingerprintHashForPackageMethod(className: String) = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE)
    definingClass(className)
    name("getFingerprintHashForPackage")
    returnType("Ljava/lang/String;")
    parameterTypes()
}
