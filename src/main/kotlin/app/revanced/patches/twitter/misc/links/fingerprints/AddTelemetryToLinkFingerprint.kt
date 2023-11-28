package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Adds telemetry to the share links
object AddTelemetryToLinkFingerprint : MethodFingerprint(
    strings = listOf("<this>", "shareParam", "sessionToken"),
)