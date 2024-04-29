package app.revanced.patches.shared.misc.integrations.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch
import com.android.tools.smali.dexlib2.AccessFlags

internal val revancedUtilsPatchesVersionFingerprint = methodFingerprint {
    returns("Ljava/lang/String;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters()
    custom { methodDef, classDef ->
        methodDef.name == "getPatchesReleaseVersion" && classDef.type == BaseIntegrationsPatch.INTEGRATIONS_CLASS_DESCRIPTOR
    }
}
