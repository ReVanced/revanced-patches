package app.revanced.patches.shared.misc.integrations

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val revancedUtilsPatchesVersionFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    custom { methodDef, classDef ->
        methodDef.name == "getPatchesReleaseVersion" && classDef.type == INTEGRATIONS_CLASS_DESCRIPTOR
    }
}
