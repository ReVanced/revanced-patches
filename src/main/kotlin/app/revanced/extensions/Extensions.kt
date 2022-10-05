package app.revanced.extensions

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.toInstruction
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.MutableMethodImplementation
import org.jf.dexlib2.builder.instruction.BuilderInstruction11n
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference
import org.w3c.dom.Node
import java.nio.file.Files

// TODO: this method does not make sense here
internal fun MutableMethodImplementation.injectHideCall(
    index: Int,
    register: Int
) {
    this.addInstruction(
        index,
        "invoke-static { v$register }, Lapp/revanced/integrations/patches/HideHomeAdsPatch;->HideHomeAds(Landroid/view/View;)V".toInstruction()
    )
}

/**
 * traverse the class hierarchy starting from the given root class
 *
 * @param targetClass the class to start traversing the class hierarchy from
 * @param callback function that is called for every class in the hierarchy
 */
fun BytecodeContext.traverseClassHierarchy(targetClass: MutableClass, callback: MutableClass.() -> Unit) {
    callback(targetClass)
    this.findClass(targetClass.superclass ?: return)?.mutableClass?.let {
        traverseClassHierarchy(it, callback)
    }
}

/**
 * apply a transform to all methods of the class
 *
 * @param transform the transformation function. original method goes in, transformed method goes out
 */
fun MutableClass.transformMethods(transform: MutableMethod.() -> MutableMethod) {
    val transformedMethods = methods.map { it.transform() }
    methods.clear()
    methods.addAll(transformedMethods)
}

/**
 * Insert an event hook at the top of the method. If the hook returns true, the event is consumed and the method will return with true
 *
 * the hook method MUST return a boolean and MUST take two parameters, like so:
 *  fun hook(thisRef: Object, eventData: Object): Boolean {}
 *
 * The final injected code will resemble the following logic:
 *  if( YouHook(this, event) ) { return true; }
 *  ...
 *
 * @param hookRef reference to the hook method
 */
internal fun MutableMethod.injectConsumableEventHook(hookRef: ImmutableMethodReference) {
    val isStaticMethod = AccessFlags.STATIC.isSet(this.accessFlags)
    this.implementation?.let { impl ->
        // create label to index 0 to continue to the normal program flow
        val lblContinueNormalFlow = impl.newLabelForIndex(0)

        // define registers
        /** V0 */
        val regV0 = 0

        /** this  */
        val regP0 = impl.registerCount - this.parameters.size - (if (isStaticMethod) 0 else 1)

        /** motionEvent */
        val regP1 = regP0 + 1

        // insert instructions at the start of the method:
        // if( Hook(this, event) ) { return true; }
        impl.addInstructions(
            0, listOf(
                // invoke-static { p0, p1 } <hook>
                BuilderInstruction35c(
                    Opcode.INVOKE_STATIC,
                    2,
                    regP0,
                    regP1,
                    0, 0, 0,
                    hookRef
                ),

                // move-result v0
                BuilderInstruction11x(
                    Opcode.MOVE_RESULT,
                    regV0
                ),

                // if-eqz v0, :continue_normal_flow
                BuilderInstruction21t(
                    Opcode.IF_EQZ,
                    regV0,
                    lblContinueNormalFlow
                ),

                // const/4 v0, 0x1
                BuilderInstruction11n(
                    Opcode.CONST_4,
                    regV0,
                    0x1
                ),

                // return v0
                BuilderInstruction11x(
                    Opcode.RETURN,
                    regV0
                )

                // :continue_normal_flow
            )
        )
    }
}

/**
 * inject resources into the patched app
 *
 * @param classLoader classloader to use for loading the resources
 * @param patchDirectoryPath path to the files. this will be the directory you created under the 'resources' source folder
 * @param resourceType the resource type, for example 'drawable'. this has to match both the source and the target
 * @param resourceFileNames names of all resources of this type to inject
 */
fun ResourceContext.injectResources(
    classLoader: ClassLoader,
    patchDirectoryPath: String,
    resourceType: String,
    resourceFileNames: List<String>
) {
    resourceFileNames.forEach { name ->
        val relativePath = "$resourceType/$name"
        val sourceRes = classLoader.getResourceAsStream("$patchDirectoryPath/$relativePath")
            ?: throw PatchResultError("could not open resource '$patchDirectoryPath/$relativePath'")

        Files.copy(
            sourceRes,
            this["res"].resolve(relativePath).toPath()
        )
    }
}

/**
 * inject strings into the patched app
 *
 * @param classLoader classloader to use for loading the resources
 * @param patchDirectoryPath path to the files. this will be the directory you created under the 'resources' source folder
 * @param languageIdentifier ISO 639-2 two- letter language code identifier (aka the one android uses for values directory)
 */
fun ResourceContext.injectStrings(
    classLoader: ClassLoader,
    patchDirectoryPath: String,
    languageIdentifier: String? = null,
) {
    val relativePath =
        if (languageIdentifier.isNullOrBlank()) "values/strings.xml" else "values/strings-$languageIdentifier.xml"

    // open source strings.xml
    val sourceInputStream = classLoader.getResourceAsStream("$patchDirectoryPath/$relativePath")
        ?: throw PatchResultError("failed to open '$patchDirectoryPath/$relativePath'")
    xmlEditor[sourceInputStream].use { sourceStringsXml ->
        val strings = sourceStringsXml.file.getElementsByTagName("resources").item(0).childNodes

        // open target strings.xml
        xmlEditor["res/$relativePath"].use { targetStringsXml ->
            val targetFile = targetStringsXml.file
            val targetRootNode = targetFile.getElementsByTagName("resources").item(0)

            // process all children strings in the source
            for (i in 0 until strings.length) {
                // clone the node from source to target
                val node = strings.item(i).cloneNode(true)
                targetFile.adoptNode(node)
                targetRootNode.appendChild(node)
            }
        }
    }
}

internal fun Node.doRecursively(action: (Node) -> Unit) {
    action(this)
    for (i in 0 until this.childNodes.length) this.childNodes.item(i).doRecursively(action)
}

internal fun String.startsWithAny(vararg prefixes: String): Boolean {
    for (prefix in prefixes)
        if (this.startsWith(prefix))
            return true

    return false
}

internal fun String.equalsAny(vararg other: String): Boolean {
    for (_other in other)
        if (this == _other)
            return true

    return false
}
