package eu.openminted.content.openaire;

import eu.openminted.registry.domain.PublicationTypeEnum;
import eu.openminted.registry.domain.RightsStatementEnum;

import java.util.List;

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

    public static void convert(List<String> publicationTypeList, PublicationTypeEnum publicationType) {
        switch (publicationType) {
            case RESEARCH_ARTICLE:
                publicationTypeList.add("Article");
                publicationTypeList.add("Software Paper");
                break;
            case DOCTORAL_THESIS:
                publicationTypeList.add("Doctoral Thesis");
                break;
            case CONFERENCE_OBJECT:
                publicationTypeList.add("Conference Object");
                break;
            case PRE_PRINT:
                publicationTypeList.add("Preprint");
                break;
            case RESEARCH_REPORT:
                publicationTypeList.add("Research");
                publicationTypeList.add("External Research Report");
                publicationTypeList.add("Internal Report");
                break;
            case BOOK:
                publicationTypeList.add("Book");
                publicationTypeList.add("Collection");
                break;
            case MASTER_THESIS:
                publicationTypeList.add("Master Thesis");
                break;
            case BOOK_PART:
                publicationTypeList.add("Part Of Book Or Chapter Of Book");
                break;
            case REPORT:
                publicationTypeList.add("Report");
                break;
            case REVIEW:
                publicationTypeList.add("Review");
                break;
            case BACHELOR_THESIS:
                publicationTypeList.add("Bachelor Thesis");
                break;
            case LECTURE:
                publicationTypeList.add("Lecture");
                break;
            case PATENT:
                publicationTypeList.add("Patent");
                break;
            case CONTRIBUTION_TO_JOURNAL:
                publicationTypeList.add("Contribution For Newspaper Or Weekly Magazine");
                break;
            case DATA_PAPER:
                publicationTypeList.add("Data Paper");
                break;
            case ANNOTATION:
                publicationTypeList.add("Annotation");
                break;
            default:
                publicationTypeList.add("Unknown");
                publicationTypeList.add("Other");
                break;
        }
    }
}
