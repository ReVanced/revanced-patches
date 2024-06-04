package app.revanced.patches.music.misc.integrations

import app.revanced.patches.music.misc.integrations.hooks.applicationInitHook
import app.revanced.patches.shared.misc.integrations.integrationsPatch

val integrationsPatch = integrationsPatch(
    applicationInitHook,
)
