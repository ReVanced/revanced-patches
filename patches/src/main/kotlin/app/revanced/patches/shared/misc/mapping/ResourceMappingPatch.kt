package app.revanced.patches.shared.misc.mapping

import app.revanced.patcher.IndexedMatcherPredicate
import app.revanced.patcher.extensions.wideLiteral
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import org.w3c.dom.Element

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
    XML("xml"),
    ;

    operator fun invoke(name: String): IndexedMatcherPredicate<Instruction> {
        val id = get(name)

        return { _, _, _ -> wideLiteral == id }
    }

    /**
     * @return A resource id of the given resource type and name.
     * @throws PatchException if the resource is not found.
     */
    operator fun get(name: String) = resourceMappings[value + name]?.id
        ?: throw PatchException("Could not find resource type: $this name: $name")

    companion object {
        private val VALUE_MAP: Map<String, ResourceType> = entries.associateBy { it.value }

        fun fromValue(value: String) = VALUE_MAP[value]
            ?: throw IllegalArgumentException("Unknown resource type: $value")
    }
}

data class ResourceElement(val type: ResourceType, val name: String, val id: Long)

private lateinit var resourceMappings: MutableMap<String, ResourceElement>

val resourceMappingPatch = resourcePatch {
    apply {
        // Use a stream of the file, since no modifications are done
        // and using a File parameter causes the file to be re-written when closed.
        document(get("res/values/public.xml").inputStream()).use { document ->
            val resources = document.documentElement.childNodes
            val resourcesLength = resources.length
            resourceMappings = HashMap(2 * resourcesLength)

            for (i in 0 until resourcesLength) {
                val node = resources.item(i) as? Element ?: continue
                if (node.nodeName != "public") continue

                val nameAttribute = node.getAttribute("name")
                if (nameAttribute.startsWith("APKTOOL")) continue

                val typeAttribute = node.getAttribute("type")
                val id = node.getAttribute("id").substring(2).toLong(16)

                val type = ResourceType.fromValue(typeAttribute)

                resourceMappings[type.value + nameAttribute] = ResourceElement(type, nameAttribute, id)
            }
        }
    }
}
