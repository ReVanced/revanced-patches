package app.revanced.patches.twitter.misc.hook.json

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.twitter.misc.extension.sharedExtensionPatch
import java.io.InvalidClassException

/**
 * Add a hook to the [jsonHookPatch].
 * Will not add the hook if it's already added.
 *
 * @param jsonHook The [JsonHook] to add.
 */
context(BytecodePatchContext)
fun addJsonHook(
    jsonHook: JsonHook,
) {
    if (jsonHook.added) return

    jsonHookPatchFingerprint.method.apply {
        // Insert hooks right before calling buildList.
        val insertIndex = jsonHookPatchFingerprint.patternMatch!!.endIndex

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

private const val JSON_HOOK_CLASS_NAMESPACE = "app/revanced/extension/twitter/patches/hook/json"
private const val JSON_HOOK_PATCH_CLASS_DESCRIPTOR = "L$JSON_HOOK_CLASS_NAMESPACE/JsonHookPatch;"
private const val BASE_PATCH_CLASS_NAME = "BaseJsonHook"
private const val JSON_HOOK_CLASS_DESCRIPTOR = "L$JSON_HOOK_CLASS_NAMESPACE/$BASE_PATCH_CLASS_NAME;"

val jsonHookPatch = bytecodePatch(
    description = "Hooks the stream which reads JSON responses.",
) {
    dependsOn(sharedExtensionPatch)

    execute {
        jsonHookPatchFingerprint.apply {
            // Make sure the extension is present.
            val jsonHookPatch = classBy { classDef -> classDef.type == JSON_HOOK_PATCH_CLASS_DESCRIPTOR }
                ?: throw PatchException("Could not find the extension.")

            matchOrNull(jsonHookPatch.immutableClass)
                ?: throw PatchException("Unexpected extension.")
        }

        val jsonFactoryClassDef =
            loganSquareFingerprint.originalClassDef // Conveniently find the type to hook a method in, via a named field.
                .fields
                .firstOrNull { it.name == "JSON_FACTORY" }
                ?.type
                .let { type -> classes.find { it.type == type } }
                ?: throw PatchException("Could not find required class.")

        // Hook the methods first parameter.
        jsonInputStreamFingerprint.match(jsonFactoryClassDef).method.addInstructions(
            0,
            """
                invoke-static { p1 }, $JSON_HOOK_PATCH_CLASS_DESCRIPTOR->parseJsonHook(Ljava/io/InputStream;)Ljava/io/InputStream;
                move-result-object p1
            """,
        )
    }

    finalize {
        // Remove hooks.add(dummyHook).
        jsonHookPatchFingerprint.method.apply {
            val addDummyHookIndex = jsonHookPatchFingerprint.patternMatch!!.endIndex - 2

            removeInstructions(addDummyHookIndex, 2)
        }
    }
}

/**
 * Create a hook class.
 * The class has to extend on **JsonHook**.
 * The class has to be a Kotlin object class, or at least have an INSTANCE field of itself.
 *
 * @param descriptor The class descriptor of the hook.
 * @throws ClassNotFoundException If the class could not be found.
 */
context(BytecodePatchContext)
class JsonHook(
    internal val descriptor: String,
) {
    internal var added = false

    init {
        classBy { it.type == descriptor }?.let {
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
