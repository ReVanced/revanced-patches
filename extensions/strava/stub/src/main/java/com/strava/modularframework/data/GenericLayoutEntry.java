package com.strava.modularframework.data;

import com.strava.analytics.AnalyticsProperties;
import com.strava.modularframework.async.EntryPlaceHolder;
import com.strava.modularframework.promotions.Promotion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class GenericLayoutEntry implements ModularEntry {
    private transient EntryPosition entryPosition;
    private final AnalyticsProperties analyticsProperties;
    private final String anchor;
    private final String category;
    private final List<ModularEntry> childrenEntries;
    private final Destination destination;
    private final String element;
    private final Object entityContext;
    private final HashMap<String, String> entry_style;
    private final List<?> eventsToTrack;

    // @SerializedName("modules")
    private List<GenericLayoutModule> genericLayoutModules;
    private final boolean hasChildren;
    private final boolean isLazyLoadedEntry;
    private Object item;
    private final ItemIdentifier itemIdentifier;
    private transient List<Module> modules;
    private final String page;

    // @SerializedName("placeholder")
    private final EntryPlaceHolder placeHolder;
    private final Promotion promotion;
    private String rank;
    private final boolean shouldHideShadowDecorator;

    // @SerializedName("updated_at")
    private final String timestamp;

    public GenericLayoutEntry(Destination destination,
                              List<Module> modules,
                              Object item,
                              ItemIdentifier itemIdentifier,
                              List<ModularEntry> childrenEntries,
                              EntryPlaceHolder entryPlaceHolder,
                              String category,
                              String page,
                              String element,
                              AnalyticsProperties analyticsProperties,
                              Promotion promotion,
                              String anchor,
                              String rank,
                              String timestamp,
                              boolean shouldHideShadowDecorator,
                              List<?> eventsToTrack,
                              HashMap<String, String> map) {
        this.destination = destination;
        this.modules = modules;
        this.itemIdentifier = itemIdentifier;
        this.childrenEntries = childrenEntries;
        this.placeHolder = entryPlaceHolder;
        this.category = category;
        this.page = page;
        this.element = element;
        this.analyticsProperties = analyticsProperties;
        this.promotion = promotion;
        this.anchor = anchor;
        this.rank = rank;
        this.timestamp = timestamp;
        this.shouldHideShadowDecorator = shouldHideShadowDecorator;
        this.eventsToTrack = eventsToTrack;
        this.entry_style = map;
        this.item = item;
        this.entityContext = null;
        this.isLazyLoadedEntry = getPlaceHolder() != null;
        this.hasChildren = !getChildrenEntries().isEmpty();
    }

    @Override
    public AnalyticsProperties getAnalyticsProperties() {
        return analyticsProperties;
    }

    @Override
    public String getAnchor() {
        return anchor;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public List<ModularEntry> getChildrenEntries() {
        return childrenEntries;
    }

    @Override
    public Destination getDestination() {
        return destination;
    }

    @Override
    public String getElement() {
        return element;
    }

    @Override
    public Object getEntityContext() {
        return entityContext;
    }

    @Override
    public EntryPosition getEntryPosition() {
        return entryPosition;
    }

    @Override
    public List<?> getEventsToTrack() {
        return eventsToTrack;
    }

    public final List<GenericLayoutModule> getGenericLayoutModules() {
        return genericLayoutModules;
    }

    @Override
    public boolean getHasChildren() {
        return hasChildren;
    }

    @Override
    public Object getItem() {
        return item;
    }

    @Override
    public ItemIdentifier getItemIdentifier() {
        return itemIdentifier;
    }

    @Override
    public String getItemProperty(String property) {
        return null;
    }

    @Override
    public List<Module> getModules() {
        return modules;
    }

    @Override
    public String getPage() {
        return page;
    }

    @Override
    public EntryPlaceHolder getPlaceHolder() {
        return placeHolder;
    }

    @Override
    public Promotion getPromotion() {
        return promotion;
    }

    @Override
    public String getRank() {
        return rank;
    }

    @Override
    public boolean getShouldHideShadowDecorator() {
        return shouldHideShadowDecorator;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public Object getTrackable() {
        return false;
    }

    @Override
    public boolean hasSameBackingItem(ItemIdentifier identifier) {
        return false;
    }

    @Override
    public boolean isGrouped() {
        return getEntryPosition() != EntryPosition.NONE;
    }

    @Override
    public boolean isLazyLoadedEntry() {
        return isLazyLoadedEntry;
    }

    @Override
    public void setEntryPosition(EntryPosition entryPosition) {
        this.entryPosition = entryPosition;
    }

    public final void setGenericLayoutModules(List<GenericLayoutModule> genericLayoutModules) {
        this.genericLayoutModules = genericLayoutModules;
    }

    @Override
    public void setItem(Object item) {
        this.item = item;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    @Override
    public void setRank(String rank) {
        this.rank = rank;
    }
}
