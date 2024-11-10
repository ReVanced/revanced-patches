package app.revanced.patches.tumblr.fixes

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val fixOldVersionsPatch = bytecodePatch(
    name = "Fix old versions",
    description = "Fixes old versions of the app (v33.2 and earlier) breaking due to Tumblr removing remnants of Tumblr" +
        " Live from the API, which causes many requests to fail. This patch has no effect on newer versions of the app.",
    use = false,
) {
    compatibleWith("com.tumblr")

    execute {
        val liveQueryParameters = listOf(
            ",?live_now",
            ",?live_streaming_user_id",
        )

        // Remove the live query parameters from the path when it's specified via a @METHOD annotation.
        for (liveQueryParameter in liveQueryParameters) {
            httpPathParserFingerprint.method.addInstructions(
                httpPathParserFingerprint.patternMatch!!.endIndex + 1,
                """
                    # urlPath = urlPath.replace(liveQueryParameter, "")
                    const-string p1, "$liveQueryParameter"
                    const-string p3, ""
                    invoke-virtual {p2, p1, p3}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
                    move-result-object p2
                """,
            )
        }

        // Remove the live query parameters when passed via a parameter which has the @Query annotation.
        // e.g. an API call could be defined like this:
        //  @GET("api/me/info")
        //  ApiResponse getCurrentUserInfo(@Query("fields[blog]") String value)
        // which would result in the path "api/me/inf0?fields[blog]=${value}"
        // Here we make sure that this value doesn't contain the broken query parameters.
        for (liveQueryParameter in liveQueryParameters) {
            addQueryParamFingerprint.method.addInstructions(
                0,
                """
                    # queryParameterValue = queryParameterValue.replace(liveQueryParameter, "")
                    const-string v0, "$liveQueryParameter"
                    const-string v1, ""
                    invoke-virtual {p2, v0, v1}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
                    move-result-object p2
                """,
            )
        }
    }
}
