package app.revanced.extension.spotify.misc.fix;

import android.view.LayoutInflater;
import android.view.View;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import com.spotify.connectstate.Connect;
import org.jetbrains.annotations.NotNull;
import xyz.gianlu.librespot.audio.decoders.Decoders;
import xyz.gianlu.librespot.audio.format.SuperAudioFormat;
import xyz.gianlu.librespot.core.Session;
import xyz.gianlu.librespot.player.Player;
import xyz.gianlu.librespot.player.PlayerConfiguration;

import java.util.Locale;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static LoginRequestListener listener;
    private static Player player;
    private static AndroidZeroconfServer server;
    
    /**
     * Injection point.
     * <br>
     * Launch login server.
     */
    public synchronized static void launchListener(int port) {
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

        try {
            Logger.printInfo(() -> "Launching connect device");

            Decoders.registerDecoder(SuperAudioFormat.VORBIS, AndroidNativeDecoder.class);
            Decoders.registerDecoder(SuperAudioFormat.MP3, AndroidNativeDecoder.class);

            xyz.gianlu.librespot.core.Session.Configuration conf = new xyz.gianlu.librespot.core.Session.Configuration.Builder()
                    .setStoreCredentials(false)
                    .setCacheEnabled(false)
                    .build();

            AndroidZeroconfServer.Builder builder = new AndroidZeroconfServer.Builder(Utils.getContext(), conf)
                    .setPreferredLocale(Locale.getDefault().getLanguage())
                    .setDeviceType(Connect.DeviceType.COMPUTER)
                    .setDeviceId("81983a3578aef8351b1e948ef8d6e3652bc7d001")
                    .setDeviceName("ReVanced");

            server = builder.create();

            server.addSessionListener(
                    new AndroidZeroconfServer.SessionListener() {
                        final PlayerConfiguration configuration = new PlayerConfiguration.Builder()
                                .setOutput(PlayerConfiguration.AudioOutput.CUSTOM)
                                .setOutputClass(AndroidSinkOutput.class.getName())
                                .build();

                        @Override
                        public void sessionClosing(@NotNull xyz.gianlu.librespot.core.Session session) {
                        }

                        @Override
                        public void sessionChanged(@NotNull Session session) {
                            Logger.printInfo(() -> "Session changed: " + session.username());

                            if (player != null) player.close();
                            player = new Player(configuration, session);
                        }
                    }
            );
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
}
