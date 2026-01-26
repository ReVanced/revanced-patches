package app.revanced.patches.protonvpn.splittunneling

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val enableSplitTunnelingUiMethodMatch = firstMethodComposite("currentModeAppNames") {
    opcodes(
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_FROM16,
        Opcode.INVOKE_DIRECT_RANGE
    )
}

internal val BytecodePatchContext.initializeSplitTunnelingSettingsUIMethod by gettingFirstMutableMethodDeclaratively {
    name("applyRestrictions")
}