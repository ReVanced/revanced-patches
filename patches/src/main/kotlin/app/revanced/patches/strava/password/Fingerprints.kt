package app.revanced.patches.strava.password

import app.revanced.patcher.fingerprint

internal val logInGetUsePasswordFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getUsePassword" && classDef.endsWith("/RequestOtpLogInNetworkResponse;")
    }
}

internal val emailChangeGetUsePasswordFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getUsePassword" && classDef.endsWith("/RequestEmailChangeWithOtpOrPasswordResponse;")
    }
}
