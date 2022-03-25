package fi.razerman.youtube;

import android.text.format.Time;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.JSONObject;

/* loaded from: classes6.dex */
public class XJson {
    public static String[] getVersion(String versionName) {
        try {
            final String[] results = new String[4];
            final String vName = versionName.replace('.', '_');
            Thread t = new Thread() { // from class: fi.razerman.youtube.XJson.1
                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    try {
                        Time now = new Time();
                        now.setToNow();
                        String time = "" + now.hour + now.minute + now.second;
                        int time_int = Integer.parseInt(time);
                        URL url = new URL("https://github.com/YTVanced/VancedBackend/releases/download/changelogs/" + vName + "?searchTime=" + time_int); // TODO change to ReVanced changelog URL.
                        url.openConnection().setReadTimeout(2000);
                        url.openConnection().setConnectTimeout(2000);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                        StringBuilder sb = new StringBuilder();
                        while (true) {
                            String line = reader.readLine();
                            if (line != null) {
                                sb.append(line).append("\n");
                            } else {
                                String json = sb.toString();
                                reader.close();
                                JSONObject jsonObject = new JSONObject(json);
                                String title = jsonObject.getString("title");
                                String message = jsonObject.getString("message");
                                String buttonpositive = jsonObject.getString("buttonpositive");
                                results[0] = title;
                                results[1] = message;
                                results[2] = buttonpositive;
                                try {
                                    String buttonnegative = jsonObject.getString("buttonnegative");
                                    results[3] = buttonnegative;
                                    return;
                                } catch (Exception e) {
                                    return;
                                }
                            }
                        }
                    } catch (Exception e2) {
                        Log.e("XError", "exception", e2);
                    }
                }
            };
            t.start();
            t.join();
            return results;
        } catch (Exception e) {
            Log.e("XError", "exception", e);
            return null;
        }
    }
}
