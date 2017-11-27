package eu.openminted.content.openaire;

import eu.openminted.content.openaire.converters.LanguageTypeConverter;
import eu.openminted.content.openaire.converters.PublicationTypeConverter;
import eu.openminted.content.openaire.converters.RightsStmtNameConverter;
import eu.openminted.registry.domain.*;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Class for converting an OpenAIRE publication xml into an OMTD documentMetadataRecord class and convert it to string xml
 */
public class PublicationResultHandler extends DefaultHandler {
    private static Logger log = Logger.getLogger(PublicationResultHandler.class.getName());

    private RightsStmtNameConverter rightsStmtNameConverter;
    private PublicationTypeConverter publicationTypeConverter;
    private LanguageTypeConverter languageTypeConverter;
    private DocumentMetadataRecord documentMetadataRecord;
    private DocumentInfo publication;
    private MetadataHeaderInfo metadataHeaderInfo;
    private MetadataIdentifier metadataIdentifier;
    private PublicationIdentifier publicationIdentifier;
    private Title title;
    private PersonInfo author;
    private DocumentDistributionInfo documentDistributionInfo;
    private RightsInfo rightsInfo;
    private JournalInfo relatedJournal;
    private DataFormatInfo dataFormatInfo;

    private String description = "";
    private String value = "";
    private boolean hasAuthor = false;
    private boolean hasRelation = false;
    private boolean hasKeyword = false;
    private boolean hasSubject = false;
    private boolean hasAbstract = false;
    private boolean hasIndexInfo = false;

    private Marshaller jaxbMarshaller;

    private String OMTDPublication;

    PublicationResultHandler() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(DocumentMetadataRecord.class);
        jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.setProperty("jaxb.fragment", true);

