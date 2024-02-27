package app.revanced.patches.reddit.layout.branding.packagename

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import org.w3c.dom.Element
import java.io.Closeable

@Patch(
    name = "Change package name",
    description = "Changes the package name for Reddit to the name specified in options.json.",
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")],
    use = false
)
@Suppress("unused")
object ChangePackageNamePatch : ResourcePatch(), Closeable {
    private const val PACKAGE_NAME_REDDIT = "com.reddit.frontpage"
    private const val CLONE_PACKAGE_NAME_REDDIT = "$PACKAGE_NAME_REDDIT.revanced"
    private const val DEFAULT_PACKAGE_NAME_REDDIT = "$PACKAGE_NAME_REDDIT.rvx"

    private lateinit var context: ResourceContext
    private lateinit var redditPackageName: String

    private val PackageNameReddit by stringPatchOption(
        key = "PackageNameReddit",
        default = DEFAULT_PACKAGE_NAME_REDDIT,
        values = mapOf(
            "Clone" to CLONE_PACKAGE_NAME_REDDIT,
            "Default" to DEFAULT_PACKAGE_NAME_REDDIT
        ),
        title = "Package name of Reddit",
        description = "The name of the package to rename the app to."
    )

    override fun execute(context: ResourceContext) {
        this.context = context

        redditPackageName = PackageNameReddit
            ?: throw PatchException("Invalid package name.")

        // Ensure device runs Android.
        try {
            // RVX Manager
            // ====
            // For some reason, in Android AAPT2, a compilation error occurs when changing the [strings.xml] of the Reddit
            // This only affects RVX Manager, and has not yet found a valid workaround
            Class.forName("android.os.Environment")
        } catch (_: ClassNotFoundException) {
            // CLI
            context.replacePackageName(redditPackageName)
        }
    }

    override fun close() {
        context["AndroidManifest.xml"].apply {
            writeText(
                readText()
                    .replace(
                        "package=\"$PACKAGE_NAME_REDDIT",
                        "package=\"$redditPackageName"
                    )
                    .replace(
                        "$PACKAGE_NAME_REDDIT.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                        "$redditPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
                    )
            )
        }
    }

    private fun ResourceContext.replacePackageName(redditPackageName: String) {
        // replace strings
        this.xmlEditor["res/values/strings.xml"].use { editor ->
            val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element

            val children = resourcesNode.childNodes
            for (i in 0 until children.length) {
                val node = children.item(i) as? Element ?: continue

                node.textContent = when (node.getAttribute("name")) {
                    "provider_authority_appdata", "provider_authority_file",
                    "provider_authority_userdata", "provider_workmanager_init"
                    -> node.textContent.replace(PACKAGE_NAME_REDDIT, redditPackageName)

                    else -> continue
                }
            }
        }

        // replace manifest permission and provider
        this["AndroidManifest.xml"].apply {
            writeText(
                readText()
                    .replace(
                        "android:authorities=\"$PACKAGE_NAME_REDDIT",
                        "android:authorities=\"$redditPackageName"
                    )
            )
        }
    }
}
