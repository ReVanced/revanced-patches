package app.revanced.patches.music.utils.settings

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.settings.ResourceUtils.YOUTUBE_MUSIC_SETTINGS_KEY
import app.revanced.patches.music.utils.settings.ResourceUtils.addMusicPreference
import app.revanced.patches.music.utils.settings.ResourceUtils.addMusicPreferenceCategory
import app.revanced.patches.music.utils.settings.ResourceUtils.addMusicPreferenceWithIntent
import app.revanced.patches.music.utils.settings.ResourceUtils.addMusicPreferenceWithoutSummary
import app.revanced.patches.music.utils.settings.ResourceUtils.addReVancedMusicPreference
import app.revanced.patches.music.utils.settings.ResourceUtils.sortMusicPreferenceCategory
import app.revanced.patches.shared.patch.settings.AbstractSettingsResourcePatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.copyXmlNode
import org.w3c.dom.Element
import java.io.Closeable

@Patch(
    name = "Settings",
    description = "Adds ReVanced Extended settings to YouTube Music.",
    dependencies = [SettingsBytecodePatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object SettingsPatch : AbstractSettingsResourcePatch(
    "music/settings"
), Closeable {
    override fun execute(context: ResourceContext) {
        contexts = context

        /**
         * create directory for the untranslated language resources
         */
        context["res/values-v21"].mkdirs()

        arrayOf(
            ResourceGroup(
                "values-v21",
                "strings.xml"
            )
        ).forEach { resourceGroup ->
            context.copyResources("music/settings", resourceGroup)
        }

        /**
         * hide divider
         */
        val styleFile = context["res/values/styles.xml"]

        styleFile.writeText(
            styleFile.readText()
                .replace(
                    "allowDividerAbove\">true",
                    "allowDividerAbove\">false"
                ).replace(
                    "allowDividerBelow\">true",
                    "allowDividerBelow\">false"
                )
        )


        /**
         * Copy colors
         */
        context.xmlEditor["res/values/colors.xml"].use { editor ->
            val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element

            for (i in 0 until resourcesNode.childNodes.length) {
                val node = resourcesNode.childNodes.item(i) as? Element ?: continue

                node.textContent = when (node.getAttribute("name")) {
                    "material_deep_teal_500" -> "@android:color/white"

                    else -> continue
                }
            }
        }

        context.addReVancedMusicPreference(YOUTUBE_MUSIC_SETTINGS_KEY)

        super.execute(context)

    }

    lateinit var contexts: ResourceContext

    internal fun addMusicPreference(
        category: CategoryType,
        key: String,
        defaultValue: String
    ) {
        addMusicPreference(category, key, defaultValue, "")
    }

    internal fun addMusicPreference(
        category: CategoryType,
        key: String,
        defaultValue: String,
        dependencyKey: String
    ) {
        val categoryValue = category.value
        contexts.addMusicPreferenceCategory(categoryValue)
        contexts.addMusicPreference(categoryValue, key, defaultValue, dependencyKey)
    }

    internal fun addMusicPreferenceWithoutSummary(
        category: CategoryType,
        key: String,
        defaultValue: String
    ) {
        val categoryValue = category.value
        contexts.addMusicPreferenceCategory(categoryValue)
        contexts.addMusicPreferenceWithoutSummary(categoryValue, key, defaultValue)
    }

    internal fun addMusicPreferenceWithIntent(
        category: CategoryType,
        key: String
    ) {
        addMusicPreferenceWithIntent(category, key, "")
    }

    internal fun addMusicPreferenceWithIntent(
        category: CategoryType,
        key: String,
        dependencyKey: String
    ) {
        val categoryValue = category.value
        contexts.addMusicPreferenceCategory(categoryValue)
        contexts.addMusicPreferenceWithIntent(categoryValue, key, dependencyKey)
    }

    override fun close() {
        /**
         * Copy arrays
         */
        contexts.copyXmlNode("music/settings/host", "values/arrays.xml", "resources")

        addMusicPreferenceWithIntent(
            CategoryType.MISC,
            "revanced_extended_settings_import_export",
            ""
        )

        CategoryType.entries.sorted().forEach {
            contexts.sortMusicPreferenceCategory(it.value)
        }
    }
}
