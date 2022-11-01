package app.revanced.patches.youtube.layout.hidealbumcards.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hidealbumcards.annotations.AlbumCardsCompatibility
import app.revanced.patches.youtube.layout.hidealbumcards.resource.patch.AlbumCardsResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

@Name("album-cards-view-fingerprint")
@AlbumCardsCompatibility
@Version("0.0.1")
object AlbumCardsFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    ),
    customFingerprint = { methodDef ->
        methodDef.implementation?.instructions?.any { instruction ->
            instruction.opcode.ordinal == Opcode.CONST.ordinal &&
            (instruction as? WideLiteralInstruction)?.wideLiteral == AlbumCardsResourcePatch.albumCardId
        } == true
    }
)