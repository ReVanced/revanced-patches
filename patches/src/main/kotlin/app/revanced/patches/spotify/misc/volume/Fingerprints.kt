package app.revanced.patches.spotify.misc.volume

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.reference.*

internal val forceLocalPlaybackFingerprint = fingerprint {
    returns("V")
    strings("mediaSessionCompat")

    // This custom check is NECESSARY for accuracy. It pinpoints the exact
    // method by finding the unique call to "setPlaybackToRemote".
    // The main reason is that there are 22 matches to the "mediaSessionCompat" string.
    custom { method, _ ->
        method.indexOfFirstInstructionOrThrow {
            getReference<MethodReference>()?.name == "setPlaybackToRemote"
        } >= 0 &&
        method.parameters.isNotEmpty()
    }
}
