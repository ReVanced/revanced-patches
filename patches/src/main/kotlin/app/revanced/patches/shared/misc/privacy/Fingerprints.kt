package app.revanced.patches.shared.misc.privacy

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.checkCast
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.Opcode

internal val youTubeCopyTextFingerprint = fingerprint {
    returns("V")
    parameters("L", "Ljava/util/Map;")
    instructions(
        opcode(Opcode.IGET_OBJECT),
        string("text/plain", location = MatchAfterWithin(2)),
        methodCall(
            smali = "Landroid/content/ClipData;->newPlainText(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Landroid/content/ClipData;",
            location = MatchAfterWithin(2)
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT, location = MatchAfterWithin(2)),
        methodCall(
            smali = "Landroid/content/ClipboardManager;->setPrimaryClip(Landroid/content/ClipData;)V",
            location = MatchAfterWithin(2)
        )
    )
}

internal val youTubeSystemShareSheetFingerprint = fingerprint {
    returns("V")
    parameters("L", "Ljava/util/Map;")
    instructions(
        methodCall(
            smali = "Landroid/content/Intent;->setClassName(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;"
        ),

        methodCall(
            smali = "Ljava/util/List;->iterator()Ljava/util/Iterator;",
            location = MatchAfterWithin(4)
        ),

        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            type = "Ljava/lang/String;",
            location = MatchAfterWithin(15)
        ),

        methodCall(
            smali = "Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;",
            location = MatchAfterWithin(15)
        )
    )
}

internal val youTubeShareSheetFingerprint = fingerprint {
    returns("V")
    parameters("L", "Ljava/util/Map;")
    instructions(
        opcode(Opcode.IGET_OBJECT),
        checkCast("Ljava/lang/String;", location = MatchAfterImmediately()),
        opcode(Opcode.GOTO, location = MatchAfterImmediately()),

        methodCall(smali = "Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;"),

        string("YTShare_Logging_Share_Intent_Endpoint_Byte_Array")
    )
}
