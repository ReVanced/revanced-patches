package com.strava.mediamodels.data;

import java.io.Serializable;

public interface MediaContent extends Serializable {
    String getCaption();

    String getId();

    String getReferenceId();

    MediaType getType();

    void setCaption(String caption);
}
