package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val textComponentDataFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L", "L")
    strings("text")
    custom { _, classDef ->
        val fields = classDef.fields
        fields.find { it.type == "Ljava/util/BitSet;" } != null &&
            fields.find { it.type == "[Ljava/lang/String;" } != null
    }
}
