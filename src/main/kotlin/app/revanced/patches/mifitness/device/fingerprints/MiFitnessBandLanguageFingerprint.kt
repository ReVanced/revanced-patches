package app.revanced.patches.mifitness.device.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object MiFitnessBandLanguageFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer;" &&
                methodDef.name == "syncBluetoothLanguage"
    },
    opcodes = listOf(Opcode.MOVE_RESULT_OBJECT)
)
