package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.interaction.seekbar.patch.EnableSeekbarTappingResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import org.jf.dexlib2.AccessFlags

object AccessibilityPlayerProgressTimeFingerprint : LiteralValueFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    literal = EnableSeekbarTappingResourcePatch.accessibilityPlayerProgressTime
)