package app.revanced.extension.shared;

public final class ConversionContext {
    /**
     * Interface to use obfuscated methods.
     */
    public interface ContextInterface {
        // Methods implemented by patch.
        StringBuilder patch_getPathBuilder();

        String patch_getIdentifier();
    }
}
