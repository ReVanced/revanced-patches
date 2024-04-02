import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch
import com.android.tools.smali.dexlib2.AccessFlags

internal object IntegrationsUtilsPatchesTimestampFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.STATIC,
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "getPatchesReleaseTimestamp" &&
                classDef.type == BaseIntegrationsPatch.INTEGRATIONS_CLASS_DESCRIPTOR
    }
)