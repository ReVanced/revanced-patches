package app.revanced.patches.youtube.video.videoid.fingerprint

import app.revanced.patcher.extensions.or
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object VideoIdParentFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "[L",
    parameters = listOf("L"),
    literalSupplier = { 524288L }
)