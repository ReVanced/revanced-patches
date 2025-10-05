package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.ResourcePatchBuilder
import app.revanced.patcher.patch.ResourcePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.copyResources
import app.revanced.util.findElementByAttributeValueOrThrow
import app.revanced.util.returnEarly
import org.w3c.dom.Element
import java.io.File

internal val mipmapDirectories = arrayOf(
    // Target app does not have ldpi icons.
    "mipmap-mdpi",
    "mipmap-hdpi",
    "mipmap-xhdpi",
    "mipmap-xxhdpi",
    "mipmap-xxxhdpi"
)

private val iconStyleNames = arrayOf(
    "rounded",
    "minimal",
    "scaled"
)

private const val ORIGINAL_USER_ICON_STYLE_NAME = "original"
private const val CUSTOM_USER_ICON_STYLE_NAME = "custom"

private const val LAUNCHER_RESOURCE_NAME_PREFIX = "revanced_launcher_"
private const val LAUNCHER_ADAPTIVE_BACKGROUND_PREFIX = "revanced_adaptive_background_"
private const val LAUNCHER_ADAPTIVE_FOREGROUND_PREFIX = "revanced_adaptive_foreground_"
private const val LAUNCHER_ADAPTIVE_MONOCHROME_PREFIX = "revanced_adaptive_monochrome_"

private val USER_CUSTOM_ADAPTIVE_FILE_NAMES = arrayOf(
    "$LAUNCHER_ADAPTIVE_BACKGROUND_PREFIX$CUSTOM_USER_ICON_STYLE_NAME.png",
    "$LAUNCHER_ADAPTIVE_FOREGROUND_PREFIX$CUSTOM_USER_ICON_STYLE_NAME.png"
)

private const val USER_CUSTOM_MONOCHROME_NAME =
    "$LAUNCHER_ADAPTIVE_MONOCHROME_PREFIX$CUSTOM_USER_ICON_STYLE_NAME.xml"

private fun formatResourceFileList(resourceNames: Array<String>) = resourceNames.joinToString("\n") { "- $it" }

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/shared/patches/CustomBrandingPatch;"

/**
 * Shared custom branding patch for YouTube and YT Music.
 */
