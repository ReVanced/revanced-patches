package app.revanced.extension.spotify.misc.fix;

import android.view.LayoutInflater;
import android.view.View;
import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static LoginRequestListener listener;
    public static volatile String clientToken;

    /**
     * Injection point.
     * <br>
     * Launch login server.
     */
    public static void launchListener(int port) {
        if (listener != null) {
            Logger.printInfo(() -> "Listener already running on port " + port);
            return;
        }

        try {
            Logger.printInfo(() -> "Launching listener on port " + port);
            listener = new LoginRequestListener(port);
        } catch (Exception ex) {
            Logger.printException(() -> "launchListener failure", ex);
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
            Logger.printException(() -> "launchLogin failure", ex);
        }
    }

    /**
     * Injection point.
     * <br>
     * Set handler to call the native login after the webview login.
     */
    public static void setNativeLoginHandler(View startLoginButton) {
        WebApp.nativeLoginHandler = (() -> {
            startLoginButton.setSoundEffectsEnabled(false);
            startLoginButton.performClick();
        });
    }

    public static void setClientToken(String newClientToken) {
        Logger.printInfo(() -> "Setting client token to " + newClientToken);
        clientToken = newClientToken;
    }
}
