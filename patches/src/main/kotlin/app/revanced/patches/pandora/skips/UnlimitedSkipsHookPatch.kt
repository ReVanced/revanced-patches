package app.revanced.patches.pandora.skips

import app.revanced.patches.pandora.misc.hook.hookPatch

@Suppress("unused")
val unlimitedSkipsHookPatch = hookPatch(
    name = "Unlimited skips",
    hookClassDescriptor = "Lapp/revanced/extension/pandora/skips/UnlimitedSkipsHook;",
)