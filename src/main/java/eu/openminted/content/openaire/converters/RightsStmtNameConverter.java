package eu.openminted.content.openaire.converters;

import eu.openminted.content.connector.utils.faceting.OMTDFacetLabels;
import eu.openminted.registry.domain.RightsStatementEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RightsStmtNameConverter {

    @Autowired
    private OMTDFacetLabels omtdFacetInitializer;

    public RightsStatementEnum convertToOMTD(String bestLicence) {

        switch (bestLicence) {
            case "Open Access":
                return RightsStatementEnum.OPEN_ACCESS;
            case "12 Months Embargo":
            case "6 Months Embargo":
            case "Embargo":
            case "Restricted":
            case "Closed Access":
                return RightsStatementEnum.RESTRICTED_ACCESS;
        }
        return null;
    }


    public void convertToOpenAIRE(List<String> rightsValues, String rights) {

        RightsStatementEnum rightsStatementEnum;

        try {
            rightsStatementEnum = RightsStatementEnum.valueOf(rights);

        } catch (IllegalArgumentException e) {
            try {
                rightsStatementEnum = omtdFacetInitializer.getOmtdGetRightsStmtEnumFromLabel().get(rights);
            } catch (Exception e1) {
                rightsValues.add(rights);
                rightsStatementEnum = null;
            }
        }

        if (rightsStatementEnum != null) {
            switch (rightsStatementEnum) {
                case OPEN_ACCESS:
                    rightsValues.add("\"Open Access\"");
                    break;
                case RESTRICTED_ACCESS:
                    rightsValues.add("\"Embargo\"");
                    break;
                default:
                    break;
            }
        }
    }

}
