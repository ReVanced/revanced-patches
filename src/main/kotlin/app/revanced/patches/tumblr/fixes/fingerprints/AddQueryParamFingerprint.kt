package app.revanced.patches.tumblr.fixes.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

// Fingerprint for the addQueryParam method from retrofit2
// https://github.com/square/retrofit/blob/trunk/retrofit/src/main/java/retrofit2/RequestBuilder.java#L186
// Injecting here allows modifying dynamically set query parameters
internal val addQueryParamFingerprint = methodFingerprint {
    parameters("Ljava/lang/String;", "Ljava/lang/String;", "Z")
    strings("Malformed URL. Base: ", ", Relative: ")
}
