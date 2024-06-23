package app.revanced.patches.shared.misc.integrations

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.FingerprintBuilder
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.Method
import java.net.URLDecoder
import java.util.jar.JarFile

internal const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/shared/Utils;"

fun integrationsPatch(vararg hooks: IntegrationsHook) = bytecodePatch(requiresIntegrations = true) {
    val revancedUtilsPatchesVersionMatch by revancedUtilsPatchesVersionFingerprint()()
    hooks.forEach { it.fingerprint() }

    execute { context ->
        if (context.classByType(INTEGRATIONS_CLASS_DESCRIPTOR) == null) {
            throw PatchException(
                "Integrations have not been merged yet. This patch can not succeed without merging the integrations.",
            )
        }

        hooks.forEach { hook -> hook(INTEGRATIONS_CLASS_DESCRIPTOR) }

        // Modify Utils method to include the patches release version.
        revancedUtilsPatchesVersionMatch.mutableMethod.apply {
            /**
             * @return The file path for the jar this classfile is contained inside.
             */
            fun getCurrentJarFilePath(): String {
                val className = object {}::class.java.enclosingClass.name.replace('.', '/') + ".class"
                val classUrl = object {}::class.java.classLoader.getResource(className)
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

            addInstructions(
                0,
                """
                    const-string v0, "$manifestValue"
                    return-object v0
                """,
            )
        }
    }
}

class IntegrationsHook internal constructor(
    val fingerprint: Fingerprint,
    private val insertIndexResolver: ((Method) -> Int),
    private val contextRegisterResolver: (Method) -> Int,
) {
    operator fun invoke(integrationsClassDescriptor: String) {
        fingerprint.match?.mutableMethod?.let { method ->
            val insertIndex = insertIndexResolver(method)
            val contextRegister = contextRegisterResolver(method)

            method.addInstruction(
                insertIndex,
                "invoke-static/range { v$contextRegister .. v$contextRegister }, " +
                    "$integrationsClassDescriptor->setContext(Landroid/content/Context;)V",
            )
        } ?: throw fingerprint.exception
    }
}

fun integrationsHook(
    insertIndexResolver: ((Method) -> Int) = { 0 },
    contextRegisterResolver: (Method) -> Int = { it.implementation!!.registerCount - 1 },
    fingerprintBuilderBlock: FingerprintBuilder.() -> Unit,
) = IntegrationsHook(fingerprint(block = fingerprintBuilderBlock), insertIndexResolver, contextRegisterResolver)
