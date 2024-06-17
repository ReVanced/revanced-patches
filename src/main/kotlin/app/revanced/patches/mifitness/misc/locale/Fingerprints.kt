package app.revanced.patches.mifitness.misc.locale

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val syncBluetoothLanguageFingerprint = fingerprint {
    opcodes(Opcode.MOVE_RESULT_OBJECT)
    custom { method, _ ->
        method.name == "syncBluetoothLanguage" &&
            method.definingClass == "Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer;"
    }
}
