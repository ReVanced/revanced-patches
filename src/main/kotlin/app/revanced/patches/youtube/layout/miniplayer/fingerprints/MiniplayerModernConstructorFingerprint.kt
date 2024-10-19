package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object MiniplayerModernConstructorFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    parameters = listOf("L"),
    literalSupplier = { 45623000L } // Magic number found in the constructor.
) {
    const val MODERN_FEATURE_FLAGS_ENABLED_KEY_LITERAL = 45622882L
    // In later targets this feature flag does nothing and is dead code.
    const val MODERN_MINIPLAYER_ENABLED_OLD_TARGETS_FEATURE_KEY = 45630429L
    const val DOUBLE_TAP_ENABLED_FEATURE_KEY_LITERAL = 45628823L
    const val DRAG_DROP_ENABLED_FEATURE_KEY_LITERAL = 45628752L
    const val INITIAL_SIZE_FEATURE_KEY_LITERAL = 45640023L
    const val ANIMATION_INTERPOLATION_FEATURE_KEY = 45647018L
    const val DROP_SHADOW_FEATURE_KEY = 45652223L
    const val ROUNDED_CORNERS_FEATURE_KEY = 45652224L
}