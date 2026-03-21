package app.revanced.extension.youtube.patches.litho;

import static app.revanced.extension.youtube.patches.LayoutReloadObserverPatch.isActionBarVisible;

import app.revanced.extension.shared.ConversionContext.ContextInterface;
import app.revanced.extension.shared.patches.litho.Filter;
import app.revanced.extension.shared.patches.litho.FilterGroup.ByteArrayFilterGroup;
import app.revanced.extension.shared.patches.litho.FilterGroup.StringFilterGroup;
import app.revanced.extension.shared.patches.litho.FilterGroupList.ByteArrayFilterGroupList;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.EngagementPanel;
import app.revanced.extension.youtube.shared.NavigationBar;
import app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class HorizontalShelvesFilter extends Filter {
    private final ByteArrayFilterGroupList descriptionBuffers = new ByteArrayFilterGroupList();
    private final ByteArrayFilterGroupList generalBuffers = new ByteArrayFilterGroupList();

    public HorizontalShelvesFilter() {
        StringFilterGroup horizontalShelves = new StringFilterGroup(null, "horizontal_shelf.e");
        addPathCallbacks(horizontalShelves);

        descriptionBuffers.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_ATTRIBUTES_SECTION,
                        // May no longer work on v20.31+, even though the component is still there.
                        "cell_video_attribute"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_FEATURED_PLACES_SECTION,
                        "yt_fill_experimental_star",
                        "yt_fill_star"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_GAMING_SECTION,
                        "yt_outline_experimental_gaming",
                        "yt_outline_gaming"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_MUSIC_SECTION,
                        "yt_outline_experimental_audio",
                        "yt_outline_audio"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_QUIZZES_SECTION,
                        "post_base_wrapper_slim"
                )
        );

        generalBuffers.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_CREATOR_STORE_SHELF,
                        "shopping_item_card_list"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYABLES,
                        "FEmini_app_destination"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_TICKET_SHELF,
                        "ticket_item.e"
                )
        );
    }

    private boolean hideShelves(ContextInterface contextInterface) {
        if (!Settings.HIDE_HORIZONTAL_SHELVES.get()) {
            return false;
        }
        return contextInterface.isHomeFeedOrRelatedVideo()
                || PlayerType.getCurrent().isMaximizedOrFullscreen()
                || isActionBarVisible.get()
                || NavigationBar.isSearchBarActive()
                || NavigationBar.isBackButtonVisible()
                || NavigationButton.getSelectedNavigationButton() != NavigationButton.LIBRARY;
    }

    @Override
    public boolean isFiltered(ContextInterface contextInterface,
                              String identifier,
                              String accessibility,
                              String path,
                              byte[] buffer,
                              StringFilterGroup matchedGroup,
                              FilterContentType contentType,
                              int contentIndex) {
        if (contentIndex != 0) {
            return false;
        }
        if (generalBuffers.check(buffer).isFiltered()) {
            return true;
        }
        if (descriptionBuffers.check(buffer).isFiltered()) {
            return EngagementPanel.isDescription() || PlayerType.getCurrent().isMaximizedOrFullscreen() || isActionBarVisible.get() || ShortsPlayerState.isOpen();
        }
        return hideShelves(contextInterface);
    }
}
