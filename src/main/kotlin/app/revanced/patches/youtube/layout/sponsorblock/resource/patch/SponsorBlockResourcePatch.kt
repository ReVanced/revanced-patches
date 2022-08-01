package app.revanced.patches.youtube.layout.sponsorblock.resource.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.util.resources.ResourceUtils.copyXmlNode
import java.io.OutputStream
import java.nio.file.Files

@Name("sponsorblock-resource-patch")
@SponsorBlockCompatibility
@Dependencies([FixLocaleConfigErrorPatch::class])
@Version("0.0.1")
class SponsorBlockResourcePatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        val classLoader = this.javaClass.classLoader

        /*
         merge SponsorBlock strings to main strings
         */
        val stringsResourcePath = "values/strings.xml"
        val stringsResourceInputStream = classLoader.getResourceAsStream("sponsorblock/$stringsResourcePath")!!

        // copy nodes from the resources node to the real resource node
        "resources".copyXmlNode(
            data.xmlEditor[stringsResourceInputStream, OutputStream.nullOutputStream()],
            data.xmlEditor["res/$stringsResourcePath"]
        ).close() // close afterwards

        /*
         merge SponsorBlock drawables to main drawables
         */
        val drawables = "drawable" to arrayOf(
            "ic_sb_adjust",
            "ic_sb_compare",
            "ic_sb_edit",
            "ic_sb_logo",
            "ic_sb_publish",
            "ic_sb_voting"
        )

        val layouts = "layout" to arrayOf(
            "inline_sponsor_overlay", "new_segment", "skip_sponsor_button"
        )

        // collect resources
        val xmlResources = arrayOf(drawables, layouts)

        // write resources
        xmlResources.forEach { (path, resourceNames) ->
            resourceNames.forEach { name ->
                val relativePath = "$path/$name.xml"

                Files.copy(
                    classLoader.getResourceAsStream("sponsorblock/$relativePath")!!,
                    data["res"].resolve(relativePath).toPath()
                )
            }
        }

        /*
        merge xml nodes from the host to their real xml files
         */

        // collect all host resources
        val hostingXmlResources = mapOf("layout" to arrayOf("youtube_controls_layout"))

        // copy nodes from host resources to their real xml files
        hostingXmlResources.forEach { (path, resources) ->
            resources.forEach { resource ->
                val hostingResourceStream = classLoader.getResourceAsStream("sponsorblock/host/$path/$resource.xml")!!

                val targetXmlEditor = data.xmlEditor["res/$path/$resource.xml"]
                "RelativeLayout".copyXmlNode(
                    data.xmlEditor[hostingResourceStream, OutputStream.nullOutputStream()],
                    targetXmlEditor
                ).also {
                    val children = targetXmlEditor.file.getElementsByTagName("RelativeLayout").item(0).childNodes

                    // Replace the startOf with the voting button view so that the button does not overlap
                    for (i in 1 until children.length) {
                        val view = children.item(i)

                        // Replace the attribute for a specific node only
                        if (!(view.hasAttributes() && view.attributes.getNamedItem("android:id").nodeValue.endsWith("live_chat_overlay_button"))) continue

                        // voting button id from the voting button view from the youtube_controls_layout.xml host file
                        val votingButtonId = "@+id/voting_button"

                        view.attributes.getNamedItem("android:layout_toStartOf").nodeValue = votingButtonId

                        break
                    }
                }.close() // close afterwards
            }
        }
        return PatchResultSuccess()
    }
}