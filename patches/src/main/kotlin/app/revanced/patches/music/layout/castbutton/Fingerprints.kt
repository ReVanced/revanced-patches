package app.revanced.patches.music.layout.castbutton

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal val mediaRouteButtonFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("Z")
    strings("MediaRouteButton")
}

internal val playerOverlayChipFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    literal { playerOverlayChip }
}
