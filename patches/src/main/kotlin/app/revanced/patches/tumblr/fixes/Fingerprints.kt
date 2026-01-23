package app.revanced.patches.tumblr.fixes

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.Opcode

// Fingerprint for the addQueryParam method from retrofit2
// https://github.com/square/retrofit/blob/trunk/retrofit/src/main/java/retrofit2/RequestBuilder.java#L186
// Injecting here allows modifying dynamically set query parameters
internal val BytecodePatchContext.addQueryParamMethod by gettingFirstMethodDeclaratively {
    parameterTypes("Ljava/lang/String;", "Ljava/lang/String;", "Z")
    strings("Malformed URL. Base: ", ", Relative: ")
}

// Fingerprint for the parseHttpMethodAndPath method from retrofit2
// https://github.com/square/retrofit/blob/ebf87b10997e2136af4d335276fa950221852c64/retrofit/src/main/java/retrofit2/RequestFactory.java#L270-L302
// Injecting here allows modifying the path/query params of API endpoints defined via annotations
internal val BytecodePatchContext.httpPathParserMethod by gettingFirstMethodDeclaratively {
    opcodes(
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_BOOLEAN,
    )
    strings("Only one HTTP method is allowed. Found: %s and %s.")
}
