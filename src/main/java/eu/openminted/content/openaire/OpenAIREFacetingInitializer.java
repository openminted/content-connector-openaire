package eu.openminted.content.openaire;

import eu.openminted.content.connector.utils.faceting.OMTDFacetEnum;

import java.util.HashMap;
import java.util.Map;

public class OpenAIREFacetingInitializer {
    private Map<String, String> OmtdOpenAIREMap = new HashMap<>();
    private String INSTANCE_TYPE_NAME = "instancetypename";
    private String RESULT_DATE_OF_ACCEPTENCE = "resultdateofacceptance";
    private String RESULT_RIGHTS = "resultrights";
    private String RESULT_LANG_ID = "resultlanguageid";

    public OpenAIREFacetingInitializer() {
        OmtdOpenAIREMap.put(OMTDFacetEnum.PUBLICATION_TYPE.value(), INSTANCE_TYPE_NAME);
        OmtdOpenAIREMap.put(OMTDFacetEnum.PUBLICATION_YEAR.value(), RESULT_DATE_OF_ACCEPTENCE);
        OmtdOpenAIREMap.put(OMTDFacetEnum.RIGHTS_STMT_NAME.value(), RESULT_RIGHTS);
        OmtdOpenAIREMap.put(OMTDFacetEnum.RIGHTS.value(), RESULT_RIGHTS);
        OmtdOpenAIREMap.put(OMTDFacetEnum.DOCUMENT_LANG.value(), RESULT_LANG_ID);

        OmtdOpenAIREMap.put(OMTDFacetEnum.PUBLISHER.value(), OMTDFacetEnum.PUBLISHER.value());
        OmtdOpenAIREMap.put(OMTDFacetEnum.KEYWORD.value(), OMTDFacetEnum.KEYWORD.value());
        OmtdOpenAIREMap.put(OMTDFacetEnum.DOCUMENT_TYPE.value(), OMTDFacetEnum.DOCUMENT_TYPE.value());

        OmtdOpenAIREMap.put(INSTANCE_TYPE_NAME, OMTDFacetEnum.PUBLICATION_TYPE.value());
        OmtdOpenAIREMap.put(RESULT_DATE_OF_ACCEPTENCE, OMTDFacetEnum.PUBLICATION_YEAR.value());
        OmtdOpenAIREMap.put(RESULT_RIGHTS, OMTDFacetEnum.RIGHTS.value());
        OmtdOpenAIREMap.put(RESULT_LANG_ID, OMTDFacetEnum.DOCUMENT_LANG.value());
    }

    public Map<String, String> getOmtdOpenAIREMap() {
        return this.OmtdOpenAIREMap;
    }
}
