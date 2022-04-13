package app.revanced.signatures.ad

import app.revanced.patcher.extensions.AccessFlagExtensions.Companion.or
import app.revanced.patcher.signature.*
import app.revanced.signatures.SignatureSupplier
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

class VideoAdsSignature : SignatureSupplier {
    override fun get() = MethodSignature(
        "show-video-ads-constructor",
        SignatureMetadata(
            method = MethodMetadata(
                definingClass = "zai",
                methodName = "<init>",
                comment = "YouTube v17.03.38"
            ),
            patcher = PatcherMetadata(
                // FIXME: Test this threshold and find the best value.
                resolverMethod = ResolverMethod.Fuzzy(2)
            )
        ),
        "V",
        AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
        listOf("L", "L", "L"),
        listOf(
            Opcode.INVOKE_DIRECT,
            Opcode.NEW_INSTANCE,
            Opcode.INVOKE_DIRECT,
            Opcode.IPUT_OBJECT,
            Opcode.NEW_INSTANCE,
            Opcode.CONST_4,
            Opcode.INVOKE_DIRECT,
            Opcode.IPUT_OBJECT,
            Opcode.NEW_INSTANCE,
            Opcode.INVOKE_DIRECT,
            Opcode.IPUT_OBJECT,
            Opcode.IPUT_OBJECT,
            Opcode.IPUT_OBJECT,
            Opcode.CONST_4,
            Opcode.IPUT_BOOLEAN,
            Opcode.RETURN_VOID
        )
    )
}