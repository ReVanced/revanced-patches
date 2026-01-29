package app.revanced.patches.youtube.interaction.dialog

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.createDialogMethodMatch by composingFirstMethod {
    returnType("V")
    parameterTypes("L", "L", "Ljava/lang/String;")
    instructions(
        method { toString() == $$"Landroid/app/AlertDialog$Builder;->setNegativeButton(ILandroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;" },
        method { toString() == $$"Landroid/app/AlertDialog$Builder;->setOnCancelListener(Landroid/content/DialogInterface$OnCancelListener;)Landroid/app/AlertDialog$Builder;" },
        method { toString() == $$"Landroid/app/AlertDialog$Builder;->create()Landroid/app/AlertDialog;" },
        method { toString() == "Landroid/app/AlertDialog;->show()V" },
    )
}
