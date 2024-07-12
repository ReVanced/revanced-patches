package app.revanced.patches.soundcloud.offline

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.soundcloud.ad.fingerprints.FeatureConstructorFingerprint
import app.revanced.patches.soundcloud.offline.fingerprints.OfflineSyncHeaderVerificationFingerprint
import app.revanced.patches.soundcloud.offline.fingerprints.OfflineSyncURLBuilderFingerprint
import app.revanced.util.getReference
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(
    name = "Allow Offline Tracks",
    compatiblePackages = [CompatiblePackage("com.soundcloud.android")],
)
@Suppress("unused")
object AllowOfflineSyncPatch : BytecodePatch(
    setOf(FeatureConstructorFingerprint, OfflineSyncURLBuilderFingerprint, OfflineSyncHeaderVerificationFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        // Enable a preset feature to allow offline track syncing by modifying the JSON server response.
        // This method is the constructor of a class representing a "Feature" object parsed from JSON data.
        // p1 is the name of the feature.
        // p2 is true if the feature is enabled, false otherwise.
        FeatureConstructorFingerprint.resultOrThrow().mutableMethod.apply {
            val afterCheckNotNullIndex = 2
            addInstructionsWithLabels(
                afterCheckNotNullIndex,
                """
                    const-string v0, "offline_sync"
                    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :skip
                    const/4 p2, 0x1
                """,
                ExternalLabel("skip", getInstruction(afterCheckNotNullIndex)),
            )
        }
        OfflineSyncURLBuilderFingerprint.resultOrThrow().mutableMethod.apply {
            /**
             * iget-object v0, p0, Lcom/soundcloud/android/offline/i;->d:Lij/d;
             * sget-object v1, Lij/a;->OFFLINE_SYNC:Lij/a;
             *
             * ^ We want the sget to instead grab HTTPS_STREAM!
             * the Offline Sync endpoint is not very friendly, it hates anyone who does not pay money >:(
             */
            val offlineSyncGetInstruction = 1
            val type = getInstruction(offlineSyncGetInstruction).getReference<FieldReference>()!!.type
            replaceInstruction(offlineSyncGetInstruction, "sget-object v1, $type->HTTPS_STREAM:$type")
        }

        OfflineSyncHeaderVerificationFingerprint.resultOrThrow().mutableMethod.apply {
            /**
             * HTTPS Stream has one major flaw. It does not return 3 specific headers (all cosmetic), which crashes Soundcloud.
             *
             * Since those are only cosmetic though, we'll simply move "" into the Variables.
             */
            // These indices are the where the null checks for the individual headers happen.
            // Before it does just move "" into there
            val firstZeroCheck = 4
            val secondZeroCheck = 8
            val thirdZeroCheck = 12
            // Patch in Reverse order to make sure no indices get shifted around
            addInstruction(thirdZeroCheck, "const-string v6, \"\"")
            addInstruction(secondZeroCheck, "const-string v2, \"\"")
            addInstruction(firstZeroCheck, "const-string v0, \"\"")
        }
    }
}
