package app.revanced.patches.mifitness.misc.locale

import app.revanced.patcher.*
import com.android.tools.smali.dexlib2.Opcode

internal val syncBluetoothLanguageMethodMatch = firstMethodComposite {
    name("syncBluetoothLanguage")
    definingClass("Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer")
    opcodes(Opcode.MOVE_RESULT_OBJECT)
}
