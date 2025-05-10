package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.thumbnail

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val fixPostThumbnailsPatch = bytecodePatch(
    name = "Fix post thumbnails",
    description = "Fixes loading post thumbnails by correcting their URLs.",
) {

    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev"
    )

    // Image URLs contain escaped ampersands (&amp;), let's replace these with unescaped ones (&).
    execute {
        customImageViewLoadFingerprint.method.addInstructions(
            0,
            """
	            # url = url.replace("&amp;", "&");
	            const-string v0, "&amp;"
	            const-string v1, "&"
	            invoke-virtual { p1, v0, v1 }, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
	            move-result-object p1
	        """
        )
    }
}
