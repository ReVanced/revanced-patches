package app.revanced.patches.viber.misc.navbar
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext

internal val tabIdClassFingerprint by fingerprint {
    strings("shouldShowTabId")
}

context(BytecodePatchContext)
internal val shouldShowTabIdMethodFingerprint get() = fingerprint {
    parameters("I", "I")
    returns("Z")
    custom { methodDef, classDef ->
        classDef == tabIdClassFingerprint.classDef
    }
}
