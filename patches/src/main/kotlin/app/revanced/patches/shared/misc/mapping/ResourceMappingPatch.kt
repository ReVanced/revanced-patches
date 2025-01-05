package app.revanced.patches.shared.misc.mapping

import app.revanced.patcher.InstructionFilter
import app.revanced.patcher.LiteralFilter
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction
import org.w3c.dom.Element
import java.lang.Runtime
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// TODO: Probably renaming the patch/this is a good idea.
lateinit var resourceMappings: List<ResourceElement>
    private set

/**
 * Identical to [LiteralFilter] except uses the literal resource value.
 */
class ResourceMappingFilter(
    private val type: String,
    private val name: String,
    maxInstructionsBefore: Int = METHOD_MAX_INSTRUCTIONS,
) : InstructionFilter(maxInstructionsBefore) {

    private val resourceId by lazy {
        resourceMappings[
            type,
            name,
        ]
    }

    override fun matches(
        context: BytecodePatchContext,
        method: Method,
        instruction: Instruction,
        methodIndex: Int
    ): Boolean {
        return (instruction as? WideLiteralInstruction)?.wideLiteral == resourceId
    }
}

val resourceMappingPatch = resourcePatch {
    val resourceMappings = Collections.synchronizedList(mutableListOf<ResourceElement>())

    execute {
        val threadCount = Runtime.getRuntime().availableProcessors()
        val threadPoolExecutor = Executors.newFixedThreadPool(threadCount)

        // Save the file in memory to concurrently read from it.
        val resourceXmlFile = get("res/values/public.xml").readBytes()

        for (threadIndex in 0 until threadCount) {
            threadPoolExecutor.execute thread@{
                document(resourceXmlFile.inputStream()).use { document ->

                    val resources = document.documentElement.childNodes
                    val resourcesLength = resources.length
                    val jobSize = resourcesLength / threadCount

                    val batchStart = jobSize * threadIndex
                    val batchEnd = jobSize * (threadIndex + 1)
                    element@ for (i in batchStart until batchEnd) {
                        // Prevent out of bounds.
                        if (i >= resourcesLength) return@thread

                        val node = resources.item(i)
                        if (node !is Element) continue

                        val nameAttribute = node.getAttribute("name")
                        val typeAttribute = node.getAttribute("type")

                        if (node.nodeName != "public" || nameAttribute.startsWith("APKTOOL")) continue

                        val id = node.getAttribute("id").substring(2).toLong(16)

                        resourceMappings.add(ResourceElement(typeAttribute, nameAttribute, id))
                    }
                }
            }
        }

        threadPoolExecutor.also { it.shutdown() }.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)

        app.revanced.patches.shared.misc.mapping.resourceMappings = resourceMappings
    }
}

operator fun List<ResourceElement>.get(type: String, name: String) = resourceMappings.firstOrNull {
    it.type == type && it.name == name
}?.id ?: throw PatchException("Could not find resource type: $type name: $name")

data class ResourceElement internal constructor(val type: String, val name: String, val id: Long)
