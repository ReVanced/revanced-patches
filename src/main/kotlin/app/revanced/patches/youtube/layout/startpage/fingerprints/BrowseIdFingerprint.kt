package app.revanced.patches.youtube.layout.startpage.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object BrowseIdFingerprint : MethodFingerprint(
    returnType = "Lcom/google/android/apps/youtube/app/common/ui/navigation/PaneDescriptor;",
    parameters = listOf(),
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    ),
    strings = listOf("FEwhat_to_watch")
)