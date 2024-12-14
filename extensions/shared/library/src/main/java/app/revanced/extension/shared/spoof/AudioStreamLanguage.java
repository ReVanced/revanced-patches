package app.revanced.extension.shared.spoof;

import java.util.Locale;

public enum AudioStreamLanguage {
    /**
     * Change nothing, same as unpatched.
     */
    DEFAULT,

    /**
     * Original language of the video.
     */
    ORIGINAL,

    /**
     * Current language the app is set to.
     */
    APP_LANGUAGE,

    // Language codes found in locale_config.xml
    // Region specific variants of Chinese/English/Spanish/French have been removed.
    AF,
    AM,
    AR,
    AS,
    AZ,
    BE,
    BG,
    BN,
    BS,
    CA,
    CS,
    DA,
    DE,
    EL,
    EN,
    ES,
    ET,
    EU,
    FA,
    FI,
    FR,
    GL,
    GU,
    HI,
    HE, // App uses obsolete 'IW' and 'HE' is modern ISO code.
    HR,
    HU,
    HY,
    ID,
    IS,
    IT,
    JA,
    KA,
    KK,
    KM,
    KN,
    KO,
    KY,
    LO,
    LT,
    LV,
    MK,
    ML,
    MN,
    MR,
    MS,
    MY,
    NE,
    NL,
    NB,
    OR,
    PA,
    PL,
    PT_BR,
    PT_PT,
    RO,
    RU,
    SI,
    SK,
    SL,
    SQ,
    SR,
    SV,
    SW,
    TA,
    TE,
    TH,
    TL,
    TR,
    UK,
    UR,
    UZ,
    VI,
    ZH,
    ZU;

    private final String iso639_1;

    AudioStreamLanguage() {
        String name = name();
        final int regionSeparatorIndex = name.indexOf('_');
        if (regionSeparatorIndex >= 0) {
            iso639_1 = name.substring(0, regionSeparatorIndex).toLowerCase(Locale.US)
                    + name.substring(regionSeparatorIndex);
        } else {
            iso639_1 = name().toLowerCase(Locale.US);
        }
    }

    public String getIso639_1() {
        // Changing the app language does not force the app to completely restart,
        // so the default needs to be the current language and not a static field.
        if (this == DEFAULT) {
            return Locale.getDefault().toLanguageTag();
        }

        return iso639_1;
    }
}
