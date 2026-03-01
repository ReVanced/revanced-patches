package app.revanced.extension.youtube.patches;

import android.app.AlertDialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class RemoveViewerDiscretionDialogPatch {
    private static final String[] VIEWER_DISCRETION_DIALOG_PLAYABILITY_STATUS = {
            "AGE_CHECK_REQUIRED",
            "AGE_VERIFICATION_REQUIRED",
            "CONTENT_CHECK_REQUIRED",
            "LOGIN_REQUIRED"
    };
    @NonNull
    private static volatile String playabilityStatus = "";

    /**
     * Injection point.
     */
    public static void confirmDialog(AlertDialog dialog) {
        // The dialog may already be shown due to the AlertDialog#create() method.
        // Call the AlertDialog#show() method only when the dialog is not shown.
        if (!dialog.isShowing()) {
            // Since the patch replaces the AlertDialog#show() method, we need to call the original method here.
            dialog.show();
        }

        if (shouldConfirmDialog()) {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (button != null) {
                Window window = dialog.getWindow();
                if (window != null) {
                    // Resize the dialog to 0 before clicking the button.
                    // If the dialog is not resized to 0, it will remain visible for about a second before closing.
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.height = 0;
                    params.width = 0;

                    // Change the size of AlertDialog to 0.
                    window.setAttributes(params);

                    // Disable AlertDialog's background dim.
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                }
                Logger.printDebug(() -> "Clicking alert dialog dismiss button");
                button.callOnClick();
            }
        }
    }

    /**
     * Injection point.
     */
    public static AlertDialog confirmDialog(AlertDialog.Builder builder) {
        AlertDialog dialog = builder.create();
        confirmDialog(dialog);
        return dialog;
    }

    /**
     * Injection point.
     * Modern-style dialog is controlled by an obfuscated class and require additional hooking to get the buttons.
     * Disabling the modern-style dialog is the simplest workaround.
     * Since the purpose of the patch is to close the dialog immediately, this isn't a problem.
     *
     * @return Whether to use modern-style dialog.
     *         If false, AlertDialog is used.
     */
    public static boolean disableModernDialog(boolean original) {
        return !shouldConfirmDialog() && original;
    }

    /**
     * Injection point.
     *
     * @param status Enum value of 'playabilityStatus.status' in '/player' endpoint responses.
     */
    public static void setPlayabilityStatus(@Nullable Enum<?> status) {
        playabilityStatus = status == null ? "" : status.name();
    }

    /**
     * The viewer discretion dialog shows when the playability status is
     * [AGE_CHECK_REQUIRED], [AGE_VERIFICATION_REQUIRED], [CONTENT_CHECK_REQUIRED], or [LOGIN_REQUIRED].
     * Verify the playability status to prevent unintended dialog closures.
     *
     * @return Whether to close the dialog.
     */
    private static boolean shouldConfirmDialog() {
        return Settings.REMOVE_VIEWER_DISCRETION_DIALOG.get()
                && Utils.containsAny(playabilityStatus, VIEWER_DISCRETION_DIALOG_PLAYABILITY_STATUS);
    }
}
