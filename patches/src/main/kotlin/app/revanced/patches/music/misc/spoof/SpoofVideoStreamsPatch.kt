package app.revanced.patches.music.misc.spoof

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patches.music.misc.gms.musicActivityOnCreateFingerprint
import app.revanced.patches.shared.misc.spoof.EXTENSION_CLASS_DESCRIPTOR
import app.revanced.patches.shared.misc.spoof.spoofVideoStreamsPatch

val spoofVideoStreamsPatch = spoofVideoStreamsPatch({
    compatibleWith("com.google.android.apps.youtube.music")
}, {
    musicActivityOnCreateFingerprint.method.addInstruction(
        0,
        "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->setClientTypeToIosMusic()V",
    )
})
