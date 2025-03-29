package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.fingerprint

internal val encoreThemeFingerprint = fingerprint {
    strings("Encore theme was not provided. Please wrap your content with ProvideEncoreTheme. For @Previews use com.spotify.encore.tooling.preview.EncorePreview()")
}
