package app.revanced.extension.spotify.misc.fix;

import android.view.LayoutInflater;
import app.revanced.extension.shared.Logger;

import android.view.View;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static LoginRequestListener listener;

    /**
     * Injection point.
     * <br>
     * Start login server.
     */
    public static void listen(int port) {
        if (listener != null) {
            Logger.printInfo(() -> "Listener already running on port " + port);
            return;
        }

        try {
            listener = new LoginRequestListener(port);
            listener.start();
            Logger.printInfo(() -> "Listener running on port " + port);
        } catch (Exception ex) {
            Logger.printException(() -> "listen failure", ex);
        }
    }

    /**
     * Injection point.
     * <br>
     * Launch login web view.
     */
    public static void login(LayoutInflater inflater) {
        try {
            WebApp.login(inflater.getContext());
        } catch (Exception ex) {
            Logger.printException(() -> "login failure", ex);
        }
    }

    public static void setLoginButton(View loginButton) {
        WebApp.setLoginButtonView(loginButton);
    }
}
