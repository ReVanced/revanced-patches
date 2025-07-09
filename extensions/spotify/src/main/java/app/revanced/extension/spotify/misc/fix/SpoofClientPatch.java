package app.revanced.extension.spotify.misc.fix;

import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static LoginRequestListener listener;

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
}
