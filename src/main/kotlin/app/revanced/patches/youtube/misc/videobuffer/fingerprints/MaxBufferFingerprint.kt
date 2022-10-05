package app.revanced.patches.youtube.misc.videobuffer.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.videobuffer.annotations.CustomVideoBufferCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction

@Name("maxbuffer-fingerprint")
@CustomVideoBufferCompatibility
@Version("0.0.1")
object MaxBufferFingerprint : MethodFingerprint(
    "I", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf(),
    listOf(Opcode.SGET_OBJECT, Opcode.IGET, Opcode.IF_EQZ, Opcode.RETURN),
    customFingerprint = { methodDef ->
        methodDef.definingClass == "Lcom/google/android/libraries/youtube/innertube/model/media/PlayerConfigModel;"
                && methodDef.implementation!!.instructions.any {
            ((it as? NarrowLiteralInstruction)?.narrowLiteral == 120000)
                    && methodDef.name == "r"
        }
    }
)