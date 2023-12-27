package app.revanced.integrations.patches;

import android.app.AlertDialog;
import app.revanced.integrations.settings.SettingsEnum;

public class RemoveViewerDiscretionDialogPatch {
    public static void confirmDialog(AlertDialog dialog) {
        if (!SettingsEnum.REMOVE_VIEWER_DISCRETION_DIALOG.getBoolean()) {
            // Since the patch replaces the AlertDialog#show() method, we need to call the original method here.
            dialog.show();
            return;
        }

        final var button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        button.setSoundEffectsEnabled(false);
        button.performClick();
    }
}
