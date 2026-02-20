package app.revanced.patches.gamehub.steam.storage

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.gamehub.misc.extension.sharedGamehubExtensionPatch
import app.revanced.patches.gamehub.misc.settings.CONTENT_TYPE_SD_CARD_STORAGE
import app.revanced.patches.gamehub.misc.settings.addSteamSetting
import app.revanced.patches.gamehub.misc.settings.settingsMenuPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val STEAM_EXTENSION = "Lapp/revanced/extension/gamehub/prefs/GameHubPrefs;"

@Suppress("unused")
val sdCardStoragePatch = bytecodePatch(
    name = "SD card Steam storage",
    description = "Allows redirecting Steam game storage to a custom location such as an SD card.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))

    dependsOn(sharedGamehubExtensionPatch, settingsMenuPatch)

    execute {
        addSteamSetting(CONTENT_TYPE_SD_CARD_STORAGE, "CONTENT_TYPE_SD_CARD_STORAGE")

        // Intercept AppMetadata.setInstallPath setter to redirect the path.
        appMetadataSetInstallPathFingerprint.method.apply {
            val iputIndex = indexOfFirstInstructionOrThrow { opcode == Opcode.IPUT_OBJECT }
            addInstructions(
                iputIndex,
                """
                    invoke-static {p1}, $STEAM_EXTENSION->getEffectiveStoragePath(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object p1
                """,
            )
        }

        // Intercept SteamDownloadExtend.setInstallDirPath setter to redirect the path.
        steamDownloadExtendSetInstallDirPathFingerprint.method.apply {
            val iputIndex = indexOfFirstInstructionOrThrow { opcode == Opcode.IPUT_OBJECT }
            addInstructions(
                iputIndex,
                """
                    invoke-static {p1}, $STEAM_EXTENSION->getEffectiveStoragePath(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object p1
                """,
            )
        }

        // DownloadGameSizeInfoDialog$computeAvailableSize$2.invokeSuspend returns Object (boxed Long).
        // Override to return available bytes on the effective storage location (SD card or internal).
        downloadDialogStorageFingerprint.method.apply {
            addInstructions(
                0,
                """
                    invoke-static {}, $STEAM_EXTENSION->getAvailableStorage()J
                    move-result-wide v0
                    invoke-static {v0, v1}, Lkotlin/coroutines/jvm/internal/Boxing;->f(J)Ljava/lang/Long;
                    move-result-object v0
                    return-object v0
                """,
            )
        }

        // SteamDownloadInfoHelper.a() calls AppMetadata.setInstallPath(path) when the current
        // install path is empty. We intercept just before that call to translate the path through
        // our extension, which may redirect it to the user-configured custom storage path.
        steamDownloadInfoHelperFingerprint.method.apply {
            val setInstallPathIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL &&
                    (this as? ReferenceInstruction)?.reference?.let {
                        it is MethodReference && it.name == "setInstallPath"
                    } == true
            }
            // invoke-virtual {v6, v1}, AppMetadata->setInstallPath(String)V
            // registerD is the second argument (the path string).
            val pathRegister = getInstruction<Instruction35c>(setInstallPathIndex).registerD

            addInstructions(
                setInstallPathIndex,
                """
                    invoke-static {v$pathRegister}, $STEAM_EXTENSION->getEffectiveStoragePath(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$pathRegister
                """,
            )
        }
    }
}
