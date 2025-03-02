package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val intentActionFingerprint = fingerprint {
    parameters("Landroid/content/Intent;")
    strings("has_handled_intent")
}

internal val browseIdFingerprint = fingerprint {
    returns("Lcom/google/android/apps/youtube/app/common/ui/navigation/PaneDescriptor;")
    parameters()
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
    strings("FEwhat_to_watch")
}