internal fun baseCustomBrandingPatch(
    addResourcePatchName: String,
    originalLauncherIconName: String,
    originalAppName: String,
    numberOfPresetAppNames: Int,
    mainActivityOnCreateFingerprint: Fingerprint,
    mainActivityName: String,
    activityAliasNameWithIntentToRemove: String,
    preferenceScreen: BasePreferenceScreen.Screen,
    block: ResourcePatchBuilder.() -> Unit = {},
    executeBlock: ResourcePatchContext.() -> Unit = {}
): ResourcePatch = resourcePatch(
    name = "Custom branding",
    description = "Adds options to change the app icon and app name."
) {
    val customName by stringOption(
        key = "customName",
        title = "App name",
        description = "Custom app name."
    )

    val customIcon by stringOption(
        key = "customIcon",
        title = "Custom icon",
        description = """
            Folder with images to use as a custom icon.
            
            The folder must contain one or more of the following folders, depending on the DPI of the device:
            ${formatResourceFileList(mipmapDirectories)}
            
            Each of the folders must contain all of the following files:
            ${USER_CUSTOM_ADAPTIVE_FILE_NAMES.joinToString("\n")}

            Optionally, the path can contain a 'drawable' folder with the monochrome icon file:
            $USER_CUSTOM_MONOCHROME_NAME
        """.trimIndentMultiline()
    )

    block()

    dependsOn(
        addResourcesPatch,
        bytecodePatch {
            execute {
                mainActivityOnCreateFingerprint.method.addInstruction(
                    0,
                    "invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->setBrandingIcon()V"
                )

                var totalNamePresets = numberOfPresetAppNames
                if (customName != null) {
                    totalNamePresets++
                }
                customNameIncludedFingerprint.method.returnEarly(totalNamePresets)
                customIconIncludedFingerprint.method.returnEarly(customIcon != null)
            }
        }
    )

    execute {
        addResources("shared", "layout.branding.baseCustomBrandingPatch")
        addResources(addResourcePatchName, "layout.branding.baseCustomBrandingPatch")

        val useCustomName = customName != null
        val useCustomIcon = customIcon != null

        preferenceScreen.addPreferences(
            if (useCustomName) {
                ListPreference(
                    key = "revanced_custom_branding_name",
                    entriesKey = "revanced_custom_branding_name_custom_entries",
                    entryValuesKey = "revanced_custom_branding_name_custom_entry_values"
                )
            } else {
                ListPreference("revanced_custom_branding_name")
            },
            if (useCustomIcon) {
                ListPreference(
                    key = "revanced_custom_branding_icon",
                    entriesKey = "revanced_custom_branding_icon_custom_entries",
                    entryValuesKey = "revanced_custom_branding_icon_custom_entry_values"
                )
            } else {
                ListPreference("revanced_custom_branding_icon")
            }
        )

        iconStyleNames.forEach { style ->
            copyResources(
                "custom-branding",
                ResourceGroup(
                    "mipmap-anydpi",
                    "$LAUNCHER_RESOURCE_NAME_PREFIX$style.xml",
                    "$LAUNCHER_ADAPTIVE_BACKGROUND_PREFIX$style.xml",
                    "$LAUNCHER_ADAPTIVE_FOREGROUND_PREFIX$style.xml",
                ),
                ResourceGroup(
                    "drawable",
                    "$LAUNCHER_ADAPTIVE_MONOCHROME_PREFIX$style.xml"
                )
            )
        }

        if (useCustomIcon) {
            copyResources(
                "custom-branding",
                ResourceGroup(
                    "mipmap-anydpi",
                    "$LAUNCHER_RESOURCE_NAME_PREFIX$CUSTOM_USER_ICON_STYLE_NAME.xml",
                )
            )

            // Copy user provided files
            val iconPathFile = File(customIcon!!.trim())

            if (!iconPathFile.exists()) {
                throw PatchException(
                    "The custom icon path cannot be found: " + iconPathFile.absolutePath
                )
            }

            if (!iconPathFile.isDirectory) {
                throw PatchException(
                    "The custom icon path must be a folder: " + iconPathFile.absolutePath
                )
            }

            val sourceFolders = iconPathFile.listFiles { file -> file.isDirectory }
                ?: throw PatchException("The custom icon path contains no subfolders: " +
                        iconPathFile.absolutePath)

            val resourceDirectory = get("res")
            var copiedFiles = false

            // For each source folder, copy the files to the target resource directories.
            sourceFolders.forEach { dpiSourceFolder ->
                val targetDpiFolder = resourceDirectory.resolve(dpiSourceFolder.name)
                if (!targetDpiFolder.exists()) return@forEach

                val customFiles = dpiSourceFolder.listFiles { file ->
                    file.isFile && file.name in USER_CUSTOM_ADAPTIVE_FILE_NAMES
                }!!

                if (customFiles.size > 0 && customFiles.size != USER_CUSTOM_ADAPTIVE_FILE_NAMES.size) {
                    throw PatchException("Must include all required icon files " +
                            "but only found " + customFiles.map { it.name })
                }

                customFiles.forEach { imgSourceFile ->
                    val imgTargetFile = targetDpiFolder.resolve(imgSourceFile.name)
                    imgSourceFile.copyTo(imgTargetFile)

                    copiedFiles = true
                }
            }

            // Copy monochrome if it provided.
            val monochromeRelativePath = "drawable/$USER_CUSTOM_MONOCHROME_NAME"
            val monochromeFile = iconPathFile.resolve(monochromeRelativePath)
            if (monochromeFile.exists()) {
                monochromeFile.copyTo(resourceDirectory.resolve(monochromeRelativePath))
                copiedFiles = true

                // Modify custom launcher.xml file to use custom monochrome image.
                val customLauncherXmlFileName = LAUNCHER_RESOURCE_NAME_PREFIX + CUSTOM_USER_ICON_STYLE_NAME
                document(
                    resourceDirectory.resolve(
                        "mipmap-anydpi/$customLauncherXmlFileName.xml"
                    ).absolutePath
                ).use { document ->
                    (document.getElementsByTagName("monochrome").item(0) as Element).setAttribute(
                        "android:drawable",
                        "@drawable/$LAUNCHER_ADAPTIVE_MONOCHROME_PREFIX$CUSTOM_USER_ICON_STYLE_NAME"
                    )
                }
            }

            if (!copiedFiles) {
                throw PatchException("Could not find any replacement images in " +
                        "patch option path: " + iconPathFile.absolutePath)
            }
        }

        document("AndroidManifest.xml").use { document ->
            // Remove the intent from the main activity since an alias will be used instead.
            document.childNodes.findElementByAttributeValueOrThrow(
                "android:name",
                activityAliasNameWithIntentToRemove
            ).childNodes.findElementByAttributeValueOrThrow(
                "android:name",
                "android.intent.action.MAIN"
            ).let { intent ->
                intent.parentNode.removeChild(intent)
            }

            // Create launch aliases that can be programmatically selected in app.
            fun createAlias(
                aliasName: String,
                iconMipmapName: String,
                appNameIndex: Int,
                enabled: Boolean
            ): Element {
                // Indexing starts at 1.
                val label = if (appNameIndex == 1) {
                    originalAppName
                } else {
                    "@string/revanced_custom_branding_name_entry_$appNameIndex"
                }
                val alias = document.createElement("activity-alias")
                alias.setAttribute("android:name", aliasName)
                alias.setAttribute("android:enabled", enabled.toString())
                alias.setAttribute("android:exported", "true")
                alias.setAttribute("android:icon", "@mipmap/$iconMipmapName")
                alias.setAttribute("android:label",label)
                alias.setAttribute("android:targetActivity", mainActivityName)

                val intentFilter = document.createElement("intent-filter")
                val action = document.createElement("action")
                action.setAttribute("android:name", "android.intent.action.MAIN")
                val category = document.createElement("category")
                category.setAttribute("android:name", "android.intent.category.LAUNCHER")

                intentFilter.appendChild(action)
                intentFilter.appendChild(category)
                alias.appendChild(intentFilter)

                return alias
            }

            val namePrefix = ".revanced_"
            val iconResourcePrefix = "revanced_launcher_"
            val application = document.getElementsByTagName("application")
                .item(0) as Element

            var numberOfPresetAppNamesPlusCustom = numberOfPresetAppNames
            if (useCustomName) {
                numberOfPresetAppNamesPlusCustom++
            }

            for (appNameIndex in 1 .. numberOfPresetAppNamesPlusCustom) {
                fun aliasName(name: String): String = namePrefix + name + '_' + appNameIndex

                application.appendChild(
                    createAlias(
                        aliasName(ORIGINAL_USER_ICON_STYLE_NAME),
                        originalLauncherIconName,
                        appNameIndex,
                        appNameIndex == 1
                    )
                )

                if (useCustomIcon) {
                    application.appendChild(
                        createAlias(
                            aliasName(CUSTOM_USER_ICON_STYLE_NAME),
                            iconResourcePrefix + CUSTOM_USER_ICON_STYLE_NAME,
                            appNameIndex,
                            false
                        )
                    )
                }

                iconStyleNames.forEachIndexed { index, style ->
                    application.appendChild(
                        createAlias(
                            aliasName(style),
                            iconResourcePrefix + style,
                            appNameIndex,
                            false,
                        )
                    )
                }
            }
        }

        executeBlock()
    }
}
