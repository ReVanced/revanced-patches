package com.strava.modularframework.data;

import com.strava.analytics.AnalyticsProperties;
import com.strava.modularframework.async.EntryPlaceHolder;
import com.strava.modularframework.promotions.Promotion;

import java.util.List;

public interface ModularEntry {
    AnalyticsProperties getAnalyticsProperties();

    String getAnchor();

    String getCategory();

    List<ModularEntry> getChildrenEntries();

    Destination getDestination();

    String getElement();

    Object getEntityContext();

    EntryPosition getEntryPosition();

    List<?> getEventsToTrack();

    boolean getHasChildren();

    Object getItem();

    ItemIdentifier getItemIdentifier();

    String getItemProperty(String property);

    List<Module> getModules();

    String getPage();

    EntryPlaceHolder getPlaceHolder();

    Promotion getPromotion();

    String getRank();

    boolean getShouldHideShadowDecorator();

    String getTimestamp();

    Object getTrackable();

    boolean hasSameBackingItem(ItemIdentifier identifier);

    boolean isGrouped();

    boolean isLazyLoadedEntry();

    void setEntryPosition(EntryPosition entryPosition);

    void setItem(Object item);

    void setRank(String str);

    // Added by patch.
    List<Module> getModules$original();
}
