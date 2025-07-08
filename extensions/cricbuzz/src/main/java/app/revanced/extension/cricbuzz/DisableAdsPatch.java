package app.revanced.extension.cricbuzz.ads;

import com.cricbuzz.android.data.rest.model.BottomBar;
import java.util.List;
import app.revanced.extension.shared.Logger;
import android.annotation.SuppressLint;

@SuppressWarnings("unused")
public final class DisableAdsPatch {
    @SuppressLint("NewApi")
    public static void filterCb11(List<BottomBar> list) {
        try {
            list.removeIf(bottomBar -> bottomBar.getName().equals("Cricbuzz11"));
        } catch (Exception ex) {
            Logger.printException(() -> "Failed removing CB11 element from BottomBar list.", ex);
        }
    }
}