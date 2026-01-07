package app.revanced.patches.blockerx.premiumunlock

import app.revanced.patcher.fingerprint

internal val getSubStatusFingerprint = fingerprint {
    returns("Z")
    parameters()
    custom { method, classDef ->
        method.name == "getSUB_STATUS" &&
            classDef.type == "Lio/funswitch/blocker/utils/sharePrefUtils/BlockerXAppSharePref;"
    }
}

