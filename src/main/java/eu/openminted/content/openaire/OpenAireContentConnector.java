package eu.openminted.content.openaire;

import eu.openminted.content.connector.ContentConnector;
import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.SearchResult;
import eu.openminted.content.connector.utils.SearchExtensions;
import eu.openminted.content.connector.utils.faceting.OMTDFacetEnum;
import eu.openminted.content.connector.utils.faceting.OMTDFacetLabels;
import eu.openminted.content.openaire.converters.DocumentTypeConverter;
import eu.openminted.content.openaire.converters.LanguageTypeConverter;
import eu.openminted.content.openaire.converters.PublicationTypeConverter;
import eu.openminted.content.openaire.converters.RightsStmtNameConverter;
import eu.openminted.registry.core.domain.Facet;
import eu.openminted.registry.core.domain.Value;
import eu.openminted.registry.domain.DocumentTypeEnum;
import eu.openminted.registry.domain.PublicationTypeEnum;
import eu.openminted.registry.domain.RightsStatementEnum;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.net.ssl.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the ContentConnector interface for the OpenAire
 */
@SuppressWarnings("WeakerAccess")
@Component
public class OpenAireContentConnector implements ContentConnector {
    private static Logger log = Logger.getLogger(OpenAireContentConnector.class.getName());
    private String schemaAddress;

    @org.springframework.beans.factory.annotation.Value("${services.openaire.getProfile}")
    private String getProfileUrl;

    @org.springframework.beans.factory.annotation.Value("${solr.hosts}")
    private String hosts;

    @org.springframework.beans.factory.annotation.Value("${solr.query.output.field}")
    private String queryOutputField;

    @org.springframework.beans.factory.annotation.Value("${solr.client.type}")
    private String solrClientType;

    @org.springframework.beans.factory.annotation.Value("${content.limit:0}")
    private Integer contentLimit;

    @org.springframework.beans.factory.annotation.Value("${solr.default.collection}")
    private String defaultCollection;

    @org.springframework.beans.factory.annotation.Value("${solr.update.default.collection:false}")
    private boolean updateCollection;

    @Autowired
    private PublicationTypeConverter publicationTypeConverter;

    @Autowired
    private OMTDFacetLabels omtdFacetLabels;

    @Autowired
    private RightsStmtNameConverter rightsStmtNameConverter;

    @Autowired
    private LanguageTypeConverter languageTypeConverter;

    @Autowired
    private DocumentTypeConverter documentTypeConverter;

    @Autowired
    private OpenAIREFacetingMapper openAIREFacetingMapper;

    public OpenAireContentConnector() {
    }

    /**
     * Search method for browsing metadata
     *
     * @param query the query as inserted in content connector service
     * @return SearchResult with metadata and facets
     */
    @Override
    public SearchResult search(Query query) {

        Query tmpQuery = new Query(query.getKeyword(), query.getParams(), query.getFacets(), query.getFrom(), query.getTo());
        if (tmpQuery.getKeyword() == null || tmpQuery.getKeyword().isEmpty()) {
            tmpQuery.setKeyword("*:*");
        }

        SearchResult searchResult = new SearchResult();
        try (OpenAireSolrClient openAireSolrClient = new OpenAireSolrClient(solrClientType, hosts, defaultCollection)) {

            Parser parser = new Parser();
            buildParams(tmpQuery);
            buildFacets(tmpQuery);
            buildFields(tmpQuery);

            QueryResponse response = openAireSolrClient.query(tmpQuery);
            searchResult.setPublications(new ArrayList<>());

            if (response.getResults() != null) {
                searchResult.setFrom((int) response.getResults().getStart());
                searchResult.setTo((int) response.getResults().getStart() + response.getResults().size());
                searchResult.setTotalHits((int) response.getResults().getNumFound());

                for (SolrDocument document : response.getResults()) {
                    // TODO: The getFieldName to get the result should be given as input
                    // There may be more than one fields, yet we care only for the result
                    // It would be nice, if this field is the only field needed, to be set once
                    // from a properties file.

                    // TODO: xml validation for the initial queries is needed and yet the oaf xsd has issues
                    // leaving xml validation for as feature in future commit

                    String xml = document.getFieldValue(queryOutputField).toString().replaceAll("\\[|\\]", "");
                    xml = xml.trim();
                    parser.parse(new InputSource(new StringReader(xml)));
                    searchResult.getPublications().add(parser.getOMTDPublication());
                }
            } else {
                searchResult.setFrom(query.getFrom());
                searchResult.setTo(query.getFrom());
                searchResult.setTotalHits(0);
            }

            Map<String, Facet> facets = new HashMap<>();

            if (response.getFacetFields() != null) {
                for (FacetField facetField : response.getFacetFields()) {
                    Facet facet = buildFacet(facetField);
                    if (facet != null && facet.getValues() != null && facet.getValues().size() > 0) {

                        // merge values that contain the same value.value
                        facet.setValues(SearchExtensions.mergeValues(facet));

                        if (!facets.containsKey(facet.getField())) {
                            facets.put(facet.getField(), facet);
                        } else {
                            // in case another facet with the same facet.field exists,
                            // merge the two facets and add the product to the map
                            // instead of the initial facet
                            facets.put(facet.getField(), SearchExtensions.mergeFacet(facets.get(facet.getField()), facet));
                        }
                    }
                }
                Facet facet = facets.get(OMTDFacetEnum.DOCUMENT_TYPE.value());
                if (facet != null) {
                    String abstractLabel = omtdFacetLabels.getDocumentTypeLabelFromEnum(DocumentTypeEnum.WITH_ABSTRACT_ONLY);
                    Value abstractsValue = new Value();
                    abstractsValue.setValue(abstractLabel);
                    abstractsValue.setLabel(abstractLabel);
                    int count = 0;
                    for (Value value : facet.getValues()) {
                        count += value.getCount();
                    }
                    abstractsValue.setCount(searchResult.getTotalHits() - count);
                    facet.getValues().add(abstractsValue);
                }
            }

            searchResult.setFacets(new ArrayList<>(facets.values()));
        } catch (Exception e) {
            log.error("OpenAireContentConnector.search", e);
        }
        return searchResult;
    }

