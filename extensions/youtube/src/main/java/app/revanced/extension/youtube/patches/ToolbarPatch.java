package app.revanced.extension.youtube.patches;

import android.view.View;
import android.widget.ImageView;

import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public class ToolbarPatch {

    /**
     * Injection point.
     */
    public static void hookToolBar(Enum<?> buttonEnum, ImageView imageView) {
        final String enumString = buttonEnum.name();
        if (enumString.isEmpty() ||
                imageView == null ||
                !(imageView.getParent() instanceof View view)) {
            return;
        }

        Logger.printDebug(() -> "enumString: " + enumString);

        hookToolBar(enumString, view);
    }

    /**
     * Injection point.
     */
    private static void hookToolBar(String enumString, View parentView) {
        // Code added by patch.
    }
}