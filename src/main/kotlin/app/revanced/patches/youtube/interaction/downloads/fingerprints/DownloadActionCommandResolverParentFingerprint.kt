package app.revanced.patches.youtube.interaction.downloads.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object DownloadActionCommandResolverParentFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("L", "L"),
    strings = listOf(
        // Strings are not unique and found in other methods.
        "com.google.android.libraries.youtube.logging.interaction_logger",
        "Unknown command"
        ),
    literalSupplier = { 16 }
)
