package app.revanced.extension.cricbuzz.ads;

import com.cricbuzz.android.data.rest.model.BottomBar;
import java.util.List;
import java.util.Iterator;
import app.revanced.extension.shared.Logger;


@SuppressWarnings("unused")
public class HideAdsPatch {
    public static void filterCb11(List<BottomBar> list) {
        try {
            Iterator<BottomBar> iterator = list.iterator();
            while (iterator.hasNext()) {
                BottomBar bar = iterator.next();
                if (bar.getName().equals("Cricbuzz11")) {
                    iterator.remove();
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "filterCb11 failure", ex);
        }
    }
}