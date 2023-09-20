package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.shorts.HideShortsComponentsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object CreateShortsButtonsFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("Z", "Z", "L"),
    literalSupplier = { HideShortsComponentsResourcePatch.reelPlayerRightCellButtonHeight }
)