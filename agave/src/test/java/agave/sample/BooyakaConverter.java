package agave.sample;

import agave.conversion.StringConverter;
import java.util.Locale;

public class BooyakaConverter implements StringConverter<String> {

    public String convert(String input, Locale locale) {
        return "Booyaka!";
    }

}
