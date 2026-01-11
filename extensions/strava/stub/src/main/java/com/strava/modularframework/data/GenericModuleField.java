package com.strava.modularframework.data;

import com.strava.analytics.AnalyticsProperties;

import java.io.Serializable;

public class GenericModuleField implements Serializable {
    private AnalyticsProperties analyticsProperties;
    private Destination destination;
    private String element;
    private String item_key;
    private String key;
    private transient Module parent;
    private String value;
    // JsonElement
    private Object value_object;

    public GenericModuleField(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public AnalyticsProperties getAnalyticsProperties() {
        return analyticsProperties;
    }

    public Destination getDestination() {
        return destination;
    }

    public String getElement() {
        return element;
    }

    public AnalyticsProperties getFieldAnalyticsProperties() {
        return analyticsProperties;
    }

    public String getItemKey() {
        return item_key;
    }

    public String getKey() {
        return key;
    }

    public Module getParent() {
        return parent;
    }

    public Object getRawValueObject() {
        return value_object;
    }

    public Object getTrackable() {
        return null;
    }

    public String getValue() {
        return value;
    }

    public void setParent(Module parent) {
        this.parent = parent;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
