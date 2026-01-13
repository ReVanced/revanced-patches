package com.strava.modularframework.data;

import com.strava.analytics.AnalyticsProperties;
import com.strava.modularframework.promotions.Promotion;
import java.util.Iterator;
import java.util.List;

public class ModularComponent implements Module {
    private final Object backgroundColor;
    private final BaseModuleFields baseModuleFields;
    private final String category;
    private final Object clickableField;
    private final String element;
    private transient Object item;
    private final ItemIdentifier itemIdentifier;
    private final List<String> itemKeys;
    private final LayoutProperties layoutProperties;
    private final String page;
    private final Promotion promotion;
    private final List<Module> submodules;
    private String type;

    public ModularComponent(String type, BaseModuleFields baseModuleFields, List<Module> submodules) {
        this.type = type;
        this.baseModuleFields = baseModuleFields;
        this.submodules = submodules;
        this.clickableField = baseModuleFields.getClickableField();
        this.itemIdentifier = baseModuleFields.getItemIdentifier();
        this.itemKeys = baseModuleFields.getItemKeys();
        this.category = baseModuleFields.getCategory();
        this.page = baseModuleFields.getPage();
        this.element = baseModuleFields.getElement();
        this.promotion = baseModuleFields.getPromotion();
        this.backgroundColor = baseModuleFields.getBackgroundColor();
        this.layoutProperties = baseModuleFields.getLayoutProperties();
    }

    @Override
    public AnalyticsProperties getAnalyticsProperties() {
        return baseModuleFields.getAnalyticsProperties();
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public Object getClickableField() {
        return clickableField;
    }

    @Override
    public String getElement() {
        return element;
    }

    @Override
    public Object getEntityContext() {
        return null;
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
        return itemKeys;
    }

    @Override
    public String getItemProperty(String property) {
        return null;
    }

    public final LayoutProperties getLayoutProperties() {
        return layoutProperties;
    }

    @Override
    public String getPage() {
        return page;
    }

    @Override
    public Promotion getPromotion() {
        return promotion;
    }

    public final List<Module> getSubmodules() {
        return submodules;
    }

    // Added by patch.
    public final List<Module> getSubmodules$original() {
        return submodules;
    }

    @Override
    public Object getTrackable() {
        return null;
    }

    @Override
    public List<?> getTrackableEvents() {
        return baseModuleFields.getTrackableEvents();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setItem(Object item) {
        this.item = item;
    }

    public void setType(String type) {
        this.type = type;
    }
}
