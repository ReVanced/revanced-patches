package app.revanced.patches.strava.password

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val logInGetUsePasswordFingerprint = fingerprint {
    opcodes(Opcode.IGET_BOOLEAN)
    custom { method, classDef ->
        method.name == "getUsePassword" && classDef.endsWith("/RequestOtpLogInNetworkResponse;")
    }
}

internal val emailChangeGetUsePasswordFingerprint = fingerprint {
    opcodes(Opcode.IGET_BOOLEAN)
    custom { method, classDef ->
        method.name == "getUsePassword" && classDef.endsWith("/RequestEmailChangeWithOtpOrPasswordResponse;")
    }
}
