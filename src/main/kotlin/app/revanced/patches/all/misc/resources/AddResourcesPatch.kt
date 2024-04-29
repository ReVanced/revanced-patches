package app.revanced.patches.all.misc.resources

import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.Document
import app.revanced.util.*
import app.revanced.util.resource.ArrayResource
import app.revanced.util.resource.BaseResource
import app.revanced.util.resource.StringResource
import org.w3c.dom.Node
import java.util.*

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
private typealias PatchResources = MutableSet<BaseResource>

/**
 * A map of resources belonging to a patch.
 */
private typealias AppResources = MutableMap<PatchId, PatchResources>

/**
 * A map of resources belonging to an app.
 */
private typealias Resources = MutableMap<AppId, AppResources>

/**
 * The value of a resource.
 * For example, `values` or `values-de`.
 */
private typealias Value = String

/**
 * A set of resources mapped by their value.
 */
private typealias MutableResources = MutableMap<Value, MutableSet<BaseResource>>

/**
 * A map of all resources associated by their value staged by [addResourcesPatch].
 */
private lateinit var stagedResources: Map<Value, Resources>

/**
 * A map of all resources added to the app by [addResourcesPatch].
 */
class AddResources internal constructor() : MutableResources by mutableMapOf() {

    /**
     * Adds a [BaseResource] to the map using [MutableMap.getOrPut].
     *
     * @param value The value of the resource. For example, `values` or `values-de`.
     * @param resource The resource to add.
     *
     * @return True if the resource was added, false if it already existed.
     */
    operator fun invoke(
        value: Value,
        resource: BaseResource,
    ) = getOrPut(value, ::mutableSetOf).add(resource)

    /**
     * Adds a list of [BaseResource]s to the map using [MutableMap.getOrPut].
     *
     * @param value The value of the resource. For example, `values` or `values-de`.
     * @param resources The resources to add.
     *
     * @return True if the resources were added, false if they already existed.
     */
    operator fun invoke(
        value: Value,
        resources: Iterable<BaseResource>,
    ) = getOrPut(value, ::mutableSetOf).addAll(resources)

    /**
     * Adds a [StringResource].
     *
     * @param name The name of the string resource.
     * @param value The value of the string resource.
     * @param formatted Whether the string resource is formatted. Defaults to `true`.
     * @param resourceValue The value of the resource. For example, `values` or `values-de`.
     *
     * @return True if the resource was added, false if it already existed.
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
     *
     * @return True if the resource was added, false if it already existed.
     */
    operator fun invoke(
        name: String,
        items: List<String>,
    ) = invoke("values", ArrayResource(name, items))

    /**
     * Puts all resources of any [Value] staged in [stagedResources] for the [Patch] to [addResources].
     *
     * @param patch The [Patch] of the patch to stage resources for.
     * @param parseIds A function that parses a set of [PatchId] each mapped to an [AppId] from the given [Patch].
     * This is used to access the resources in [addResources] to stage them in [stagedResources].
     * The default implementation assumes that the [Patch] has a name and declares packages it is compatible with.
     *
     * @return True if any resources were added, false if none were added.
     *
     * @see addResourcesPatch
     */
    operator fun invoke(
        patch: Patch<*>,
        parseIds: (Patch<*>) -> Map<AppId, Set<PatchId>> = {
            val patchId = patch.name ?: throw PatchException("Patch has no name")
            val packages = patch.compatiblePackages ?: throw PatchException("Patch has no compatible packages")

            buildMap<AppId, MutableSet<PatchId>> {
                packages.forEach { (appId, _) ->
                    getOrPut(appId) { mutableSetOf() }.add(patchId)
                }
            }
        },
    ): Boolean {
        var result = false

        // Stage resources for the given patch to addResourcesPatch associated with their value.
        parseIds(patch).forEach { (appId, patchIds) ->
            patchIds.forEach { patchId ->
                stagedResources.forEach { (value, resources) ->
                    resources[appId]?.get(patchId)?.let { patchResources ->
                        if (invoke(value, patchResources)) result = true
                    }
                }
            }
        }

        return result
    }

