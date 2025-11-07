package app.revanced.patches.music.layout.notificationbutton

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal val topBarMenuItemImageViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters()
    literal { topBarMenuItemImageView }
}
