package app.revanced.patches.twitter.misc.hook.patch.recommendation.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.twitter.misc.hook.json.patch.JsonHookPatch
import app.revanced.patches.twitter.misc.hook.patch.BaseHookPatchPatch
import app.revanced.patches.twitter.misc.hook.patch.recommendation.annotations.HideRecommendedUsersCompatibility

@Patch
@Name("hide-recommended-users")
@DependsOn([JsonHookPatch::class])
@Description("Hides recommended users.")
@HideRecommendedUsersCompatibility
@Version("0.0.1")
class HideRecommendedUsersPatch : BaseHookPatchPatch(HOOK_CLASS_DESCRIPTOR) {
    private companion object {
        const val HOOK_CLASS_DESCRIPTOR =
            "Lapp/revanced/twitter/patches/hook/patch/recommendation/RecommendedUsersHook;"
    }
}