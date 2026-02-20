package app.revanced.patches.gamehub.misc.ota

import app.revanced.patcher.fingerprint

internal val baseOtaRepositoryFingerprint = fingerprint {
    strings("https://www.xiaoji.com/firmware/update/x1/")
}
