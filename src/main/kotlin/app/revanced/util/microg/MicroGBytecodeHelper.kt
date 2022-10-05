package app.revanced.util.microg

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.util.microg.Constants.ACTIONS
import app.revanced.util.microg.Constants.AUTHORITIES
import app.revanced.util.microg.Constants.MICROG_VENDOR
import app.revanced.util.microg.Constants.PERMISSIONS
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c
import org.jf.dexlib2.iface.instruction.formats.Instruction21c
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.immutable.reference.ImmutableStringReference

/**
 * Helper class for applying bytecode patches needed for the microg-support patches.
 */
internal object MicroGBytecodeHelper {

    /**
     * Transform strings with package name out of [fromPackageName] and [toPackageName].
     *
     * @param fromPackageName Original package name.
     * @param toPackageName The package name to accept.
     **/
    fun packageNameTransform(fromPackageName: String, toPackageName: String): (String) -> String? {
        return { referencedString ->
            when (referencedString) {
                "$fromPackageName.SuggestionsProvider",
                "$fromPackageName.fileprovider" -> referencedString.replace(fromPackageName, toPackageName)
                else -> null
            }
        }
    }

    /**
     * Prime method data class for the [MicroGBytecodeHelper] class.
     *
     * @param primeMethodFingerprint The prime methods [MethodFingerprint].
     * @param fromPackageName Original package name.
     * @param toPackageName The package name to accept.
     **/
    data class PrimeMethodTransformationData(
        val primeMethodFingerprint: MethodFingerprint,
        val fromPackageName: String,
        val toPackageName: String
    ) {
        /**
         * Patch the prime method to accept the new package name.
         */
        fun transformPrimeMethodPackageName() {
            val primeMethod = primeMethodFingerprint.result!!.mutableMethod
            val implementation = primeMethod.implementation!!

            var register = 2
            val index = implementation.instructions.indexOfFirst {
                if (it.opcode != Opcode.CONST_STRING) return@indexOfFirst false

                val instructionString = ((it as Instruction21c).reference as StringReference).string
                if (instructionString != fromPackageName) return@indexOfFirst false

                register = it.registerA
                return@indexOfFirst true
            }

            primeMethod.replaceInstruction(
                index, "const-string v$register, \"$toPackageName\""
            )
        }
    }

    /**
     * Patch the bytecode to work with MicroG.
     * Note: this only handles string constants to gms (intent actions, authorities, ...).
     * If the app employs additional checks to validate the installed gms package, you'll have to handle those in the app- specific patch
     *
     * @param context The context.
     * @param additionalStringTransforms Additional transformations applied to all const-string references.
     * @param primeMethodTransformationData Data to patch the prime method.
     * @param earlyReturns List of [MethodFingerprint] to return the resolved methods early.
     */
    fun patchBytecode(
        context: BytecodeContext,
        additionalStringTransforms: Array<(str: String) -> String?>,
        primeMethodTransformationData: PrimeMethodTransformationData,
        earlyReturns: List<MethodFingerprint>
    ) {
        earlyReturns.returnEarly()
        primeMethodTransformationData.transformPrimeMethodPackageName()

        val allTransforms = arrayOf(
            MicroGBytecodeHelper::commonTransform,
            MicroGBytecodeHelper::contentUrisTransform,
            *additionalStringTransforms
        )

        // transform all strings using all provided transforms, first match wins
        context.transformStringReferences transform@{
            for (transformFn in allTransforms) {
                val s = transformFn(it)
                if (s != null) return@transform s
            }

            return@transform null
        }
    }

    /**
     * const-string transform function for common gms string references.
     *
     * @param referencedString The string to transform.
     */
    private fun commonTransform(referencedString: String): String? =
        when (referencedString) {
            "com.google",
            "com.google.android.gms",
            in PERMISSIONS,
            in ACTIONS,
            in AUTHORITIES -> referencedString.replace("com.google", MICROG_VENDOR)

            // subscribedfeeds has no vendor prefix for whatever reason...
            "subscribedfeeds" -> "${MICROG_VENDOR}.subscribedfeeds"
            else -> null
        }


    /**
     * const-string transform function for strings containing gms content uris / authorities.
     */
    private fun contentUrisTransform(str: String): String? {
        // only when content:// uri
        if (str.startsWith("content://")) {
            // check if matches any authority
            for (authority in AUTHORITIES) {
                val uriPrefix = "content://$authority"
                if (str.startsWith(uriPrefix)) {
                    return str.replace(
                        uriPrefix,
                        "content://${authority.replace("com.google", MICROG_VENDOR)}"
                    )
                }
            }

            // gms also has a 'subscribedfeeds' authority, check for that one too
            val subFeedsUriPrefix = "content://subscribedfeeds"
            if (str.startsWith(subFeedsUriPrefix)) {
                return str.replace(subFeedsUriPrefix, "content://${MICROG_VENDOR}.subscribedfeeds")
            }
        }

        return null
    }

    /**
     * Transform all constant string references using a transformation function.
     *
     * @param transformFn string transformation function. if null, string is not changed.
     */
    private fun BytecodeContext.transformStringReferences(transformFn: (str: String) -> String?) {
        classes.forEach { classDef ->
            var mutableClass: MutableClass? = null

            // enumerate all methods
            classDef.methods.forEach classLoop@{ methodDef ->
                var mutableMethod: MutableMethod? = null
                val implementation = methodDef.implementation ?: return@classLoop

                // enumerate all instructions and find const-string
                implementation.instructions.forEachIndexed implLoop@{ index, instruction ->
                    // skip all that are not const-string
                    if (instruction.opcode != Opcode.CONST_STRING) return@implLoop
                    val str = ((instruction as Instruction21c).reference as StringReference).string

                    // call transform function
                    val transformedStr = transformFn(str)
                    if (transformedStr != null) {
                        // make class and method mutable, if not already
                        mutableClass = mutableClass ?: proxy(classDef).mutableClass
                        mutableMethod = mutableMethod ?: mutableClass!!.methods.first {
                            it.name == methodDef.name && it.parameterTypes.containsAll(methodDef.parameterTypes)
                        }

                        // replace instruction with updated string
                        mutableMethod!!.implementation!!.replaceInstruction(
                            index,
                            BuilderInstruction21c(
                                Opcode.CONST_STRING,
                                instruction.registerA,
                                ImmutableStringReference(
                                    transformedStr
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Return the resolved methods of a list of [MethodFingerprint] early.
     */
    private fun List<MethodFingerprint>.returnEarly() {
        this.forEach { fingerprint ->
            val result = fingerprint.result!!
            val stringInstructions = when (result.method.returnType.first()) {
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
            result.mutableMethod.addInstructions(
                0, stringInstructions
            )
        }
    }
}
