package app.revanced.patches.shared.misc.mapping

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import org.w3c.dom.Element
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object ResourceMappingPatch : ResourcePatch() {
    internal lateinit var resourceMappings: List<ResourceElement>
        private set

    private val THREAD_COUNT = Runtime.getRuntime().availableProcessors()
    private val threadPoolExecutor = Executors.newFixedThreadPool(THREAD_COUNT)

    override fun execute(context: ResourceContext) {
        // save the file in memory to concurrently read from
        val resourceXmlFile = context.get("res/values/public.xml").readBytes()

        // create a synchronized list to store the resource mappings
        val mappings = Collections.synchronizedList(mutableListOf<ResourceElement>())

        for (threadIndex in 0 until THREAD_COUNT) {
            threadPoolExecutor.execute thread@{
                context.xmlEditor[resourceXmlFile.inputStream()].use { editor ->
                    val document = editor.file

                    val resources = document.documentElement.childNodes
                    val resourcesLength = resources.length
                    val jobSize = resourcesLength / THREAD_COUNT

                    val batchStart = jobSize * threadIndex
                    val batchEnd = jobSize * (threadIndex + 1)
                    element@ for (i in batchStart until batchEnd) {
                        // make sure to not go out of bounds when rounding errors occur at calculating the jobSize
                        if (i >= resourcesLength) return@thread

                        val node = resources.item(i)
                        if (node !is Element) continue

                        val nameAttribute = node.getAttribute("name")
                        val typeAttribute = node.getAttribute("type")

                        if (node.nodeName != "public" || nameAttribute.startsWith("APKTOOL")) continue

                        val id = node.getAttribute("id").substring(2).toLong(16)

                        mappings.add(ResourceElement(typeAttribute, nameAttribute, id))
                    }
                }
            }
        }

        threadPoolExecutor
            .also { it.shutdown() }
            .awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)

        resourceMappings = mappings
    }

    data class ResourceElement(val type: String, val name: String, val id: Long)

    /**
     * @throws PatchException if the resource is not found.
     */
    fun firstIdForResource(type: String, name: String) : Long {
        val resource = resourceMappings.find {
            it.type == type && it.name == name
        } ?: throw PatchException("Could not find resource type: $type with name: $name")

        return resource.id
    }
}