        rightsStmtNameConverter = new RightsStmtNameConverter();
        publicationTypeConverter = new PublicationTypeConverter();
        languageTypeConverter = new LanguageTypeConverter();
        documentMetadataRecord = new DocumentMetadataRecord();
        Document document = new Document();
        publication = new DocumentInfo();
        document.setPublication(publication);
        metadataIdentifier = new MetadataIdentifier();
        metadataHeaderInfo = new MetadataHeaderInfo();
        metadataHeaderInfo.setMetadataRecordIdentifier(metadataIdentifier);
        documentMetadataRecord.setMetadataHeaderInfo(metadataHeaderInfo);
        documentMetadataRecord.setDocument(document);
        rightsInfo = new RightsInfo();
        relatedJournal = new JournalInfo();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        /*
            DocumentMetadataRecord
         */
        if (qName.equalsIgnoreCase("result")) {
            /*
                Set by default the document type to abstract until we find a solution to this
             */
            documentMetadataRecord.getDocument().getPublication().setDocumentType(DocumentTypeEnum.WITH_ABSTRACT_ONLY);
        }
        /*
            Title
        */
        else if (qName.equalsIgnoreCase("title")) {
            String classname = attributes.getValue("classname");
            title = new Title();
            TitleTypeEnum titleTypeEnum;
            try {
                if (classname != null) {
                    titleTypeEnum = TitleTypeEnum.valueOf(classname);
                    title.setTitleType(titleTypeEnum);
                }
            } catch (IllegalArgumentException ex) {
                // main title otherwise?
            }
        }
        /*
            Authors
            Authors as long as other elements are provided by the <rel> elements,
            in combination with their enclosed elements
         */
        else if (qName.equalsIgnoreCase("rel")) {
            hasRelation = true;
        } else if (qName.equalsIgnoreCase("to")) {
            String classAttribute = attributes.getValue("class");
            ResultRelationsEnum resultRelationsEnum = ResultRelationsEnum.fromValue(classAttribute);

            switch (resultRelationsEnum) {
                case HAS_AUTHOR:
                    hasAuthor = true;
                    author = new PersonInfo();
                    break;
                default:
                    hasAuthor = false;
                    break;
            }
        }
        /*
            PublicationType
         */
        else if (qName.equalsIgnoreCase("instancetype")) {
            String classname = attributes.getValue("classname");
            PublicationTypeEnum publicationTypeEnum = publicationTypeConverter.convertToOMTD(classname);

            if (publicationTypeEnum == PublicationTypeEnum.OTHER) {
                String schemeid = attributes.getValue("schemeid");
                if (!schemeid.isEmpty())
                    publicationIdentifier.setSchemeURI("http://api.openaire.eu/vocabularies/" + schemeid + "/" + classname);
                else {
                    publicationIdentifier.setSchemeURI("http://api.openaire.eu/vocabularies/dnet:publication_resource/UNKNOWN");
                }
            }

            publication.setPublicationType(publicationTypeEnum);
        }
        /*
            PublicationIdentifierSchemeName & schemeURI (if necessary)
         */
        else if (qName.equalsIgnoreCase("pid")) {
            publicationIdentifier = new PublicationIdentifier();
            String classname = attributes.getValue("classname");
            PublicationIdentifierSchemeNameEnum publicationIdentifierSchemeNameEnum;
            try {
                publicationIdentifierSchemeNameEnum = PublicationIdentifierSchemeNameEnum.fromValue(classname);
            } catch (IllegalArgumentException ex) {
                publicationIdentifierSchemeNameEnum = PublicationIdentifierSchemeNameEnum.OTHER;
            }
            publicationIdentifier.setPublicationIdentifierSchemeName(publicationIdentifierSchemeNameEnum);

            if (publicationIdentifierSchemeNameEnum == PublicationIdentifierSchemeNameEnum.OTHER) {
                String schemeid = attributes.getValue("schemeid");
                if (!schemeid.isEmpty())
                    publicationIdentifier.setSchemeURI("http://api.openaire.eu/vocabularies/" + schemeid + "/" + classname);
                else {
                    publicationIdentifier.setSchemeURI("http://api.openaire.eu/vocabularies/dnet:pid_types/UNKNOWN");
                }
            }
        }
        /*
            MetadataInfo
         */
        else if (qName.equalsIgnoreCase("dri:objIdentifier")) {
            value = "";
        }
        /*
            MetadataCreationDate
            Start of dri:dateOfCollection element
         */
        else if (qName.equalsIgnoreCase("dri:dateOfCollection")) {
            value = "";
        }
        /*
            MetadataLastDateUpdated
            Start of dri:dateOfTransformation element
         */
        else if (qName.equalsIgnoreCase("dri:dateOfTransformation")) {
            value = "";
        }
        /*
            Title element
            Title elements can be found within a <rel> element.
            Notice that OMTD title is the one NOT within a <rel> element.
         */
        else if (qName.equalsIgnoreCase("title")) {
            value = "";
        }
        /*
            DocumentLanguage
         */
        else if (qName.equalsIgnoreCase("language")) {
            value = "";
            String classid = attributes.getValue("classid");
            String language = languageTypeConverter.convertCodeToLanguage(classid);
            publication.getDocumentLanguages().add(language);
        }
        /*
            DocumentDistributionInfo (preparation for accessing the downloading URL)
         */
        else if (qName.equalsIgnoreCase("webresource")) {
            value = "";
            documentDistributionInfo = new DocumentDistributionInfo();
        } else if (qName.equalsIgnoreCase("url")) {
            documentDistributionInfo = new DocumentDistributionInfo();
            value = "";
        }
        /*
            Subjects and Keywords
         */
        else if (qName.equalsIgnoreCase("subject")) {
            value = "";
            String classid = attributes.getValue("classid");
            String schemeid = attributes.getValue("schemeid");

            if (classid.equalsIgnoreCase("keyword")) {
                hasKeyword = true;
            } else if (schemeid.equalsIgnoreCase("dnet:subject_classification_typologies")) {
                hasSubject = true;
            }
        }
        /*
            Abstract
         */
        else if (qName.equalsIgnoreCase("description")) {
            value = "";
            description = "";
            hasAbstract = true;
        }
        /*
            License is still under investigation
            As it looks like, license is used within journals.
            This is an element not yet processed
         */
        else if (qName.equalsIgnoreCase("bestlicense")) {
            String classid = attributes.getValue("classid");
            String classname = attributes.getValue("classname");

            LicenceInfo licenceInfo = new LicenceInfo();
            licenceInfo.setLicence(LicenceEnum.NON_STANDARD_LICENCE_TERMS);
            licenceInfo.setNonStandardLicenceTermsURL("http://api.openaire.eu/vocabularies/dnet:access_modes#" + classid);
            rightsInfo.setRightsStatement(rightsStmtNameConverter.convertToOMTD(classname));
            rightsInfo.getLicenceInfos().add(licenceInfo);
        }
        /*
            Journal
         */
        else if (qName.equalsIgnoreCase("journal")) {
            value = "";
            String eissn = attributes.getValue("eissn");
            String issn = attributes.getValue("issn");
            String lissn = attributes.getValue("lissn");

            if (publication.getJournal() == null) {
                publication.setJournal(new JournalInfo());
            }

            if (eissn != null && !eissn.isEmpty()) {
                JournalIdentifier journalIdentifier = new JournalIdentifier();
                journalIdentifier.setValue(eissn);
                publication.getJournal().getIdentifiers().add(journalIdentifier);
            }
            if (issn != null && !issn.isEmpty()) {
                JournalIdentifier journalIdentifier = new JournalIdentifier();
                journalIdentifier.setValue(issn);
                publication.getJournal().getIdentifiers().add(journalIdentifier);
            }
            if (lissn != null && !lissn.isEmpty()) {
                JournalIdentifier journalIdentifier = new JournalIdentifier();
                journalIdentifier.setValue(lissn);
                publication.getJournal().getIdentifiers().add(journalIdentifier);
            }
        }
        /*
            contributor
         */
        else if (qName.equalsIgnoreCase("contributor")) {
            value = "";
        }
        /*
            indexinfo
         */
        else if (qName.equalsIgnoreCase("indexinfo")) {
            documentMetadataRecord.getDocument().getPublication().setDocumentType(DocumentTypeEnum.WITH_FULL_TEXT);
            dataFormatInfo = new DataFormatInfo();
            value = "";
            hasIndexInfo = true;
        } else if (qName.equalsIgnoreCase("hashkey")) {
            value = "";
        } else if (qName.equalsIgnoreCase("mimetype")) {
            value = "";
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        try {
        /*
            End of DocumentMetadataRecord element (end of current publication)
            For the time being it prints the xml as a string
         */
            if (qName.equalsIgnoreCase("result")) {
                StringWriter sw = new StringWriter();
                try {
                    jaxbMarshaller.marshal(documentMetadataRecord, sw);
                    OMTDPublication = sw.toString() + "\n";
                } catch (JAXBException e) {
                    log.error("PublicationResultHandler.endElement@result", e);
                }
            }
        /*
            MetadataInfo
         */
            else if (qName.equalsIgnoreCase("dri:objIdentifier")) {
                documentMetadataRecord.getMetadataHeaderInfo().getMetadataRecordIdentifier().setValue(value);
                value = "";
            }
        /*
            MetadataCreationDate
            End of dri:dateOfCollection element
         */
            else if (qName.equalsIgnoreCase("dri:dateOfCollection")) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                if (!value.trim().isEmpty()) {
                    try {
                        java.util.Date date = simpleDateFormat.parse(value);
                        GregorianCalendar gregorianCalendar = new GregorianCalendar();
                        gregorianCalendar.setTime(date);
                        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                        documentMetadataRecord.getMetadataHeaderInfo().setMetadataCreationDate(xmlGregorianCalendar);
                    } catch (ParseException | DatatypeConfigurationException e) {
                        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        java.util.Date date;
                        try {
                            date = simpleDateFormat.parse(value);

                            GregorianCalendar gregorianCalendar = new GregorianCalendar();
                            gregorianCalendar.setTime(date);
                            XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                            documentMetadataRecord.getMetadataHeaderInfo().setMetadataCreationDate(xmlGregorianCalendar);
                        } catch (ParseException | DatatypeConfigurationException e1) {
                            log.error("PublicationResultHandler.dateOfCollection", e1);
                        }
                    }
                }
            }
        /*
            MetadataLastDateUpdated
            End of dri:dateOfTransformation element
         */
            else if (qName.equalsIgnoreCase("dri:dateOfTransformation")) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                if (!value.trim().isEmpty()) {
                    try {
                        java.util.Date date = simpleDateFormat.parse(value);
                        GregorianCalendar gregorianCalendar = new GregorianCalendar();
                        gregorianCalendar.setTime(date);
                        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                        documentMetadataRecord.getMetadataHeaderInfo().setMetadataLastDateUpdated(xmlGregorianCalendar);
                    } catch (ParseException | DatatypeConfigurationException e) {
                        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            java.util.Date date = simpleDateFormat.parse(value);
                            GregorianCalendar gregorianCalendar = new GregorianCalendar();
                            gregorianCalendar.setTime(date);
                            XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                            documentMetadataRecord.getMetadataHeaderInfo().setMetadataLastDateUpdated(xmlGregorianCalendar);
                        } catch (ParseException | DatatypeConfigurationException e1) {
                            log.error("PublicationResultHandler.dateOfTransformation", e1);
                        }
                    }
                }
            }
        /*
            End of title element
            Title elements can be found within a <rel> element.
            Notice that OMTD title is the one NOT within a <rel> element.
         */
            else if (qName.equalsIgnoreCase("title")) {
                // If title is within a <rel> element, hasRelation should be true, otherwise it is false
                if (!hasRelation) {
                    title.setValue(value);
                    publication.getTitles().add(title);
                    value = "";
                }
            } else if (qName.equalsIgnoreCase("pid")) {
                publicationIdentifier.setValue(value);
                publication.getIdentifiers().add(publicationIdentifier);
                value = "";
            }
        /*
            PersonIdentifier
            End of <to> of the <rel> element
         */
            else if (qName.equalsIgnoreCase("to")) {
                if (hasAuthor) {
                    PersonIdentifier personIdentifier = new PersonIdentifier();
                    personIdentifier.setPersonIdentifierSchemeName(PersonIdentifierSchemeNameEnum.OTHER);
                    personIdentifier.setValue(value);
                    author.getPersonIdentifiers().add(personIdentifier);
                    value = "";
                }
            }
        /*
            PersonName
            End of fullname
         */
            else if (qName.equalsIgnoreCase("fullname")) {

                if (author == null) {
                    author = new PersonInfo();
                }

                String[] fullname = value.split(",");

                if (fullname.length == 2) {
                    author.setSurname(fullname[0].trim());
                    author.setGivenName(fullname[1].trim());
                } else {
                    author.setSurname(value);
                }
                value = "";
            }
        /*
            Author
            End of rel element
         */
            else if (qName.equalsIgnoreCase("rel")) {
                hasRelation = false;
                if (hasAuthor) {
                    hasAuthor = false;
                    publication.getAuthors().add(author);
                }
            }
        /*
            PublicationDate
            End of dateofacceptance element
         */
            else if (qName.equalsIgnoreCase("dateofacceptance")) {
                // In case dateofacceptance element is within a rel element, hasRelation should be true, otherwise it is false
                if (!hasRelation) {
                    Date date = new Date();
                    String[] dateOfAcceptance = value.split("-");

                    try {
                        switch (dateOfAcceptance.length) {
                            case 1:
                                if (!dateOfAcceptance[0].trim().isEmpty())
                                    date.setYear(Integer.valueOf(dateOfAcceptance[0].trim()));
                                break;
                            case 2:
                                date.setYear(Integer.valueOf(dateOfAcceptance[0]));
                                date.setMonth(Integer.valueOf(dateOfAcceptance[1]));
                                break;
                            case 3:
                                date.setYear(Integer.valueOf(dateOfAcceptance[0]));
                                date.setMonth(Integer.valueOf(dateOfAcceptance[1]));
                                date.setDay(Integer.valueOf(dateOfAcceptance[2]));
                                break;
                            default:
                                break;
                        }

                        publication.setPublicationDate(date);
                    } catch (NumberFormatException ex) {
                        log.error("Error parsing string:" + value);
                    }
                    value = "";
                }
            }
        /*
            Publisher
            End of publisher element
            publisher refers to original publisher (element publisher)
            or to the collectedfrom publisher who actually gives the publicationIdentifier?
         */
            else if (qName.equalsIgnoreCase("publisher")) {
                if (!hasRelation && !value.trim().isEmpty()) {
//                ActorInfo actorInfo = new ActorInfo();
                    OrganizationInfo relatedOrganization = new OrganizationInfo();
                    OrganizationName organizationName = new OrganizationName();
                    organizationName.setValue(value.trim());

                    relatedOrganization.getOrganizationNames().add(organizationName);
//                actorInfo.setRelatedOrganization(relatedOrganization);
                    publication.setPublisher(relatedOrganization);
                    value = "";
                }
            }
        /*
            DownloadURL
            End of url element
         */
            else if (qName.equalsIgnoreCase("url")) {
                if (!value.trim().isEmpty()) {
                    documentDistributionInfo.setDistributionLocation(value);
                    value = "";
                }
            }
        /*
            DistributionMedium
            End of webresource element
         */
            else if (qName.equalsIgnoreCase("webresource")) {
                publication.setRightsInfo(rightsInfo);

                publication.getDistributions().add(documentDistributionInfo);
            }
        /*
            Subjects and Keywords
            End of subject element
         */
            else if (qName.equalsIgnoreCase("subject")) {
                if (hasKeyword) {
                    publication.getKeywords().add(value);
                    hasKeyword = false;
                } else if (hasSubject) {
                    Subject subject = new Subject();
                    subject.setValue(value);
                    subject.setClassificationSchemeName(ClassificationSchemeName.OTHER);
                    publication.getSubjects().add(subject);
                    hasSubject = false;
                }
            }
        /*
            Abstract
            End of description
         */
            else if (qName.equalsIgnoreCase("description")) {
                if (hasAbstract) {
                    Abstract documentAbstract = new Abstract();
                    documentAbstract.setValue(description);
                    publication.getAbstracts().add(documentAbstract);
                    hasAbstract = false;
                    description = "";
                }
            }
        /*
            Contributor
            End of contributor

            Contributors are either RelatedPersons or RelatedOrganizations.
            It is not clear when the first or the latter is used, so I am using the second as default.
         */
            else if (qName.equalsIgnoreCase("contributor")) {
                if (!value.trim().isEmpty()) {
                    ActorInfo contributor = new ActorInfo();
                    OrganizationInfo relatedOrganization = new OrganizationInfo();
                    OrganizationName organizationName = new OrganizationName();
                    organizationName.setValue(value);

                    relatedOrganization.getOrganizationNames().add(organizationName);
                    contributor.setRelatedOrganization(relatedOrganization);

                    publication.getContributors().add(contributor);
                    value = "";
                }
            }
        /*
            Journal
         */
            else if (qName.equalsIgnoreCase("journal")) {
                if (!value.trim().isEmpty()) {
                    if (publication.getJournal() == null) publication.setJournal(relatedJournal);
                    JournalTitle journalTitle = new JournalTitle();
                    journalTitle.setValue(value);
                    publication.getJournal().getJournalTitles().add(journalTitle);
                    value = "";
                }
            }
        /*
         fulltext
         */
            else if (qName.equalsIgnoreCase("fulltext")) {
                if (!value.trim().isEmpty()) {
                    if (publication.getJournal() == null) publication.setJournal(relatedJournal);
                    JournalTitle journalTitle = new JournalTitle();
                    journalTitle.setValue(value);
                    publication.getJournal().getJournalTitles().add(journalTitle);
                    value = "";
                }
            }
        /*
            indexinfo
            url element is described in another case above
         */
            else if (qName.equalsIgnoreCase("indexinfo")) {
                hasIndexInfo = false;
                documentDistributionInfo.setDataFormatInfo(dataFormatInfo);
            } else if (qName.equalsIgnoreCase("hashkey")) {
                if (hasIndexInfo) {
                    documentDistributionInfo.setHashkey(value);
                }
            } else if (qName.equalsIgnoreCase("mimetype")) {
                if (hasIndexInfo) {
                    DataFormatType mimeType;
                    try {
                        mimeType = DataFormatType.fromValue(value);
                    } catch (IllegalArgumentException e) {
                        mimeType = null;
                    }

                    if (dataFormatInfo.getDataFormat() == null && mimeType != null)
                        dataFormatInfo.setDataFormat(mimeType);
                }
            } else if (qName.equalsIgnoreCase("format")) {
                if (hasIndexInfo) {
                    DataFormatType mimeType;
                    try {
                        mimeType = DataFormatType.fromValue(value);

                    } catch (IllegalArgumentException e) {
                        mimeType = null;
                    }

                    if (dataFormatInfo.getDataFormat() != null) {
                        if (mimeType != null && mimeType != dataFormatInfo.getDataFormat()) {
                            dataFormatInfo.setDataFormat(mimeType);
                        }
                    } else {
                        if (mimeType != null)
                            dataFormatInfo.setDataFormat(mimeType);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Something went wrong", e);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        value = new String(ch, start, length);

        // Because in some abstracts (description elements) are used notations with tagged elements
        // description is produced by concatenating each value until hasAbstract is false.
        if (hasAbstract) {
            description += value;
        }
    }

    String getOMTDPublication() {
        return OMTDPublication;
    }
}
