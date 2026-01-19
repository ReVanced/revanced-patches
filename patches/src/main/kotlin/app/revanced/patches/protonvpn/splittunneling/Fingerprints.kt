package app.revanced.patches.protonvpn.splittunneling

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val enableSplitTunnelingUiFingerprint = fingerprint {
    strings("currentModeAppNames")
    opcodes(
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_FROM16,
        Opcode.INVOKE_DIRECT_RANGE
    )
}

internal val initializeSplitTunnelingSettingsUIFingerprint = fingerprint {
    custom { method, _ ->
        method.name == "applyRestrictions"
    }
}