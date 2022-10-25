package app.revanced.patches.youtube.layout.hidealbumcards.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hidealbumcards.annotations.AlbumCardsCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("album-cards-view-fingerprint")
@AlbumCardsCompatibility
@Version("0.0.1")
object AlbumCardsFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    listOf("L", "L", "L", "L", "L", "L", "[B"),
    listOf(
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    )
)