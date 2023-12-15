package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.Preference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.copyXmlNode

@Patch(dependencies = [SettingsPatch::class, ResourceMappingPatch::class])
internal object SponsorBlockResourcePatch : ResourcePatch() {

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("SponsorBlock")
        SettingsPatch.addPreference(
            Preference(
                "revanced_sponsorblock_settings_title",
                "revanced_sponsorblock_settings_summary",
                SettingsPatch.createReVancedSettingsIntent("sponsorblock_settings_intent")
            )
        )

        val classLoader = this.javaClass.classLoader

        /*
         merge SponsorBlock drawables to main drawables
         */

        context.copyResources(
            "youtube/sponsorblock",
            ResourceGroup(
                "drawable",
                "revanced_ic_sb_adjust.xml",
                "revanced_ic_sb_compare.xml",
                "revanced_ic_sb_edit.xml",
                "revanced_ic_sb_logo.xml",
                "revanced_ic_sb_publish.xml",
                "revanced_ic_sb_voting.xml"
            ),
            ResourceGroup(
                "layout",
                "inline_sponsor_overlay.xml",
                "new_segment.xml",
                "skip_sponsor_button.xml"
            ),
            ResourceGroup(
                // required resource for back button, because when the base APK is used, this resource will not exist
                "drawable",
                "revanced_ic_sb_adjust.xml",
                "revanced_ic_sb_compare.xml",
                "revanced_ic_sb_edit.xml",
                "revanced_ic_sb_logo.xml",
                "revanced_ic_sb_publish.xml",
                "revanced_ic_sb_voting.xml"
            ),
            ResourceGroup(
                // required resource for back button, because when the base APK is used, this resource will not exist
                "drawable-xxxhdpi", "quantum_ic_skip_next_white_24.png"
            )
        )

        /*
        merge xml nodes from the host to their real xml files
         */

        // copy nodes from host resources to their real xml files
        val hostingResourceStream =
            classLoader.getResourceAsStream("youtube/sponsorblock/host/layout/youtube_controls_layout.xml")!!

        val targetXmlEditor = context.xmlEditor["res/layout/youtube_controls_layout.xml"]
        "RelativeLayout".copyXmlNode(
            context.xmlEditor[hostingResourceStream],
            targetXmlEditor
        ).also {
            val children =
                targetXmlEditor.file.getElementsByTagName("RelativeLayout").item(0).childNodes

            // Replace the startOf with the voting button view so that the button does not overlap
            for (i in 1 until children.length) {
                val view = children.item(i)

                // Replace the attribute for a specific node only
                if (!(view.hasAttributes() && view.attributes.getNamedItem("android:id").nodeValue.endsWith(
                        "live_chat_overlay_button"
                    ))
                ) continue

                // voting button id from the voting button view from the youtube_controls_layout.xml host file
                val votingButtonId = "@+id/revanced_sb_voting_button"

                view.attributes.getNamedItem("android:layout_toStartOf").nodeValue = votingButtonId

                break
            }
        }.close() // close afterwards
    }
}