package app.revanced.patches.mifitness.misc.locale

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.syncBluetoothLanguageMethodMatch by composingFirstMethod {
    name("syncBluetoothLanguage")
    definingClass("Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer")
    opcodes(Opcode.MOVE_RESULT_OBJECT)
}