    /**
     * Method for downloading fullText linked documents (pdf, xml etc)
     *
     * @param s the ID of the metadata
     * @return the document in the form of InputStream
     */
    @Override
    public InputStream downloadFullText(String s) {
        InputStream inputStream = null;
        try {
            Query query = new Query();
            query.setParams(new HashMap<>());
            // use escape characters for special symbol of ':' in metadata identifier
            s = s.replaceAll("\\:", "\\\\:");

            query.getParams().put("__indexrecordidentifier", new ArrayList<>());
            query.getParams().get("__indexrecordidentifier").add(s);
            query.setKeyword("*:*");

            try (OpenAireSolrClient openAireSolrClient = new OpenAireSolrClient(solrClientType, hosts, defaultCollection)) {
                QueryResponse response = openAireSolrClient.query(query);
                if (response.getResults() != null) {
                    for (SolrDocument document : response.getResults()) {
                        String downloadUrl;

                        try {
                            if (document.getFieldValue("fulltext") != null) {
                                downloadUrl = document.getFieldValue("fulltext").toString();

                                URL url = new URL(downloadUrl);
                                URLConnection connection = url.openConnection();
                                connection.connect();
                                String contentType = connection.getContentType();
                                if (contentType.toLowerCase().contains("html")) continue;
                                inputStream = url.openStream();
                                break;
                            }
                        } catch (IOException e) {
                            log.error("downloadFullText: Error while streaming document. Proceeding to next document if any!");
                        }
                    }
                }

            }
        } catch (MalformedURLException e) {
            log.error("downloadFullText: MalformedURLException ", e);
        } catch (IOException e) {
            log.error("downloadFullText: IOException ", e);
        } catch (ParserConfigurationException e) {
            log.error("downloadFullText: ParserConfigurationException ", e);
        } catch (XPathExpressionException e) {
            log.error("downloadFullText: XPathExpressionException ", e);
        } catch (SAXException e) {
            log.error("downloadFullText: SAXException ", e);
        } catch (Exception e) {
            log.error("downloadFullText: Exception ", e);
        }
        return inputStream;
    }

