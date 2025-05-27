package app.revanced.patches.primevideo.misc.permissions

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.asSequence
import app.revanced.util.getNode
import org.w3c.dom.Element

@Suppress("unused")
val renamePermissionsPatch = resourcePatch(
    name = "Rename shared permissions",
    description = "Rename certain permissions shared across Amazon apps. " +
            "Applying this patch can fix installation errors, but can also break features in certain apps.",
    use = false
) {
    compatibleWith("com.amazon.avod.thirdpartyclient")

    val permissionNames = setOf(
        "com.amazon.identity.permission.CAN_CALL_MAP_INFORMATION_PROVIDER",
        "com.amazon.identity.auth.device.perm.AUTH_SDK",
        "com.amazon.dcp.sso.permission.account.changed",
        "com.amazon.dcp.sso.permission.AmazonAccountPropertyService.property.changed",
        "com.amazon.identity.permission.CALL_AMAZON_DEVICE_INFORMATION_PROVIDER",
        "com.amazon.appmanager.preload.permission.READ_PRELOAD_DEVICE_INFO_PROVIDER"
    )

    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.getNode("manifest") as Element

            val permissions = manifest
                .getElementsByTagName("permission")
                .asSequence()
                .map { Pair(it as Element, it.getAttribute("android:name")) }
                .filter { (_, name) -> name in permissionNames }

            if (permissions.none()) throw PatchException("Could not find any permissions to rename")

            permissions.forEach { (element, name) ->
                element.setAttribute("android:name", "revanced.$name")
            }
        }
    }
}
