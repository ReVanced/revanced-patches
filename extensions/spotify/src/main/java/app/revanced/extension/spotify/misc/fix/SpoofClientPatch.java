package app.revanced.extension.spotify.misc.fix;

import android.view.LayoutInflater;
import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static LoginRequestListener listener;
    
    /**
     * Injection point.
     */
    public static void listen(int port) {
        if (listener != null) {
            Logger.printDebug(() -> "Listener already running on port " + port);
            return;
        }

        try {
            listener = new LoginRequestListener(port);
            listener.start();
            Logger.printDebug(() -> "Listener running on port " + port);
        } catch (Exception ex) {
            Logger.printException(() -> "listen failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void login(LayoutInflater inflater) {
        WebApp.login(inflater.getContext());
    }
}
