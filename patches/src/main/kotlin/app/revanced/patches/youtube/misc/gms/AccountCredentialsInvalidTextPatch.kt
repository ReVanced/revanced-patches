package app.revanced.patches.youtube.misc.gms

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/AccountCredentialsInvalidTextPatch;"

internal var ic_offline_no_content_upside_down = -1L
    private set
internal var offline_no_content_body_text_not_offline_eligible = -1L
    private set

private val accountCredentialsInvalidTextResourcePatch = resourcePatch {
    execute {
        ic_offline_no_content_upside_down = resourceMappings[
            "drawable",
            "ic_offline_no_content_upside_down"
        ]

        offline_no_content_body_text_not_offline_eligible = resourceMappings[
            "string",
            "offline_no_content_body_text_not_offline_eligible"
        ]
    }
}

internal val accountCredentialsInvalidTextPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        accountCredentialsInvalidTextResourcePatch,
        addResourcesPatch
    )

    execute {
        addResources("youtube", "misc.gms.accountCredentialsInvalidTextPatch")

        // If the user recently changed their account password,
        // the app can show "You're offline. Check your internet connection."
        // even when the internet is available.  For this situation
        // YouTube + MicroG shows an offline error message.
        //
        // Change the error text to inform the user to uninstall and reinstall MicroG.
        // The user can also fix this by deleting the MicroG account but
        // MicroG accounts look almost identical to Google device accounts
        // and it's more foolproof to instead uninstall/reinstall.
        arrayOf(
            specificNetworkErrorViewControllerFingerprint,
            loadingFrameLayoutControllerFingerprint
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                val resourceIndex = indexOfFirstLiteralInstructionOrThrow(
                    offline_no_content_body_text_not_offline_eligible
                )
                val getStringIndex = indexOfFirstInstructionOrThrow(resourceIndex) {
                    val reference = getReference<MethodReference>()
                    reference?.name == "getString"
                }
                val register = getInstruction<OneRegisterInstruction>(getStringIndex + 1).registerA

                addInstructions(
                    getStringIndex + 2,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getOfflineNetworkErrorString(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$register  
                    """
                )
            }
        }
    }
}
