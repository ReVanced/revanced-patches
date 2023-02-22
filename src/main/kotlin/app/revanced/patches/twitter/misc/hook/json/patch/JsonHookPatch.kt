package app.revanced.patches.twitter.misc.hook.json.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.*
import app.revanced.patches.twitter.misc.hook.json.fingerprints.JsonHookPatchFingerprint
import app.revanced.patches.twitter.misc.hook.json.fingerprints.JsonInputStreamFingerprint
import app.revanced.patches.twitter.misc.hook.json.fingerprints.LoganSquareFingerprint
import java.io.InvalidClassException

@Name("json-hook")
@Description("Hooks the stream which reads JSON responses.")
@Version("0.0.1")
class JsonHookPatch : BytecodePatch(
    listOf(LoganSquareFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        // Make sure the integrations are present.
        val jsonHookPatch = context.findClass { it.type == JSON_HOOK_PATCH_CLASS_DESCRIPTOR }
            ?: return PatchResultError("Could not find integrations.")

        // Allow patch to inject hooks into the patches integrations.
        jsonHookPatchFingerprintResult = JsonHookPatchFingerprint.also {
            it.resolve(context, jsonHookPatch.immutableClass)
        }.result ?: return PatchResultError("Unexpected integrations.")

        // Conveniently find the type to hook a method in, via a named field.
        val jsonFactory = LoganSquareFingerprint.result
            ?.classDef
            ?.fields
            ?.firstOrNull { it.name == "JSON_FACTORY" }
            ?.type
            .let { type ->
                context.findClass { it.type == type }?.mutableClass
            } ?: return PatchResultError("Could not find required class.")

        // Hook the methods first parameter.
        JsonInputStreamFingerprint
            .also { it.resolve(context, jsonFactory) }
            .result
            ?.mutableMethod
            ?.addInstructions(
                0,
                """
                    invoke-static { p1 }, $JSON_HOOK_PATCH_CLASS_DESCRIPTOR->parseJsonHook(Ljava/io/InputStream;)Ljava/io/InputStream;
                    move-result-object p1
                """
            ) ?: return PatchResultError("Could not find method to hook.")

        return PatchResultSuccess()
    }

    /**
     * Create a hook class.
     * The class has to extend on **JsonHook**.
     * The class has to be a Kotlin object class, or at least have an INSTANCE field of itself.
     *
     * @param context The [BytecodeContext] of the current patch.
     * @param descriptor The class descriptor of the hook.
     */
    internal class Hook(context: BytecodeContext, private val descriptor: String) {
        private var added = false

        /**
         * Add the hook.
         */
        internal fun add() {
            if (added) return

            jsonHookPatchFingerprintResult.apply {
                mutableMethod.apply {
                    addInstructions(
                        scanResult.patternScanResult!!.startIndex,
                        """
                            sget-object v1, $descriptor->INSTANCE:$descriptor
                            invoke-virtual {v0, v1}, Lkotlin/collections/builders/ListBuilder;->add(Ljava/lang/Object;)Z
                        """
                    )
                }
            }

            added = true
        }

        init {
            context.findClass { it.type == descriptor }?.let {
                it.mutableClass.also { classDef ->
                    if (
                        classDef.superclass != JSON_HOOK_CLASS_DESCRIPTOR ||
                        !classDef.fields.any { field -> field.name == "INSTANCE" }
                    ) throw InvalidClassException(classDef.type, "Not a hook class")

                }
            } ?: throw ClassNotFoundException("Failed to find hook class")
        }
    }

    private companion object {
        const val JSON_HOOK_CLASS_NAMESPACE = "app/revanced/twitter/patches/hook/json"

        const val JSON_HOOK_PATCH_CLASS_DESCRIPTOR = "L$JSON_HOOK_CLASS_NAMESPACE/JsonHookPatch;"

        const val BASE_PATCH_CLASS_NAME = "BaseJsonHook"

        const val JSON_HOOK_CLASS_DESCRIPTOR = "L$JSON_HOOK_CLASS_NAMESPACE/$BASE_PATCH_CLASS_NAME;"

        private lateinit var jsonHookPatchFingerprintResult: MethodFingerprintResult
    }
}