package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import com.android.tools.smali.dexlib2.AccessFlags

internal val encoreThemeFingerprint = fingerprint {
    strings("Encore theme was not provided. Please wrap your content with ProvideEncoreTheme. For @Previews use com.spotify.encore.tooling.preview.EncorePreview()")
}

internal const val HOME_CATEGORY_PILL_COLOR_LITERAL = 45412406L

internal val homeCategoryPillColorsFingerprint = fingerprint{
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    custom { method, _ ->
        method.containsLiteralInstruction(0x33000000)
                && method.containsLiteralInstruction(HOME_CATEGORY_PILL_COLOR_LITERAL)
    }
}