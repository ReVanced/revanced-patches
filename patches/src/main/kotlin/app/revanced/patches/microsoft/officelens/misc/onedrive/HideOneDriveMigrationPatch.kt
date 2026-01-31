package app.revanced.patches.microsoft.officelens.misc.onedrive

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideOneDriveMigrationPatch = bytecodePatch(
    name = "Hide OneDrive Migration",
) {
    compatibleWith("com.microsoft.office.officelens"("16.0.19628.20008"))

    execute {
        hasMigratedToOneDriveFingerprint.method.replaceInstructions(
            0,
            """
                sget-object v0, Lcom/microsoft/office/officelens/scansMigration/LensMigrationStage;->PreMigration:Lcom/microsoft/office/officelens/scansMigration/LensMigrationStage;
                return-object v0
            """,
        )
    }
}