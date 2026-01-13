package com.strava.modularframework.data;

import com.strava.analytics.AnalyticsProperties;
import com.strava.modularframework.promotions.Promotion;
import java.util.List;

public final class BaseModuleFields {
    private final AnalyticsProperties analyticsProperties;
    private final Object backgroundColor;
    private final String category;
    private final Object clickableField;
    private final String element;
    private final ItemIdentifier itemIdentifier;
    private final List<String> itemKeys;
    private final LayoutProperties layoutProperties;
    private final String page;
    private final ModularEntry parentEntry;
    private final Module parentModule;
    private final Promotion promotion;
    private final List<?> trackableEvents;

    public BaseModuleFields(Module module,
                            ModularEntry modularEntry,
                            Object clickableField,
                            ItemIdentifier itemIdentifier,
                            List<String> itemKeys,
                            Object backgroundColor,
                            String category,
                            String page,
                            String element,
                            AnalyticsProperties analyticsProperties,
                            Promotion promotion,
                            List<?> trackableEvents,
                            LayoutProperties layoutProperties) {
        this.parentModule = module;
        this.parentEntry = modularEntry;
        this.clickableField = clickableField;
        this.itemIdentifier = itemIdentifier;
        this.itemKeys = itemKeys;
        this.backgroundColor = backgroundColor;
        this.category = category;
        this.page = page;
        this.element = element;
        this.analyticsProperties = analyticsProperties;
        this.promotion = promotion;
        this.trackableEvents = trackableEvents;
        this.layoutProperties = layoutProperties;
    }

    public final AnalyticsProperties getAnalyticsProperties() {
        return analyticsProperties;
    }

    public final Object getBackgroundColor() {
        return backgroundColor;
    }

    public final String getCategory() {
        return category;
    }

    public final Object getClickableField() {
        return clickableField;
    }

    public final String getElement() {
        return element;
    }

    public final ItemIdentifier getItemIdentifier() {
        return itemIdentifier;
    }

    public final List<String> getItemKeys() {
        return itemKeys;
    }

    public final LayoutProperties getLayoutProperties() {
        return layoutProperties;
    }

    public final String getPage() {
        return page;
    }

    public final Promotion getPromotion() {
        return promotion;
    }

    public final List<?> getTrackableEvents() {
        return trackableEvents;
    }
}
