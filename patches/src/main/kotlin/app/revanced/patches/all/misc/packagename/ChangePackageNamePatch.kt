package app.revanced.patches.all.misc.packagename

import app.revanced.patcher.patch.Option
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.util.asSequence
import org.w3c.dom.Element

lateinit var packageNameOption: Option<String>
lateinit var updatePermissions: Option<Boolean>
lateinit var updateProviders: Option<Boolean>

/**
 * Set the package name to use.
 * If this is called multiple times, the first call will set the package name.
 *
 * @param fallbackPackageName The package name to use if the user has not already specified a package name.
 * @return The package name that was set.
 * @throws OptionException.ValueValidationException If the package name is invalid.
 */
fun setOrGetFallbackPackageName(fallbackPackageName: String): String {
    val packageName = packageNameOption.value!!

    return if (packageName == packageNameOption.default) {
        fallbackPackageName.also { packageNameOption.value = it }
    } else {
        packageName
    }
}

private const val RECEIVER_NOT_EXPORTED = "DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"

val changePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Appends \".revanced\" to the package name by default. Changing the package name of the app can lead to unexpected issues.",
    use = false,
) {
    packageNameOption = stringOption(
        key = "packageName",
        default = "Default",
        values = mapOf("Default" to "Default"),
        title = "Package name",
        description = "The name of the package to rename the app to.",
        required = true,
    ) {
        it == "Default" || it!!.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$"))
    }

    updatePermissions = booleanOption(
        key = "updatePermissions",
        default = false,
        title = "Update permissions",
        description = "Update compatibility receiver permissions.",
        required = false
    )

    updateProviders = booleanOption(
        key = "updateProviders",
        default = false,
        title = "Update providers",
        description = "Update provider names declared by the app.",
        required = false
    )

    finalize {
        document("AndroidManifest.xml").use { document ->

            val replacementPackageName = packageNameOption.value

            val manifest = document.getElementsByTagName("manifest").item(0) as Element
            val packageName = manifest.getAttribute("package")
            val newPackageName = if (replacementPackageName != packageNameOption.default) {
                replacementPackageName!!
            } else {
                "${packageName}.revanced"
            }

            manifest.setAttribute("package", newPackageName)


            if (updatePermissions.value == true) {
                val permissions = manifest.getElementsByTagName("permission").asSequence()
                val usesPermissions = manifest.getElementsByTagName("uses-permission").asSequence()

                (permissions + usesPermissions)
                    .map { it as Element }
                    .filter { it.getAttribute("android:name") == "$packageName.$RECEIVER_NOT_EXPORTED" }
                    .forEach { it.setAttribute("android:name", "$newPackageName.$RECEIVER_NOT_EXPORTED") }
            }

            if(updateProviders.value == true){
                val providers = manifest.getElementsByTagName("provider").asSequence()

                for (node in providers) {
                    val provider = node as Element

                    val authorities = provider.getAttribute("android:authorities")
                    if (!authorities.startsWith("$packageName.")) continue

                    provider.setAttribute("android:authorities", authorities.replace(packageName, newPackageName))
                }
            }
        }
    }
}

