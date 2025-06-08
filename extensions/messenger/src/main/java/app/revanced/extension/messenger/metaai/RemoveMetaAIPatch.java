package app.revanced.extension.messenger.metaai;

@SuppressWarnings("unused")
public class RemoveMetaAIPatch {
    public static boolean overrideBooleanFlag(long id, boolean value) {
        // This catches all flag ids related to Meta AI.
        // A list of specific ones that need disabling would probably be better,
        // but these ids seem to change slightly with each update.
        if ((id & 0x7FFFFFC000000000L) == 0x810A8000000000L) {
            return false;
        }

        return value;
    }
}
