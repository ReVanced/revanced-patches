package app.revanced.patches.youtube.interaction.dialog

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

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


internal val BytecodePatchContext.createModernDialogMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    instructions(
        Opcode.MOVE_RESULT(),
        method { toString() == $$"Landroid/app/AlertDialog$Builder;->setIcon(I)Landroid/app/AlertDialog$Builder;" },
        method { toString() == $$"Landroid/app/AlertDialog$Builder;->create()Landroid/app/AlertDialog;" }
    )
}

internal val BytecodePatchContext.playabilityStatusEnumMethod by gettingFirstImmutableMethod(
    "OK",
    "ERROR",
    "UNPLAYABLE",
    "LOGIN_REQUIRED",
    "CONTENT_CHECK_REQUIRED",
    "AGE_CHECK_REQUIRED",
    "LIVE_STREAM_OFFLINE",
    "FULLSCREEN_ONLY",
    "GL_PLAYBACK_REQUIRED",
    "AGE_VERIFICATION_REQUIRED",
)