package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.addString
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import com.android.tools.smali.dexlib2.Opcode

internal val intentActionFingerprint = fingerprint {
    parameters("Landroid/content/Intent;")
    instructions(
        addString("has_handled_intent"),
    )
}

internal val browseIdFingerprint = fingerprint {
    returns("Lcom/google/android/apps/youtube/app/common/ui/navigation/PaneDescriptor;")

    // parameters() // 20.30 and earlier is no parameters.  20.31+ parameter is L.
    instructions(
        addString("FEwhat_to_watch"),
        512(),
        fieldAccess(opcode = Opcode.IPUT_OBJECT, type = "Ljava/lang/String;"),
    )
}
