package app.revanced.patches.spotify.layout.hide.createbutton

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.patches.spotify.shared.IS_SPOTIFY_LEGACY_APP_TARGET
import java.util.logging.Logger

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/spotify/layout/hide/createbutton/HideCreateButtonPatch;"

@Suppress("unused")
val hideCreateButtonPatch = bytecodePatch(
    name = "Hide Create button",
    description = "Hides the \"Create\" button in the navigation bar."
) {
    compatibleWith("com.spotify.music")

    dependsOn(sharedExtensionPatch)

    execute {
        if (IS_SPOTIFY_LEGACY_APP_TARGET) {
            Logger.getLogger(this::class.java.name).warning(
                "Create button does not exist in legacy app target.  No changes applied."
            )
            return@execute
        }

        val navigationBarItemSetClassDef = navigationBarItemSetClassFingerprint.originalClassDef

        // The NavigationBarItemSet constructor accepts multiple parameters which represent each navigation bar item.
        // Each item is manually checked whether it is not null and then added to a LinkedHashSet.
        // Since the order of the items can differ, we are required to check every parameter to see whether it is the
        // Create button. So, for every parameter passed to the method, invoke our extension method and overwrite it
        // to null in case it is Create button.
        navigationBarItemSetConstructorFingerprint.match(navigationBarItemSetClassDef).method.apply {
            // Add 1 to the index because the first parameter register is `this`.
            val parameterTypesWithRegister = parameterTypes.mapIndexed { index, parameterType ->
                parameterType to (index + 1)
            }

            val returnNullIfIsCreateButtonDescriptor =
                "$EXTENSION_CLASS_DESCRIPTOR->returnNullIfIsCreateButton(Ljava/lang/Object;)Ljava/lang/Object;"

            parameterTypesWithRegister.reversed().forEach { (parameterType, parameterRegister) ->
                addInstructions(
                    0,
                    """
                        invoke-static { p$parameterRegister }, $returnNullIfIsCreateButtonDescriptor
                        move-result-object p$parameterRegister
                        check-cast p$parameterRegister, $parameterType
                    """
                )
            }
        }
    }
}
