package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.shorts.HideShortsComponentsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object ShortsBottomBarContainerFingerprint : LiteralValueFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
    strings = listOf(
        "r_pfvc"
    ),
    literalSupplier = { HideShortsComponentsResourcePatch.bottomBarContainer }
)