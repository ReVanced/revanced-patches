package app.revanced.patches.shared.patch.elements

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import kotlin.io.path.exists

abstract class AbstractRemoveStringsElementsPatch(
    private val paths: Array<String>,
    private val replacements: Array<String>
) : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        paths.forEach { path ->
            val resDirectory = context["res"]
            val targetXmlPath = resDirectory.resolve(path).resolve("strings.xml").toPath()

            if (targetXmlPath.exists()) {
                val targetXml = context["res/$path/strings.xml"]

                replacements.forEach replacementsLoop@{ replacement ->
                    targetXml.writeText(
                        targetXml.readText()
                            .replaceFirst(""" {4}<string name="$replacement".+""".toRegex(), "")
                    )
                }
            }
        }
    }
}
