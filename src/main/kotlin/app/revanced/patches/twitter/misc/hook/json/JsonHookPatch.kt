package app.revanced.patches.twitter.misc.hook.json

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.twitter.misc.hook.json.fingerprints.JsonHookPatchFingerprint
import app.revanced.patches.twitter.misc.hook.json.fingerprints.JsonInputStreamFingerprint
import app.revanced.patches.twitter.misc.hook.json.fingerprints.LoganSquareFingerprint
import java.io.Closeable
import java.io.InvalidClassException

@Patch(
    description = "Hooks the stream which reads JSON responses.",
    requiresIntegrations = true
)
object JsonHookPatch : BytecodePatch(
    setOf(LoganSquareFingerprint)
), Closeable {
    private const val JSON_HOOK_CLASS_NAMESPACE = "app/revanced/integrations/twitter/patches/hook/json"
    private const val JSON_HOOK_PATCH_CLASS_DESCRIPTOR = "L$JSON_HOOK_CLASS_NAMESPACE/JsonHookPatch;"
    private const val BASE_PATCH_CLASS_NAME = "BaseJsonHook"
    private const val JSON_HOOK_CLASS_DESCRIPTOR = "L$JSON_HOOK_CLASS_NAMESPACE/$BASE_PATCH_CLASS_NAME;"

    /**
     * The [JsonHookPatchHook] of the [JsonHookPatch].
     *
     * @see JsonHookPatchHook
     */
    internal lateinit var hooks: JsonHookPatchHook

    override fun execute(context: BytecodeContext) {
        JsonHookPatchFingerprint.also {
            // Make sure the integrations are present.
            val jsonHookPatch = context.findClass { classDef -> classDef.type == JSON_HOOK_PATCH_CLASS_DESCRIPTOR }
                ?: throw PatchException("Could not find integrations.")

            if (!it.resolve(context, jsonHookPatch.immutableClass))
                throw PatchException("Unexpected integrations.")
        }.let { hooks = JsonHookPatchHook(it) }

        // Conveniently find the type to hook a method in, via a named field.
        val jsonFactory = LoganSquareFingerprint.result
            ?.classDef
            ?.fields
            ?.firstOrNull { it.name == "JSON_FACTORY" }
            ?.type
            .let { type ->
                context.findClass { it.type == type }?.mutableClass
            } ?: throw PatchException("Could not find required class.")

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
            ) ?: throw PatchException("Could not find method to hook.")
    }

    /**
     * Create a hook class.
     * The class has to extend on **JsonHook**.
     * The class has to be a Kotlin object class, or at least have an INSTANCE field of itself.
     *
     * @param context The [BytecodeContext] of the current patch.
     * @param descriptor The class descriptor of the hook.
     * @throws ClassNotFoundException If the class could not be found.
     */
    internal class
    Hook(context: BytecodeContext, internal val descriptor: String) {
        internal var added = false

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

    /**
     * A hook for the [JsonHookPatch].
     *
     * @param jsonHookPatchFingerprint The [JsonHookPatchFingerprint] to hook.
     */
    internal class JsonHookPatchHook(jsonHookPatchFingerprint: MethodFingerprint): Closeable {
        private val jsonHookPatchFingerprintResult = jsonHookPatchFingerprint.result!!
        private val jsonHookPatchIndex = jsonHookPatchFingerprintResult.scanResult.patternScanResult!!.endIndex

        /**
         * Add a hook to the [JsonHookPatch].
         * Will not add the hook if it's already added.
         *
         * @param hook The [Hook] to add.
         */
        fun addHook(hook: Hook) {
            if (hook.added) return

            jsonHookPatchFingerprintResult.mutableMethod.apply {
                // Insert hooks right before calling buildList.
                val insertIndex = jsonHookPatchIndex

                addInstructions(
                    insertIndex,
                    """
                            sget-object v1, ${hook.descriptor}->INSTANCE:${hook.descriptor}
                            invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
                        """
                )
            }

            hook.added = true
        }

        override fun close() {
            // Remove hooks.add(dummyHook).
            jsonHookPatchFingerprintResult.mutableMethod.apply {
                val addDummyHookIndex = jsonHookPatchIndex - 2

                removeInstructions(addDummyHookIndex, 2)
            }
        }
    }

    override fun close() = hooks.close()

}