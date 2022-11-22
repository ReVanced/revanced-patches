package app.revanced.patches.twitter.ad.timeline.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object TimelineTweetJsonParserFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IPUT_OBJECT,
        Opcode.GOTO,
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IPUT_OBJECT,
        Opcode.RETURN_VOID,
    ), strings = listOf("tweetPromotedMetadata", "promotedMetadata", "hasModeratedReplies", "conversationAnnotation"),
    customFingerprint = { methodDef -> methodDef.name == "parseField" }
)