package app.revanced.patches.tumblr.featureflags.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// This fingerprint targets the method to get the value of a Feature in the class "com.tumblr.configuration.Feature".
// Features seem to be Tumblr's A/B testing program.
// Feature states are loaded from the server in the "api-http2.tumblr.com/v2/config" request on (first) startup.
// A lot of features are returned there, but most of them do not seem to do anything (anymore).
// They were likely removed in newer App versions to always be on, but are still returned
// as enabled for old App versions.
// Some features seem to be very old and never removed, though, such as Google Login.
// The startIndex of the opcode pattern is at the start of the function after the arg null check.
// we want to insert our instructions there.
internal object GetFeatureValueFingerprint : MethodFingerprint(
    strings = listOf("feature"),
    opcodes = listOf(
        Opcode.IF_EQZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT
    ),
    returnType = "Ljava/lang/String;",
    parameters = listOf("L", "Z"),
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL
)