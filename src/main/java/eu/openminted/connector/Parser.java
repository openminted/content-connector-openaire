package eu.openminted.connector;

import eu.openminted.openaire.ResultRelationsEnum;
import eu.openminted.registry.domain.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class Parser {
    private int count;

    /***
     * Reads the URL address to parse the publications retrieved from the OpenAire search API
     * @param address the URL to the OpenAire search API
     * @return the number of documents processed
     */
    public int execute(String address) {
        try {
            count = 0;
            URL url = new URL(address);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
                DocumentMetadataRecord documentMetadataRecord;
                Document document;
                DocumentInfo publication;
                MetadataHeaderInfo metadataHeaderInfo;
                PublicationIdentifierSchemeNameEnum publicationIdentifierSchemeNameEnum;
                PublicationIdentifier publicationIdentifier;
                Title title;
                RelatedPerson author;
                Language language;
                DocumentDistributionInfo documentDistributionInfo;
                Subject subject;
                Abstract documentAbstract;

                String description = "";

                String value = "";
                boolean hasAuthor = false;
                boolean hasRelation = false;
                boolean hasKeyword = false;
                boolean hasSubject = false;
                boolean hasAbstract = false;


                public void startElement(String uri, String localName, String qName,
                                         Attributes attributes) throws SAXException {

                    // New publication - create new DocumentMetadataRecord
                    // and initialize its elements
                    if (qName.equalsIgnoreCase("result")) {
                        documentMetadataRecord = new DocumentMetadataRecord();
                        document = new Document();
                        publication = new DocumentInfo();
                        // set by default the document type to abstract until we find a solution to this
                        publication.setDocumentType(DocumentTypeEnum.ABSTRACT);
                        document.setPublication(publication);
                        metadataHeaderInfo = new MetadataHeaderInfo();
                        documentMetadataRecord.setMetadataHeaderInfo(metadataHeaderInfo);
                        documentMetadataRecord.setDocument(document);
                    }
                    //
                    // title
                    //
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
                    //
                    // Authors
                    //
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
                    //
                    // pulicationType
                    //
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

                    } else if (qName.equalsIgnoreCase("pid")) {
                        String classname = attributes.getValue("classname");
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

                    } else if (qName.equalsIgnoreCase("licence")) {

                    }
                    //
                    // DocumentLanguage
                    //
                    else if (qName.equalsIgnoreCase("language")) {
                        String classid = attributes.getValue("classid");
                        String classname = attributes.getValue("classname");
                        language = new Language();
                        language.setLanguageTag(classname);
                        language.setLanguageId(classid);
                        //TODO: OpenAire language as retrieved from classId should be converted to OMTD language format
                        publication.getDocumentLanguages().add(language);
                    }
                    //
                    // DocumentDistributionInfo (preparation for accessing the downloading URL
                    //
                    else if (qName.equalsIgnoreCase("webresource")) {
                        documentDistributionInfo = new DocumentDistributionInfo();
                    }
                    //
                    // Subjects and Keywords
                    //
                    else if (qName.equalsIgnoreCase("subject")) {
                        String classid = attributes.getValue("classid");
                        String schemeid = attributes.getValue("schemeid");

                        if (classid.equalsIgnoreCase("keyword")) {
                            hasKeyword = true;
                        } else if (schemeid.equalsIgnoreCase("dnet:subject_classification_typologies")) {
                            hasSubject = true;
                        }
                    }
                    //
                    // Abstract
                    //
                    else if (qName.equalsIgnoreCase("description")) {
                        hasAbstract = true;
                    }
                    //
                    // licence
                    //
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

                public void endElement(String uri, String localName,
                                       String qName) throws SAXException {

                    //
                    // end of documentMetadataRecord element (end of current publication)
                    //
                    if (qName.equalsIgnoreCase("result")) {
                        try {
                            JAXBContext jaxbContext = JAXBContext.newInstance(DocumentMetadataRecord.class);
                            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                            StringWriter sw = new StringWriter();
                            jaxbMarshaller.marshal(documentMetadataRecord, sw);
                            String xmlString = sw.toString();
                            System.out.println(xmlString);
                            count++;
                        }
                        catch (JAXBException e) {
                            e.printStackTrace();
                        }
                    }
                    //
                    // MetadataInfo
                    //
                    else if (qName.equalsIgnoreCase("dri:objIdentifier")) {
                        MetadataIdentifier metadataIdentifier = new MetadataIdentifier();
                        metadataIdentifier.setValue(value);
                        metadataHeaderInfo.setMetadataRecordIdentifier(metadataIdentifier);

                    }
                    //
                    // end of dri:dateOfCollection element
                    //
                    else if (qName.equalsIgnoreCase("dri:dateOfCollection")) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        try {
                            java.util.Date date = simpleDateFormat.parse(value);
                            GregorianCalendar gregorianCalendar = new GregorianCalendar();
                            gregorianCalendar.setTime(date);
                            XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                            metadataHeaderInfo.setMetadataCreationDate(xmlGregorianCalendar);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (DatatypeConfigurationException e) {
                            e.printStackTrace();
                        }
                    }
                    //
                    // end of dri:dateOfTransformation element
                    //
                    else if(qName.equalsIgnoreCase("dri:dateOfTransformation")){
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        try {
                            java.util.Date date = simpleDateFormat.parse(value);
                            GregorianCalendar gregorianCalendar = new GregorianCalendar();
                            gregorianCalendar.setTime(date);
                            XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                            metadataHeaderInfo.setMetadataLastDateUpdated(xmlGregorianCalendar);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (DatatypeConfigurationException e) {
                            e.printStackTrace();
                        }
                    }
                    //
                    // End of title element
                    //
                    else if (qName.equalsIgnoreCase("title")) {
                        // In case title element is within a <rel> element, hasRelation should be true.
                        // Otherwise it should be false
                        if (!hasRelation) {
                            title.setValue(value);
                            publication.getTitles().add(title);
                        }
                    }
                    //
                    // end of to element
                    //
                    else if (qName.equalsIgnoreCase("to")) {
                        if (hasAuthor) {
                            PersonIdentifier personIdentifier = new PersonIdentifier();
                            personIdentifier.setValue(value);
                            author.getPersonIdentifiers().add(personIdentifier);
                        }
                    }
                    //
                    // End of fullname element
                    //
                    else if (qName.equalsIgnoreCase("fullname")) {
                        PersonName personName = new PersonName();
                        personName.setValue(value);
                        author.getPersonNames().add(personName);
                    }
                    //
                    // End of rel element
                    //
                    else if (qName.equalsIgnoreCase("rel")) {
                        hasRelation = false;
                        if (hasAuthor) {
                            hasAuthor = false;
                            publication.getAuthors().add(author);
                        }
                    }
                    //
                    // End of dateofacceptance element
                    //
                    else if (qName.equalsIgnoreCase("dateofacceptance")) {
                        // In case dateofacceptance element is within a rel element, hasRelation should be true.
                        // Otherwise it should be false
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
                    //
                    // End of publisher element
                    //
                    else if (qName.equalsIgnoreCase("publisher")) {
                        // TODO: publisher refers to original publisher (element publisher)
                        // or to the collectedfrom publisher who actually gives the publicationIdentifier?

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
                    //
                    // end of webresource element
                    //
                    else if (qName.equalsIgnoreCase("url")) {
                        if (!value.trim().isEmpty()) {
                            documentDistributionInfo.getDistributionMediums().add(DistributionMediumEnum.DOWNLOADABLE);
                            documentDistributionInfo.getDownloadURLs().add(value);
                        }
                    }
                    //
                    // end of webresource element
                    //
                    else if (qName.equalsIgnoreCase("webresource")) {
                        // just in case there is none download url
                        if (documentDistributionInfo.getDownloadURLs().size() < 1)
                            documentDistributionInfo.getDistributionMediums().add(DistributionMediumEnum.OTHER);
                        publication.getDistributions().add(documentDistributionInfo);
                    }
                    //
                    // end of subject element
                    //
                    else if (qName.equalsIgnoreCase("subject")) {
                        if (hasKeyword) {
                            publication.getKeywords().add(value);
                            hasKeyword = false;
                        } else if (hasSubject) {
                            subject = new Subject();
                            subject.setValue(value);
                            publication.getSubjects().add(subject);
                            hasSubject = false;
                        }
                    }
                    //
                    // end of subject description
                    //
                    else if (qName.equalsIgnoreCase("description")) {
                        if (hasAbstract) {
                            documentAbstract = new Abstract();
                            documentAbstract.setValue(description);
                            publication.getAbstracts().add(documentAbstract);
                            hasAbstract = false;
                            description = "";
                        }
                    }
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

                public void characters(char ch[], int start, int length) throws SAXException {
                    value = new String(ch, start, length);
                    if (hasAbstract) {
                        description += value;
                    }
                }
            };

            saxParser.parse(new InputSource(url.openStream()), handler);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;
    }
}
