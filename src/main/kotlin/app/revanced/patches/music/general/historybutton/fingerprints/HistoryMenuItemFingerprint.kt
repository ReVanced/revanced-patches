package app.revanced.patches.music.general.historybutton.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.HistoryMenuItem
import app.revanced.util.containsWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object HistoryMenuItemFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Landroid/view/Menu;"),
    opcodes = listOf(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID
    ),
    customFingerprint = { methodDef, classDef ->
        methodDef.containsWideLiteralInstructionIndex(HistoryMenuItem)
                && classDef.methods.count() == 5
    }
)
