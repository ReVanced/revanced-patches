package app.revanced.patches.twitter.misc.hook

@Suppress("unused")
val hideRecommendedUsersPatch = hookPatch(
    name = "Hide recommended users",
    hookClassDescriptor = "Lapp/revanced/extension/twitter/patches/hook/patch/recommendation/RecommendedUsersHook;",
)
