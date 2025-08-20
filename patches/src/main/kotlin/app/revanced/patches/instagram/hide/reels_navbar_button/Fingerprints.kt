
package app.revanced.patches.instagram.hide.reels_navbar_button

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

val tabCreateButtonsFingerprint = fingerprint {
    returns("V")
    strings("InstagramMainActivity.createTabButtons")

    opcodes(
        Opcode.INVOKE_INTERFACE, // Ljava/util/List;->size()I
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_STATIC, // Customise Button
        Opcode.INVOKE_VIRTUAL, // Add Button
        //Opcode.SGET_OBJECT,
        //Opcode.IF_NE,
        //Opcode.INVOKE_VIRTUAL,
        //Opcode.MOVE_RESULT_OBJECT,
        //Opcode.INVOKE_VIRTUAL // getTag
    )
}