package app.revanced.patches.music.flyoutpanel.compactdialog

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.music.flyoutpanel.compactdialog.fingerprints.DialogSolidFingerprint
import app.revanced.patches.music.utils.integrations.Constants.FLYOUT
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Enable compact dialog",
    description = "Adds an option to enable the compact flyout menu on phones.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object CompactDialogPatch : BytecodePatch(
    setOf(DialogSolidFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        DialogSolidFingerprint.result?.let {
            with(
                context
                    .toMethodWalker(it.method)
                    .nextMethod(it.scanResult.patternScanResult!!.endIndex, true)
                    .getMethod() as MutableMethod
            ) {
                addInstructions(
                    2, """
                        invoke-static {p0}, $FLYOUT->enableCompactDialog(I)I
                        move-result p0
                        """
                )
            }
        } ?: throw DialogSolidFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.FLYOUT,
            "revanced_enable_compact_dialog",
            "true"
        )

    }
}
