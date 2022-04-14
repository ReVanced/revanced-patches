package app.revanced.patches.layout

import app.revanced.patcher.PatcherData
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.*
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

private val compatiblePackages = listOf("com.google.android.youtube")

class HideSuggestionsPatch : Patch(
    metadata = PatchMetadata(
        "hide-suggestions",
        "Hide suggestions patch",
        "Hide suggested videos.",
        compatiblePackages,
        "1.0.0"
    ),
    signatures = listOf(
        MethodSignature(
            methodSignatureMetadata = MethodSignatureMetadata(
                name = "hide-suggestions-parent-method",
                methodMetadata = MethodMetadata(null, null), // unknown
                patternScanMethod = PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages = compatiblePackages,
                description = "Signature for a parent method, which is needed to find the actual method required to be patched.",
                version = "0.0.1"
            ),
            returnType = "V",
            accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
            methodParameters = listOf("L", "Z"),
            opcodes = listOf(
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.IPUT_OBJECT,
                Opcode.CONST_16,
                Opcode.INVOKE_VIRTUAL,
                Opcode.INVOKE_VIRTUAL,
                Opcode.NEW_INSTANCE,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.INVOKE_DIRECT,
                Opcode.IPUT_OBJECT,
                Opcode.NEW_INSTANCE,
                Opcode.CONST_4,
                Opcode.INVOKE_DIRECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.NEW_INSTANCE,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.INVOKE_DIRECT,
                Opcode.IPUT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.INVOKE_VIRTUAL,
                Opcode.RETURN_VOID
            )
        )
    )
) {
    override fun execute(patcherData: PatcherData): PatchResult {
        return PatchResultSuccess() // FIXME: fix below
        /*
        val result = signatures.first().result!!.findParentMethod(
            MethodSignature(
                methodSignatureMetadata = MethodSignatureMetadata(
                    name = "hide-suggestions-method",
                    methodMetadata = MethodMetadata(null, null), // unknown
                    patternScanMethod = PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                    compatiblePackages = compatiblePackages,
                    description = "Signature for the method, which is required to be patched.",
                    version = "0.0.1"
                ),
                returnType = "V",
                accessFlags = AccessFlags.FINAL or AccessFlags.PUBLIC,
                listOf("Z"),
                listOf(
                    Opcode.IPUT_BOOLEAN,
                    Opcode.IGET_OBJECT,
                    Opcode.IGET_BOOLEAN,
                    Opcode.INVOKE_VIRTUAL,
                    Opcode.RETURN_VOID
                )
            )
        ) ?: return PatchResultError("Method old-quality-patch-method has not been found")

        // deep clone the method in order to add a new register
        // TODO: replace by a mutable method implementation with settable register count when available
        val originalMethod = result.immutableMethod
        val originalImplementation = originalMethod.implementation!!
        val clonedMethod = ImmutableMethod(
            originalMethod.returnType,
            originalMethod.name,
            originalMethod.parameters,
            originalMethod.returnType,
            originalMethod.accessFlags,
            originalMethod.annotations,
            originalMethod.hiddenApiRestrictions,
            ImmutableMethodImplementation(
                originalImplementation.registerCount + 1, // additional register for the boolean
                originalImplementation.instructions,
                originalImplementation.tryBlocks,
                originalImplementation.debugItems,
            )
        ).toMutable() // create mutable clone out of the immutable method clone

        val clonedImplementation = clonedMethod.implementation!!

        // fix the instructions registers
        clonedImplementation.instructions.forEachIndexed { index, it ->
            val opcode = it.opcode
            // increment all registers (instance register and object register) by 1
            // due to adding a new virtual register for the boolean value
            clonedImplementation.replaceInstruction(
                index,
                when (it) {
                    is Instruction22c -> BuilderInstruction22c(
                        opcode,
                        it.registerA + 1, // increment register
                        it.registerB + 1, // increment register
                        it.reference
                    )
                    is Instruction35c -> BuilderInstruction35c(
                        opcode,
                        1,
                        it.registerC + 1, // increment register
                        0,
                        0,
                        0,
                        0,
                        it.reference
                    )
                    else -> return@forEachIndexed
                }
            )
        }

        // resolve the class proxy
        val clazz = result.definingClassProxy.resolve()

        // remove the old method & add the clone with our additional register
        clazz.methods.remove(originalMethod)
        clazz.methods.add(clonedMethod)

        // Proxy the first parameter of our clone by passing it to the RemoveSuggestions method
        // TODO: this crashes, find out why
        clonedImplementation.addInstructions(
            0,
            """
                invoke-static/range { v2 .. v2 }, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                move-result-object v0
                invoke-static { v0 }, Lfi/razerman/youtube/XAdRemover;->RemoveSuggestions(Ljava/lang/Boolean;)Ljava/lang/Boolean;
                move-result-object v0
                invoke-virtual/range { v0 .. v0 }, Ljava/lang/Boolean;->booleanValue()Z
                move-result v2
            """.trimIndent().toInstructions()
        )
        return PatchResultSuccess()
        */
    }
}