package app.revanced.patches.youtube.layout.hide.signintotvpopup

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val signInToTvPopupFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters("Ljava/lang/String;", "Z", "L")
    opcodes(
        Opcode.CONST, //mdx_seamless_tv_sign_in_drawer_fragment_title
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.CONST, //mdx_seamless_tv_sign_in_drawer_fragment_subtitle
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.MOVE,
        Opcode.MOVE_OBJECT,
        Opcode.INVOKE_VIRTUAL_RANGE,
        Opcode.MOVE_RESULT,
    )
}