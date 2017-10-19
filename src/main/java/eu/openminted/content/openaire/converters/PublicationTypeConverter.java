package eu.openminted.content.openaire.converters;

import eu.openminted.content.connector.utils.faceting.OMTDFacetLabels;
import eu.openminted.registry.domain.PublicationTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PublicationTypeConverter {

    @Autowired
    private OMTDFacetLabels omtdFacetLabels;

    public PublicationTypeEnum convertToOMTD(String bestLicence) {

        switch (bestLicence.toLowerCase()) {
            case "article":
            case "software paper":
            case "research":
                return PublicationTypeEnum.RESEARCH_ARTICLE;
            case "thesis":
                return PublicationTypeEnum.THESIS;
            case "bachelor thesis":
                return PublicationTypeEnum.BACHELOR_THESIS;
            case "doctoral thesis":
                return PublicationTypeEnum.DOCTORAL_THESIS;
            case "conference object":
                return PublicationTypeEnum.CONFERENCE_OBJECT;
            case "preprint":
                return PublicationTypeEnum.PRE_PRINT;
            case "external research report":
            case "internal report":
                return PublicationTypeEnum.RESEARCH_REPORT;
            case "book":
            case "collection":
                return PublicationTypeEnum.BOOK;
            case "master thesis":
                return PublicationTypeEnum.MASTER_THESIS;
            case "part of book or chapter of book":
                return PublicationTypeEnum.BOOK_PART;
            case "report":
                return PublicationTypeEnum.REPORT;
            case "review":
                return PublicationTypeEnum.REVIEW;
            case "lecture":
                return PublicationTypeEnum.LECTURE;
            case "patent":
                return PublicationTypeEnum.PATENT;
            case "contribution for newspaper or weekly magazine":
                return PublicationTypeEnum.CONTRIBUTION_TO_JOURNAL;
            case "data paper":
                return PublicationTypeEnum.DATA_PAPER;
            case "annotation":
                return PublicationTypeEnum.ANNOTATION;
            case "unknown":
            case "other":
            case "image":
            case "sound":
            case "dataset":
            case "software":
            case "event":
            case "newsletter":
            default:
                return PublicationTypeEnum.OTHER;
        }
    }

    public void convertToOpenAIRE(List<String> publicationTypeList, String publication) {
        PublicationTypeEnum publicationType;

        try {
            publicationType = PublicationTypeEnum.valueOf(publication);

        } catch (IllegalArgumentException e) {
            try {
                publicationType = omtdFacetLabels.getPublicationTypeEnumFromLabel().get(publication);
            } catch (Exception e1) {
                publicationTypeList.add(publication);
                publicationType = null;
            }
        }

        if (publicationType != null) {
            switch (publicationType) {
                case RESEARCH_ARTICLE:
                    publicationTypeList.add("Article");
                    publicationTypeList.add("Software Paper");
                    break;
                case THESIS:
                    publicationTypeList.add("Doctoral Thesis");
                    publicationTypeList.add("Master Thesis");
                    publicationTypeList.add("Bachelor Thesis");
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
                case RESEARCH_PROPOSAL:
                    publicationTypeList.add("Research");
                    publicationTypeList.add("Article");
                    publicationTypeList.add("Software Paper");
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
}
