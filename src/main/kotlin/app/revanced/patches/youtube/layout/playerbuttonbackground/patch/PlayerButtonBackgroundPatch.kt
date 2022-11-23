package app.revanced.patches.youtube.layout.playerbuttonbackground.patch

import app.revanced.extensions.doRecursively
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patches.youtube.layout.playerbuttonbackground.annotations.PlayerButtonBackgroundCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import org.w3c.dom.Element

@Patch
@DependsOn([FixLocaleConfigErrorPatch::class])
@Name("remove-player-button-background")
@Description("Removes the background from the video player buttons.")
@PlayerButtonBackgroundCompatibility
@Version("0.0.1")
class PlayerButtonBackgroundPatch : ResourcePatch {
    private companion object {
        const val RESOURCE_FILE_PATH = "res/drawable/player_button_circle_background.xml"
    }
    
    override fun execute(context: ResourceContext): PatchResult {
        context.xmlEditor[RESOURCE_FILE_PATH].use { editor ->
            editor.file.doRecursively node@{ node ->
                if (node !is Element) return@node

                node.getAttributeNode("android:color")?.let { attribute ->
                    attribute.textContent = "@android:color/transparent"
                }
            }
        }

        return PatchResultSuccess()
    }
}
