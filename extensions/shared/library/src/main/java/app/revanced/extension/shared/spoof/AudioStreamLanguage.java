package app.revanced.extension.shared.spoof;

import java.util.Locale;

import app.revanced.extension.shared.Utils;

public enum AudioStreamLanguage {
    /**
     * YouTube default.
     * Can be the original language or can be app language,
     * depending on what YouTube decides to pick as the default.
     */
    DEFAULT,

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

    private final String language;

    AudioStreamLanguage() {
        language = name().toLowerCase(Locale.US);
    }

    public String getLanguage() {
        // Changing the app language does not force the app to completely restart,
        // so the default needs to be the current language and not a static field.
        if (this == DEFAULT) {
            return Locale.getDefault().getLanguage();
        }

        return language;
    }
}
