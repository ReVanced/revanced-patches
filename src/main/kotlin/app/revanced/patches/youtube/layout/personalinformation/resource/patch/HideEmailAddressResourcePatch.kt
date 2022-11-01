package app.revanced.patches.youtube.layout.personalinformation.resource.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.layout.personalinformation.annotations.HideEmailAddressCompatibility
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference

@Name("hide-email-address-resource-patch")
@HideEmailAddressCompatibility
@DependsOn([SettingsPatch::class, ResourceMappingResourcePatch::class])
@Version("0.0.1")
class HideEmailAddressResourcePatch : ResourcePatch {
    companion object {
        internal var accountSwitcherAccessibilityLabelId: Long = -1
    }

    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_email_address",
                StringResource("revanced_hide_email_address_title", "Hide the email address"),
                false,
                StringResource("revanced_hide_email_address_summary_on", "Email address is hidden"),
                StringResource("revanced_hide_email_address_summary_off", "Email address is visible")
            )
        )

        accountSwitcherAccessibilityLabelId = ResourceMappingResourcePatch.resourceMappings.single {
            it.type == "string" && it.name == "account_switcher_accessibility_label"
        }.id

        return PatchResultSuccess()
    }
}