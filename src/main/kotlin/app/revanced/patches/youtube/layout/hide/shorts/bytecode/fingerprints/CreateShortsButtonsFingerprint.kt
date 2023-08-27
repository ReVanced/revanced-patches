package app.revanced.patches.youtube.layout.hide.shorts.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.shorts.resource.patch.HideShortsComponentsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object CreateShortsButtonsFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("Z", "Z", "L"),
    literal = HideShortsComponentsResourcePatch.reelPlayerRightCellButtonHeight
)