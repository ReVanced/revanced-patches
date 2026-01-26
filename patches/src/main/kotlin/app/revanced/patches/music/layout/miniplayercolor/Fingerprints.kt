package app.revanced.patches.music.layout.miniplayercolor

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val miniPlayerConstructorMethodMatch = firstMethodComposite {
    returnType("V")
    instructions(
        ResourceType.ID("mpp_player_bottom_sheet"),
        "sharedToggleMenuItemMutations"(),
    )
}

/**
 * Matches to the class found in [miniPlayerConstructorMethodMatch].
 */
internal val switchToggleColorMethodMatch = firstMethodComposite {
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
