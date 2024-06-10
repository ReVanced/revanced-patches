package app.revanced.patches.spotify.navbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.spotify.navbar.showBottomNavigationItemsTextId
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val addNavBarItemFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    literal { showBottomNavigationItemsTextId }
}
