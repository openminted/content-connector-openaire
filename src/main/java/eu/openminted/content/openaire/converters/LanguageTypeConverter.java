package eu.openminted.content.openaire.converters;

import eu.openminted.content.connector.utils.language.LanguageUtils;
import eu.openminted.registry.domain.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LanguageTypeConverter {

    @Autowired
    private LanguageUtils languageUtils;

    public LanguageTypeConverter() {
        languageUtils = new LanguageUtils();
    }

    /**
     * Converts a language code to a language name as specified in Utils LanguageConverter
     * This method is mainly used when we create OMTD objects from OpenAIRE codes
     * @param classid the code to find the corresponding language name
     * @return Language element with Id (code) and Tag (name)
     */
    public Language convertCodeToLanguage(String classid) {
        Language language;
        String classId = null;

        String[] classIds = classid.split("/");
        if (classIds.length > 0) classId = classIds[0].replaceAll("\\'", "");

        language = getLanguageAndCode(classId);

        if ((language == null
                || language.getLanguageTag() == null)
                && classIds.length > 1) {
            classId = classIds[1].replaceAll("\\'", "");
            language = getLanguageAndCode(classId);
        }
        // for testing purposes only uncomment the following lines
//        if (language == null) System.out.println("The classId was " + classId + " from classid " + classid);
//        else if (language.getLanguageTag() == null) System.out.println("The tag is null classId was " + classId + " from classid " + classid);

        return language;
    }

    /**
     * Assisting method for convertCodeToLanguage
     * It iterates through various ISO code lists to find corresponding codes and languages
     * @param code the code to find the corresponding language name
     * @return Language element with Id (code) and Tag (name)
     */
    private Language getLanguageAndCode(String code) {
        Language language = null;
        if (languageUtils.getLangCodeToName().containsKey(code)) {
            language = new Language();

            language.setLanguageId(code);
            language.setLanguageTag(languageUtils.getLangCodeToName().get(code));

        } else if (languageUtils.getConvert639_2Bto639_1().containsKey(code)) {
            language = new Language();

            code = languageUtils.getConvert639_2Bto639_1().get(code);
            language.setLanguageId(code);
            language.setLanguageTag(languageUtils.getLangCodeToName().get(code));

        } else if (languageUtils.getConvert639_2Tto639_1().containsKey(code)) {
            language = new Language();

            code = languageUtils.getConvert639_2Tto639_1().get(code);
            language.setLanguageId(code);
            language.setLanguageTag(languageUtils.getLangCodeToName().get(code));

        } else if (languageUtils.getConvert639_3to639_1().containsKey(code)) {
            language = new Language();

            code = languageUtils.getConvert639_3to639_1().get(code);
            language.setLanguageId(code);
            language.setLanguageTag(languageUtils.getLangCodeToName().get(code));
        } else if (languageUtils.getObsoletes().containsKey(code)) {
            language = new Language();

            code = languageUtils.getObsoletes().get(code);
            language.setLanguageId(code);
            language.setLanguageTag(languageUtils.getLangCodeToName().get(code));
        }
        return language;
    }

    /**
     * Method that converts an OMTD language name to the corresponding codes
     * from various ISO code lists in order to look for at OpenAIRE.
     * This method is used prior to an OpenAIRE query
     * @param languageNameList the OpenAire list for the query
     * @param languageName the language name to look for
     */
    public void convertToOpenAIRE(List<String> languageNameList, String languageName) {

        if (languageUtils.getLangNameToCode().containsKey(languageName)) {
            List<String> codes = getCodesFrom639_1Code(languageUtils.getLangNameToCode().get(languageName));
            languageNameList.addAll(codes);
        }
    }

    /**
     * Assisting method for convertToOpenAIRE
     * @param code code to look for into other ISO coding lists
     * @return List of codes from other ISO coding lists and the code to look for
     */
    private List<String> getCodesFrom639_1Code(String code) {
        List<String> codes = new ArrayList<>();

        if (languageUtils.getConvert639_1to639_2B().containsKey(code)) {
            codes.add(languageUtils.getConvert639_1to639_2B().get(code));
        }

        if (languageUtils.getConvert639_1to639_2T().containsKey(code)) {
            codes.add(languageUtils.getConvert639_1to639_2T().get(code));
        }

        if (languageUtils.getConvert639_1to639_3().containsKey(code)) {
            codes.add(languageUtils.getConvert639_1to639_3().get(code));
        }

        if (languageUtils.getObsoletes().containsKey(code)) {
            codes.add(languageUtils.getObsoletes().get(code));
        }

        if (languageUtils.getLangCodeToName().containsKey(code)) {
            codes.add(code);
        }

        return codes;
    }
}
