package app.revanced.patches.all.misc.resources

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.*
import app.revanced.util.resource.ArrayResource
import app.revanced.util.resource.BaseResource
import app.revanced.util.resource.StringResource
import org.w3c.dom.Node
import java.io.Closeable
import java.util.*

private typealias AppId = String
private typealias PatchId = String

// It may be more efficient to use a set of Node instead of BaseResource.
// This would avoid having to first create a BaseResource and then back to a Node
// and would allow to retain custom attributes not considered by implementations of BaseResource.
// BaseResource is currently used anyway because it is more readable in terms of what's going on in the code.
// First BaseResource is created and added to the AddResourcesPatch and then the AddResourcesPatch is closed
// which adds all BaseResource to the app resources.
private typealias PatchResources = Set<BaseResource>
private typealias AppResources = Map<String, PatchResources>
private typealias Resources = Map<String, AppResources>

@Patch(description = "Add resources such as strings or arrays to the app.")
object AddResourcesPatch : ResourcePatch(), MutableSet<BaseResource> by mutableSetOf(), Closeable {
    private lateinit var xmlFileHolder: ResourceContext.XmlFileHolder

    // A map of all the bundled resources.
    private lateinit var resources: Set<Resources>

    override fun execute(context: ResourceContext) {
        xmlFileHolder = context.xmlEditor

        resources = buildSet {
            /**
             * Reads all resources from the given [resourceKind].xml file present under `resources/addresources`
             * and adds them to the map.
             *
             * @param resourceKind The kind of resources to read.
             * @param transform A function that transforms a [Node] to a [BaseResource].
             */
            fun addResources(
                resourceKind: String,
                transform: (Node) -> BaseResource,
            ) = add(buildMap {
                inputStreamFromBundledResource(
                    "resources",
                    "addresources/$resourceKind.xml"
                )?.let { stream ->
                    xmlFileHolder[stream].use {
                        it.file.getElementsByTagName("app").asSequence().forEach { app ->
                            val appName = app.attributes.getNamedItem("id").textContent

                            this[appName] = buildMap {
                                app.forEachChild { patch ->
                                    val patchName = patch.attributes.getNamedItem("name").textContent

                                    this[patchName] = mutableSetOf<BaseResource>().apply {
                                        patch.forEachChild { resourceNode ->
                                            val resource = transform(resourceNode)

                                            add(resource)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            })

            // Stage all resources to a temporary list.
            try {
                // TODO: Below strongly violates the open-closed principle.
                //  This should be rewritten to be more flexible in order to support any kind of resource
                //  that may be added in the future, otherwise as of now, only strings and arrays can be added.

                // TODO: This does not yet add strings other than English. Scan for all available languages and add
                //  them as well.
                addResources("strings", StringResource::fromNode)
                addResources("arrays", ArrayResource::fromNode)
            } catch (e: Exception) {
                throw PatchException("Failed to read resources", e)
            }
        }
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
     * - Adds strings under `resources/addresources/strings.xml` as [StringResource].
     * - Adds strings under `resources/addresources/values-<language>/strings.xml` as [StringResource]
     * and sets [StringResource.language] to `<language>`.
     * - Adds arrays under `resources/addresources/arrays.json` as [ArrayResource].
     *
     * @param patch The class of the patch to add resources for.
     * @see AddResourcesPatch.close
     */
    operator fun invoke(patch: PatchClass) {
        patch.qualifiedName ?: throw PatchException("Patch class name is null")

        // TODO: This is not good. As this patch is subject to public API, it should be rewritten to be more flexible.
        //  This may require dropping the convention of the current xml structures and instead qualify each string
        //  resource with the full patch class qualifier.
        /**
         * Extracts the app id and patch id from the given [PatchClass]
         * in order to add the resources for the given [PatchClass] to [AddResourcesPatch].
         *
         * This requires the [PatchClass] to be named as follows:
         * `app.revanced.patches.<app id>.<patch name>`
         */
        fun PatchClass.extractResourceIds(): Pair<AppId, PatchId> = with(
            this.qualifiedName!!.split(".")
        ) { this[2] to subList(3, size).joinToString(".") }

        val (appId, patchId) = patch.extractResourceIds()

        // From all available resources, add the resources for the given patch.
        resources.forEach { it[appId]?.get(patchId)?.forEach(::add) }
    }

    /**
     * Adds all [BaseResource] to the app resources.
     *
     * - Adds [StringResource] to `res/values-<language code>/strings.xml`
     * or `res/values/strings.xml`if [StringResource.language] is [Locale.ENGLISH].
     * - Adds [ArrayResource] to `res/values/arrays.xml`.
     */
    override fun close() {
        val strings = xmlFileHolder["res/values/strings.xml"]
        val arrays = xmlFileHolder["res/values/arrays.xml"]

        val stringResources = strings.getNode("resources")
        val arraysResources = arrays.getNode("resources")

        fun addResource(resource: BaseResource) {
            when (resource) {
                // TODO: Duplicates may be an issue here. I am not sure if these should be checked at all.
                //  In theory no duplicates should be added at all.
                // TODO: This only supports adding resources to the default language.
                //  Instead, read StringResource#language and add the resource to the corresponding language.
                is StringResource -> stringResources.addResource(resource)
                is ArrayResource -> arraysResources.addResource(resource) { addResource(it) }
                else -> throw NotImplementedError("Unsupported resource type")
            }
        }
        forEach(::addResource)

        strings.close()
        arrays.close()
    }
}
