package app.revanced.patches.youtube.layout.hide.shorts.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.util.patch.LiteralValueFingerprint
import app.revanced.patches.youtube.layout.hide.shorts.resource.patch.HideShortsComponentsResourcePatch
import org.jf.dexlib2.AccessFlags

object CreateShortsButtonsFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("Z", "Z", "L"),
    literal = HideShortsComponentsResourcePatch.reelPlayerRightLargeIconSize
)