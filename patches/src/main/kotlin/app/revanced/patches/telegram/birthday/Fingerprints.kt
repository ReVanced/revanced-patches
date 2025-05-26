package app.revanced.patches.telegram.birthday

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Located @ org.telegram.messenger.BirthdayController#getState
internal val birthdayStateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Lorg/telegram/messenger/BirthdayController\$BirthdayState")
    parameters()
    opcodes(
            Opcode.MOVE_RESULT,
            Opcode.IF_EQZ,
            Opcode.RETURN_OBJECT
    )
    custom { methodDef, classDef ->
        methodDef.name == "getState" && classDef.endsWith("/BirthdayController;")
    }
}
