package app.revanced.patches.strava.password

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.logInGetUsePasswordMethod by gettingFirstMethodDeclaratively {
    name("getUsePassword")
    definingClass("/RequestOtpLogInNetworkResponse;")
}

internal val BytecodePatchContext.emailChangeGetUsePasswordMethod by gettingFirstMethodDeclaratively {
    name("getUsePassword")
    definingClass("/RequestEmailChangeWithOtpOrPasswordResponse;")
}
