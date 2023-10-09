package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Adds telemetry to the share links
object AddTelemetryToLinkFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC.value or AccessFlags.STATIC.value or AccessFlags.FINAL.value,
    opcodes = listOf(Opcode.RETURN_OBJECT),
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;", "I", "Ljava/lang/String;"),
    strings = listOf("<this>", "shareParam", "sessionToken"),
)