package android.os;

import java.io.File;

public final class Environment {
    public static String DIRECTORY_DOWNLOADS = "Download";

    public static File getExternalStoragePublicDirectory(final String type) {
        throw new UnsupportedOperationException("Stub");
    }
}
