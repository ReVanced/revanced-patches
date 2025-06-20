package app.revanced.extension.messenger.metaai;

import java.util.*;

import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public class RemoveMetaAIPatch {
    private static final Set<Long> loggedIDs = Collections.synchronizedSet(new HashSet<>());

    public static boolean overrideBooleanFlag(long id, boolean value) {
        try {
            if (Long.toString(id).startsWith("REPLACED_BY_PATCH")) {
                if (loggedIDs.add(id))
                    Logger.printInfo(() -> "Overriding " + id + " from " + value + " to false");

                return false;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "overrideBooleanFlag failure", ex);
        }

        return value;
    }
}
