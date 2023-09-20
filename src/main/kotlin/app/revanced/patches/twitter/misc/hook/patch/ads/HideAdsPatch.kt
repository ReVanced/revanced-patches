package app.revanced.patches.twitter.misc.hook.patch.ads

import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.twitter.misc.hook.json.JsonHookPatch
import app.revanced.patches.twitter.misc.hook.patch.BaseHookPatchPatch

@Patch(
    name = "Hide ads",
    description = "Hides ads.",
    dependencies = [JsonHookPatch::class],
    compatiblePackages = [CompatiblePackage("com.twitter.android")]
)
@Suppress("unused")
object HideAdsPatch : BaseHookPatchPatch("Lapp/revanced/twitter/patches/hook/patch/ads/AdsHook;")