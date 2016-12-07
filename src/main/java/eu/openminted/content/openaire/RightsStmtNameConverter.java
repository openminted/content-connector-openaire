package eu.openminted.content.openaire;

import eu.openminted.registry.domain.RightsStmtNameEnum;

public class RightsStmtNameConverter {
    public static RightsStmtNameEnum convert(String bestLicence) {

        switch (bestLicence) {
            case "Open Access":
                return RightsStmtNameEnum.OPEN_ACCESS;
            case "12 Months Embargo":
            case "6 Months Embargo":
            case "Embargo":
                return RightsStmtNameEnum.EMBARGOED_ACCESS;
            case "Restricted":
                return RightsStmtNameEnum.RESTRICTED_ACCESS;
            case "Closed Access":
                return RightsStmtNameEnum.CLOSED_ACCESS;
        }
        return null;
    }
}
