package app.revanced.extension.reddit.patches;

import com.reddit.domain.model.ILink;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class FilterPromotedLinksPatch {

    /**
     * Injection point.
     *
     * Filters list from promoted links.
     **/
    public static List<?> filterChildren(final Iterable<?> links) {
        final List<Object> filteredList = new ArrayList<>();

        for (Object item : links) {
            if (item instanceof ILink && ((ILink) item).getPromoted()) continue;

            filteredList.add(item);
        }

        return filteredList;
    }
}
