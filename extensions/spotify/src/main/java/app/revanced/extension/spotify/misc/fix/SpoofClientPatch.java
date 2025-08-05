package app.revanced.extension.spotify.misc.fix;

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
    private static RequestListener listener;
    private static Player player;
    private static AndroidZeroconfServer server;

    /**
     * Injection point. Launch requests listener server.
     */
    public synchronized static void launchListener(int port) {
        if (listener != null) {
            Logger.printInfo(() -> "Listener already running on port " + port);
            return;
        }

        try {
            Logger.printInfo(() -> "Launching listener on port " + port);
            listener = new RequestListener(port);
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
                    .setDeviceType(Connect.DeviceType.AUDIO_DONGLE)
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
}
