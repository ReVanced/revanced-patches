package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.layout.theme.ThemePatch
import app.revanced.patches.youtube.misc.settings.settingsPatch

val themePatch = bytecodePatch(
    name = "Theme",
    description = "Adds options for theming and applies a custom background theme (dark background theme defaults to amoled black).",
) {
    dependsOn(
        ThemePatch,
        settingsPatch,
    )

    compatibleWith("com.google.android.youtube"(
        "18.38.44",
        "18.49.37",
        "19.16.39",
        "19.25.37",
        "19.34.42",
        "19.43.41",
        "19.45.38",
        "19.46.42",
        "19.47.53",
    ))
}
