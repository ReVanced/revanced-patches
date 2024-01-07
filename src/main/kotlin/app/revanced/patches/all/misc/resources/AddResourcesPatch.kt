package app.revanced.patches.all.misc.resources

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.addResource
import app.revanced.util.getNode
import app.revanced.util.resource.ArrayResource
import app.revanced.util.resource.BaseResource
import app.revanced.util.resource.StringResource
import java.io.Closeable
import java.util.*

@Patch(description = "Add resources such as strings or arrays to the app.")
object AddResourcesPatch : ResourcePatch(), MutableSet<BaseResource> by mutableSetOf(), Closeable {
    private lateinit var xmlFileHolder: ResourceContext.XmlFileHolder

    override fun execute(context: ResourceContext) {
        xmlFileHolder = context.xmlEditor
    }

    /**
     * Adds a [StringResource].
     *
     * @param name The name of the string resource.
     * @param value The value of the string resource.
     * @param formatted Whether the string resource is formatted. Defaults to `true`.
     * @param language The language of the string resource. Defaults to [Locale.ENGLISH].
     */
    operator fun invoke(
        name: String,
        value: String,
        formatted: Boolean = true,
        language: String = Locale.ENGLISH.language
    ) = add(StringResource(name, value, formatted, language))

    /**
     * Adds an [ArrayResource].
     *
     * @param name The name of the array resource.
     * @param items The items of the array resource.
     */
    operator fun invoke(
        name: String,
        items: List<StringResource>
    ) = add(ArrayResource(name, items))

    /**
     * Adds all resources from the given [patch] present under `resources/addresources`.
     *
     * - Adds strings under `resources/addresources/strings.json` as [StringResource].
     * - Adds strings under `resources/addresources/values-<language>/strings.xml` as [StringResource]
     * and sets [StringResource.language] to `<language>`.
     * - Adds arrays under `resources/addresources/arrays.json` as [ArrayResource].
     *
     * @param patch The class of the patch to add resources for.
     * @see AddResourcesPatch.close
     */
    operator fun invoke(patch: PatchClass) {
        // TODO Implement this.
    }

    // TODO: Rewrite this accordingly so that it does what the description says.
    /**
     * Adds all [BaseResource] to the app resources.
     *
     * - Adds [StringResource] to `res/values-<language code>/strings.xml`
     * or `res/values/strings.xml`if [StringResource.language] is [Locale.ENGLISH].
     * - Adds [ArrayResource] to `res/values/arrays.xml`.
     */
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
