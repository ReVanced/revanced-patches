package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object MiniplayerModernConstructorFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    parameters = listOf("L"),
    literalSupplier = { 45623000L } // Magic number found in the constructor.
) {
    const val MODERN_MINIPLAYER_ENABLED_FEATURE_KEY_LITERAL = 45622882L
    const val DOUBLE_TAP_ENABLED_FEATURE_KEY_LITERAL = 45628823L
    const val DRAG_DROP_ENABLED_FEATURE_KEY_LITERAL = 45628752L
    const val MINIPLAYER_SIZE_FEATURE_KEY_LITERAL = 45640023L
}