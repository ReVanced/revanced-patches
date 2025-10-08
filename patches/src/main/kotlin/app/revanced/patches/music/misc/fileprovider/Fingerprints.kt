package app.revanced.patches.music.utils.fix.fileprovider

import app.revanced.patcher.fingerprint

internal val fileProviderResolverFingerprint = fingerprint {
    returns("L")
    strings(
        "android.support.FILE_PROVIDER_PATHS",
        "Name must not be empty"
    )
}