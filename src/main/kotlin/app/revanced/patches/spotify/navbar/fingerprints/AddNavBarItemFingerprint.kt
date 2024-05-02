package app.revanced.patches.spotify.navbar.fingerprints

import app.revanced.patches.spotify.navbar.showBottomNavigationItemsTextId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val addNavBarItemFingerprint = literalValueFingerprint(literalSupplier = { showBottomNavigationItemsTextId }) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
}
