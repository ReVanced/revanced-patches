package app.revanced.patches.all.misc.resources

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.addResource
import app.revanced.util.getNode
import app.revanced.util.resource.ArrayResource
import app.revanced.util.resource.BaseResource
import app.revanced.util.resource.StringResource
import java.io.Closeable

@Patch(
    description = "Add resources such as strings or arrays to the app.",
)
object AddResourcesPatch : ResourcePatch(), Closeable, MutableSet<BaseResource> by mutableSetOf() {
    private lateinit var xmlFileHolder: ResourceContext.XmlFileHolder

    override fun execute(context: ResourceContext) {
        xmlFileHolder = context.xmlEditor
    }

    fun addString(name: String, value: String, formatted: Boolean = false) = add(StringResource(name, value, formatted))

    override fun close() {
        val strings = xmlFileHolder["res/values/strings.xml"]
        val stringResources = strings.getNode("resources")

        val arrays = xmlFileHolder["res/values/arrays.xml"]
        val arraysResources = arrays.getNode("resources")

        val addedStrings = hashSetOf<String>()

        fun addResource(resource: BaseResource) {
            when (resource) {
                is StringResource -> {
                    // Strings are unique, so don't add them twice.
                    if (addedStrings.contains(resource.name)) return
                    addedStrings.add(resource.name)

                    stringResources.addResource(resource)
                }
                is ArrayResource -> arraysResources.addResource(resource) { addResource(it) }
                else -> throw NotImplementedError("Unsupported resource type")
            }
        }

        forEach(::addResource)

        strings.close()
        arrays.close()
    }
}
