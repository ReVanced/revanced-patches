package app.revanced.patches.all.misc.resources

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch.resources
import app.revanced.util.*
import app.revanced.util.resource.ArrayResource
import app.revanced.util.resource.BaseResource
import app.revanced.util.resource.StringResource
import org.w3c.dom.Node
import java.io.Closeable

/**
 * An identifier of an app. For example, `youtube`.
 */
private typealias AppId = String
/**
 * An identifier of a patch. For example, `ad.general.HideAdsPatch`.
 */
private typealias PatchId = String

/**
 * A set of resources of a patch.
 */
private typealias PatchResources = Set<BaseResource>
/**
 * A map of resources belonging to a patch.
 */
private typealias AppResources = Map<PatchId, PatchResources>
/**
 * A map of resources belonging to an app.
 */
private typealias Resources = MutableMap<AppId, AppResources>

/**
 * The value of a resource.
 * For example, `values` or `values-de`.
 */
private typealias Value = String

@Patch(description = "Add resources such as strings or arrays to the app.")
object AddResourcesPatch : ResourcePatch(), MutableMap<Value, MutableSet<BaseResource>> by mutableMapOf(), Closeable {
    private lateinit var xmlFileHolder: ResourceContext.XmlFileHolder

    /**
     * A map of all resources associated by their value staged by [execute].
     */
    private lateinit var resources: Map<Value, Resources>

