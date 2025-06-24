package app.revanced.extension.spotify.misc.fix;

import android.view.View;
import androidx.annotation.Nullable;


import static android.content.Context.MODE_PRIVATE;

public class Helper {
    @Nullable
    private static View button = null;

    public static void setButton(View b) {
            button = b;
    }

    @Nullable
    public static View getButton() {
        return button;
    }
}
