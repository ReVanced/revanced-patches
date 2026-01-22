package app.revanced.patches.mifitness.misc.locale

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.syncBluetoothLanguageMethod by gettingFirstMutableMethodDeclaratively {
    name("syncBluetoothLanguage")
    definingClass("Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer")
    opcodes(Opcode.MOVE_RESULT_OBJECT)
}
