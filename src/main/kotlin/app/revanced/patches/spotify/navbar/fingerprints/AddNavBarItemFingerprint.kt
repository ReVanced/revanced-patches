package app.revanced.patches.spotify.navbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.spotify.navbar.PremiumNavbarTabResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object AddNavBarItemFingerprint : LiteralValueFingerprint(
     returnType = "V",
     accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
     literalSupplier = { PremiumNavbarTabResourcePatch.showBottomNavigationItemsTextId },
)