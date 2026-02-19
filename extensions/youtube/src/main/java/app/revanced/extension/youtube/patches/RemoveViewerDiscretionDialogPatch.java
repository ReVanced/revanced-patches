package app.revanced.extension.youtube.patches;

import android.app.AlertDialog;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class RemoveViewerDiscretionDialogPatch {

    /**
     * Injection point.
     */
    public static void confirmDialog(AlertDialog dialog) {
        if (Settings.REMOVE_VIEWER_DISCRETION_DIALOG.get()) {
            Logger.printDebug(() -> "Clicking alert dialog dismiss button");

            final var button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setSoundEffectsEnabled(false);
            button.performClick();
            return;
        }

        // Since the patch replaces the AlertDialog#show() method, we need to call the original method here.
        Logger.printDebug(() -> "Showing alert dialog");
        dialog.show();
    }
}
