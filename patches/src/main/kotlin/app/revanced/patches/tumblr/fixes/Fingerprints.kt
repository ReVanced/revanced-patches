package app.revanced.patches.tumblr.fixes

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint

// Fingerprint for the addQueryParam method from retrofit2
// https://github.com/square/retrofit/blob/trunk/retrofit/src/main/java/retrofit2/RequestBuilder.java#L186
// Injecting here allows modifying dynamically set query parameters
internal val addQueryParamFingerprint = fingerprint {
    parameters("Ljava/lang/String;", "Ljava/lang/String;", "Z")
    strings("Malformed URL. Base: ", ", Relative: ")
}

// Fingerprint for the parseHttpMethodAndPath method from retrofit2
// https://github.com/square/retrofit/blob/ebf87b10997e2136af4d335276fa950221852c64/retrofit/src/main/java/retrofit2/RequestFactory.java#L270-L302
// Injecting here allows modifying the path/query params of API endpoints defined via annotations
internal val httpPathParserFingerprint = fingerprint {
    opcodes(
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_BOOLEAN,
    )
    strings("Only one HTTP method is allowed. Found: %s and %s.")
}
