package app.revanced.patches.youtube.misc.quic.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object ExperimentalCronetEngineBuilderFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC.value,
    parameters = listOf("Z"),
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("/ExperimentalCronetEngine\$Builder;") && methodDef.name == "enableQuic" }
)