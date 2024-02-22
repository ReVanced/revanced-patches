package app.revanced.patches.all.interaction.gestures

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch

@Patch(
    name = "Predictive back gesture",
    description = "Enables the predictive back gesture introduced on Android 13.",
    use = false,
)
@Suppress("unused")
object PredictiveBackGesturePatch : ResourcePatch() {
    private const val FLAG = "android:enableOnBackInvokedCallback"

    override fun execute(context: ResourceContext) {
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val document = editor.file

            with(document.getElementsByTagName("application").item(0)) {
                if (attributes.getNamedItem(FLAG) != null) return@with

                document.createAttribute(FLAG)
                    .apply { value = "true" }
                    .let(attributes::setNamedItem)
            }
        }
    }
}
