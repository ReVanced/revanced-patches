package app.revanced.patches.music.layout.miniplayercolor

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.miniPlayerConstructorMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    instructions(
        ResourceType.ID("mpp_player_bottom_sheet"),
    )
    strings("sharedToggleMenuItemMutations")
}

/**
 * Matches to the class found in [miniPlayerConstructorMethod].
 */
internal val BytecodePatchContext.switchToggleColorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L", "J")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IGET,
    )
}
