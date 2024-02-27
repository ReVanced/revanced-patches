package app.revanced.patches.music.layout.branding.name

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.music.utils.integrations.Constants.LANGUAGE_LIST
import app.revanced.patches.shared.patch.elements.AbstractRemoveStringsElementsPatch

@Patch(
    name = "Custom branding name YouTube Music",
    description = "Renames the YouTube Music app to the name specified in options.json.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object CustomBrandingNamePatch : AbstractRemoveStringsElementsPatch(
    LANGUAGE_LIST,
    arrayOf("app_launcher_name", "app_name")
) {
    private const val APP_NAME_NOTIFICATION = "ReVanced Extended Music"
    private const val APP_NAME_LAUNCHER = "RVX Music"

    private val AppNameNotification by stringPatchOption(
        key = "AppNameNotification",
        default = APP_NAME_NOTIFICATION,
        values = mapOf(
            "Full name" to APP_NAME_NOTIFICATION,
            "Short name" to APP_NAME_LAUNCHER
        ),
        title = "App name in notification panel",
        description = "The name of the app as it appears in the notification panel.",
        required = true
    )

    private val AppNameLauncher by stringPatchOption(
        key = "AppNameLauncher",
        default = APP_NAME_LAUNCHER,
        values = mapOf(
            "Full name" to APP_NAME_NOTIFICATION,
            "Short name" to APP_NAME_LAUNCHER
        ),
        title = "App name in launcher",
        description = "The name of the app as it appears in the launcher.",
        required = true
    )

    override fun execute(context: ResourceContext) {
        super.execute(context)

        AppNameNotification?.let { notificationName ->
            AppNameLauncher?.let { launcherName ->
                context.xmlEditor["res/values/strings.xml"].use { editor ->
                    val document = editor.file

                    mapOf(
                        "app_name" to notificationName,
                        "app_launcher_name" to launcherName
                    ).forEach { (k, v) ->
                        val stringElement = document.createElement("string")

                        stringElement.setAttribute("name", k)
                        stringElement.textContent = v

                        document.getElementsByTagName("resources").item(0)
                            .appendChild(stringElement)
                    }
                }
            } ?: throw PatchException("Invalid app name.")
        } ?: throw PatchException("Invalid app name.")
    }
}
