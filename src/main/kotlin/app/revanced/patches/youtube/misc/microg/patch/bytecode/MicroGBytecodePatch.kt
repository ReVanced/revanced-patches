package app.revanced.patches.youtube.misc.microg.patch.bytecode

import app.revanced.extensions.startsWithAny
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.data.implementation.proxy
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.smali.toInstruction
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility
import app.revanced.patches.youtube.misc.microg.patch.resource.enum.StringReplaceMode
import app.revanced.patches.youtube.misc.microg.shared.Constants.BASE_MICROG_PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.BASE_REVANCED_PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.signatures.GooglePlayUtilitySignature
import app.revanced.patches.youtube.misc.microg.signatures.IntegrityCheckSignature
import app.revanced.patches.youtube.misc.microg.signatures.PrimeSignature
import app.revanced.patches.youtube.misc.microg.signatures.ServiceCheckSignature
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.MutableMethodImplementation
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c
import org.jf.dexlib2.iface.instruction.formats.Instruction21c
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.immutable.reference.ImmutableStringReference

@Patch
@Name("microg-bytecode-patch")
@Description("Patch to allow YouTube ReVanced to run without root and under a different package name.")
@MicroGPatchCompatibility
@Version("0.0.1")
class MicroGBytecodePatch : BytecodePatch(
    listOf(
        IntegrityCheckSignature, ServiceCheckSignature, GooglePlayUtilitySignature, PrimeSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        // smali patches
        disablePlayServiceChecks()
        data.classes.forEach { classDef ->
            var proxiedClass: MutableClass? = null

            classDef.methods.forEach methodLoop@{ method ->
                val implementation = method.implementation ?: return@methodLoop

                var proxiedImplementation: MutableMethodImplementation? = null

                implementation.instructions.forEachIndexed { i, instruction ->
                    if (instruction.opcode == Opcode.CONST_STRING) {
                        val stringValue = ((instruction as Instruction21c).reference as StringReference).string

                        val replaceMode =
                            if (stringValue.startsWith("com.google.android.gms.chimera.container")) // https://github.com/TeamVanced/VancedMicroG/pull/139/file
                                StringReplaceMode.DO_NOT_REPLACE
                            else if (stringValue == "com.google" || stringValue.startsWithAny(
                                    "com.google.android.gms.auth.accounts",
                                    "com.google.android.gms.chimera",
                                    "com.google.android.c2dm",
                                    "com.google.android.c2dm",
                                    "com.google.android.gsf",
                                    "com.google.android.c2dm",
                                    "com.google.iid",
                                    "content://com.google.settings"
                                )
                            ) {
                                StringReplaceMode.REPLACE_WITH_MICROG
                            } else if (stringValue.startsWithAny(
                                    "com.google.android.youtube.SuggestionsProvider",
                                    "com.google.android.youtube.fileprovider"
                                )
                            ) {
                                StringReplaceMode.REPLACE_WITH_REVANCED
                            } else {
                                StringReplaceMode.DO_NOT_REPLACE
                            }


                        if (replaceMode != StringReplaceMode.DO_NOT_REPLACE) {
                            if (proxiedClass == null) {
                                proxiedClass = data.proxy(classDef).resolve()
                            }

                            if (proxiedImplementation == null) {
                                proxiedImplementation = proxiedClass!!.methods.first {
                                    it.name == method.name && it.parameterTypes.containsAll(method.parameterTypes)
                                }.implementation!!
                            }

                            val newString =
                                if (replaceMode == StringReplaceMode.REPLACE_WITH_REVANCED) stringValue.replace(
                                    "com.google.android.youtube", BASE_REVANCED_PACKAGE_NAME
                                )
                                else stringValue.replace("com.google", BASE_MICROG_PACKAGE_NAME)

                            proxiedImplementation!!.replaceInstruction(
                                i, BuilderInstruction21c(
                                    Opcode.CONST_STRING, instruction.registerA, ImmutableStringReference(newString)
                                )
                            )
                        }
                    }
                }
            }
        }


        // replace string back
        val implementation =
            signatures.elementAt(2).result!!.findParentMethod(@Name("do-not-replace-method") @MatchingMethod(
                "Llpe;", "c"
            ) @DirectPatternScanMethod @MicroGPatchCompatibility @Version("0.0.1") object : MethodSignature(
                "L", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L"), null, listOf("com.google.android.gms")
            ) {})!!.method.implementation!!

        implementation.replaceInstruction(
            implementation.instructions.indexOfFirst { it.opcode == Opcode.CONST_STRING },
            "const-string v0, \"com.google.android.gms\"".toInstruction()
        )

        // allow GC to clean unused/ replaced immutable class definitions after this call
        data.classes.applyProxies()

        return PatchResultSuccess()
    }

    private fun disablePlayServiceChecks() {
        for (i in 0 until signatures.count() - 1) {
            val result = signatures.elementAt(i).result!!
            val stringInstructions = when (result.immutableMethod.returnType.first()) {
                'L' -> """
                        const/4 v0, 0x0
                        return-object v0
                        """
                'V' -> "return-void"
                'I' -> """
                        const/4 v0, 0x0
                        return v0
                        """
                else -> throw Exception("This case should never happen.")
            }
            result.method.implementation!!.addInstructions(
                0, stringInstructions.trimIndent().toInstructions()
            )
        }

        val implementation = signatures.last().result!!.method.implementation!!

        var register = 2
        val index = implementation.instructions.indexOfFirst {
            if (it.opcode != Opcode.CONST_STRING) return@indexOfFirst false

            val instructionString = ((it as Instruction21c).reference as StringReference).string
            if (instructionString != "com.google.android.youtube") return@indexOfFirst false

            register = it.registerA
            return@indexOfFirst true
        }

        implementation.replaceInstruction(
            index, "const-string v$register, \"$BASE_REVANCED_PACKAGE_NAME\"".toInstruction()
        )
    }
}