package app.revanced.extension.shared.spoof;

import java.util.Locale;

public enum AudioStreamLanguage {
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
        iso639_1 = name().replace('_', '-');
    }

    public String getIso639_1() {
        // Changing the app language does not force the app to completely restart,
        // so the default needs to be the current language and not a static field.
        if (this == DEFAULT) {
            // Android VR requires uppercase language code.
            return Locale.getDefault().toLanguageTag().toUpperCase(Locale.US);
        }

        return iso639_1;
    }
}
