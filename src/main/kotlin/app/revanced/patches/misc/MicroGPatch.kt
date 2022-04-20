package app.revanced.patches.misc

import app.revanced.extensions.startsWithAny
import app.revanced.patcher.PatcherData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.*
import app.revanced.patcher.proxy
import app.revanced.patcher.proxy.mutableTypes.MutableClass
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.toInstruction
import app.revanced.patcher.smali.toInstructions
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.MutableMethodImplementation
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c
import org.jf.dexlib2.iface.instruction.formats.Instruction21c
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.immutable.reference.ImmutableStringReference

private const val BASE_MICROG_PACKAGE_NAME = "com.mgoogle"
private const val BASE_REVANCED_PACKAGE_NAME = "app.revanced.android.youtube"

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.youtube", listOf("17.14.35")
    )
)

private val metadata = PatchMetadata(
    "microg",
    "MicroG Patch",
    "Patch to allow YouTube ReVanced to run without root and under a different package name.",
    compatiblePackages,
    "0.0.1"
)

private val description = "Signature required for ${metadata.name}."

enum class StringReplaceMode {
    REPLACE_WITH_MICROG, REPLACE_WITH_REVANCED, DO_NOT_REPLACE
}

class MicroGPatch : Patch(
    metadata, listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "google-play-sig-check-method",
                MethodMetadata("Ldsf;", "d"),
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                description,
                "0.0.1"
            ), "L", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L", "L"), listOf(
                Opcode.MOVE_OBJECT_FROM16,
                Opcode.CONST_STRING,
                Opcode.CONST_STRING,
                Opcode.NEW_INSTANCE,
                Opcode.MOVE_OBJECT_FROM16,
                Opcode.INVOKE_DIRECT,
                Opcode.CONST_4,
                Opcode.CONST_STRING,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IF_EQ,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.SGET,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_WIDE,
                Opcode.CONST_WIDE,
                Opcode.CONST_STRING,
                Opcode.CONST_4,
                Opcode.CONST_STRING,
                Opcode.CONST_4,
                Opcode.CMP_LONG,
                Opcode.IF_GEZ,
                Opcode.CONST_16,
                Opcode.GOTO_16,
                Opcode.CONST_STRING,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_16,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.GOTO,
                Opcode.MOVE_EXCEPTION,
                Opcode.CONST_STRING,
                Opcode.INVOKE_STATIC,
                Opcode.CONST_4,
                Opcode.IGET_OBJECT,
                Opcode.IF_EQZ,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.CONST,
                Opcode.IF_NE,
                Opcode.CONST_16,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.SGET_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IF_NEZ,
                Opcode.CONST_STRING,
                Opcode.GOTO,
                Opcode.NEW_ARRAY
            ), listOf("This should never happen.", "GooglePlayServicesUtil", "Google Play Store signature invalid.")
        ), MethodSignature(
            MethodSignatureMetadata(
                "google-play-service-checker-method",
                MethodMetadata("Llpe;", "d"),
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                description,
                "0.0.1"
            ), "V", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L", "I"), listOf(
                Opcode.SGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IF_EQZ,
                Opcode.SGET_OBJECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.INVOKE_VIRTUAL,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_STATIC,
                Opcode.IF_NEZ,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.THROW,
                Opcode.NEW_INSTANCE,
                Opcode.CONST_STRING,
                Opcode.INVOKE_DIRECT,
                Opcode.THROW,
                Opcode.RETURN_VOID
            ), listOf("Google Play Services not available")
        ), MethodSignature(
            MethodSignatureMetadata(
                "google-play-utility-method",
                MethodMetadata("Llpe;", "b"),
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                description,
                "0.0.1"
            ), "I", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L", "L"), listOf(
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST,
                Opcode.INVOKE_VIRTUAL,
                Opcode.GOTO,
                Opcode.CONST_STRING,
                Opcode.CONST_STRING,
                Opcode.INVOKE_STATIC,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.CONST_4,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IF_EQZ,
                Opcode.GOTO,
                Opcode.SGET_OBJECT,
                Opcode.MONITOR_ENTER,
                Opcode.SGET_BOOLEAN,
                Opcode.IF_EQZ,
                Opcode.MONITOR_EXIT,
                Opcode.GOTO,
                Opcode.SPUT_BOOLEAN,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_16,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.MONITOR_EXIT,
                Opcode.GOTO,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.SPUT,
                Opcode.GOTO,
                Opcode.MOVE_EXCEPTION,
                Opcode.CONST_STRING,
                Opcode.CONST_STRING,
                Opcode.INVOKE_STATIC,
                Opcode.MONITOR_EXIT,
                Opcode.SGET,
                Opcode.IF_EQZ,
                Opcode.CONST,
                Opcode.IF_NE,
                Opcode.GOTO,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.THROW,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.THROW,
                Opcode.MOVE_EXCEPTION,
                Opcode.MONITOR_EXIT,
                Opcode.THROW,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT,
                Opcode.CONST_4,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IF_NEZ,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IF_EQZ,
                Opcode.GOTO,
                Opcode.CONST_4,
                Opcode.GOTO,
                Opcode.CONST_4,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.SPUT_OBJECT,
                Opcode.SGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IF_NEZ,
                Opcode.CONST_4,
                Opcode.GOTO,
                Opcode.CONST_4,
                Opcode.INVOKE_STATIC,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_16,
                Opcode.IF_EQZ,
                Opcode.CONST_STRING,
                Opcode.CONST_16,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.GOTO,
                Opcode.CONST_STRING,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.CONST_16,
                Opcode.GOTO_16,
                Opcode.CONST_4,
                Opcode.CONST_STRING,
                Opcode.CONST_16,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT,
                Opcode.IF_NEZ,
                Opcode.CONST_STRING,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.GOTO,
                Opcode.IF_EQZ,
                Opcode.INVOKE_STATIC,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT,
                Opcode.IF_NEZ,
                Opcode.CONST_STRING,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.GOTO,
                Opcode.IF_EQZ,
                Opcode.IF_EQZ,
                Opcode.IGET_OBJECT,
                Opcode.AGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.AGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IF_NEZ,
                Opcode.CONST_STRING,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.GOTO,
                Opcode.IGET,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT,
                Opcode.IF_GE,
                Opcode.IGET,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.INVOKE_VIRTUAL,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.INVOKE_VIRTUAL,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.INVOKE_VIRTUAL,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.CONST_4,
                Opcode.GOTO,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.GOTO,
                Opcode.MOVE_EXCEPTION,
                Opcode.CONST_STRING,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.GOTO,
                Opcode.IGET_BOOLEAN,
                Opcode.IF_NEZ,
                Opcode.CONST_4,
                Opcode.GOTO,
                Opcode.RETURN,
                Opcode.CONST_STRING,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_STRING,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.RETURN
            ), listOf("This should never happen.", "MetadataValueReader", "com.google.android.gms")
        ), MethodSignature(
            MethodSignatureMetadata(
                "google-play-prime-method",
                MethodMetadata("Louy;", "e"),
                PatternScanMethod.Direct(),
                compatiblePackages,
                description,
                "0.0.1"
            ), null, null, null, null, listOf("com.google.android.GoogleCamera", "com.android.vending")
        )
    )
) {
    override fun execute(patcherData: PatcherData): PatchResult {
        // smali patches
        disablePlayServiceChecks()
        patcherData.classes.forEach { classDef ->
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
                                proxiedClass = patcherData.proxy(classDef).resolve()
                            }

                            if (proxiedImplementation == null) {
                                proxiedImplementation =
                                    proxiedClass!!.methods.first {
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

                        // TODO: phenotype reference -> microg reference
                        //if (instruction is ReferenceInstruction) {
                        //    val proxy = patcherData.proxy(classDef).resolve()
                        //    val implementation = proxy.methods.first { it.name == method.name }.implementation!!
                        //    when (instruction.referenceType) {
                        //        ReferenceType.METHOD -> {
                        //            val reference = instruction.reference as MethodReference
                        //            if (!reference.name.startsWith("com.google.android.gms.phenotype")) return@forEachIndexed

                        //            val modifiedReference = ImmutableMethodReference(
                        //                reference.definingClass.replace("com.google", BASE_MICROG_PACKAGE_NAME),
                        //                reference.name,
                        //                reference.parameterTypes.map {
                        //                    it.toString().replace("com.google", BASE_MICROG_PACKAGE_NAME)
                        //                },
                        //                reference.returnType.replace("com.google", BASE_MICROG_PACKAGE_NAME),
                        //            );

                        //            val newInstruction = when (instruction.opcode.format) {
                        //                Format.Format35c -> {
                        //                    val instruction35c = instruction as Instruction35c
                        //                    BuilderInstruction35c(
                        //                        instruction.opcode,
                        //                        instruction35c.registerCount,
                        //                        instruction35c.registerC,
                        //                        instruction35c.registerD,
                        //                        instruction35c.registerE,
                        //                        instruction35c.registerF,
                        //                        instruction35c.registerG,
                        //                        modifiedReference
                        //                    )
                        //                }
                        //                Format.Format3rc ->
                        //                    BuilderInstruction3rc(
                        //                        instruction.opcode,
                        //                    )
                        //                Format.Format45cc ->
                        //                    BuilderInstruction45cc(
                        //                        instruction.opcode,
                        //                    )
                        //                Format.Format4rcc ->
                        //                    BuilderInstruction4rcc(
                        //                        instruction.opcode,
                        //                    )
                        //            }
                        //            implementation.replaceInstruction(
                        //                i,

                        //                )
                        //        }
                        //        ReferenceType.METHOD_PROTO -> {

                        //        }
                        //        ReferenceType.TYPE -> {

                        //        }
                        //        ReferenceType.CALL_SITE -> {

                        //        }
                        //        ReferenceType.METHOD_HANDLE -> {

                        //        }
                        //        ReferenceType.FIELD -> {

                        //        }
                        //        ReferenceType.NONE -> {

                        //        }
                        //    }
                        //}

                    }
                }
            }
        }

        // replace string back
        val implementation = signatures.elementAt(2).result!!.findParentMethod(
            MethodSignature(
                MethodSignatureMetadata(
                    "do-not-replace-method",
                    MethodMetadata("Llpe;", "c"),
                    PatternScanMethod.Direct(),
                    compatiblePackages,
                    description,
                    "0.0.1"
                ), "L", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L"), null, listOf("com.google.android.gms")
            )
        )!!.method.implementation!!

        implementation.replaceInstruction(
            implementation.instructions.indexOfFirst { it.opcode == Opcode.CONST_STRING },
            "const-string v0, \"com.google.android.gms\"".toInstruction()
        )

        // allow GC to clean unused/ replaced immutable class definitions after this call
        patcherData.classes.applyProxies()

        // TODO: resource patches
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