package app.revanced.extension.spotify.misc.fix;

import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static LoginServer loginServer;

    public static void startLoginServer(int port) {
        if (loginServer != null) {
            Logger.printDebug(() -> "Login server already running on port " + port);
            return;
        }

        try {
            loginServer = new LoginServer(port);
            loginServer.start();
            Logger.printDebug(() -> "Login server running on port " + port);
        } catch (Exception ex) {
            Logger.printException(() -> "startLoginServer failure", ex);
        }
    }
}
