package app.revanced.patches.protonvpn.splittunneling

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val uiUnlock = fingerprint {
    strings("currentModeAppNames")
    opcodes(
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_FROM16,
        Opcode.INVOKE_DIRECT_RANGE
    )
    custom { method, classdef ->
        method.name == "<init>" && classdef.type == "Lcom/protonvpn/android/redesign/settings/ui/SettingsViewModel\$SettingViewState\$SplitTunneling;"
    }
}

internal val settingInit = fingerprint {
    custom { method, classdef ->
        method.name == "applyRestrictions" &&
        classdef.type == "Lcom/protonvpn/android/settings/data/BaseApplyEffectiveUserSettings;"
    }
}