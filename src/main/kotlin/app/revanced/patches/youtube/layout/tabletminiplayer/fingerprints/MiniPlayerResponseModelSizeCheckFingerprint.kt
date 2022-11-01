package app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.annotations.TabletMiniPlayerCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("mini-player-response-model-size-check-fingerprint")
@TabletMiniPlayerCompatibility
@Version("0.0.1")
object MiniPlayerResponseModelSizeCheckFingerprint : MethodFingerprint(
    "L",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("L", "L"),
    listOf(
        Opcode.RETURN_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
    )
)