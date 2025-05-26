package app.revanced.patches.primevideo.misc.permissions

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.asSequence
import app.revanced.util.getNode
import org.w3c.dom.Element

@Suppress("unused")
val renamePermissionsPatch = resourcePatch(
    name = "Rename shared permissions",
    "Rename certain permissions shared across Amazon apps. " +
            "Enabling this can fix installation errors, but this can also break features in certain apps.",
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

            manifest
                .getElementsByTagName("permission")
                .asSequence()
                .map { it as Element }
                .filter { it.getAttribute("android:name") in permissionNames }
                .forEach {
                    val name = it.getAttribute("android:name")
                    it.setAttribute("android:name", "revanced.$name")
                }
        }
    }
}