package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object FullscreenSeekbarThumbnailsQualityFingerprint : LiteralValueFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf(),
    literalSupplier = { 45399684L }
)