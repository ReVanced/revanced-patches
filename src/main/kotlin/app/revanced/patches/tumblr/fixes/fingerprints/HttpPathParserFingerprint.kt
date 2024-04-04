package app.revanced.patches.tumblr.fixes.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

// Fingerprint for the parseHttpMethodAndPath method from retrofit2
// https://github.com/square/retrofit/blob/ebf87b10997e2136af4d335276fa950221852c64/retrofit/src/main/java/retrofit2/RequestFactory.java#L270-L302
// Injecting here allows modifying the path/query params of API endpoints defined via annotations
internal object HttpPathParserFingerprint : MethodFingerprint(
    strings = listOf("Only one HTTP method is allowed. Found: %s and %s."),
    opcodes = listOf(
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_BOOLEAN,
    ),
)
