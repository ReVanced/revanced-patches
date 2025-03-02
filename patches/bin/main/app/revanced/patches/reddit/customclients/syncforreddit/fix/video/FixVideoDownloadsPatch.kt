package app.revanced.patches.reddit.customclients.syncforreddit.fix.video

import app.revanced.patcher.patch.bytecodePatch

@Deprecated(
    message = "Patch was move to a different package",
    ReplaceWith("app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.video.fixVideoDownloadsPatch")
)
@Suppress("unused")
val fixVideoDownloadsPatch = bytecodePatch {
    dependsOn(app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.video.fixVideoDownloadsPatch)

    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )
}