package app.revanced.patches.twitter.misc.hook.patch.ads

import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.twitter.misc.hook.json.JsonHookPatch
import app.revanced.patches.twitter.misc.hook.patch.BaseHookPatch

@Patch(
    name = "Hide ads",
    dependencies = [JsonHookPatch::class],
    compatiblePackages = [CompatiblePackage("com.twitter.android")],
)
@Suppress("unused")
object HideAdsHookPatch : BaseHookPatch("Lapp/revanced/integrations/twitter/patches/hook/patch/ads/AdsHook;")
