package app.revanced.patches.strava.password

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val logInGetUsePasswordFingerprint = fingerprint {
    opcodes(Opcode.IGET_BOOLEAN)
    custom { method, classDef ->
        classDef.endsWith("/RequestOtpLogInNetworkResponse;") && method.name == "getUsePassword"
    }
}

internal val emailChangeGetUsePasswordFingerprint = fingerprint {
    opcodes(Opcode.IGET_BOOLEAN)
    custom { method, classDef ->
        classDef.endsWith("/RequestEmailChangeWithOtpOrPasswordResponse;") && method.name == "getUsePassword"
    }
}
