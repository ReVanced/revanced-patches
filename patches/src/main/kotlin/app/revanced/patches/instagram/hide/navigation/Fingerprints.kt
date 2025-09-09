
package app.revanced.patches.instagram.hide.navigation

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val tabCreateButtonsLoopStartFingerprint = fingerprint {
    returns("V")
    strings("InstagramMainActivity.createTabButtons")
    opcodes(
        //Loop Start
        Opcode.IF_GE, // Check if index is finished (index, size)
        //Injection
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT
    )
}

internal val tabCreateButtonsLoopEndFingerprint = fingerprint {
    returns("V")
    strings("InstagramMainActivity.createTabButtons")
    opcodes(
        Opcode.IPUT_OBJECT,
        // Injection Jump
        Opcode.ADD_INT_LIT8, //Increase Index
        Opcode.GOTO_16 // Jump to loopStart
        // LoopEnd
        )
}
