package app.revanced.patches.protonvpn.splittunneling

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.enableSplitTunnelingUiMethod by gettingFirstMutableMethodDeclaratively("currentModeAppNames") {
    opcodes(
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_FROM16,
        Opcode.INVOKE_DIRECT_RANGE
    )
}

internal val BytecodePatchContext.initializeSplitTunnelingSettingsUIMethod by gettingFirstMutableMethodDeclaratively {
    name("applyRestrictions")
}