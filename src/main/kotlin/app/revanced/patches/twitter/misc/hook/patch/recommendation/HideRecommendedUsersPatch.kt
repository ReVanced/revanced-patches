package app.revanced.patches.twitter.misc.hook.patch.recommendation

import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.twitter.misc.hook.json.JsonHookPatch
import app.revanced.patches.twitter.misc.hook.patch.BaseHookPatch

@Patch(
    name = "Hide recommended users",
    dependencies = [JsonHookPatch::class],
    compatiblePackages = [CompatiblePackage("com.twitter.android")]
)
@Suppress("unused")
object HideRecommendedUsersPatch : BaseHookPatch(
    "Lapp/revanced/integrations/twitter/patches/hook/patch/recommendation/RecommendedUsersHook;"
)