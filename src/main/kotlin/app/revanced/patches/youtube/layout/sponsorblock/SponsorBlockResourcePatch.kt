package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.copyXmlNode

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class,
        AddResourcesPatch::class
    ]
)
internal object SponsorBlockResourcePatch : ResourcePatch() {

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            IntentPreference(
                "revanced_sb_settings_title",
                intent = SettingsPatch.newIntent("revanced_sb_settings_intent")
            )
        )
        val classLoader = this.javaClass.classLoader

        AddResourcesPatch(this::class)

        arrayOf(
            ResourceGroup(
                "layout",
                "inline_sponsor_overlay.xml",
                "new_segment.xml",
                "skip_sponsor_button.xml"
            ),
            ResourceGroup(
                // required resource for back button, because when the base APK is used, this resource will not exist
                "drawable",
                "ic_sb_adjust.xml",
                "ic_sb_compare.xml",
                "ic_sb_edit.xml",
                "ic_sb_logo.xml",
                "ic_sb_publish.xml",
                "ic_sb_voting.xml"
            ),
            ResourceGroup(
                // required resource for back button, because when the base APK is used, this resource will not exist
                "drawable-xxxhdpi", "quantum_ic_skip_next_white_24.png"
            )
        ).forEach { resourceGroup ->
            context.copyResources("sponsorblock", resourceGroup)
        }

        // copy nodes from host resources to their real xml files
        val hostingResourceStream =
            classLoader.getResourceAsStream("sponsorblock/host/layout/youtube_controls_layout.xml")!!

        val targetXmlEditor = context.xmlEditor["res/layout/youtube_controls_layout.xml"]
        "RelativeLayout".copyXmlNode(
            context.xmlEditor[hostingResourceStream],
            targetXmlEditor
        ).also {
            val children = targetXmlEditor.file.getElementsByTagName("RelativeLayout").item(0).childNodes

            // Replace the startOf with the voting button view so that the button does not overlap
            for (i in 1 until children.length) {
                val view = children.item(i)

                // Replace the attribute for a specific node only
                if (!(view.hasAttributes() && view.attributes.getNamedItem("android:id").nodeValue.endsWith("live_chat_overlay_button"))) continue

                // voting button id from the voting button view from the youtube_controls_layout.xml host file
                val votingButtonId = "@+id/sb_voting_button"

                view.attributes.getNamedItem("android:layout_toStartOf").nodeValue = votingButtonId

                break
            }
        }.close()
    }
}