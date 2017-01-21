package eu.openminted.content.connector;

import eu.openminted.registry.domain.Facet;
import eu.openminted.registry.domain.Value;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
@Component
@ComponentScan("eu.openminted.content")
public class OpenAireConnector implements ContentConnector {
    private static Logger log = Logger.getLogger(OpenAireConnector.class.getName());
    private String schemaAddress;

    @Autowired
    private OpenAireSolrClient solrClient;

    private Map<String, String> OmtdOpenAIREMap = new HashMap<>();
    private Map<String, String> OmtdFacetLabels = new HashMap<>();

    public OpenAireConnector() {
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
                Facet documentTypeFacet = buildFacet(FACET_DOCUMENT_TYPE_FIELD, FACET_DOCUMENT_TYPE_LABEL, FACET_DOCUMENT_TYPE_COUNT_NAME, searchResult.getTotalHits());
                facets.put(documentTypeFacet.getField(), documentTypeFacet);
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

                String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + document.getFieldValue("__result").toString().replaceAll("\\[|\\]", "");
                parser.parse(new InputSource(new StringReader(xml)));
                searchResult.getPublications().add(parser.getOMTDPublication());
            }
        } catch (Exception e) {
            log.error("OpenAireConnector.search", e);
        }
        return searchResult;
    }

    private Validator createValidator(String schemaFileUrl) throws MalformedURLException, SAXException {
        log.info("Waiting for XML Validator");
        URL schemaUrl = new URL(schemaFileUrl);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaUrl);
        return schema.newValidator();
    }

    @Override
    public InputStream downloadFullText(String s) {
        InputStream inputStream = null;
        try {
            inputStream = new URL("http://adonis.athenarc.gr/pdfs/" + s + ".pdf").openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    @Override
    public InputStream fetchMetadata(Query query) {

        buildParams(query);
        buildFacets(query);
        buildFields(query);
        buildSort(query);

        OpenAireSolrClient client = new OpenAireSolrClient();
        client.setDefaultCollection(solrClient.getDefaultCollection());
        client.setHosts(solrClient.getHosts());

        PipedInputStream inputStream = new PipedInputStream();
        try {
            new Thread(() ->
                    client.fetchMetadata(query)).start();

            client.getPipedOutputStream().connect(inputStream);

            client.getPipedOutputStream().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
            client.getPipedOutputStream().write("<OMTDPublications>\n".getBytes());
        } catch (IOException e) {

            log.error("OpenAireConnector.fetchMetadata", e);
            try {
                inputStream.close();
            } catch (IOException e1) {
                log.error("OpenAireConnector.fetchMetadata", e1);
            }
        }

        return inputStream;
    }

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
     * @param query the query as inserted in Content-Connector-Service
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
     * @param query the query as inserted in Content-Connector-Service
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
     * Adds field parameter `fl` and adds necessary value `__result` for the OpenAIRE index query
     * @param query the query as inserted in Content-Connector-Service
     */
    private void buildFields(Query query) {

        if (query.getParams() == null) query.setParams(new HashMap<>());

        if (!query.getParams().containsKey("fl")) {
            query.getParams().put("fl", new ArrayList<>());
            query.getParams().get("fl").add("__result");
        } else {
            if (!query.getParams().get("fl").contains("__result")) {
                query.getParams().get("fl").add("__result");
            }
        }
    }

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
}
