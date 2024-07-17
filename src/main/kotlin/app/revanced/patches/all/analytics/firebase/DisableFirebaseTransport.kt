package app.revanced.patches.all.analytics.firebase

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.analytics.firebase.fingerprints.SendFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Disable Firebase transport",
    description = "Prevents the sending of Firebase Logging and Firebase Crashlytics logs to Google's servers."
)
@Suppress("unused")
object DisableFirebaseTransport : BytecodePatch(
    setOf(SendFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // Neutralize the method sending data to the backend
        SendFingerprint.resultOrThrow().mutableMethod.addInstructions(0,"return-void")
    }
}