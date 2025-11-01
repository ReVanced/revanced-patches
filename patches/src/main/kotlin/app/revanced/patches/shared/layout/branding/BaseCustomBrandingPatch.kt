package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.ResourcePatchBuilder
import app.revanced.patcher.patch.ResourcePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.all.misc.packagename.setOrGetFallbackPackageName
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.copyResources
import app.revanced.util.findElementByAttributeValueOrThrow
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.removeFromParent
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import java.util.logging.Logger

private val mipmapDirectories = mapOf(
    // Target app does not have ldpi icons.
    "mipmap-mdpi" to "108x108 px",
    "mipmap-hdpi" to "162x162 px",
    "mipmap-xhdpi" to "216x216 px",
    "mipmap-xxhdpi" to "324x324 px",
    "mipmap-xxxhdpi" to "432x432 px"
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
private const val NOTIFICATION_ICON_NAME = "revanced_notification_icon"

private val USER_CUSTOM_ADAPTIVE_FILE_NAMES = arrayOf(
    "$LAUNCHER_ADAPTIVE_BACKGROUND_PREFIX$CUSTOM_USER_ICON_STYLE_NAME.png",
    "$LAUNCHER_ADAPTIVE_FOREGROUND_PREFIX$CUSTOM_USER_ICON_STYLE_NAME.png"
)

private const val USER_CUSTOM_MONOCHROME_FILE_NAME = "$LAUNCHER_ADAPTIVE_MONOCHROME_PREFIX$CUSTOM_USER_ICON_STYLE_NAME.xml"
private const val USER_CUSTOM_NOTIFICATION_ICON_FILE_NAME = "${NOTIFICATION_ICON_NAME}_$CUSTOM_USER_ICON_STYLE_NAME.xml"

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/shared/patches/CustomBrandingPatch;"

/**
 * Shared custom branding patch for YouTube and YT Music.
 */
internal fun baseCustomBrandingPatch(
    addResourcePatchName: String,
    originalLauncherIconName: String,
    originalAppName: String,
    originalAppPackageName: String,
    isYouTubeMusic: Boolean,
    numberOfPresetAppNames: Int,
    mainActivityOnCreateFingerprint: Fingerprint,
    mainActivityName: String,
    activityAliasNameWithIntents: String,
    preferenceScreen: BasePreferenceScreen.Screen,
    block: ResourcePatchBuilder.() -> Unit,
    executeBlock: ResourcePatchContext.() -> Unit = {}
): ResourcePatch = resourcePatch(
    name = "Custom branding",
    description = "Adds options to change the app icon and app name. " +
            "Branding cannot be changed for mounted (root) installations."
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
            ${mipmapDirectories.keys.joinToString("\n") { "- $it" }}
            
            Each of the folders must contain all of the following files:
            ${USER_CUSTOM_ADAPTIVE_FILE_NAMES.joinToString("\n")}
            
            The image dimensions must be as follows:
            ${mipmapDirectories.map { (dpi, dim) -> "- $dpi: $dim" }.joinToString("\n")}

            Optionally, the path contains a 'drawable' folder with any of the monochrome icon files:
            $USER_CUSTOM_MONOCHROME_FILE_NAME
            $USER_CUSTOM_NOTIFICATION_ICON_FILE_NAME
        """.trimIndentMultiline()
    )

    block()

    dependsOn(
        addResourcesPatch,
        resourceMappingPatch,
        addBrandLicensePatch,
        bytecodePatch {
            execute {
                mainActivityOnCreateFingerprint.method.addInstruction(
                    0,
                    "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->setBranding()V"
                )

                numberOfPresetAppNamesExtensionFingerprint.method.returnEarly(numberOfPresetAppNames)

                notificationFingerprint.method.apply {
                    val getBuilderIndex = if (isYouTubeMusic) {
                        // YT Music the field is not a plain object type.
                        indexOfFirstInstructionOrThrow {
                            getReference<FieldReference>()?.type == "Landroid/app/Notification\$Builder;"
                        }
                    } else {
                        // Find the field name of the notification builder. Field is an Object type.
                        val builderCastIndex = indexOfFirstInstructionOrThrow {
                            val reference = getReference<TypeReference>()
                            opcode == Opcode.CHECK_CAST &&
                                    reference?.type == "Landroid/app/Notification\$Builder;"
                        }
                        indexOfFirstInstructionReversedOrThrow(builderCastIndex) {
                            getReference<FieldReference>()?.type == "Ljava/lang/Object;"
                        }
                    }

                    val builderFieldName = getInstruction<ReferenceInstruction>(getBuilderIndex)
                        .getReference<FieldReference>()

                    findInstructionIndicesReversedOrThrow(
                        Opcode.RETURN_VOID
                    ).forEach { index ->
                        addInstructionsAtControlFlowLabel(
                            index,
                            """
                                move-object/from16 v0, p0
                                iget-object v0, v0, $builderFieldName
                                check-cast v0, Landroid/app/Notification${'$'}Builder;
                                invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->setNotificationIcon(Landroid/app/Notification${'$'}Builder;)V
                            """
                        )
                    }
                }
            }
        },
    )

    finalize {
        // Can only check if app is root installation by checking if change package name patch is in use.
        // and can only do that in the finalize block here.
        // The UI preferences cannot be selectively added here, because the settings finalize block
        // may have already run and the settings are already wrote to file.
        // Instead, show a warning if any patch option was used (A rooted device launcher ignores the manifest changes),
        // and the non-functional in-app settings are removed on app startup by extension code.
        if (customName != null || customIcon != null) {
            if (setOrGetFallbackPackageName(originalAppPackageName) == originalAppPackageName) {
                Logger.getLogger(this::class.java.name).warning(
                    "Custom branding does not work with root installation. No changes applied."
                )
            }
        }
    }

    execute {
        addResources("shared", "layout.branding.baseCustomBrandingPatch")
        addResources(addResourcePatchName, "layout.branding.customBrandingPatch")

        preferenceScreen.addPreferences(
            if (customName != null ) {
                ListPreference(
                    key = "revanced_custom_branding_name",
                    entriesKey = "revanced_custom_branding_name_custom_entries",
                    entryValuesKey = "revanced_custom_branding_name_custom_entry_values"
                )
            } else {
                ListPreference("revanced_custom_branding_name")
            },
            if (customIcon != null) {
                ListPreference(
                    key = "revanced_custom_branding_icon",
                    entriesKey = "revanced_custom_branding_icon_custom_entries",
                    entryValuesKey = "revanced_custom_branding_icon_custom_entry_values"
                )
            } else {
                ListPreference("revanced_custom_branding_icon")
            }
        )

        val useCustomName = customName != null
        val useCustomIcon = customIcon != null

        iconStyleNames.forEach { style ->
            copyResources(
                "custom-branding",
                ResourceGroup(
                    "drawable",
                    "$LAUNCHER_ADAPTIVE_BACKGROUND_PREFIX$style.xml",
                    "$LAUNCHER_ADAPTIVE_FOREGROUND_PREFIX$style.xml",
                    "$LAUNCHER_ADAPTIVE_MONOCHROME_PREFIX$style.xml",
                ),
                ResourceGroup(
                    "mipmap-anydpi",
                    "$LAUNCHER_RESOURCE_NAME_PREFIX$style.xml"
                )
            )
        }

        copyResources(
            "custom-branding",
            // Push notification 'small' icon.
            ResourceGroup(
                "drawable",
                "$NOTIFICATION_ICON_NAME.xml"
            ),

            // Copy template user icon, because the aliases must be added even if no user icon is provided.
            ResourceGroup(
                "drawable",
                USER_CUSTOM_MONOCHROME_FILE_NAME,
                USER_CUSTOM_NOTIFICATION_ICON_FILE_NAME
            ),
            ResourceGroup(
                "mipmap-anydpi",
                "$LAUNCHER_RESOURCE_NAME_PREFIX$CUSTOM_USER_ICON_STYLE_NAME.xml"
            )
        )

        // Copy template icon files.
        mipmapDirectories.keys.forEach { dpi ->
            copyResources(
                "custom-branding",
                ResourceGroup(
                    dpi,
                    "$LAUNCHER_ADAPTIVE_BACKGROUND_PREFIX$CUSTOM_USER_ICON_STYLE_NAME.png",
                    "$LAUNCHER_ADAPTIVE_FOREGROUND_PREFIX$CUSTOM_USER_ICON_STYLE_NAME.png",
                )
            )
        }

        document("AndroidManifest.xml").use { document ->
            // Create launch aliases that can be programmatically selected in app.
            fun createAlias(
                aliasName: String,
                iconMipmapName: String,
                appNameIndex: Int,
                useCustomName: Boolean,
                enabled: Boolean,
                intents: NodeList
            ): Element {
                val label = if (useCustomName) {
                    if (customName == null) {
                        "Custom" // Dummy text, and normally cannot be seen.
                    } else {
                        customName!!
                    }
                } else if (appNameIndex == 1) {
                    // Indexing starts at 1.
                    originalAppName
                } else {
                    "@string/revanced_custom_branding_name_entry_$appNameIndex"
                }
                val alias = document.createElement("activity-alias")
                alias.setAttribute("android:name", aliasName)
                alias.setAttribute("android:enabled", enabled.toString())
                alias.setAttribute("android:exported", "true")
                alias.setAttribute("android:icon", "@mipmap/$iconMipmapName")
                alias.setAttribute("android:label", label)
                alias.setAttribute("android:targetActivity", mainActivityName)

                // Copy all intents from the original alias so long press actions still work.
                if (isYouTubeMusic) {
                    val intentFilter = document.createElement("intent-filter").apply {
                        val action = document.createElement("action")
                        action.setAttribute("android:name", "android.intent.action.MAIN")
                        appendChild(action)

                        val category = document.createElement("category")
                        category.setAttribute("android:name", "android.intent.category.LAUNCHER")
                        appendChild(category)
                    }
                    alias.appendChild(intentFilter)
                } else {
                    for (i in 0 until intents.length) {
                        alias.appendChild(
                            intents.item(i).cloneNode(true)
                        )
                    }
                }

                return alias
            }

            val application = document.getElementsByTagName("application").item(0) as Element
            val intentFilters = document.childNodes.findElementByAttributeValueOrThrow(
                "android:name",
                activityAliasNameWithIntents
            ).childNodes

            // The YT application name can appear in some places along side the system
            // YouTube app, such as the settings app list and in the "open with" file picker.
            // Because the YouTube app cannot be completely uninstalled and only disabled,
            // use a custom name for this situation to disambiguate which app is which.
            application.setAttribute(
                "android:label",
                "@string/revanced_custom_branding_name_entry_2"
            )

            for (appNameIndex in 1 .. numberOfPresetAppNames) {
                fun aliasName(name: String): String = ".revanced_" + name + '_' + appNameIndex

                val useCustomNameLabel = (useCustomName && appNameIndex == numberOfPresetAppNames)

                // Original icon.
                application.appendChild(
                    createAlias(
                        aliasName = aliasName(ORIGINAL_USER_ICON_STYLE_NAME),
                        iconMipmapName = originalLauncherIconName,
                        appNameIndex = appNameIndex,
                        useCustomName = useCustomNameLabel,
                        enabled = (appNameIndex == 1),
                        intentFilters
                    )
                )

                // Bundled icons.
                iconStyleNames.forEachIndexed { index, style ->
                    application.appendChild(
                        createAlias(
                            aliasName = aliasName(style),
                            iconMipmapName = LAUNCHER_RESOURCE_NAME_PREFIX + style,
                            appNameIndex = appNameIndex,
                            useCustomName = useCustomNameLabel,
                            enabled = false,
                            intentFilters
                        )
                    )
                }

                // User provided custom icon.
                //
                // Must add all aliases even if the user did not provide a custom icon of their own.
                // This is because if the user installs with an option, then repatches without the option,
                // the alias must still exist because if it was previously enabled and then it's removed
                // the app will become broken and cannot launch. Even if the app data is cleared
                // it still cannot be launched and the only fix is to uninstall the app.
                // To prevent this, always include all aliases and use dummy data if needed.
                application.appendChild(
                    createAlias(
                        aliasName = aliasName(CUSTOM_USER_ICON_STYLE_NAME),
                        iconMipmapName = LAUNCHER_RESOURCE_NAME_PREFIX + CUSTOM_USER_ICON_STYLE_NAME,
                        appNameIndex = appNameIndex,
                        useCustomName = useCustomNameLabel,
                        enabled = false,
                        intentFilters
                    )
                )
            }

            // Remove the main action from the original alias, otherwise two apps icons
            // can be shown in the launcher. Can only be done after adding the new aliases.
            intentFilters.findElementByAttributeValueOrThrow(
                "android:name",
                "android.intent.action.MAIN"
            ).removeFromParent()
        }

        // Copy custom icons last, so if the user enters an invalid icon path
        // and an exception is thrown then the critical manifest changes are still made.
        if (useCustomIcon) {
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

            val resourceDirectory = get("res")
            var copiedFiles = false

            // For each source folder, copy the files to the target resource directories.
            iconPathFile.listFiles {
                file -> file.isDirectory && file.name in mipmapDirectories
            }!!.forEach { dpiSourceFolder ->
                val targetDpiFolder = resourceDirectory.resolve(dpiSourceFolder.name)
                if (!targetDpiFolder.exists()) {
                    // Should never happen.
                    throw IllegalStateException("Resource not found: $dpiSourceFolder")
                }

                val customFiles = dpiSourceFolder.listFiles { file ->
                    file.isFile && file.name in USER_CUSTOM_ADAPTIVE_FILE_NAMES
                }!!

                if (customFiles.isNotEmpty() && customFiles.size != USER_CUSTOM_ADAPTIVE_FILE_NAMES.size) {
                    throw PatchException("Must include all required icon files " +
                            "but only found " + customFiles.map { it.name })
                }

                customFiles.forEach { imgSourceFile ->
                    val imgTargetFile = targetDpiFolder.resolve(imgSourceFile.name)
                    imgSourceFile.copyTo(target = imgTargetFile, overwrite = true)

                    copiedFiles = true
                }
            }

            // Copy monochrome and small notification icon if it provided.
            arrayOf(
                USER_CUSTOM_MONOCHROME_FILE_NAME,
                USER_CUSTOM_NOTIFICATION_ICON_FILE_NAME
            ).forEach { fileName ->
                val relativePath = "drawable/$fileName"
                val file = iconPathFile.resolve(relativePath)
                if (file.exists()) {
                    file.copyTo(
                        target = resourceDirectory.resolve(relativePath),
                        overwrite = true
                    )
                    copiedFiles = true
                }
            }

            if (!copiedFiles) {
                throw PatchException("Expected to find directories and files: "
                        + USER_CUSTOM_ADAPTIVE_FILE_NAMES.contentToString()
                        + "\nBut none were found in the provided option file path: " + iconPathFile.absolutePath)
            }
        }

        executeBlock()
    }
}
