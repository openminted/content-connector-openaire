package eu.openminted.content.openaire;

import eu.openminted.content.connector.ContentConnector;
import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.SearchResult;
import eu.openminted.registry.domain.Facet;
import eu.openminted.registry.domain.Value;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
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
import java.util.*;

@SuppressWarnings("WeakerAccess")
@Component
public class OpenAireContentConnector implements ContentConnector {
    private static Logger log = Logger.getLogger(OpenAireContentConnector.class.getName());
    private String schemaAddress;
    private String defaultCollection;

    @org.springframework.beans.factory.annotation.Value("${services.openaire.getProfile}")
    private String getProfileUrl;

    @org.springframework.beans.factory.annotation.Value("${solr.hosts}")
    private String hosts;

    @org.springframework.beans.factory.annotation.Value("${solr.query.limit}")
    private String queryLimit;

    @org.springframework.beans.factory.annotation.Value("${solr.query.output.field}")
    private String queryOutputField;

    private Map<String, String> OmtdOpenAIREMap = new HashMap<>();
    private Map<String, String> OmtdFacetLabels = new HashMap<>();

    public OpenAireContentConnector() {
        String PUBLICATION_TYPE = "publicationType";
        String PUBLICATION_YEAR = "publicationYear";
        String PUBLISHER = "publisher";
        String RIGHTS_STMT_NAME = "rightsStmtName";
        String LICENCE = "licence";
        String DOCUMENT_LANG = "documentLanguage";
        String KEYWORD = "keyword";
        String INSTANCE_TYPE_NAME = "instancetypename";
        String RESULT_DATE_OF_ACCEPTENCE = "resultdateofacceptance";
        String RESULT_RIGHTS = "resultrights";
        String RESULT_LANG_NAME = "resultlanguagename";

        OmtdOpenAIREMap.put(PUBLICATION_TYPE.toLowerCase(), INSTANCE_TYPE_NAME);
        OmtdOpenAIREMap.put(PUBLICATION_YEAR.toLowerCase(), RESULT_DATE_OF_ACCEPTENCE);
        OmtdOpenAIREMap.put(RIGHTS_STMT_NAME.toLowerCase(), RESULT_RIGHTS);
        OmtdOpenAIREMap.put(LICENCE.toLowerCase(), RESULT_RIGHTS);
        OmtdOpenAIREMap.put(DOCUMENT_LANG.toLowerCase(), RESULT_LANG_NAME);

        OmtdOpenAIREMap.put(INSTANCE_TYPE_NAME.toLowerCase(), PUBLICATION_TYPE);
        OmtdOpenAIREMap.put(RESULT_DATE_OF_ACCEPTENCE.toLowerCase(), PUBLICATION_YEAR);
        OmtdOpenAIREMap.put(RESULT_RIGHTS.toLowerCase(), LICENCE);
        OmtdOpenAIREMap.put(RESULT_LANG_NAME.toLowerCase(), DOCUMENT_LANG);

        OmtdFacetLabels.put(PUBLICATION_TYPE, "Publication Type");
        OmtdFacetLabels.put(PUBLICATION_YEAR, "Publication Year");
        OmtdFacetLabels.put(RIGHTS_STMT_NAME, "Rights Statement");
        OmtdFacetLabels.put(LICENCE, "Licence");
        OmtdFacetLabels.put(DOCUMENT_LANG, "Language");
    }

    /***
     * Spring initialization method.
     * Start timer to update defaultConnection. Time period is set to 10 minutes.
     */
    @PostConstruct
    public void init() {
        // awaiting period is 10 minutes
        final long period = (long) 10 * 60 * 1000;

        // Assert that query limit is a proper integer
        try {
            Integer.parseInt(queryLimit);

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    updateDefaultConnection();
                }
            };

