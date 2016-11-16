package eu.openminted.content.openaire;

import eu.openminted.registry.domain.*;
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

public class PublicationResultHandler extends DefaultHandler {
    private DocumentMetadataRecord documentMetadataRecord;
    private DocumentInfo publication;
    private MetadataHeaderInfo metadataHeaderInfo;
    private PublicationIdentifier publicationIdentifier;
    private Title title;
    private RelatedPerson author;
    private DocumentDistributionInfo documentDistributionInfo;
    private String description = "";
    private String value = "";
    private boolean hasAuthor = false;
    private boolean hasRelation = false;
    private boolean hasKeyword = false;
    private boolean hasSubject = false;
    private boolean hasAbstract = false;
    private List<DocumentMetadataRecord> OMTDPublications;
    private Marshaller jaxbMarshaller;

    public PublicationResultHandler() throws JAXBException {
        OMTDPublications = new ArrayList<>();
        JAXBContext jaxbContext = JAXBContext.newInstance(DocumentMetadataRecord.class);
        jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        /*
            DocumentMetadataRecord
         */
        if (qName.equalsIgnoreCase("result")) {
            documentMetadataRecord = new DocumentMetadataRecord();
            Document document = new Document();
            publication = new DocumentInfo();
            document.setPublication(publication);
            metadataHeaderInfo = new MetadataHeaderInfo();
            documentMetadataRecord.setMetadataHeaderInfo(metadataHeaderInfo);
            documentMetadataRecord.setDocument(document);
            /*
                Set by default the document type to abstract until we find a solution to this
             */
            publication.setDocumentType(DocumentTypeEnum.ABSTRACT);
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
        }
        else if (qName.equalsIgnoreCase("to")) {
            String classAttribute = attributes.getValue("class");
            ResultRelationsEnum resultRelationsEnum = ResultRelationsEnum.fromValue(classAttribute);

            switch (resultRelationsEnum) {
                case HAS_AUTHOR:
                    hasAuthor = true;
                    author = new RelatedPerson();
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
            PublicationTypeEnum publicationTypeEnum;
            try {
                publicationTypeEnum = PublicationTypeEnum.fromValue(classname);
            } catch (IllegalArgumentException ex) {
                publicationTypeEnum = PublicationTypeEnum.OTHER;
                String schemeid = attributes.getValue("schemeid");
                if (!schemeid.isEmpty())
                    publicationIdentifier.setSchemeURI("http://api.openaire.eu/vocabularies/" + schemeid + "/" + classname);
                else {
                    publicationIdentifier.setSchemeURI("http://api.openaire.eu/vocabularies/dnet:publication_resource/UNKNOWN");
                }
            }
            publication.setPublicationType(publicationTypeEnum);
        } else if (qName.equalsIgnoreCase("collectedfrom")) {
            String id = attributes.getValue("id");
            publicationIdentifier = new PublicationIdentifier();
            publicationIdentifier.setValue(id);

        }
        /*
            PublicationIdentifierSchemeName & schemeURI (if necessary)
         */
        else if (qName.equalsIgnoreCase("pid")) {
            String classname = attributes.getValue("classname");
            PublicationIdentifierSchemeNameEnum publicationIdentifierSchemeNameEnum;
            try {
                publicationIdentifierSchemeNameEnum = PublicationIdentifierSchemeNameEnum.fromValue(classname);
            } catch (IllegalArgumentException ex) {
                publicationIdentifierSchemeNameEnum = PublicationIdentifierSchemeNameEnum.OTHER;
            }
            publicationIdentifier.setPublicationIdentifierSchemeName(publicationIdentifierSchemeNameEnum);

            if (publicationIdentifierSchemeNameEnum != PublicationIdentifierSchemeNameEnum.OTHER) {
                publicationIdentifier.setSchemeURI("");
            } else {
                String schemeid = attributes.getValue("schemeid");
                if (!schemeid.isEmpty())
                    publicationIdentifier.setSchemeURI("http://api.openaire.eu/vocabularies/" + schemeid + "/" + classname);
                else {
                    publicationIdentifier.setSchemeURI("http://api.openaire.eu/vocabularies/dnet:pid_types/UNKNOWN");
                }
            }

            publication.getIdentifiers().add(publicationIdentifier);

        }
        /*
            DocumentLanguage
            OpenAire is using a 3 letters coding for the language id, though OMTD is using a 2 letters one.
            TODO: Convert OpenAire language coding to OMTD coding
         */
        else if (qName.equalsIgnoreCase("language")) {
            String classid = attributes.getValue("classid");
            String classname = attributes.getValue("classname");
            Language language = new Language();
            language.setLanguageTag(classname);
            language.setLanguageId(classid);
            publication.getDocumentLanguages().add(language);
        }
        /*
            DocumentDistributionInfo (preparation for accessing the downloading URL)
         */
        else if (qName.equalsIgnoreCase("webresource")) {
            documentDistributionInfo = new DocumentDistributionInfo();
        }
        /*
            Subjects and Keywords
         */
        else if (qName.equalsIgnoreCase("subject")) {
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
            hasAbstract = true;
        }
        /*
            Licence is still under investigation
            As it looks like, licence is used within journals.
            This is an element not yet processed
         */
        else if (qName.equalsIgnoreCase("bestlicence")) {
            String classid = attributes.getValue("classid");
            String classname = attributes.getValue("classname");
            RightsInfo rightsInfo = new RightsInfo();
            LicenceInfo licenceInfo = new LicenceInfo();
            licenceInfo.setLicence(LicenceEnum.NON_STANDARD_LICENCE_TERMS);
            licenceInfo.setNonStandardLicenceTermsURL(classid);

            rightsInfo.getLicenceInfos().add(licenceInfo);
            JournalInfo journalInfo = new JournalInfo();
            journalInfo.getRights().add(rightsInfo);

        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        /*
            End of DocumentMetadataRecord element (end of current publication)
            For the time being it prints the xml as a string
         */
        if (qName.equalsIgnoreCase("result")) {
            OMTDPublications.add(documentMetadataRecord);
        }
        /*
            MetadataInfo
         */
        else if (qName.equalsIgnoreCase("dri:objIdentifier")) {
            MetadataIdentifier metadataIdentifier = new MetadataIdentifier();
            metadataIdentifier.setValue(value);
            metadataHeaderInfo.setMetadataRecordIdentifier(metadataIdentifier);

        }
        /*
            MetadataCreationDate
            End of dri:dateOfCollection element
         */
        else if (qName.equalsIgnoreCase("dri:dateOfCollection")) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                java.util.Date date = simpleDateFormat.parse(value);
                GregorianCalendar gregorianCalendar = new GregorianCalendar();
                gregorianCalendar.setTime(date);
                XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                metadataHeaderInfo.setMetadataCreationDate(xmlGregorianCalendar);
            } catch (ParseException | DatatypeConfigurationException e) {
                e.printStackTrace();
            }
        }
        /*
            MetadataLastDateUpdated
            End of dri:dateOfTransformation element
         */
        else if(qName.equalsIgnoreCase("dri:dateOfTransformation")){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                java.util.Date date = simpleDateFormat.parse(value);
                GregorianCalendar gregorianCalendar = new GregorianCalendar();
                gregorianCalendar.setTime(date);
                XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                metadataHeaderInfo.setMetadataLastDateUpdated(xmlGregorianCalendar);
            } catch (ParseException | DatatypeConfigurationException e) {
                e.printStackTrace();
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
            }
        }
        /*
            PersonIdentifier
            End of <to> of the <rel> element
         */
        else if (qName.equalsIgnoreCase("to")) {
            if (hasAuthor) {
                PersonIdentifier personIdentifier = new PersonIdentifier();
                personIdentifier.setValue(value);
                author.getPersonIdentifiers().add(personIdentifier);
            }
        }
        /*
            PersonName
            End of fullname
         */
        else if (qName.equalsIgnoreCase("fullname")) {
            PersonName personName = new PersonName();
            personName.setValue(value);
            author.getPersonNames().add(personName);
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
            }
        }
        /*
            Publisher
            End of publisher element
            TODO: publisher refers to original publisher (element publisher)
            TODO: or to the collectedfrom publisher who actually gives the publicationIdentifier?
         */
        else if (qName.equalsIgnoreCase("publisher")) {
            if (!hasRelation) {
                ActorInfo actorInfo = new ActorInfo();
                RelatedOrganization relatedOrganization = new RelatedOrganization();
                OrganizationName organizationName = new OrganizationName();
                organizationName.setValue(value);

                relatedOrganization.getOrganizationNames().add(organizationName);
                actorInfo.setRelatedOrganization(relatedOrganization);
                publication.setPublisher(actorInfo);
            }
        }
        /*
            DownloadURL
            End of url element
         */
        else if (qName.equalsIgnoreCase("url")) {
            if (!value.trim().isEmpty()) {
                documentDistributionInfo.getDistributionMediums().add(DistributionMediumEnum.DOWNLOADABLE);
                documentDistributionInfo.getDownloadURLs().add(value);
            }
        }
        /*
            DistributionMedium
            End of webresource element
         */
        else if (qName.equalsIgnoreCase("webresource")) {
            // just in case there is none download url
            if (documentDistributionInfo.getDownloadURLs().size() < 1)
                documentDistributionInfo.getDistributionMediums().add(DistributionMediumEnum.OTHER);
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
                Contributor contributor = new Contributor();
                RelatedOrganization relatedOrganization = new RelatedOrganization();
                OrganizationName organizationName = new OrganizationName();
                organizationName.setValue(value);

                relatedOrganization.getOrganizationNames().add(organizationName);
                contributor.setRelatedOrganization(relatedOrganization);

                publication.getContributors().add(contributor);
            }
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

    public List<DocumentMetadataRecord> getOMTDPublications() {
        return OMTDPublications;
    }
}
