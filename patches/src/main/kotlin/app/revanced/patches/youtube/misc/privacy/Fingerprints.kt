package app.revanced.patches.youtube.misc.privacy

import app.revanced.patcher.checkCast
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val copyTextFingerprint by fingerprint {
    returns("V")
    parameters("L", "Ljava/util/Map;")
    instructions(
        opcode(Opcode.IGET_OBJECT),
        string("text/plain", maxAfter = 2),
        methodCall(
            smali = "Landroid/content/ClipData;->newPlainText(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Landroid/content/ClipData;",
            maxAfter = 2
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxAfter = 2),
        methodCall(
            smali = "Landroid/content/ClipboardManager;->setPrimaryClip(Landroid/content/ClipData;)V",
            maxAfter = 2
        )
    )
}

internal val youtubeShareSheetFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "Ljava/util/Map;")
    instructions(
        methodCall(
            smali = "Landroid/content/Intent;->setClassName(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;"
        ),

        methodCall(
            smali = "Ljava/util/List;->iterator()Ljava/util/Iterator;",
            maxAfter = 4
        ),

        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            type = "Ljava/lang/String;",
            maxAfter = 15
        ),

        methodCall(
            smali = "Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;",
            maxAfter = 15
        )
    )
}

internal val systemShareSheetFingerprint by fingerprint {
    returns("V")
    parameters("L", "Ljava/util/Map;")
    instructions(
        opcode(Opcode.IGET_OBJECT),
        checkCast("Ljava/lang/String;", maxAfter = 0),
        opcode(Opcode.GOTO, maxAfter = 0),

        methodCall(smali = "Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;"),

        string("YTShare_Logging_Share_Intent_Endpoint_Byte_Array")
    )
}
