package app.revanced.patches.spotify.misc.extension

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patches.shared.misc.extension.extensionHook
import app.revanced.patches.spotify.shared.mainActivityOnCreateFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal val mainActivityOnCreateHook = extensionHook(fingerprint = mainActivityOnCreateFingerprint)

internal val loadOrbitLibraryHook = extensionHook(
    insertIndexResolver = {
        loadOrbitLibraryFingerprint.stringMatches!!.last().index
    },
    contextRegisterResolver = { method ->
        val contextReferenceIndex = method.indexOfFirstInstruction {
            getReference<FieldReference>()?.type == "Landroid/content/Context;"
        }
        val contextRegister = method.getInstruction<TwoRegisterInstruction>(contextReferenceIndex).registerA

        "v$contextRegister"
    },
    fingerprint = loadOrbitLibraryFingerprint,
)
