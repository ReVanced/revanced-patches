package app.revanced.extension.strava;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import okhttp3.*;

import app.revanced.extension.shared.Utils;

import java.io.IOException;
import java.io.OutputStream;

@SuppressLint("NewApi")
public final class Media {
    public static void downloadPhoto(String url, String name) {
        Utils.runOnBackgroundThread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                ResponseBody body = response.body();
                String mimeType = body.contentType().toString();
                String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                ContentResolver resolver = Utils.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, name + '.' + extension);
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Strava");
                Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                        : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Uri row = resolver.insert(collection, values);
                try (OutputStream outputStream = resolver.openOutputStream(row)) {
                    // body.byteStream().transferTo(outputStream);
                    byte[] buffer = new byte[1024 * 8];
                    int length;
                    while ((length = body.byteStream().read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                } finally {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(row, values, null);
                }
                int successId = Resources.Strings.saveImageSuccess();
                String successText = successId != 0
                        ? Utils.getResourceString(successId)
                        : "✔️";
                Utils.showToastShort(successText);
            } catch (IOException e) {
                int failureId = Resources.Strings.saveFailure();
                String failureText = failureId != 0
                        ? Utils.getResourceString(failureId)
                        : "❌";
                Utils.showToastLong(failureText + ' ' + e.getLocalizedMessage());
            }
        });
    }
}
