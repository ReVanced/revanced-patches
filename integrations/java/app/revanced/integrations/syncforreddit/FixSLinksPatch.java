package app.revanced.integrations.syncforreddit;

import android.os.StrictMode;
import app.revanced.integrations.shared.Logger;

import java.net.HttpURLConnection;
import java.net.URL;

public final class FixSLinksPatch {
    public static String resolveSLink(String link) {
        if (link.matches(".*reddit\\.com/r/[^/]+/s/[^/]+")) {
            Logger.printInfo(() -> "Resolving " + link);
            try {
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("HEAD");

                // Disable strict mode in order to allow network access on the main thread.
                // This is not ideal, but it's the easiest solution for now.
                final var currentPolicy = StrictMode.getThreadPolicy();
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                connection.connect();
                String location = connection.getHeaderField("location");
                connection.disconnect();

                // Restore the original strict mode policy.
                StrictMode.setThreadPolicy(currentPolicy);

                Logger.printInfo(() -> "Resolved " + link + " -> " + location);

                return location;
            } catch (Exception e) {
                Logger.printException(() -> "Failed to resolve " + link, e);
            }
        }

        return link;
    }
}
