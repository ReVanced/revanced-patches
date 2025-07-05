package app.revanced.patches.shared.misc.mapping

import app.revanced.patcher.InstructionFilter.Companion.METHOD_MAX_INSTRUCTIONS
import app.revanced.patcher.LiteralFilter
import app.revanced.patcher.literal
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element
import java.util.Collections

data class ResourceElement(val type: String, val name: String, val id: Long)

private lateinit var resourceMappings: MutableMap<String, ResourceElement>

private fun setResourceId(type: String, name: String, id: Long) {
    resourceMappings[type + name] = ResourceElement(type, name, id)
}

/**
 * @return A resource id of the given resource type and name.
 * @throws PatchException if the resource is not found.
 */
fun getResourceId(type: String, name: String) = resourceMappings[type + name]?.id
    ?: throw PatchException("Could not find resource type: $type name: $name")

/**
 * @return All resource elements.  If a single resource id is needed instead use [getResourceId].
 */
fun getResourceElements() = Collections.unmodifiableCollection(resourceMappings.values)

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
        // Use a stream of the file, since no modifications are done
        // and using a File parameter causes the file to be re-wrote when closed.
        document(get("res/values/public.xml").inputStream()).use { document ->
            val resources = document.documentElement.childNodes
            val resourcesLength = resources.length
            resourceMappings = HashMap<String, ResourceElement>(2 * resourcesLength)

            for (i in 0 until resourcesLength) {
                val node = resources.item(i) as? Element ?: continue
                if (node.nodeName != "public") continue

                val nameAttribute = node.getAttribute("name")
                if (nameAttribute.startsWith("APKTOOL")) continue

                val typeAttribute = node.getAttribute("type")
                val id = node.getAttribute("id").substring(2).toLong(16)

                setResourceId(typeAttribute, nameAttribute, id)
            }
        }
    }
}
