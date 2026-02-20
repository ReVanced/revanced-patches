package app.revanced.patches.gamehub.misc.logserver

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.StringReference

internal val logHttpServerPageFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/winemu/core/server/log/LogHttpServer;" &&
            method.implementation?.instructions?.any { instruction ->
                instruction.opcode == Opcode.CONST_STRING &&
                    instruction.getReference<StringReference>()?.string?.contains("WinEmu Log Server") == true
            } == true
    }
}
