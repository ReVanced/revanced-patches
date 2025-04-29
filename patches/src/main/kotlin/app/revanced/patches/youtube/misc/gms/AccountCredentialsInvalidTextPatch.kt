package app.revanced.patches.youtube.misc.gms

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/AccountCredentialsInvalidTextPatch;"

internal val accountCredentialsInvalidTextPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
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
            fingerprint.apply {
                val index = instructionMatches.last().index
                val register = method.getInstruction<OneRegisterInstruction>(index).registerA

                method.addInstructions(
                    index + 1,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getOfflineNetworkErrorString(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$register  
                    """
                )
            }
        }
    }
}
