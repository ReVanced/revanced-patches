package app.revanced.patches.spotify.misc.extension

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patches.shared.misc.extension.extensionHook
import app.revanced.patches.spotify.shared.mainActivityOnCreateFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

internal val mainActivityOnCreateHook = extensionHook(fingerprint = mainActivityOnCreateFingerprint)

internal val loadOrbitLibraryHook = extensionHook(
    insertIndexResolver = {
        loadOrbitLibraryFingerprint.stringMatches!!.last().index
    },
    contextRegisterResolver = { method ->
        val insertIndex = loadOrbitLibraryFingerprint.stringMatches!!.last().index
        val contextRegister = method.getInstruction<TwoRegisterInstruction>(insertIndex - 1).registerA

        "v$contextRegister"
    },
    fingerprint = loadOrbitLibraryFingerprint,
)