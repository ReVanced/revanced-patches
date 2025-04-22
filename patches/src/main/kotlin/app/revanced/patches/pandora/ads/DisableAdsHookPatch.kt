package app.revanced.patches.pandora.ads

import app.revanced.patches.pandora.misc.hook.hookPatch

@Suppress("unused")
val disableAdsHookPatch = hookPatch(
    name = "Disable audio ads",
    hookClassDescriptor = "Lapp/revanced/extension/pandora/ads/DisableAdsHook;",
)