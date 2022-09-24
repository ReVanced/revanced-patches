package app.revanced.util.microg

import app.revanced.patcher.data.impl.ResourceData

/**
 * Helper class for applying resource patches needed for the microg-support patches.
 */
internal object MicroGResourceHelper {
    /**
     * Patch the manifest to work with MicroG.
     *
     * @param data Bytecode data instance.
     * @param fromPackageName Original package name.
     * @param toPackageName The package name to accept.
     * @param toName The new name of the app.
     */
    fun patchManifest(
        data: ResourceData,
        fromPackageName: String,
        toPackageName: String,
        toName: String
    ) {
        val manifest = data["AndroidManifest.xml"].readText()
        data["AndroidManifest.xml"].writeText(
            manifest.replace(
                "package=\"$fromPackageName",
                "package=\"$toPackageName"
            ).replace(
                "android:label=\"@string/app_name",
                "android:label=\"$toName"
            ).replace(
                "android:label=\"@string/app_launcher_name",
                "android:label=\"$toName"
            ).replace(
                "android:authorities=\"$fromPackageName",
                "android:authorities=\"$toPackageName"
            ).replace(
                "$fromPackageName.permission.C2D_MESSAGE",
                "$toPackageName.permission.C2D_MESSAGE"
            ).replace(
                "com.google.android.c2dm",
                "${Constants.MICROG_VENDOR}.android.c2dm"
            ).replace(
                "</queries>",
                "<package android:name=\"${Constants.MICROG_VENDOR}.android.gms\"/></queries>"
            )
        )
    }
}