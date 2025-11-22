package app.revanced.patches.music.layout.miniplayercolor

import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val miniPlayerConstructorFingerprint = fingerprint {
    returns("V")
    instructions(
        resourceLiteral(ResourceType.ID, "mpp_player_bottom_sheet")
    )
    strings("sharedToggleMenuItemMutations")
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
