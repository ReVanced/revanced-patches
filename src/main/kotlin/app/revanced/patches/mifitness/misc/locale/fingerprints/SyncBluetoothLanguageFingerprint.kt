package app.revanced.patches.mifitness.misc.locale.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object SyncBluetoothLanguageFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer;" &&
            methodDef.name == "syncBluetoothLanguage"
    },
    opcodes = listOf(Opcode.MOVE_RESULT_OBJECT),
)