    /**
     * Method for downloading metadata where the query's criteria are applicable
     *
     * @param query the query as inserted in content connector service
     * @return The metadata in the form of InputStream
     */
    @Override
    public InputStream fetchMetadata(Query query) {

        Query tmpQuery = new Query(query.getKeyword(), query.getParams(), query.getFacets(), query.getFrom(), query.getTo());

        if (tmpQuery.getKeyword() == null || tmpQuery.getKeyword().isEmpty()) {
            tmpQuery.setKeyword("*:*");
        }

        // Setting query rows up to 10 for improving speed between fetching and importing metadata
        // and not waiting log for metadata to load in memory prior their transport to the omtd service
        tmpQuery.setTo(10);

        buildParams(tmpQuery);
        buildFacets(tmpQuery);
        buildFields(tmpQuery);
        buildSort(tmpQuery);

        final Query openaireQuery = tmpQuery;
        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream();

        try {
            new Thread(() -> {
                try (OpenAireSolrClient openAireSolrClient = new OpenAireSolrClient(solrClientType, hosts, defaultCollection, contentLimit)) {
                    openAireSolrClient.fetchMetadata(openaireQuery, new OpenAireStreamingResponseCallback(outputStream, queryOutputField));
                    outputStream.flush();
                    outputStream.write("</documentMetadataRecords>\n".getBytes());
                } catch (Exception e) {
                    log.info("Fetching metadata has been interrupted. See debug for details!");
                    log.debug("OpenAireSolrClient.fetchMetadata", e);
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        log.error("OpenAireSolrClient.fetchMetadata", e);
                    }
                }
            }).start();

            outputStream.connect(inputStream);
            outputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
            outputStream.write("<documentMetadataRecords>\n".getBytes());
        } catch (IOException e) {
            log.info("Fetching metadata has been interrupted. See debug for details!");
            log.debug("OpenAireContentConnector.fetchMetadata", e);
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e1) {
                log.error("OpenAireContentConnector.fetchMetadata Inner exception!", e1);
            }
        } catch (Exception e) {
            log.error("OpenAireContentConnector.fetchMetadata Generic exception!", e);
        }

        return inputStream;
    }

    /**
     * Method that returns the name of the connector
     *
     * @return OpenAIRE
     */
    @Override
    public String getSourceName() {
        return "OpenAIRE";
    }

    /**
     * Creates an individual OMTD Facet from an OpenAIRE FacetField of a SearchResult
     *
     * @param facetField the OpenAIRE FacetField of the SearchResult
     * @return OMTD Facet
     */
    private Facet buildFacet(FacetField facetField) {

        Facet facet = null;
        String facetFieldName = facetField.getName().toLowerCase();
        if (openAIREFacetingMapper.getOmtdOpenAIREMap().containsKey(facetFieldName)) {
            facet = new Facet();
            String field = openAIREFacetingMapper.getOmtdOpenAIREMap().get(facetFieldName);
            facet.setField(field);
            OMTDFacetEnum facetEnum = OMTDFacetEnum.fromValue(field);
            facet.setLabel(omtdFacetLabels.getFacetLabelsFromEnum(facetEnum));
            List<Value> values = new ArrayList<>();
            for (FacetField.Count count : facetField.getValues()) {
                Value value = new Value();

                if (field.equalsIgnoreCase(OMTDFacetEnum.RIGHTS.value())) {
                    RightsStatementEnum rightsStatementEnum = rightsStmtNameConverter.convertToOMTD(count.getName());

                    if (rightsStatementEnum != null
                            && !omtdFacetLabels.getRightsStmtLabelFromEnum(rightsStatementEnum).isEmpty()) {
                        value.setValue(omtdFacetLabels.getRightsStmtLabelFromEnum(rightsStatementEnum));
                        value.setLabel(omtdFacetLabels.getRightsStmtLabelFromEnum(rightsStatementEnum));
                    } else {
                        value.setValue(count.getName());
                        value.setLabel(count.getName());
                    }
                } else if (field.equalsIgnoreCase(OMTDFacetEnum.PUBLICATION_TYPE.value())) {
                    PublicationTypeEnum publicationTypeEnum = publicationTypeConverter.convertToOMTD(count.getName());

                    if (!omtdFacetLabels.getPublicationTypeLabelFromEnum(publicationTypeEnum).isEmpty()) {
                        value.setValue(omtdFacetLabels.getPublicationTypeLabelFromEnum(publicationTypeEnum));
                        value.setLabel(omtdFacetLabels.getPublicationTypeLabelFromEnum(publicationTypeEnum));
                    } else {
                        value.setValue(count.getName());
                        value.setLabel(count.getName());
                    }
                } else if (field.equalsIgnoreCase(OMTDFacetEnum.DOCUMENT_LANG.value())) {

                    String language = languageTypeConverter.convertCodeToLanguage(count.getName());

                    if (language != null) {
                        value.setValue(language);
                    }
                } else if (field.equalsIgnoreCase(OMTDFacetEnum.PUBLICATION_YEAR.value())) {
                    value.setValue(count.getName().substring(0, 4));
                    value.setLabel(count.getName().substring(0, 4));

                } else if (field.equalsIgnoreCase(OMTDFacetEnum.DOCUMENT_TYPE.value())) {
                    String fulltextDocumentType = omtdFacetLabels
                            .getDocumentTypeLabelFromEnum(DocumentTypeEnum.WITH_FULL_TEXT);


                    for (Value value1 : values) {
                        if (value1.getValue().equalsIgnoreCase(fulltextDocumentType)) {
                            value = value1;
                            break;
                        }
                    }
                    value.setValue(fulltextDocumentType);
                    value.setLabel(fulltextDocumentType);

                } else {
                    value.setValue(count.getName());
                    value.setLabel(count.getName());
                }

                value.setCount(value.getCount() + (int)count.getCount());

                if (!values.contains(value))
                    values.add(value);
            }
            facet.setValues(values);
        }
        return facet;
    }

    /**
     * Converts OMTD facets to OpenAIRE facets suitable for
     *
     * @param query the query as inserted in Content-OpenAireContentConnector-Service
     */
    private void buildFacets(Query query) {

        List<String> facetsToAdd = new ArrayList<>();
        if (query.getFacets() != null && query.getFacets().size() > 0) {
            for (String facet : query.getFacets()) {
                if (facet != null && !facet.isEmpty()) {
                    if (openAIREFacetingMapper.getOmtdOpenAIREMap().containsKey(facet.toLowerCase())) {
                        facetsToAdd.add(openAIREFacetingMapper.getOmtdOpenAIREMap().get(facet.toLowerCase()));
                    }
                }
            }
            // TODO: 13/11/2017 remove the following line if tests with OpenMinTeD service are OK
//            facetsToAdd.add(openAIREFacetingMapper.getOmtdOpenAIREMap().get(OMTDFacetEnum.DOCUMENT_TYPE.value()));
            query.setFacets(facetsToAdd);
        }
    }

    /**
     * Converts OMTD parameters to OpenAIRE parameters
     *
     * @param query the query as inserted in Content-OpenAireContentConnector-Service
     */
    private void buildParams(Query query) {

        Map<String, List<String>> openAireParams = new HashMap<>();

        if (query.getParams() != null && query.getParams().size() > 0) {

            for (String key : query.getParams().keySet()) {

                if (key.equalsIgnoreCase(OMTDFacetEnum.SOURCE.value())) continue;
                if (key.equalsIgnoreCase(OMTDFacetEnum.DOCUMENT_TYPE.value())) {
                    String documentTypeKey = openAIREFacetingMapper.getOmtdOpenAIREMap().get(key.toLowerCase());
                    openAireParams.put(documentTypeKey, new ArrayList<>());
                    String abstractDocumentType = omtdFacetLabels.
                            getDocumentTypeLabelFromEnum(DocumentTypeEnum.WITH_ABSTRACT_ONLY);
                    if (query.getParams().get(key).size() > 1) continue;

                    if (query.getParams().get(key).get(0).equalsIgnoreCase(abstractDocumentType)) {
                        String existingkeyword = query.getKeyword();
                        existingkeyword += " AND NOT " + documentTypeKey + ":*";
                        query.setKeyword(existingkeyword);
                    } else {
                        documentTypeConverter.convertToOpenAIRE(openAireParams.get(documentTypeKey),
                                omtdFacetLabels.
                                        getDocumentTypeLabelFromEnum(DocumentTypeEnum.WITH_FULL_TEXT));
                    }


                } else if (key.equalsIgnoreCase(OMTDFacetEnum.PUBLICATION_TYPE.value())) {
                    String publicationKey = openAIREFacetingMapper.getOmtdOpenAIREMap().get(key.toLowerCase());
                    openAireParams.put(publicationKey, new ArrayList<>());

                    // populate openAireParams with publication types
                    for (String publicationType : query.getParams().get(key)) {
                        publicationTypeConverter.convertToOpenAIRE(openAireParams.get(publicationKey), publicationType);
                    }
                } else if (key.equalsIgnoreCase(OMTDFacetEnum.RIGHTS.value())) {
                    String rightsKey = openAIREFacetingMapper.getOmtdOpenAIREMap().get(key.toLowerCase());
                    openAireParams.put(rightsKey, new ArrayList<>());

                    // populate openAireParams with right statement types
                    for (String rightsValue : query.getParams().get(key)) {
                        rightsStmtNameConverter.convertToOpenAIRE(openAireParams.get(rightsKey), rightsValue);
                    }
                } else if (key.equalsIgnoreCase(OMTDFacetEnum.DOCUMENT_LANG.value())) {
                    String languageKey = openAIREFacetingMapper.getOmtdOpenAIREMap().get(key.toLowerCase());
                    openAireParams.put(languageKey, new ArrayList<>());

                    // populate openAireParams with language code Ids
                    for (String languageValue : query.getParams().get(key)) {
                        languageTypeConverter.convertToOpenAIRE(openAireParams.get(languageKey), languageValue.toLowerCase());
                    }
                } else {
                    if (openAIREFacetingMapper.getOmtdOpenAIREMap().containsKey(key.toLowerCase())) {
                        openAireParams.put(openAIREFacetingMapper.getOmtdOpenAIREMap().get(key.toLowerCase()), new ArrayList<>(query.getParams().get(key)));
                    }
                }
            }

            query.setParams(openAireParams);
        }
    }

    /**
     * Adds field parameter `fl` and adds necessary value of queryOutputField (now `__result`)
     * for the OpenAIRE index query
     *
     * @param query the query as inserted in Content-OpenAireContentConnector-Service
     */
    private void buildFields(Query query) {

        if (query.getParams() == null) query.setParams(new HashMap<>());

        if (!query.getParams().containsKey("fl")) {
            query.getParams().put("fl", new ArrayList<>());
            query.getParams().get("fl").add(queryOutputField);
        } else {
            if (!query.getParams().get("fl").contains(queryOutputField)) {
                query.getParams().get("fl").add(queryOutputField);
            }
        }
    }

    /**
     * Adds sorting parameter for the query
     *
     * @param query the query as inserted in Content-OpenAireContentConnector-Service
     */
    private void buildSort(Query query) {

        if (query.getParams() == null) query.setParams(new HashMap<>());
        if (!query.getParams().containsKey("sort")) {
            query.getParams().put("sort", new ArrayList<>());
            query.getParams().get("sort").add("__indexrecordidentifier desc");
        } else {
            if (!query.getParams().get("sort").contains("__indexrecordidentifier desc")
                    && !query.getParams().get("sort").contains("__indexrecordidentifier asc")) {
                query.getParams().get("sort").add("__indexrecordidentifier desc");
            }
        }
    }

    /**
     * Updates defaultConnection by querying services.openaire.eu profile
     */
    @Scheduled(fixedRate = (long)10 * 60 * 1000, initialDelay = 0)
    private void updateDefaultConnection() {

        if (updateCollection) {
            InputStream inputStream;
            URLConnection con;
            try {
                URL url = new URL(getProfileUrl);
                Authenticator.setDefault(new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("admin", "driver".toCharArray());
                    }
                });

                try {
                    con = url.openConnection();
                    inputStream = con.getInputStream();
                } catch (SSLHandshakeException e) {

                    // Create a trust manager that does not validate certificate chains
                    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }};

                    // Install the all-trusting trust manager
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, trustAllCerts, new SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    con = url.openConnection();
                    inputStream = con.getInputStream();
                }

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                XPath xpath = XPathFactory.newInstance().newXPath();

                Document doc = dbf.newDocumentBuilder().parse(inputStream);
                String value = (String) xpath.evaluate("//RESOURCE_PROFILE/BODY/CONFIGURATION/SERVICE_PROPERTIES/PROPERTY[@key=\"mdformat\"]/@value", doc, XPathConstants.STRING);

                if (value != null && !value.isEmpty())
                    defaultCollection = value.toUpperCase() + "-index-openaire";

                log.debug("Updating defaultCollection to '" + defaultCollection + "'");
            } catch (IOException e) {

                log.error("Error applying SSLContext - IOException", e);
            } catch (NoSuchAlgorithmException e) {

                log.error("Error applying SSLContext - NoSuchAlgorithmException", e);
            } catch (KeyManagementException e) {

                log.error("Error applying SSLContext - KeyManagementException", e);
            } catch (SAXException e) {

                log.error("Error parsing value - SAXException", e);
            } catch (XPathExpressionException e) {

                log.error("Error parsing value - XPathExpressionException", e);
            } catch (ParserConfigurationException e) {

                log.error("Error parsing value - ParserConfigurationException", e);
            }
        }
    }

    public String getSchemaAddress() {
        return schemaAddress;
    }

    public void setSchemaAddress(String schemaAddress) {
        this.schemaAddress = schemaAddress;
    }
}
