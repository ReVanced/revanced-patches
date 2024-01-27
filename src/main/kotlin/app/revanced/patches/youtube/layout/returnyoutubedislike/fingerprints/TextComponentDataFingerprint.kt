package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object TextComponentDataFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    parameters = listOf("L", "L"),
    strings = listOf("text"),
    customFingerprint = { _, classDef ->
        val fields = classDef.fields
        fields.find { it.type == "Ljava/util/BitSet;" } != null &&
        fields.find { it.type == "[Ljava/lang/String;" } != null
    }
)