package app.revanced.patches.youtube.flyoutpanel.feed.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object BottomSheetMenuItemBuilderFingerprint : MethodFingerprint(
    returnType = "L",
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT
    ),
    strings = listOf("ElementTransformer, ElementPresenter and InteractionLogger cannot be null")
)