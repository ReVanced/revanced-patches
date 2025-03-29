package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.patch.stringOption

val backgroundColor by stringOption(
    key = "backgroundColor",
    default = "#fff2d2bd",
    title = "Primary background color",
    description = "The background color. Can be a hex color or a resource reference.",
    required = true,
)

val backgroundColorSecondary by stringOption(
    key = "backgroundColorSecondary",
    default = "#ff121212",
    title = "Secondary background color",
    description = "The secondary background color. (e.g. playlist list, player arist, credits). Can be a hex color or a resource reference.",
    required = true,
)

val accentColor by stringOption(
    key = "accentColor",
    default = "#ff1ed760",
    title = "Accent color",
    description = "The accent color ('Spotify green' by default). Can be a hex color or a resource reference.",
    required = true,
)

val accentColorPressed by stringOption(
    key = "accentColorPressed",
    default = "#ff169c46",
    title = "Pressed dark theme accent color",
    description =
        "The color when accented buttons are pressed, by default slightly darker than accent. Can be a hex color or a resource reference.",
    required = true,
)
