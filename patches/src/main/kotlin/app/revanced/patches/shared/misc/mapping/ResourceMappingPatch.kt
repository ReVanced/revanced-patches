package app.revanced.patches.shared.misc.mapping

import app.revanced.patcher.InstructionLocation
import app.revanced.patcher.LiteralFilter
import app.revanced.patcher.literal
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element
import java.util.Collections

enum class ResourceType(val value: String) {
    ANIM("anim"),
    ANIMATOR("animator"),
    ARRAY("array"),
    ATTR("attr"),
    BOOL("bool"),
    COLOR("color"),
    DIMEN("dimen"),
    DRAWABLE("drawable"),
    FONT("font"),
    FRACTION("fraction"),
    ID("id"),
    INTEGER("integer"),
    INTERPOLATOR("interpolator"),
    LAYOUT("layout"),
    MENU("menu"),
    MIPMAP("mipmap"),
    NAVIGATION("navigation"),
    PLURALS("plurals"),
    RAW("raw"),
    STRING("string"),
    STYLE("style"),
    STYLEABLE("styleable"),
    TRANSITION("transition"),
    VALUES("values"),
    XML("xml");

    companion object {
        private val VALUE_MAP: Map<String, ResourceType> = entries.associateBy { it.value }

        fun fromValue(value: String) = VALUE_MAP[value]
            ?: throw IllegalArgumentException("Unknown resource type: $value")
    }
}

data class ResourceElement(val type: ResourceType, val name: String, val id: Long)

private lateinit var resourceMappings: MutableMap<String, ResourceElement>

private fun setResourceId(type: ResourceType, name: String, id: Long) {
    resourceMappings[type.value + name] = ResourceElement(type, name, id)
}

/**
 * @return A resource id of the given resource type and name.
 * @throws PatchException if the resource is not found.
 */
fun getResourceId(type: ResourceType, name: String) = resourceMappings[type.value + name]?.id
    ?: throw PatchException("Could not find resource type: $type name: $name")

/**
 * @return All resource elements.  If a single resource id is needed instead use [getResourceId].
 */
fun getResourceElements() = Collections.unmodifiableCollection(resourceMappings.values)

/**
 * @return If the resource exists.
 */
fun hasResourceId(type: ResourceType, name: String) = resourceMappings[type.value + name] != null

/**
 * Identical to [LiteralFilter] except uses a decoded resource literal value.
 *
 * Any patch with fingerprints of this filter must
 * also declare [resourceMappingPatch] as a dependency.
 */
fun resourceLiteral(
    type: ResourceType,
    name: String,
    location : InstructionLocation = InstructionLocation.MatchAfterAnywhere()
) = literal({ getResourceId(type, name) }, null, location)


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

                setResourceId(ResourceType.fromValue(typeAttribute), nameAttribute, id)
            }
        }
    }
}
