package app.revanced.integrations.sponsorblock;

import android.content.Context;
import android.content.res.Resources;


import androidx.annotation.NonNull;

import java.util.HashMap;

import app.revanced.integrations.utils.LogHelper;

public class StringRef {
    private static Resources resources;
    private static String packageName;

    /**
     * Called in Application onCreate, should be called as soon as possible when after application startup
     *
     * @param context Any context, it will be used to obtain string resources
     */
    public static void setContext(Context context) {
        if (context == null) return;
        resources = context.getApplicationContext().getResources();
        packageName = context.getPackageName();
    }

    private static final HashMap<String, StringRef> strings = new HashMap<>();

    /**
     * Gets strings reference from shared collection or creates if not exists yet,
     * this method should be called if you want to get StringRef
     *
     * @param id string resource name/id
     * @return String reference that'll resolve to excepted string, may be from cache
     */
    @NonNull
    public static StringRef sf(@NonNull String id) {
        StringRef ref = strings.get(id);
        if (ref == null) {
            ref = new StringRef(id);
            strings.put(id, ref);
        }
        return ref;
    }

    /**
     * Gets string value by string id, shorthand for <code>sf(id).toString()</code>
     *
     * @param id string resource name/id
     * @return String value from string.xml
     */
    @NonNull
    public static String str(@NonNull String id) {
        return sf(id).toString();
    }

    /**
     * Gets string value by string id, shorthand for <code>sf(id).toString()</code> and formats the string
     * with given args.
     *
     * @param id   string resource name/id
     * @param args the args to format the string with
     * @return String value from string.xml formatted with given args
     */
    @NonNull
    public static String str(@NonNull String id, Object... args) {
        return String.format(str(id), args);
    }


    /**
     * Creates a StringRef object that'll not change it's value
     *
     * @param value value which toString() method returns when invoked on returned object
     * @return Unique StringRef instance, its value will never change
     */
    @NonNull
    public static StringRef constant(@NonNull String value) {
        final StringRef ref = new StringRef(value);
        ref.resolved = true;
        return ref;
    }

    /**
     * Shorthand for <code>constant("")</code>
     * Its value always resolves to empty string
     */
    @NonNull
    public static final StringRef empty = constant("");

    @NonNull
    private String value;
    private boolean resolved;

    public StringRef(@NonNull String resName) {
        this.value = resName;
    }

    @Override
    @NonNull
    public String toString() {
        if (!resolved) {
            resolved = true;
            if (resources != null) {
                final int identifier = resources.getIdentifier(value, "string", packageName);
                if (identifier == 0)
                    LogHelper.printException("StringRef", "Resource not found: " + value);
                else
                    value = resources.getString(identifier);
            }
        }
        return value;
    }
}
