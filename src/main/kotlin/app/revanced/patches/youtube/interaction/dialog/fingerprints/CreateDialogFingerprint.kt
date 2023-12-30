package app.revanced.patches.youtube.interaction.dialog.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object CreateDialogFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PROTECTED.value,
    listOf("L", "L", "Ljava/lang/String;"),
    listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL // dialog.show()
    )
)
