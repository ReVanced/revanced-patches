package app.revanced.patches.instagram.misc.integrations.fingerprints

import app.revanced.patches.instagram.misc.integrations.fingerprints.MainActivityOnCreateFingerprint.getApplicationContextIndex
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal object MainActivityOnCreateFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.name == "onCreate" && methodDef.definingClass == "Lcom/instagram/app/InstagramAppShell;"
    },
    insertIndexResolver = { method ->
        getApplicationContextIndex = method.indexOfFirstInstructionOrThrow {
            getReference<MethodReference>()?.name == "onCreate"
        }

        getApplicationContextIndex + 1 // Below the invoke-super instruction.
    },
    contextRegisterResolver = { method ->
        val moveResultInstruction = method.implementation!!.instructions.elementAt(getApplicationContextIndex)
            as BuilderInstruction35c
        moveResultInstruction.registerC
    },
) {
    private var getApplicationContextIndex = -1
}
