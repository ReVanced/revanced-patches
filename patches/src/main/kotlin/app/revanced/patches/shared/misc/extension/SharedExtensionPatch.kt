package app.revanced.patches.shared.misc.extension

import app.revanced.patcher.*
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.firstMutableClassDef
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.Method
import java.net.URLDecoder
import java.util.jar.JarFile

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/shared/Utils;"

/**
 * A patch to extend with an extension shared with multiple patches.
 *
 * @param extensionName The name of the extension to extend with.
 */
fun sharedExtensionPatch(
    extensionName: String,
    vararg hooks: ExtensionHook,
) = bytecodePatch {
    dependsOn(sharedExtensionPatch(hooks = hooks))

    extendWith("extensions/$extensionName.rve")
}

/**
 * A patch to extend with the "shared" extension.
 *
 * @param hooks The hooks to get the application context for use in the extension,
 * commonly for the onCreate method of exported activities.
 */
fun sharedExtensionPatch(
    vararg hooks: ExtensionHook,
) = bytecodePatch {
    extendWith("extensions/shared.rve")

    apply {
        // Verify the extension class exists.
        firstMutableClassDef(EXTENSION_CLASS_DESCRIPTOR)
    }

    afterDependents {
        // The hooks are made in afterDependents to ensure that the context is hooked before any other patches.
        hooks.forEach { hook -> hook(EXTENSION_CLASS_DESCRIPTOR) }

        // Modify Utils method to include the patches release version.
        /**
         * @return The file path for the jar this classfile is contained inside.
         */
        fun getCurrentJarFilePath(): String {
            val className = object {}::class.java.enclosingClass.name.replace('.', '/') + ".class"
            val classUrl = object {}::class.java.classLoader?.getResource(className)
            if (classUrl != null) {
                val urlString = classUrl.toString()

                if (urlString.startsWith("jar:file:")) {
                    val end = urlString.lastIndexOf('!')

                    return URLDecoder.decode(urlString.substring("jar:file:".length, end), "UTF-8")
                }
            }
            throw IllegalStateException("Not running from inside a JAR file.")
        }

        /**
         * @return The value for the manifest entry,
         *         or "Unknown" if the entry does not exist or is blank.
         */
        @Suppress("SameParameterValue")
        fun getPatchesManifestEntry(attributeKey: String) = JarFile(getCurrentJarFilePath()).use { jarFile ->
            jarFile.manifest.mainAttributes.entries.firstOrNull { it.key.toString() == attributeKey }?.value?.toString()
                ?: "Unknown"
        }

        val manifestValue = getPatchesManifestEntry("Version")

        getPatchesReleaseVersionMethod.returnEarly(manifestValue)
    }
}

class ExtensionHook internal constructor(
    private val getInsertIndex: Method.() -> Int,
    private val getContextRegister: Method.() -> String,
    private val build: context(MutableList<String>) MutablePredicateList<Method>.() -> Unit,
) {
    context(context: BytecodePatchContext)
    operator fun invoke(extensionClassDescriptor: String) {
        val method = context.firstMutableMethodDeclaratively(build = build)
        val insertIndex = method.getInsertIndex()
        val contextRegister = method.getContextRegister()

        method.addInstruction(
            insertIndex,
            "invoke-static/range { $contextRegister .. $contextRegister }, " +
                "$extensionClassDescriptor->setContext(Landroid/content/Context;)V",
        )
    }
}

fun extensionHook(
    getInsertIndex: Method.() -> Int = { 0 },
    getContextRegister: Method.() -> String = { "p0" },
    build: context(MutableList<String>) MutablePredicateList<Method>.() -> Unit,
) = ExtensionHook(getInsertIndex, getContextRegister, build)

/**
 * Creates an extension hook from a non-obfuscated activity, which typically is the main activity
 * defined in the app manifest.xml file.
 *
 * @param activityClassType Either the full activity class type such as `Lcom/company/MainActivity;`
 *                          or the 'ends with' string for the activity such as `/MainActivity;`
 */
fun activityOnCreateExtensionHook(activityClassType: String): ExtensionHook {
    if (!activityClassType.endsWith(';')) {
        throw IllegalArgumentException("Activity class type does not end with semicolon: $activityClassType")
    }

    val fullClassType = activityClassType.startsWith('L')

    return extensionHook {
        name("onCreate")

        if (fullClassType) {
            definingClass(activityClassType)
        } else {
            definingClass { endsWith(activityClassType) }
        }

        returnType("V")
        parameterTypes("Landroid/os/Bundle;")
    }
}
