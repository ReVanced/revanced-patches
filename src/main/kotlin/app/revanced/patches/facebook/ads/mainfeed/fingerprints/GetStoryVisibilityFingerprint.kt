package app.revanced.patches.facebook.ads.mainfeed.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Annotation
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal object GetStoryVisibilityFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = (AccessFlags.PUBLIC or AccessFlags.STATIC),
    opcodes = listOf(
        Opcode.INSTANCE_OF,
        Opcode.IF_NEZ,
        Opcode.INSTANCE_OF,
        Opcode.IF_NEZ,
        Opcode.INSTANCE_OF,
        Opcode.IF_NEZ,
        Opcode.CONST
    ),
    strings = listOf("This should not be called for base class object"),
)