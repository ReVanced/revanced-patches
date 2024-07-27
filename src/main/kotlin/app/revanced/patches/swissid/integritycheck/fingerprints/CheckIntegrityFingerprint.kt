package app.revanced.patches.swissid.integritycheck.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object CheckIntegrityFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Lcom/swisssign/deviceintegrity/model/DeviceIntegrityResult;"),
    strings = listOf("it", "result")
)
