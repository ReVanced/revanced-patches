package app.revanced.patches.youtube.misc.backgroundplayback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object BackgroundPlaybackManagerShortsFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("L"),
    returnType = "Z",
    literalSupplier = { 151635310 },
)
