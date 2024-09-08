package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerModernEnabledFingerprint.MODERN_MINIPLAYER_ENABLED_FEATURE_KEY_LITERAL
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object MiniplayerModernEnabledFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    parameters = listOf("L"),
    literalSupplier = { MODERN_MINIPLAYER_ENABLED_FEATURE_KEY_LITERAL }
) {
    const val MODERN_MINIPLAYER_ENABLED_FEATURE_KEY_LITERAL = 45630429L
}