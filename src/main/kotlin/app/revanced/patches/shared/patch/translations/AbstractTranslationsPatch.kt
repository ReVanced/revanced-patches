package app.revanced.patches.shared.patch.translations

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.util.classLoader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

abstract class AbstractTranslationsPatch(
    private val sourceDirectory: String,
    private val languageArray: Array<String>
) : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        languageArray.forEach { language ->
            val directory = "values-$language-v21"
            val relativePath = "$language/strings.xml"

            context["res/$directory"].mkdir()

            Files.copy(
                classLoader.getResourceAsStream("$sourceDirectory/translations/$relativePath")!!,
                context["res"].resolve("$directory/strings.xml").toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }
}
