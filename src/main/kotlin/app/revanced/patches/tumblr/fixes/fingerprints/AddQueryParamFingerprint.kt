package app.revanced.patches.tumblr.fixes.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

// Fingerprint for the addQueryParam method from retrofit2
// https://github.com/square/retrofit/blob/trunk/retrofit/src/main/java/retrofit2/RequestBuilder.java#L186
// Injecting here allows modifying dynamically set query parameters
internal object AddQueryParamFingerprint : MethodFingerprint(
    strings = listOf("Malformed URL. Base: ", ", Relative: "),
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;", "Z"),
)
