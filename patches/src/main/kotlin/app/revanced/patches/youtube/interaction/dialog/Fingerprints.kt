package app.revanced.patches.youtube.interaction.dialog

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.methodCall
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.createDialogMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    parameterTypes("L", "L", "Ljava/lang/String;")
    instructions(
        methodCall(smali = "Landroid/app/AlertDialog\$Builder;->setNegativeButton(ILandroid/content/DialogInterface\$OnClickListener;)Landroid/app/AlertDialog\$Builder;"),
        methodCall(smali = "Landroid/app/AlertDialog\$Builder;->setOnCancelListener(Landroid/content/DialogInterface\$OnCancelListener;)Landroid/app/AlertDialog\$Builder;"),
        methodCall(smali = "Landroid/app/AlertDialog\$Builder;->create()Landroid/app/AlertDialog;"),
        methodCall(smali = "Landroid/app/AlertDialog;->show()V"),
    )
}
