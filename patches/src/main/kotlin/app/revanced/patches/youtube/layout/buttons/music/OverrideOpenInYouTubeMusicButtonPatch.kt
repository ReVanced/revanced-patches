package app.revanced.patches.youtube.layout.buttons.music

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.forEachInstructionAsSequence
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import org.w3c.dom.Element

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/OverrideOpenInYouTubeMusicButtonPatch;"

val packageNameOption = stringOption(
    name = "YouTube Music package name",
    description = "The package name of the YouTube Music app to open when clicking the 'Open in YouTube Music' button.",
    default = "app.revanced.android.apps.youtube.music",
    values = mapOf(
        "Original package name" to "com.google.android.apps.youtube.music",
        "ReVanced default package name" to "app.revanced.android.apps.youtube.music"
    ),
    required = true,
)

private val overrideOpenInYouTubeMusicManifestResourcePatch = resourcePatch {
    apply {
        val packageName by packageNameOption

        document("AndroidManifest.xml").use { document ->
            val queriesList = document.getElementsByTagName("queries")

            val queries = if (queriesList.length > 0) queriesList.item(0) as Element
            else document.createElement("queries").also(document::appendChild)

            document.createElement("package").apply {
                setAttribute("android:name", packageName)
            }.let(queries::appendChild)
        }
    }
}

@Suppress("unused")
val overrideOpenInYouTubeMusicButtonPatch = bytecodePatch(
    name = "Override 'Open in YouTube Music' button",
    description = "Overrides the button to open YouTube Music under a different package name. " +
            "By default, it overrides to the ReVanced default package name of YouTube Music.",
) {
    dependsOn(
        settingsPatch,
        addResourcesPatch,
        overrideOpenInYouTubeMusicManifestResourcePatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45",
            "20.44.38"
        ),
    )

    val packageName by packageNameOption()

    apply {
        addResources("youtube", "layout.buttons.music.overrideOpenInYouTubeMusicButtonPatch")

        PreferenceScreen.GENERAL.addPreferences(
            PreferenceCategory(
                titleKey = null,
                tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
                preferences = setOf(SwitchPreference(key = "revanced_override_open_in_youtube_music_button"))
            )
        )

        getOverridePackageNameMethod.returnEarly(packageName!!)

        forEachInstructionAsSequence({ _, _, instruction, index ->
            if (instruction.opcode != Opcode.INVOKE_VIRTUAL) return@forEachInstructionAsSequence null
            val reference = instruction.methodReference ?: return@forEachInstructionAsSequence null
            if (reference.definingClass != "Landroid/content/Intent;") return@forEachInstructionAsSequence null

            when (reference.name) {
                "setPackage" if reference.parameterTypes == listOf("Ljava/lang/String;") ->
                    index to "overrideSetPackage(Landroid/content/Intent;Ljava/lang/String;)Landroid/content/Intent;"

                "setData" if reference.parameterTypes == listOf("Landroid/net/Uri;") ->
                    index to "overrideSetData(Landroid/content/Intent;Landroid/net/Uri;)Landroid/content/Intent;"

                else -> null
            }
        }) { method, (index, methodDescriptor) ->
            val invokeString = when (val instruction = method.getInstruction(index)) {
                is RegisterRangeInstruction ->
                    "invoke-static/range { v${instruction.startRegister} .. v${instruction.startRegister + instruction.registerCount - 1} }"

                is FiveRegisterInstruction ->
                    "invoke-static { v${instruction.registerC}, v${instruction.registerD} }"

                else -> return@forEachInstructionAsSequence
            }

            method.replaceInstruction(
                index,
                "$invokeString, $EXTENSION_CLASS_DESCRIPTOR->$methodDescriptor"
            )
        }
    }
}
