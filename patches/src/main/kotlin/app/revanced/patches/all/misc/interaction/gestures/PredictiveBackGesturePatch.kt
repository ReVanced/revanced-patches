package app.revanced.patches.all.misc.interaction.gestures

import app.revanced.patcher.patch.creatingResourcePatch

@Suppress("unused", "ObjectPropertyName")
val `Predictive back gesture` by creatingResourcePatch(
    description = "Enables the predictive back gesture introduced on Android 13.",
    use = false,
) {
    apply {
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
