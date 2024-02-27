package app.revanced.patches.shared.patch.microg

import app.revanced.patcher.data.ResourceContext

/**
 * Helper class for applying resource patches needed for the microg-support patches.
 */
object MicroGResourceHelper {
    /**
     * Patch the manifest to work with MicroG.
     *
     * @param fromPackageName Original package name.
     * @param toPackageName The package name to accept.
     */
    fun ResourceContext.patchManifest(
        fromPackageName: String,
        toPackageName: String
    ) {
        val manifest = this["AndroidManifest.xml"]

        manifest.writeText(
            manifest.readText()
                .replace(
                    "package=\"$fromPackageName",
                    "package=\"$toPackageName"
                ).replace(
                    "android:authorities=\"$fromPackageName",
                    "android:authorities=\"$toPackageName"
                ).replace(
                    "$fromPackageName.permission.C2D_MESSAGE",
                    "$toPackageName.permission.C2D_MESSAGE"
                ).replace(
                    "$fromPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                    "$toPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
                ).replace(
                    "com.google.android.c2dm",
                    "${Constants.MICROG_VENDOR}.android.c2dm"
                ).replace(
                    "</queries>",
                    "<package android:name=\"${Constants.MICROG_VENDOR}.android.gms\"/></queries>"
                )
        )
    }

    /**
     * Patch the settings fragment to work with MicroG.
     *
     * @param fromPackageName Original package name.
     * @param toPackageName The package name to accept.
     */
    fun ResourceContext.patchSetting(
        fromPackageName: String,
        toPackageName: String
    ) {
        val prefs = this["res/xml/settings_fragment.xml"]

        prefs.writeText(
            prefs.readText()
                .replace(
                    "android:targetPackage=\"$fromPackageName",
                    "android:targetPackage=\"$toPackageName"
                )
        )
    }
}