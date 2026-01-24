package app.revanced.patches.strava.password

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.logInGetUsePasswordMethod by gettingFirstMutableMethodDeclaratively {
    name("getUsePassword")
    definingClass { endsWith("/RequestOtpLogInNetworkResponse;") }
}

internal val BytecodePatchContext.emailChangeGetUsePasswordMethod by gettingFirstMutableMethodDeclaratively {
    name("getUsePassword")
    definingClass { endsWith("/RequestEmailChangeWithOtpOrPasswordResponse;") }
}
