package app.revanced.patches.metanav.scaling.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.metanav.scaling.annotations.FixScalingCompatibility
import org.w3c.dom.Element

@Patch
@Name("fix-scaling")
@Description("Scales the content properly.")
@FixScalingCompatibility
@Version("0.0.1")
class FixScalingPatch : ResourcePatch {
	override fun execute(context: ResourceContext): PatchResult {
		context.xmlEditor["assets/startScreenCanvas.html"].use { editor ->
			val svgNode = editor
				.file
				.getElementsByTagName("svg")
				.item(0) as Element

			svgNode.setAttribute("height", "750")
		}

		return PatchResultSuccess()
	}
}
