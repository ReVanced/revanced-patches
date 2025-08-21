package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.redgifs

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.customclients.sync.syncforreddit.extension.sharedExtensionPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/syncforreddit/FixRedgifsPatch;"
private const val FETCH_VIDEO_URL_METHOD = "fetchVideoUrl(Ljava/lang/String;ZLcom/android/volley/Response\$Listener;)V"

@Suppress("unused")
val fixRedgifsPatch = bytecodePatch(
    name = "Fix Redgifs",
    description = "Fixes Redgifs playback.",
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    execute {
        deliverRegifsOauthResponseFingerprint.method.addInstructions(
            0,
            """
                iget-object p1, p0, Lt8/c;->a:Lt8/d;
                iget-object p1, p1, Lt8/d;->a:Ljava/lang/String;
                iget-object v0, p0, Lt8/c;->a:Lt8/d;
                iget-boolean v0, v0, Lt8/d;->b:Z
                iget-object v1, p0, Lt8/c;->a:Lt8/d;
                iget-object v1, v1, Lt8/d;->e:Lcom/android/volley/Response${'$'}Listener;

                invoke-static { p1, v0, v1 }, $EXTENSION_CLASS_DESCRIPTOR->$FETCH_VIDEO_URL_METHOD
                return-void
            """,
        )
    }
}
