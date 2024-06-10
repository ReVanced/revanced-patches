package app.revanced.patches.twitter.misc.hook.patch

@Suppress("unused")
val hideRecommendedUsersPatch = hookPatch(
    name = "Hide recommended users",
    hookClassDescriptor = "Lapp/revanced/integrations/twitter/patches/hook/patch/recommendation/RecommendedUsersHook;",
)
