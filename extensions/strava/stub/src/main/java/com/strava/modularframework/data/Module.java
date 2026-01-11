package com.strava.modularframework.data;

import com.strava.analytics.AnalyticsProperties;
import com.strava.modularframework.promotions.Promotion;

import java.util.List;

public interface Module {
    AnalyticsProperties getAnalyticsProperties();

    String getCategory();

    Object getClickableField();

    String getElement();

    Object getEntityContext();

    Object getItem();

    ItemIdentifier getItemIdentifier();

    List<String> getItemKeys();

    String getItemProperty(String property);

    String getPage();

    Promotion getPromotion();

    Object getTrackable();

    List<?> getTrackableEvents();

    String getType();

    void setItem(Object item);
}
