package app.revanced.patches.mifitness.misc.locale.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val syncBluetoothLanguageFingerprint = methodFingerprint {
    opcodes(
        Opcode.MOVE_RESULT_OBJECT
    )
    custom { methodDef, classDef ->
        classDef.type == "Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer;" &&
                methodDef.name == "syncBluetoothLanguage"
    }
}