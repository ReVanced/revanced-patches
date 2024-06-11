package app.revanced.patches.mifitness.misc.locale

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val syncBluetoothLanguageFingerprint = methodFingerprint {
    opcodes(Opcode.MOVE_RESULT_OBJECT)
    custom { method, _ ->
        method.name == "syncBluetoothLanguage" &&
            method.definingClass == "Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer;"
    }
}
