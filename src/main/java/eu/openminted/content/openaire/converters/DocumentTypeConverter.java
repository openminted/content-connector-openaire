package eu.openminted.content.openaire.converters;

import eu.openminted.content.connector.utils.faceting.OMTDFacetLabels;
import eu.openminted.registry.domain.DocumentTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentTypeConverter {
    @Autowired
    private OMTDFacetLabels omtdFacetLabels;

    /**
     * Method to convert openaire property to omtd property
     *
     * @param documentType the value of the property
     * @return the corresponding enum
     */
    public DocumentTypeEnum convertToOMTD(String documentType) {

        switch (documentType.toLowerCase()) {
            case "fulltext":
                return DocumentTypeEnum.WITH_FULL_TEXT;
            default:
                return DocumentTypeEnum.WITH_ABSTRACT_ONLY;
        }
    }

    /**
     * Method to convert an omtd to openaire property and add it into a list of properties
     * @param documentTypeList list of properties to filter openaire
     * @param documentType value to convert
     */
    public void convertToOpenAIRE(List<String> documentTypeList, String documentType) {

        if (documentType.equalsIgnoreCase(omtdFacetLabels.
                getDocumentTypeLabelFromEnum(DocumentTypeEnum.WITH_FULL_TEXT))) {
            documentTypeList.add("*");
        }
    }
}
