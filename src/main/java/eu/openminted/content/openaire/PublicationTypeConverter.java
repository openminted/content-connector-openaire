package eu.openminted.content.openaire;

import eu.openminted.registry.domain.PublicationTypeEnum;
import eu.openminted.registry.domain.RightsStatementEnum;

public class PublicationTypeConverter {
    public static PublicationTypeEnum convert(String bestLicence) {

        switch (bestLicence) {
            case "Article":
            case "Software Paper":
                return PublicationTypeEnum.RESEARCH_ARTICLE;
            case "Doctoral Thesis":
                return PublicationTypeEnum.DOCTORAL_THESIS;
            case "Conference Object":
                return PublicationTypeEnum.CONFERENCE_OBJECT;
            case "Preprint":
                return PublicationTypeEnum.PRE_PRINT;
            case "Research":
            case "External Research Report":
            case "Internal Report":
                return PublicationTypeEnum.RESEARCH_REPORT;
            case "Book":
            case "Collection":
                return PublicationTypeEnum.BOOK;
            case "Master Thesis":
                return PublicationTypeEnum.MASTER_THESIS;
            case "Part Of Book Or Chapter Of Book":
                return PublicationTypeEnum.BOOK_PART;
            case "Report":
                return PublicationTypeEnum.REPORT;
            case "Review":
                return PublicationTypeEnum.REVIEW;
            case "Bachelor Thesis":
                return PublicationTypeEnum.BACHELOR_THESIS;
            case "Lecture":
                return PublicationTypeEnum.LECTURE;
            case "Patent":
                return PublicationTypeEnum.PATENT;
            case "Contribution For Newspaper Or Weekly Magazine":
                return PublicationTypeEnum.CONTRIBUTION_TO_JOURNAL;
            case "Data Paper":
                return PublicationTypeEnum.DATA_PAPER;
            case "Annotation":
                return PublicationTypeEnum.ANNOTATION;
            case "Unknown":
            case "Other":
            case "Image":
            case "Sound":
            case "Dataset":
            case "Software":
            case "Event":
            case "Newsletter":
            default:
                return PublicationTypeEnum.OTHER;
        }
    }
}