    /*
    The strategy of this patch is to stage resources present in `/resources/addresources`.
    These resources are organized by their respective value and patch.

    On AddResourcesPatch#execute, all resources are staged in a temporary map.
    After that, other patches that depend on AddResourcesPatch can call
    AddResourcesPatch#invoke(PatchClass) to stage resources belonging to that patch
    from the temporary map to AddResourcesPatch.

    After all patches that depend on AddResourcesPatch have been executed,
    AddResourcesPatch#close is finally called to add all staged resources to the app.
     */
    override fun execute(context: ResourceContext) {
        xmlFileHolder = context.xmlEditor
        resources = buildMap {
            /**
             * Puts resources under `/resources/addresources/<value>/<resourceKind>.xml` into the map.
             *
             * @param value The value of the resource. For example, `values` or `values-de`.
             * @param resourceKind The kind of the resource. For example, `strings` or `arrays`.
             * @param transform A function that transforms the [Node]s from the XML files to a [BaseResource].
             */
            fun addResources(
                value: Value,
                resourceKind: String,
                transform: (Node) -> BaseResource,
            ) {
                inputStreamFromBundledResource(
                    "addresources",
                    "$value/$resourceKind.xml"
                )?.let { stream ->
                    // Add the resources associated with the given value to the map,
                    // instead of overwriting it.
                    // This covers the example case such as adding strings and arrays of the same value.
                    getOrPut(value, ::mutableMapOf).apply {
                        xmlFileHolder[stream].use {
                            it.file.getElementsByTagName("app").asSequence().forEach { app ->
                                val appId = app.attributes.getNamedItem("id").textContent

                                this[appId] = buildMap {
                                    app.forEachChildElement { patch ->
                                        val patchId = patch.attributes.getNamedItem("id").textContent

                                        this[patchId] = mutableSetOf<BaseResource>().apply {
                                            patch.forEachChildElement { resourceNode ->
                                                val resource = transform(resourceNode)

                                                add(resource)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Stage all resources to a temporary map.
            // Staged resources consumed by AddResourcesPatch#invoke(PatchClass)
            // are later used in AddResourcesPatch#close.
            try {
                // TODO: Add all resources inside `resources/addresources`, not just from "values".
                addResources("values", "strings", StringResource::fromNode)
                addResources("values", "arrays", ArrayResource::fromNode)
            } catch (e: Exception) {
                throw PatchException("Failed to read resources", e)
            }
        }
    }

    /**
     * Adds a [BaseResource] to the map using [MutableMap.getOrPut].
     *
     * @param value The value of the resource. For example, `values` or `values-de`.
     * @param resource The resource to add.
     */
    operator fun invoke(value: Value, resource: BaseResource) {
        getOrPut(value, ::mutableSetOf) += resource
    }

    /**
     * Adds a list of [BaseResource]s to the map using [MutableMap.getOrPut].
     *
     * @param value The value of the resource. For example, `values` or `values-de`.
     * @param resources The resources to add.
     */
    operator fun invoke(value: Value, resources: Iterable<BaseResource>) {
        getOrPut(value, ::mutableSetOf) += resources
    }

    /**
     * Adds a [BaseResource] but uses `values` as the value for the resource.
     *
     * @param resource The string resource to add.
     */
    operator fun plusAssign(resource: BaseResource) = invoke("values", resource)

    /**
     * Adds a [StringResource].
     *
     * @param name The name of the string resource.
     * @param value The value of the string resource.
     * @param formatted Whether the string resource is formatted. Defaults to `true`.
     * @param resourceValue The value of the resource. For example, `values` or `values-de`.
     */
    operator fun invoke(
        name: String,
        value: String,
        formatted: Boolean = true,
        resourceValue: Value = "values",
    ) = invoke(resourceValue, StringResource(name, value, formatted))

    /**
     * Adds an [ArrayResource].
     *
     * @param name The name of the array resource.
     * @param items The items of the array resource.
     */
    operator fun invoke(
        name: String,
        items: List<StringResource>
    ) {
        this += ArrayResource(name, items)
    }

    /**
     * Puts all resources of any [Value] staged in [resources] for the given [PatchClass] to [AddResourcesPatch].
     *
     * @param patch The class of the patch to add resources for.
     * @param parseIds A function that parses the [AppId] and [PatchId] from the given [PatchClass].
     * This is used to access the resources in [resources] to stage them in [AddResourcesPatch].
     * The default implementation assumes that the [PatchClass] name has the following format:
     * `<any>.<any>.<any>.<app id>.<patch id>`
     * @see AddResourcesPatch.close
     */
    operator fun invoke(
        patch: PatchClass,
        parseIds: PatchClass.() -> Pair<AppId, PatchId> = {
            val qualifiedName = qualifiedName ?: throw PatchException("Patch class name is null")

            // This requires qualifiedName to have the following format:
            // `<any>.<any>.<any>.<app id>.<patch id>`
            with(qualifiedName.split(".")) {
                this[3] to subList(4, size).joinToString(".")
            }
        }
    ) {
        val (appId, patchId) = patch.parseIds()

        // Stage resources for the given patch to AddResourcesPatch associated with their value.
        resources.forEach { (value, resources) ->
            resources[appId]?.get(patchId)?.let { patchResources -> invoke(value, patchResources) }
                ?: throw PatchException("Resources for patch with id $patchId not found for app with id $appId")
        }
    }

    // TODO: For all kinds of resources staged in AddResourcesPatch,
    //  open the respective XML file and add the resources to the app
    //  while respecting the value of the resource.
    /**
     * Adds all resources staged in [AddResourcesPatch] to the app.
     * This is called after all patches that depend on [AddResourcesPatch] have been executed.
     */
    override fun close() {
        val strings = xmlFileHolder["res/values/strings.xml"]
        val arrays = xmlFileHolder["res/values/arrays.xml"]

        val stringResources = strings.getNode("resources")
        val arraysResources = arrays.getNode("resources")

        fun Node.add(value: Value, resource: BaseResource) {
            when (resource) {
                // TODO: Duplicates may be an issue here. I am not sure if these should be checked at all.
                //  In theory no duplicates should be added at all.
                is StringResource -> addResource(resource)
                is ArrayResource -> addResource(resource) { add(value, it) }
                else -> throw NotImplementedError("Unsupported resource type")
            }
        }

        forEach { (value, resources) ->
            // resources.forEach { resource -> add(value, resource) }
        }

        strings.close()
        arrays.close()
    }
}
