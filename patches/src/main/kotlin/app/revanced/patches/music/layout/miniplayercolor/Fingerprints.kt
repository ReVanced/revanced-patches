package app.revanced.patches.music.layout.miniplayercolor

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val miniPlayerConstructorFingerprint = fingerprint {
    returns("V")
    strings("sharedToggleMenuItemMutations")
    literal { mpp_player_bottom_sheet }
}

/**
 * Matches to the class found in [miniPlayerConstructorFingerprint].
 */
internal val switchToggleColorFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    parameters("L", "J")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IGET
    )
}
