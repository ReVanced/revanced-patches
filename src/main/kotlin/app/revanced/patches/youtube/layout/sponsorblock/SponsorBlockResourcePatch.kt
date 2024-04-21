package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.settings.SettingsResourcePatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.copyXmlNode
import app.revanced.util.inputStreamFromBundledResource

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class,
        AddResourcesPatch::class,
    ],
)
internal object SponsorBlockResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsResourcePatch += IntentPreference(
            key = "revanced_settings_screen_10",
            titleKey = "revanced_sb_settings_title",
            summaryKey = null,
            intent = SettingsPatch.newIntent("revanced_sb_settings_intent")
        )

        arrayOf(
            ResourceGroup(
                "layout",
                "revanced_sb_inline_sponsor_overlay.xml",
                "revanced_sb_new_segment.xml",
                "revanced_sb_skip_sponsor_button.xml",
            ),
            ResourceGroup(
                // required resource for back button, because when the base APK is used, this resource will not exist
                "drawable",
                "revanced_sb_adjust.xml",
                "revanced_sb_backward.xml",
                "revanced_sb_compare.xml",
                "revanced_sb_edit.xml",
                "revanced_sb_forward.xml",
                "revanced_sb_logo.xml",
                "revanced_sb_publish.xml",
                "revanced_sb_voting.xml",
            ),
            ResourceGroup(
                // required resource for back button, because when the base APK is used, this resource will not exist
                "drawable-xxxhdpi",
                "quantum_ic_skip_next_white_24.png",
            ),
        ).forEach { resourceGroup ->
            context.copyResources("sponsorblock", resourceGroup)
        }

        // copy nodes from host resources to their real xml files

        val hostingResourceStream =
            inputStreamFromBundledResource(
                "sponsorblock",
                "host/layout/youtube_controls_layout.xml",
            )!!

        var modifiedControlsLayout = false
        val editor = context.xmlEditor["res/layout/youtube_controls_layout.xml"]
        "RelativeLayout".copyXmlNode(
            context.xmlEditor[hostingResourceStream],
            editor,
        ).also {
            val document = editor.file

            val children = document.getElementsByTagName("RelativeLayout").item(0).childNodes

            // Replace the startOf with the voting button view so that the button does not overlap
            for (i in 1 until children.length) {
                val view = children.item(i)

                // Replace the attribute for a specific node only
                if (!(
                        view.hasAttributes() &&
                            view.attributes.getNamedItem(
                                "android:id",
                            ).nodeValue.endsWith("live_chat_overlay_button")
                        )
                ) {
                    continue
                }

                // voting button id from the voting button view from the youtube_controls_layout.xml host file
                val votingButtonId = "@+id/revanced_sb_voting_button"

                view.attributes.getNamedItem("android:layout_toStartOf").nodeValue = votingButtonId

                modifiedControlsLayout = true
                break
            }
        }.close()

        if (!modifiedControlsLayout) throw PatchException("Could not modify controls layout")
    }
}
