package app.revanced.patches.shared.misc.mapping

import app.revanced.patcher.InstructionFilter.Companion.METHOD_MAX_INSTRUCTIONS
import app.revanced.patcher.LiteralFilter
import app.revanced.patcher.literal
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element
import java.lang.Runtime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private lateinit var resourceMappings: Map<String, ResourceElement>

/**
 * @return A resource id of the given resource type and name.
 * @throws PatchException if the resource is not found.
 */
fun getResourceId(type: String, name: String) = resourceMappings[type + name]?.id
    ?: throw PatchException("Could not find resource type: $type name: $name")

/**
 * @return If the resource exists.
 */
fun hasResourceId(type: String, name: String) = resourceMappings[type + name] != null

/**
 * Identical to [LiteralFilter] except uses a decoded resource literal value.
 *
 * Any patch with fingerprints of this filter must
 * also declare [resourceMappingPatch] as a dependency.
 */
fun resourceLiteral(
    type: String,
    name: String,
    maxBefore: Int = METHOD_MAX_INSTRUCTIONS,
) = literal({ getResourceId(type, name) }, null, maxBefore)


val resourceMappingPatch = resourcePatch {
    execute {
        val mappings : Map<String, ResourceElement>
        val threadCount = Runtime.getRuntime().availableProcessors()
        val threadPoolExecutor = Executors.newFixedThreadPool(threadCount)

        // Save the file in memory to concurrently read from it.
        val resourceXmlFile = get("res/values/public.xml").readBytes()

        document(resourceXmlFile.inputStream()).use { document ->
            // Need to synchronize while building the map, but after it's built
            // no synchronization is needed. Don't use a synchronized map and
            // instead only synchronize while building.
            val lock = Object()
            val resources = document.documentElement.childNodes
            val resourcesLength = resources.length
            val jobSize = resourcesLength / threadCount
            mappings = HashMap<String, ResourceElement>(2 * resourcesLength)

            for (threadIndex in 0 until threadCount) {
                threadPoolExecutor.execute thread@{
                    val batchStart = jobSize * threadIndex
                    val batchEnd = jobSize * (threadIndex + 1)

                    for (i in batchStart until batchEnd) {
                        // Prevent out of bounds.
                        if (i >= resourcesLength) return@thread

                        val node = resources.item(i)
                        if (node !is Element) continue

                        val nameAttribute = node.getAttribute("name")
                        val typeAttribute = node.getAttribute("type")

                        if (node.nodeName != "public" || nameAttribute.startsWith("APKTOOL")) continue

                        val id = node.getAttribute("id").substring(2).toLong(16)

                        val resourceElement = ResourceElement(typeAttribute, nameAttribute, id)

                        synchronized(lock) {
                            mappings[typeAttribute + nameAttribute] = resourceElement
                        }
                    }
                }
            }
        }

        threadPoolExecutor.also { it.shutdown() }.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)

        resourceMappings = mappings
    }
}

data class ResourceElement internal constructor(val type: String, val name: String, val id: Long)
