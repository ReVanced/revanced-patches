package app.revanced.extension.shared;

public final class ConversionContext {
    /**
     * Interface to use obfuscated methods.
     */
    public interface ContextInterface {
        // Methods implemented by patch.
        StringBuilder patch_getPathBuilder();

        String patch_getIdentifier();

        default boolean isHomeFeedOrRelatedVideo() {
            return toString().contains("horizontalCollectionSwipeProtector=null");
        }

        default boolean isSubscriptionOrLibrary() {
            return toString().contains("heightConstraint=null");
        }
    }
}
