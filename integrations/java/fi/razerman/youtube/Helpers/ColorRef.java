package fi.razerman.youtube.Helpers;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;

/* loaded from: classes6.dex */
public class ColorRef {
    public static final String CREDITS = "Converted from jakubweg's StringRef https://github.com/YTVanced/SponsorBlock/blob/master/app/src/main/java/pl/jakubweg/StringRef.java";
    public static final String TAG = "ColorRef";
    private static final HashMap<String, ColorRef> colors = new HashMap<>();
    private static String packageName;
    private static Resources resources;
    @NonNull
    private final String colorName;
    private boolean resolved;
    private Integer value;

    public static void setContext(Context context) {
        if (context != null) {
            resources = context.getApplicationContext().getResources();
            packageName = context.getPackageName();
        }
    }

    @NonNull
    /* renamed from: cf */
    public static ColorRef m32591cf(@NonNull String resName, @NonNull Integer defaultValue) {
        ColorRef ref = colors.get(resName);
        if (ref != null) {
            return ref;
        }
        ColorRef ref2 = new ColorRef(resName, defaultValue);
        colors.put(resName, ref2);
        return ref2;
    }

    @NonNull
    public static Integer color(@NonNull String resName, @NonNull Integer defaultValue) {
        return m32591cf(resName, defaultValue).resolve();
    }

    public ColorRef(@NonNull String resName, @NonNull Integer defaultValue) {
        this.colorName = resName;
        this.value = defaultValue;
    }

    @NonNull
    public Integer resolve() {
        if (!this.resolved) {
            this.resolved = true;
            Resources resources2 = resources;
            if (resources2 != null) {
                try {
                    this.value = resources2.getColor(resources2.getIdentifier(this.colorName, "color", packageName));
                } catch (Resources.NotFoundException e) {
                    Log.e(TAG, "Resource not found: " + this.value);
                }
            }
        }
        return this.value;
    }
}
