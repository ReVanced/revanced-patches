package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import com.android.tools.smali.dexlib2.AccessFlags

internal val encoreThemeFingerprint = fingerprint {
    strings("Encore theme was not provided.") // Partial string match.
}

internal const val SETTINGS_HEADER_COLOR_LITERAL = 0xFF282828
internal const val HOME_CATEGORY_PILL_COLOR_LITERAL = 0xFF333333
internal const val PLAYLIST_BACKGROUND_COLOR_LITERAL = 0xFF121212
internal const val SHARE_MENU_BACKGROUND_COLOR_LITERAL = 0xFF1F1F1F

internal val homeCategoryPillColorsFingerprint = fingerprint{
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    custom { method, _ ->
        method.containsLiteralInstruction(HOME_CATEGORY_PILL_COLOR_LITERAL) &&
                method.containsLiteralInstruction(0x33000000)
    }
}

internal val settingsHeaderColorFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    custom { method, _ ->
        method.containsLiteralInstruction(SETTINGS_HEADER_COLOR_LITERAL) &&
                method.containsLiteralInstruction(0)
    }
}
