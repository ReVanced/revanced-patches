package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.data.ResourceContext
import app.revanced.patches.all.misc.strings.AddResourcesPatch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.AbstractSettingsResourcePatch
import app.revanced.patches.shared.settings.preference.impl.IntentPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.copyStrings
import app.revanced.util.resource.StringResource
import org.w3c.dom.Element

object SettingsResourcePatch : AbstractSettingsResourcePatch(
    IntentPreference(
        StringResource("revanced_settings", "ReVanced"),
        StringResource("revanced_settings_summary", "Settings for ReVanced"),
        SettingsPatch.newIntent("revanced_settings")
    ) to "settings_fragment",
    dependencies = setOf(
        ResourceMappingPatch::class,
        AddResourcesPatch::class,
    )
) {
    // Used for a fingerprint from SettingsPatch.
    internal var appearanceStringId = -1L

    override fun execute(context: ResourceContext) {
        super.execute(context)

        // Used for a fingerprint from SettingsPatch.
        appearanceStringId = ResourceMappingPatch.resourceMappings.find {
            it.type == "string" && it.name == "app_theme_appearance_dark"
        }!!.id

        arrayOf(
            ResourceGroup("layout", "revanced_settings_with_toolbar.xml")
        ).forEach { resourceGroup ->
            context.copyResources("settings", resourceGroup)
        }

        // Modify the manifest and add a data intent filter to the LicenseActivity.
        // Some devices freak out if undeclared data is passed to an intent,
        // and this change appears to fix the issue.
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            // A xml regular-expression would probably work better than this manual searching.
            val manifestNodes = editor.file.getElementsByTagName("manifest").item(0).childNodes
            for (i in 0..manifestNodes.length) {
                val node = manifestNodes.item(i)
                if (node != null && node.nodeName == "application") {
                    val applicationNodes = node.childNodes
                    for (j in 0..applicationNodes.length) {
                        val applicationChild = applicationNodes.item(j)
                        if (applicationChild is Element && applicationChild.nodeName == "activity"
                            && applicationChild.getAttribute("android:name") == "com.google.android.libraries.social.licenses.LicenseActivity"
                        ) {
                            val intentFilter = editor.file.createElement("intent-filter")
                            val mimeType = editor.file.createElement("data")
                            mimeType.setAttribute("android:mimeType", "text/plain")
                            intentFilter.appendChild(mimeType)
                            applicationChild.appendChild(intentFilter)
                            break
                        }
                    }
                }
            }
        }

        context.copyStrings("settings/host/values/strings.xml")
    }
}