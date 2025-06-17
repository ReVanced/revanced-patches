package app.revanced.patches.meta.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Deprecated("Instead use the Instagram or Threads specific hide ads patch")
@Suppress("unused")
val hideAdsPatch = bytecodePatch {
    execute {
        adInjectorFingerprint.method.returnEarly(false)
    }
}
