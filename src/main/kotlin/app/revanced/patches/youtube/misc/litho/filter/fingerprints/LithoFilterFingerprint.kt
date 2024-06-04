package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val lithoFilterFingerprint = methodFingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    custom { _, classDef ->
        classDef.endsWith("LithoFilterPatch;")
    }
}
