package app.revanced.patches.viber.misc.navbar
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext

internal val tabIdClassFingerprint = fingerprint {
    strings("shouldShowTabId")
}

context(_: BytecodePatchContext)
internal val shouldShowTabIdMethodFingerprint get() = fingerprint {
    parameters("I", "I")
    returns("Z")
    custom { _, classDef ->
        classDef == tabIdClassFingerprint.classDef
    }
}
