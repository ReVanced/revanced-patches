package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.patch.stringOption

internal val spotifyBackgroundColor = stringOption(
    key = "backgroundColor",
    default = "@android:color/black",
    title = "Primary background color",
    description = "The background color. Can be a hex color or a resource reference.",
    required = true,
)

internal val spotifyBackgroundColorSecondary = stringOption(
    key = "backgroundColorSecondary",
    default = "#FF121212",
    title = "Secondary background color",
    description = "The secondary background color. (e.g. playlist list, player arist, credits). Can be a hex color or a resource reference.",
    required = true,
)

internal val spotifyAccentColor = stringOption(
    key = "accentColor",
    default = "#FF1ED760",
    title = "Accent color",
    description = "The accent color ('Spotify green' by default). Can be a hex color or a resource reference.",
    required = true,
)

internal val spotifyAccentColorPressed = stringOption(
    key = "accentColorPressed",
    default = "#FF169C46",
    title = "Pressed dark theme accent color",
    description =
        "The color when accented buttons are pressed, by default slightly darker than accent. Can be a hex color or a resource reference.",
    required = true,
)
