package app.revanced.patches.youtube.interaction.dialog

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall

internal val createDialogFingerprint by fingerprint {
    returns("V")
    parameters("L", "L", "Ljava/lang/String;")
    instructions(
        literal(0x7f140232),
        methodCall(smali = "Landroid/app/AlertDialog\$Builder;->setNegativeButton(ILandroid/content/DialogInterface\$OnClickListener;)Landroid/app/AlertDialog\$Builder;"),
        methodCall(smali = "Landroid/app/AlertDialog\$Builder;->setOnCancelListener(Landroid/content/DialogInterface\$OnCancelListener;)Landroid/app/AlertDialog\$Builder;"),
        methodCall(smali = "Landroid/app/AlertDialog\$Builder;->create()Landroid/app/AlertDialog;"),
        methodCall(smali = "Landroid/app/AlertDialog;->show()V")
    )
}
