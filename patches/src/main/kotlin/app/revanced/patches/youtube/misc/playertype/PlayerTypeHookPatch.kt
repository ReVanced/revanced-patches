package app.revanced.patches.youtube.misc.playertype

import app.revanced.patcher.*
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/PlayerTypeHookPatch;"

val playerTypeHookPatch = bytecodePatch(
    description = "Hook to get the current player type and video playback state.",
) {
    dependsOn(sharedExtensionPatch, resourceMappingPatch)

    apply {
        firstMethodDeclaratively {
            definingClass("/YouTubePlayerOverlaysLayout;")
            accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
            returnType("V")
            parameterTypes(playerTypeEnumMethod.immutableClassDef.type)
        }.addInstruction(
            0,
            "invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->setPlayerType(Ljava/lang/Enum;)V",
        )

        reelWatchPagerMethodMatch.method.apply {
            val index = reelWatchPagerMethodMatch[-1]
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstruction(
                index + 1,
                "invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->onShortsCreate(Landroid/view/View;)V",
            )
        }

        val controlStateType = controlsStateToStringMethod.immutableClassDef.type

        val videoStateEnumMethod = videoStateEnumMethod
        firstMethodComposite {
            accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
            returnType("V")
            parameterTypes(controlStateType)
            instructions(
                field {
                    definingClass == controlStateType && type == videoStateEnumMethod.immutableClassDef.type
                },
                // Obfuscated parameter field name.
                ResourceType.STRING("accessibility_play"),
                ResourceType.STRING("accessibility_pause"),
            )
        }.let {
            it.method.apply {
                val videoStateFieldName = getInstruction<ReferenceInstruction>(it[0]).reference

                addInstructions(
                    0,
                    """
                        iget-object v0, p1, $videoStateFieldName  # copy VideoState parameter field
                        invoke-static {v0}, $EXTENSION_CLASS_DESCRIPTOR->setVideoState(Ljava/lang/Enum;)V
                    """,
                )
            }
        }
    }
}
