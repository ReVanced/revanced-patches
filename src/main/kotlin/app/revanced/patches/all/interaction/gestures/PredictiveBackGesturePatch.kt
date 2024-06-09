package app.revanced.patches.all.interaction.gestures

import app.revanced.patcher.patch.resourcePatch

@Suppress("unused")
val predictiveBackGesturePatch = resourcePatch(
    name = "Predictive back gesture",
    description = "Enables the predictive back gesture introduced on Android 13.",
    use = false,
) {
    val flag = "android:enableOnBackInvokedCallback"

    execute { context ->
        context.document["AndroidManifest.xml"].use {
            with(it.getElementsByTagName("application").item(0)) {
                if (attributes.getNamedItem(flag) != null) return@with

                it.createAttribute(flag)
                    .apply { value = "true" }
                    .let(attributes::setNamedItem)
            }
        }
    }
}