    /**
     * Puts all resources for the given [appId] and [patchId] staged in [addResources] to [addResourcesPatch].
     *
     *
     * @return True if any resources were added, false if none were added.
     *
     * @see addResourcesPatch
     */
    operator fun invoke(
        appId: AppId,
        patchId: String,
    ) = stagedResources.forEach { (value, resources) ->
        resources[appId]?.get(patchId)?.let { patchResources ->
            invoke(value, patchResources)
        }
    }
}
val addResources = AddResources()

val addResourcesPatch = resourcePatch(
    description = "Add resources such as strings or arrays to the app.",
) {
    /*
    The strategy of this patch is to stage resources present in `/resources/addresources`.
    These resources are organized by their respective value and patch.

    On addResourcesPatch#execute, all resources are staged in a temporary map.
    After that, other patches that depend on addResourcesPatch can call
    addResourcesPatch#invoke(Patch) to stage resources belonging to that patch
    from the temporary map to addResourcesPatch.

    After all patches that depend on addResourcesPatch have been executed,
    addResourcesPatch#finalize is finally called to add all staged resources to the app.
     */
    execute { context ->
        stagedResources = buildMap {
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
                    "$value/$resourceKind.xml",
                )?.let { stream ->
                    // Add the resources associated with the given value to the map,
                    // instead of overwriting it.
                    // This covers the example case such as adding strings and arrays of the same value.
                    getOrPut(value, ::mutableMapOf).apply {
                        context.document[stream].use { document ->
                            document.getElementsByTagName("app").asSequence().forEach { app ->
                                val appId = app.attributes.getNamedItem("id").textContent

                                getOrPut(appId, ::mutableMapOf).apply {
                                    app.forEachChildElement { patch ->
                                        val patchId = patch.attributes.getNamedItem("id").textContent

                                        getOrPut(patchId, ::mutableSetOf).apply {
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
            // Staged resources consumed by addResourcesPatch#invoke(Patch)
            // are later used in addResourcesPatch#finalize.
            try {
                val addStringResources = { value: Value ->
                    addResources(value, "strings", StringResource::fromNode)
                }
                Locale.getISOLanguages().asSequence().map { "values-$it" }.forEach { addStringResources(it) }
                addStringResources("values")

                addResources("values", "arrays", ArrayResource::fromNode)
            } catch (e: Exception) {
                throw PatchException("Failed to read resources", e)
            }
        }
    }

    /**
     * Adds all resources staged in [addResourcesPatch] to the app.
     * This is called after all patches that depend on [addResourcesPatch] have been executed.
     */
    finalize { context ->
        operator fun MutableMap<String, Pair<Document, Node>>.invoke(
            value: Value,
            resource: BaseResource,
        ) {
            // TODO: Fix open-closed principle violation by modifying BaseResource#serialize so that it accepts
            //  a Value and the map of documents. It will then get or put the document suitable for its resource type
            //  to serialize itself to it.
            val resourceFileName =
                when (resource) {
                    is StringResource -> "strings"
                    is ArrayResource -> "arrays"
                    else -> throw NotImplementedError("Unsupported resource type")
                }

            getOrPut(resourceFileName) {
                val targetFile =
                    context["res/$value/$resourceFileName.xml"].also {
                        it.parentFile?.mkdirs()
                        it.createNewFile()
                    }

                context.document[targetFile.path].let { document ->

                    // Save the target node here as well
                    // in order to avoid having to call document.getNode("resources")
                    // but also save the document so that it can be closed later.
                    document to document.getNode("resources")
                }
            }.let { (_, targetNode) ->
                targetNode.addResource(resource) { invoke(value, it) }
            }
        }

        addResources.forEach { (value, resources) ->
            // A map of document associated by their kind (e.g. strings, arrays).
            // Each document is accompanied by the target node to which resources are added.
            // A map is used because Map#getOrPut allows opening a new document for the duration of a resource value.
            // This is done to prevent having to open the files for every resource that is added.
            // Instead, it is cached once and reused for resources of the same value.
            // This map is later accessed to close all documents for the current resource value.
            val documents = mutableMapOf<String, Pair<Document, Node>>()

            resources.forEach { resource -> documents(value, resource) }

            documents.values.forEach { (document, _) -> document.close() }
        }
    }
}
