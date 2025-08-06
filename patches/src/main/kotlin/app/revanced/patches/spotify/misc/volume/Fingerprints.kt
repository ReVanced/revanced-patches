package app.revanced.patches.spotify.misc.volume

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.reference.*;

internal val forceLocalPlaybackFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Lp/vgc;")

    strings("mediaSessionCompat")

    custom { method, _ ->
        method.indexOfFirstInstruction {
            getReference<MethodReference>()?.name == "setPlaybackToRemote"
        } >= 0
    }
}