package com.ss.android.ugc.aweme.commerce;

import java.io.Serializable;

public class AwemeCommerceStruct implements Serializable {
    public long brandedContentType;
    public long brandOrganicType;

    public boolean isBrandedContent() {
        return this.brandedContentType > 0;
    }

    public boolean isBrandOrganicContent() {
        return this.brandOrganicType > 0;
    }
}