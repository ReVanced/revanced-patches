package com.ss.android.ugc.aweme.feed.model;

import com.ss.android.ugc.aweme.commerce.model.ShopAdStruct;
import com.ss.android.ugc.aweme.commerce.model.SimplePromotion;
import com.ss.android.ugc.aweme.search.ecom.data.Product;
import com.ss.android.ugc.aweme.commerce.AwemeCommerceStruct;
import java.util.List;

public class Aweme {
    
    // Internal Feed Type Identifiers
    public int awemeType; 
    public int adLinkType;
    
    // Live Stream Data
    public RoomStruct room;
    
    // Monetization & Sponsored Traffic
    public boolean isAd;
    public boolean isSoftAd;
    public AwemeRawAd awemeRawAd;
    public AwemeCommerceStruct mCommerceVideoAuthInfo;

    // E-Commerce / Shop Data
    public List<Object> productsInfo;
    public List<Object> simplePromotions;
    public ShopAdStruct shopAdStruct;
    
    // Non-Video Feed Injections (Fake Awemes)
    public boolean isReferralFakeAweme;
    public boolean isRecBigCardFakeAweme;

    // Social & Follow Recommendations
    public int recommendCardType;
    public List<Object> familiarRecommendUser;

    // Content Engagement Statistics
    public AwemeStatistics statistics;

    // Story Metadata
    public boolean isTikTokStory;

    public int getAwemeType() { throw new UnsupportedOperationException("Stub"); }
    public RoomStruct getRoom() { throw new UnsupportedOperationException("Stub"); }
    public boolean isAd() { throw new UnsupportedOperationException("Stub"); }
    public boolean isSoftAd() { throw new UnsupportedOperationException("Stub"); }
    public AwemeStatistics getStatistics() { throw new UnsupportedOperationException("Stub"); }
    
    // Stub methods for legacy compatibility
    public String getAid() { throw new UnsupportedOperationException("Stub"); }
    public boolean isLiveReplay() { throw new UnsupportedOperationException("Stub"); }
    public long getLiveId() { throw new UnsupportedOperationException("Stub"); }
    public String getLiveType() { throw new UnsupportedOperationException("Stub"); }
    public boolean isWithPromotionalMusic() { throw new UnsupportedOperationException("Stub"); }
    public String getShareUrl() { throw new UnsupportedOperationException("Stub"); }
    public List getImageInfos() { throw new UnsupportedOperationException("Stub"); }
}