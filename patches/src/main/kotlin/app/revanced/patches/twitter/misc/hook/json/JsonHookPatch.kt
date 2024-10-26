package app.revanced.patches.twitter.misc.hook.json

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.misc.extension.sharedExtensionPatch
import java.io.Closeable
import java.io.InvalidClassException

/**
 * The [JsonHookPatchHook] of the [jsonHookPatch].
 *
 * @see JsonHookPatchHook
 */
internal lateinit var jsonHooks: JsonHookPatchHook
    private set

private const val JSON_HOOK_CLASS_NAMESPACE = "app/revanced/extension/twitter/patches/hook/json"
private const val JSON_HOOK_PATCH_CLASS_DESCRIPTOR = "L$JSON_HOOK_CLASS_NAMESPACE/JsonHookPatch;"
private const val BASE_PATCH_CLASS_NAME = "BaseJsonHook"
private const val JSON_HOOK_CLASS_DESCRIPTOR = "L$JSON_HOOK_CLASS_NAMESPACE/$BASE_PATCH_CLASS_NAME;"

val jsonHookPatch = bytecodePatch(
    description = "Hooks the stream which reads JSON responses.",
) {
    dependsOn(sharedExtensionPatch)

    val loganSquareMatch by loganSquareFingerprint()

    execute { context ->
        jsonHookPatchFingerprint.apply {
            // Make sure the extension is present.
            val jsonHookPatch = context.classBy { classDef -> classDef.type == JSON_HOOK_PATCH_CLASS_DESCRIPTOR }
                ?: throw PatchException("Could not find the extension.")

            if (!match(context, jsonHookPatch.immutableClass)) {
                throw PatchException("Unexpected extension.")
            }
        }.let { jsonHooks = JsonHookPatchHook(it) }

        // Conveniently find the type to hook a method in, via a named field.
        val jsonFactory = loganSquareMatch
            .classDef
            .fields
            .firstOrNull { it.name == "JSON_FACTORY" }
            ?.type
            .let { type ->
                context.classBy { it.type == type }?.mutableClass
            } ?: throw PatchException("Could not find required class.")

        // Hook the methods first parameter.
        jsonInputStreamFingerprint
            .apply { match(context, jsonFactory) }
            .match
            ?.mutableMethod
            ?.addInstructions(
                0,
                """
                    invoke-static { p1 }, $JSON_HOOK_PATCH_CLASS_DESCRIPTOR->parseJsonHook(Ljava/io/InputStream;)Ljava/io/InputStream;
                    move-result-object p1
                """,
            ) ?: throw PatchException("Could not find method to hook.")
    }

    finalize {
        jsonHooks.close()
    }
}

/**
 * Create a hook class.
 * The class has to extend on **JsonHook**.
 * The class has to be a Kotlin object class, or at least have an INSTANCE field of itself.
 *
 * @param context The [BytecodePatchContext] of the current patch.
 * @param descriptor The class descriptor of the hook.
 * @throws ClassNotFoundException If the class could not be found.
 */
class JsonHook(context: BytecodePatchContext, internal val descriptor: String) {
    internal var added = false

    init {
        context.classBy { it.type == descriptor }?.let {
            it.mutableClass.also { classDef ->
                if (
                    classDef.superclass != JSON_HOOK_CLASS_DESCRIPTOR ||
                    !classDef.fields.any { field -> field.name == "INSTANCE" }
                ) {
                    throw InvalidClassException(classDef.type, "Not a hook class")
                }
            }
        } ?: throw ClassNotFoundException("Failed to find hook class $descriptor")
    }
}

/**
 * A hook for the [jsonHookPatch].
 *
 * @param jsonHookPatchFingerprint The [jsonHookPatchFingerprint] to hook.
 */
class JsonHookPatchHook(jsonHookPatchFingerprint: Fingerprint) : Closeable {
    private val jsonHookPatchMatch = jsonHookPatchFingerprint.match!!
    private val jsonHookPatchIndex = jsonHookPatchMatch.patternMatch!!.endIndex

    /**
     * Add a hook to the [jsonHookPatch].
     * Will not add the hook if it's already added.
     *
     * @param jsonHook The [JsonHook] to add.
     */
    fun addHook(jsonHook: JsonHook) {
        if (jsonHook.added) return

        jsonHookPatchMatch.mutableMethod.apply {
            // Insert hooks right before calling buildList.
            val insertIndex = jsonHookPatchIndex

            addInstructions(
                insertIndex,
                """
                            sget-object v1, ${jsonHook.descriptor}->INSTANCE:${jsonHook.descriptor}
                            invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
                        """,
            )
        }

        jsonHook.added = true
    }

    override fun close() {
        // Remove hooks.add(dummyHook).
        jsonHookPatchMatch.mutableMethod.apply {
            val addDummyHookIndex = jsonHookPatchIndex - 2

            removeInstructions(addDummyHookIndex, 2)
        }
    }
}
