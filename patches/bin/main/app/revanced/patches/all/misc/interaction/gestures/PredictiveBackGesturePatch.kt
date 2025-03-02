package app.revanced.patches.all.misc.interaction.gestures

import app.revanced.patcher.patch.resourcePatch

@Suppress("unused")
val predictiveBackGesturePatch = resourcePatch(
    name = "Predictive back gesture",
    description = "Enables the predictive back gesture introduced on Android 13.",
    use = false,
) {
    execute {
        val flag = "android:enableOnBackInvokedCallback"

        document("AndroidManifest.xml").use { document ->
            with(document.getElementsByTagName("application").item(0)) {
                if (attributes.getNamedItem(flag) != null) return@with

                document.createAttribute(flag)
                    .apply { value = "true" }
                    .let(attributes::setNamedItem)
            }
        }
    }
}
