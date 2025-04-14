package app.revanced.patches.reddit.customclients.redditisfun.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption

val imgurAlbumsPatch = bytecodePatch(
    name = "Use public imgur API",
    description = "Fix imgur albums not loading."
) {
    compatibleWith(
        "com.andrewshu.android.reddit",
        "com.andrewshu.android.redditdonation",
    )

    val clientId by stringOption(
        key = "imgur-client-id",
        // Obtained from: https://s.imgur.com/desktop-assets/js/main.[snip].js | grep apiClientId
        default = "546c25a59c58ad7",
        title = "Imgur client ID",
        description = "The default value should work for most users",
        required = true,
    )

    execute {
        val m = imgurApiFingerprint.method
        m.removeInstructions(m.instructions.size)
        val androidNetUriBuilder = "android/net/Uri\$Builder"
        m.addInstructions(0, """
    new-instance v0, L$androidNetUriBuilder;
    invoke-direct {v0}, L$androidNetUriBuilder;-><init>()V
    const-string v1, "https"
    invoke-virtual {v0, v1}, L$androidNetUriBuilder;->scheme(Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object v0
    const-string v1, "api.imgur.com"
    invoke-virtual {v0, v1}, L$androidNetUriBuilder;->authority(Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object v0
    const-string v1, "3"
    invoke-virtual {v0, v1}, L$androidNetUriBuilder;->appendPath(Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object v0
    if-eqz p1, :cond_0
    const-string p1, "gallery"
    invoke-virtual {v0, p1}, L$androidNetUriBuilder;->appendPath(Ljava/lang/String;)L$androidNetUriBuilder;
    :cond_0
    const-string p1, "album"
    invoke-virtual {v0, p1}, L$androidNetUriBuilder;->appendPath(Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object p1
    invoke-virtual {p1, p0}, L$androidNetUriBuilder;->appendPath(Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object p0
    const-string v0, "client_id"
    const-string v1, "$clientId"
    invoke-virtual {p0, v0, v1}, L$androidNetUriBuilder;->appendQueryParameter(Ljava/lang/String;Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object p0
    invoke-virtual {p0}, L$androidNetUriBuilder;->build()Landroid/net/Uri;
    move-result-object p0
    return-object p0
        """.trimIndent())
    }
}
