package ir.mamap.app;


/**
 * Created by hosein on 2/11/2017.
 */

public enum LanguageType {
    Persian,
    English,
    French,
    German,
    Arabic;

    public int getValue() {
        return this.ordinal();
    }

    public static LanguageType forValue(Integer value) {
        return values()[(value == null) ? 0 : value];
    }

    public static LanguageType forValue(String code) {
        if (code == null) {
            return LanguageType.Persian;
        }
        switch (code) {
            case "en":
                return LanguageType.English;
            case "fa":
                return LanguageType.Persian;
            case "fr":
                return LanguageType.French;
            case "de":
                return LanguageType.German;
            case "ar":
                return LanguageType.Arabic;
            default:
                return LanguageType.Persian;
        }
    }

    public static String getStringCode(LanguageType languageType) {
        switch (languageType) {
            case Persian:
                return "fa";
            case English:
                return "en";
            case French:
                return "fr";
            case German:
                return "de";
            case Arabic:
                return "ar";
            default:
                return "fa";
        }
    }

    public static String getString(LanguageType languageType) {
        switch (languageType) {
            case English:
                return "English";
            case Persian:
                return "فارسی";
            default:
                return "فارسی";
        }
    }
}

