package app.revanced.integrations.youtube.patches;

import android.app.AlertDialog;
import app.revanced.integrations.youtube.settings.Settings;

/** @noinspection unused*/
public class RemoveViewerDiscretionDialogPatch {
    public static void confirmDialog(AlertDialog dialog) {
        if (!Settings.REMOVE_VIEWER_DISCRETION_DIALOG.get()) {
            // Since the patch replaces the AlertDialog#show() method, we need to call the original method here.
            dialog.show();
            return;
        }

        final var button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        button.setSoundEffectsEnabled(false);
        button.performClick();
    }
}
