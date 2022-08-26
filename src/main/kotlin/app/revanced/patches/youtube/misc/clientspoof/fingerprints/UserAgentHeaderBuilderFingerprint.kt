package app.revanced.patches.youtube.misc.clientspoof.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.clientspoof.annotations.ClientSpoofCompatibility
import org.jf.dexlib2.Opcode

@Name("user-agent-header-builder-fingerprint")
@ClientSpoofCompatibility
@DirectPatternScanMethod
@Version("0.0.1")
object UserAgentHeaderBuilderFingerprint : MethodFingerprint(
    null,
    null,
    listOf("L", "L", "L"),
    listOf(Opcode.MOVE_RESULT_OBJECT, Opcode.INVOKE_VIRTUAL),
    listOf("(Linux; U; Android "),
)