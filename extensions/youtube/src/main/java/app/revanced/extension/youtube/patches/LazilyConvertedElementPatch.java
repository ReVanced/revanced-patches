package app.revanced.extension.youtube.patches;

import java.util.List;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.ConversionContext.ContextInterface;


@SuppressWarnings("unused")
public class LazilyConvertedElementPatch {
    private static final String LAZILY_CONVERTED_ELEMENT = "LazilyConvertedElement";

    /**
     * Injection point.
     */
    public static void onTreeNodeResultLoaded(ContextInterface contextInterface, List<Object> treeNodeResultList) {
        if (treeNodeResultList == null || treeNodeResultList.isEmpty()) {
            return;
        }
        String firstElement = treeNodeResultList.get(0).toString();
        if (!LAZILY_CONVERTED_ELEMENT.equals(firstElement)) {
            return;
        }
        String identifier = contextInterface.patch_getIdentifier();
        if (Utils.isNotEmpty(identifier)) {
            onLazilyConvertedElementLoaded(identifier, treeNodeResultList);
        }
    }

    private static void onLazilyConvertedElementLoaded(String identifier, List<Object> treeNodeResultList) {
        // Code added by patch.
    }
}
