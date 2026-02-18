package app.revanced.patches.tumblr.fixes

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

// Matches the addQueryParam method from retrofit2:
// https://github.com/square/retrofit/blob/trunk/retrofit/src/main/java/retrofit2/RequestBuilder.java#L186.
// Injecting here allows modifying dynamically set query parameters.
internal val BytecodePatchContext.addQueryParamMethod by gettingFirstMethodDeclaratively("Malformed URL. Base: ", ", Relative: ") {
    parameterTypes("Ljava/lang/String;", "Ljava/lang/String;", "Z")
}

// Matches the parseHttpMethodAndPath method from retrofit2:
// https://github.com/square/retrofit/blob/ebf87b10997e2136af4d335276fa950221852c64/retrofit/src/main/java/retrofit2/RequestFactory.java#L270-L302
// Injecting here allows modifying the path/query params of API endpoints defined via annotations.
internal val BytecodePatchContext.httpPathParserMethodMatch by composingFirstMethod("Only one HTTP method is allowed. Found: %s and %s.") {
    opcodes(
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_BOOLEAN,
    )
}
