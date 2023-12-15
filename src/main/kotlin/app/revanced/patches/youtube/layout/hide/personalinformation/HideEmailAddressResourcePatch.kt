package app.revanced.patches.youtube.layout.hide.personalinformation

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(dependencies = [SettingsPatch::class, ResourceMappingPatch::class])
@Suppress("unused")
internal object HideEmailAddressResourcePatch : ResourcePatch() {
    internal var accountSwitcherAccessibilityLabelId: Long = -1

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("HideEmailAddress")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_email_address",
                "revanced_hide_email_address_title",
                "revanced_hide_email_address_summary_on",
                "revanced_hide_email_address_summary_off"
            )
        )

        accountSwitcherAccessibilityLabelId = ResourceMappingPatch.resourceMappings.single {
            it.type == "string" && it.name == "account_switcher_accessibility_label"
        }.id
    }
}