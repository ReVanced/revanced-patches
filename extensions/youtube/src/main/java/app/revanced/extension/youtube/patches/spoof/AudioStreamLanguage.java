package app.revanced.extension.youtube.patches.spoof;

import app.revanced.extension.shared.Utils;

public enum AudioStreamLanguage {
    DEFAULT(Utils.getContext().getResources()
            .getConfiguration().locale.getLanguage()),

    // Language codes found in locale_config.xml
    // Chinese languages use the same language codes as localized resources.
    // Region specific variants of English/Spanish/French have been removed,
    // and for those variants the user can pick the app language option.
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
    SR_LATN,
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
    ZH_CN,
    ZH_TW,
    ZU;

    public final String iso_639_1;

    AudioStreamLanguage(String iso_639_1) {
        this.iso_639_1 = iso_639_1;
    }

    AudioStreamLanguage() {
        iso_639_1 = this.name().replace('_', '-');
    }
}
