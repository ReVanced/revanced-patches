package app.revanced.patches.mifitness.device.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MiFitnessBandLanguageFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer;" &&
                methodDef.name == "syncBluetoothLanguage"
    }
)