            Timer timer = new Timer(true);
            timer.schedule(timerTask, 0, period);
        } catch (NumberFormatException e) {
            log.error("OpenAireContentConnector: Wrong solr query limit number!", e);
        }
    }

    /***
     * Search method for browsing metadata
     * @param query the query as inserted in Content-OpenAireContentConnector-Service
     * @return SearchResult with metadata and facets
     */
    @Override
    public SearchResult search(Query query) {

        SearchResult searchResult = new SearchResult();
        final String FACET_DOCUMENT_TYPE_FIELD = "documentType";
        final String FACET_DOCUMENT_TYPE_LABEL = "Document Type";
        final String FACET_DOCUMENT_TYPE_COUNT_NAME = "fullText";

        try {
            Parser parser = new Parser();
            buildParams(query);
            buildFacets(query);
            buildFields(query);

            OpenAireSolrClient solrClient = new OpenAireSolrClient();
            solrClient.setHosts(hosts);
            solrClient.setQueryLimit(Integer.parseInt(queryLimit));
            solrClient.setDefaultCollection(defaultCollection);

            QueryResponse response = solrClient.query(query);

            searchResult.setFrom((int) response.getResults().getStart());
            searchResult.setTo((int) response.getResults().getStart() + response.getResults().size());
            searchResult.setTotalHits((int) response.getResults().getNumFound());

            Map<String, Facet> facets = new HashMap<>();

            if (response.getFacetFields() != null) {
                for (FacetField facetField : response.getFacetFields()) {
                    Facet facet = buildFacet(facetField);
                    if (facet != null) {
                        if (!facets.containsKey(facet.getField())) {
                            facets.put(facet.getField(), facet);
                        }
                    }
                }
                // Facet Field documenttype does not exist in OpenAIRE, so we added it explicitly
                if (searchResult.getTotalHits() > 0) {
                    Facet documentTypeFacet = buildFacet(FACET_DOCUMENT_TYPE_FIELD, FACET_DOCUMENT_TYPE_LABEL, FACET_DOCUMENT_TYPE_COUNT_NAME, searchResult.getTotalHits());
                    facets.put(documentTypeFacet.getField(), documentTypeFacet);
                }
            }

            searchResult.setFacets(new ArrayList<>(facets.values()));
            searchResult.setPublications(new ArrayList<>());

            for (SolrDocument document : response.getResults()) {
                // TODO: The getFieldName to get the result should be given as input
                // There may be more than one fields, yet we care only for the result
                // It would be nice, if this field is the only field needed, to be set once
                // from a properties file.

                // TODO: xml validation for the initial queries is needed and yet the oaf xsd has issues
                // leaving xml validation for as feature in future commit

                String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + document.getFieldValue(queryOutputField).toString().replaceAll("\\[|\\]", "");
                parser.parse(new InputSource(new StringReader(xml)));
                searchResult.getPublications().add(parser.getOMTDPublication());
            }
        } catch (Exception e) {
            log.error("OpenAireContentConnector.search", e);
        }
        return searchResult;
    }

    /***
     * Method for downloading fullText linked pdf
     * @param s the ID of the metadata
     * @return the pdf in the form of InputStream
     */
    @Override
    public InputStream downloadFullText(String s) {
        InputStream inputStream = null;
        try {
            inputStream = new URL("http://adonis.athenarc.gr/pdfs/" + s + ".pdf").openStream();
        } catch (MalformedURLException e) {
            log.error("downloadFullText: MalformedURLException ", e);
        } catch (IOException e) {
            log.error("downloadFullText: IOException ", e);
        }
        return inputStream;
    }

    /***
     * Method for downloading metadata where the query's criteria are applicable
     * @param query the query as inserted in Content-OpenAireContentConnector-Service
     * @return The metadata in the form of InputStream
     */
    @Override
    public InputStream fetchMetadata(Query query) {
        buildParams(query);
        buildFacets(query);
        buildFields(query);
        buildSort(query);

        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream();
        final OpenAireSolrClient client;

        try {

            client = new OpenAireSolrClient();
            client.setDefaultCollection(defaultCollection);
            client.setHosts(hosts);
            client.setQueryLimit(Integer.parseInt(queryLimit));
            new Thread(() -> {
                try {
                    client.fetchMetadata(query, new OpenAireStreamingResponseCallback(outputStream, queryOutputField));
                    outputStream.flush();
                    outputStream.write("</OMTDPublications>\n".getBytes());
                } catch (IOException e) {
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
            outputStream.write("<OMTDPublications>\n".getBytes());
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

    /***
     * Method that returns the name of the connector
     * @return OpenAIRE
     */
    @Override
    public String getSourceName() {
        return "OpenAIRE";
    }

    public String getSchemaAddress() {
        return schemaAddress;
    }

    public void setSchemaAddress(String schemaAddress) {
        this.schemaAddress = schemaAddress;
    }

    public String getDefaultCollection() {
        return defaultCollection;
    }

    public void setDefaultCollection(String defaultCollection) {
        this.defaultCollection = defaultCollection;
    }

    /***
     * Creates an individual OMTD Facet from an OpenAIRE FacetField of a SearchResult
     * @param facetField the OpenAIRE FacetField of the SearchResult
     * @return OMTD Facet
     */
    private Facet buildFacet(FacetField facetField) {

        Facet facet = null;
        if (OmtdOpenAIREMap.containsKey(facetField.getName().toLowerCase())) {
            facet = new Facet();
            String field = OmtdOpenAIREMap.get(facetField.getName().toLowerCase());
            facet.setField(field);
            facet.setLabel(OmtdFacetLabels.get(field));
            List<Value> values = new ArrayList<>();
            for (FacetField.Count count : facetField.getValues()) {
                if (count.getCount() == 0) continue;
                Value value = new Value();
                value.setValue(count.getName());
                value.setCount((int) count.getCount());
                values.add(value);
            }
            facet.setValues(values);
        }
        return facet;
    }

    /***
     * Creates an individual OMTD Facet
     * @param field facet's name
     * @param countName count's name
     * @param countValue count's value
     * @return OMTD Facet
     */
    private Facet buildFacet(String field, String label, String countName, int countValue) {
        Facet facet = new Facet();
        facet.setLabel(label);
        facet.setField(field);

        List<Value> values = new ArrayList<>();
        Value value = new Value();
        value.setValue(countName);
        value.setCount(countValue);
        values.add(value);

        facet.setValues(values);
        return facet;
    }

    /***
     * Converts OMTD facets to OpenAIRE facets suitable for
     * @param query the query as inserted in Content-OpenAireContentConnector-Service
     */
    private void buildFacets(Query query) {

        List<String> facetsToAdd = new ArrayList<>();
        if (query.getFacets() != null && query.getFacets().size() > 0) {
            for (String facet : query.getFacets()) {
                if (facet != null && !facet.isEmpty()) {
                    if (OmtdOpenAIREMap.containsKey(facet.toLowerCase())) {
                        facetsToAdd.add(OmtdOpenAIREMap.get(facet.toLowerCase()));
                    }
                }
            }
            query.setFacets(facetsToAdd);
        }
    }

    /***
     * Converts OMTD parameters to OpenAIRE parameters
     * @param query the query as inserted in Content-OpenAireContentConnector-Service
     */
    private void buildParams(Query query) {

        Map<String, List<String>> openAireParams = new HashMap<>();
        if (query.getParams() != null && query.getParams().size() > 0) {
            for (String key : query.getParams().keySet()) {
                if (OmtdOpenAIREMap.containsKey(key.toLowerCase())) {
                    openAireParams.put(OmtdOpenAIREMap.get(key.toLowerCase()), new ArrayList<>(query.getParams().get(key)));
                }
            }

            query.setParams(openAireParams);
        }
    }

    /***
     * Adds field parameter `fl` and adds necessary value of queryOutputField (now `__result`)
     * for the OpenAIRE index query
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

    /***
     * Adds sorting parameter for the query
     * @param query the query as inserted in Content-OpenAireContentConnector-Service
     */
    private void buildSort(Query query) {

        if (query.getParams() == null) query.setParams(new HashMap<>());
        if (!query.getParams().containsKey("sort")) {
            query.getParams().put("sort", new ArrayList<>());
            query.getParams().get("sort").add("__indexrecordidentifier desc");
        } else {
            if (!query.getParams().get("sort").contains("__indexrecordidentifier desc")) {
                query.getParams().get("sort").add("__indexrecordidentifier desc");
            }
        }
    }

    /***
     * Updates defaultConnection by querying services.openaire.eu
     */
    protected void updateDefaultConnection() {
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
