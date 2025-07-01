package app.revanced.extension.spotify.misc.fix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    @SuppressLint("StaticFieldLeak")
    private static LoginRequestListener listener;

    /**
     * Injection point.
     * <br>
     * Launch login server.
     */
    public static void launchListen(Context context, int port) {
        Logger.printInfo(() -> "Setting context for WebApp");
        WebApp.context = context;

        if (listener != null) {
            Logger.printInfo(() -> "Listener already running on port " + port);
            return;
        }

        try {
            Logger.printInfo(() -> "Launching listener on port " + port);
            listener = new LoginRequestListener(port);
        } catch (Exception ex) {
            Logger.printException(() -> "listen failure", ex);
        }
    }

    /**
     * Injection point.
     * <br>
     * Launch login web view.
     */
    public static void launchLogin(LayoutInflater inflater) {
        try {
            WebApp.launchLogin(inflater.getContext());
        } catch (Exception ex) {
            Logger.printException(() -> "login failure", ex);
        }
    }
}
