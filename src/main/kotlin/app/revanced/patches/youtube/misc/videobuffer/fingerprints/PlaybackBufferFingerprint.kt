package app.revanced.patches.youtube.misc.videobuffer.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.videobuffer.annotations.CustomVideoBufferCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction

@Name("playbackbuffer-fingerprint")
@MatchingMethod(
    "Lcom/google/android/libraries/youtube/innertube/model/media/PlayerConfigModel;", "p"
)
@DirectPatternScanMethod
@CustomVideoBufferCompatibility
@Version("0.0.1")
object PlaybackBufferFingerprint : MethodFingerprint(
    "I", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf(),
    listOf(Opcode.IF_LEZ, Opcode.RETURN),
    null,
    customFingerprint = { methodDef ->
        methodDef.definingClass.equals("Lcom/google/android/libraries/youtube/innertube/model/media/PlayerConfigModel;")
                && methodDef.implementation!!.instructions.any {
            ((it as? NarrowLiteralInstruction)?.narrowLiteral == 1600)
        }
    }
)