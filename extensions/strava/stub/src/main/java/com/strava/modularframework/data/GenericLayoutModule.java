package com.strava.modularframework.data;

import com.strava.analytics.AnalyticsProperties;
import com.strava.modularframework.promotions.Promotion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GenericLayoutModule implements Serializable, Module {
    private transient Object clickableField;
    private final Destination destination = null;
    private final String element = null;
    private final String[] eventsToTrack = null;
    private transient boolean isVisible;
    private transient Object item;
    private transient ItemIdentifier itemIdentifier;

    // @SerializedName("analytics_properties")
    private final AnalyticsProperties moduleAnalyticsProperties = null;
    private final GenericModuleField[] moduleFields;
    private transient ModularEntry parentEntry;
    private transient Module parentModule;
    private final boolean shouldTrackImpressions = false;
    private GenericLayoutModule[] submodules;
    private final String type;

    public GenericLayoutModule(String type, GenericModuleField[] moduleFields) {
        this.type = type;
        this.isVisible = true;
        this.moduleFields = moduleFields;
    }

    private final ModularEntry getParentEntryOrThrow() {
        if (parentEntry != null) {
            return parentEntry;
        }
        throw new IllegalStateException();
    }

    public final void attachReferences(GenericLayoutModule module) {}

    @Override
    public AnalyticsProperties getAnalyticsProperties() {
        return null;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public Object getClickableField() {
        return clickableField;
    }

    public final Destination getDestination() {
        return destination;
    }

    @Override
    public String getElement() {
        return element;
    }

    @Override
    public Object getEntityContext() {
        return null;
    }

    public final String[] getEventsToTrack() {
        return eventsToTrack;
    }

    public final GenericModuleField getField(String key) {
        return null;
    }

    public final GenericModuleField[] getFields() {
        return moduleFields;
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
    public List<String> getItemKeys() {
        return null;
    }

    @Override
    public String getItemProperty(String property) {
        return null;
    }

    public final AnalyticsProperties getModuleAnalyticsProperties() {
        return moduleAnalyticsProperties;
    }

    @Override
    public String getPage() {
        return null;
    }

    public final ModularEntry getParentEntry$modular_framework_productionRelease() {
        return parentEntry;
    }

    public final Module getParentModule$modular_framework_productionRelease() {
        return parentModule;
    }

    @Override
    public Promotion getPromotion() {
        return null;
    }

    public final boolean getShouldTrackImpressions() {
        return shouldTrackImpressions;
    }

    /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
    public final GenericLayoutModule[] getSubmodules() {
        return submodules;
    }

    @Override
    public Object getTrackable() {
        return null;
    }

    @Override
    public List<?> getTrackableEvents() {
        return null;
    }

    @Override
    public String getType() {
        return type;
    }

    public final boolean isVisible() {
        return isVisible;
    }

    @Override
    public void setItem(Object item) {
        this.item = item;
    }

    public final void setSubmodules(GenericLayoutModule[] submodules) {
        this.submodules = submodules;
    }

    public final void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public final void setupWithParent(ModularEntry entry, Module module) {}
}
