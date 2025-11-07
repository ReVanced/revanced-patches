package app.revanced.patches.music.layout.historybutton

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.Opcode

internal val historyMenuItemFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/Menu;")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID
    )
    literal { historyMenuItem }
    custom { _, classDef ->
        classDef.methods.count() == 5
    }
}

internal val historyMenuItemOfflineTabFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/Menu;")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID
    )
    literal { historyMenuItem }
    literal { offlineSettingsMenuItem }
}
