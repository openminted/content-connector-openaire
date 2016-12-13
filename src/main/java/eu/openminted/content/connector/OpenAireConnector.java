package eu.openminted.content.connector;

import eu.openminted.registry.domain.Facet;
import eu.openminted.registry.domain.Value;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
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
import java.util.*;

@Component
public class OpenAireConnector implements ContentConnector {
    private static Logger log = Logger.getLogger(OpenAireConnector.class.getName());
    private String schemaAddress;

    private Map<String, String> facetConverter = new HashMap<>();

    OpenAireConnector() {
        String PUBLICATION_TYPE = "publicationType";
        String PUBLICATION_DATE = "publicationDate";
        String PUBLISHER = "publisher";
        String RIGHTS_STMT_NAME = "rightsStmtName";
        String LICENCE = "licence";
        String DOCUMENT_LANG = "documentLanguage";
        String KEYWORD = "keyword";
        String INSTANCE_TYPE_NAME = "instancetypename";
        String RESULT_DATE_OF_ACCEPTENCE = "resultdateofacceptance";
        String RESULT_RIGHTS = "resultrights";
        String RESULT_LANG_NAME = "resultlanguagename";
        String RESULT_TYPE_ID = "resulttypeid";

        facetConverter.put(PUBLICATION_TYPE.toLowerCase(), INSTANCE_TYPE_NAME);
        facetConverter.put(PUBLICATION_DATE.toLowerCase(), RESULT_DATE_OF_ACCEPTENCE);
        facetConverter.put(RIGHTS_STMT_NAME.toLowerCase(), RESULT_RIGHTS);
        facetConverter.put(LICENCE.toLowerCase(), RESULT_RIGHTS);
        facetConverter.put(DOCUMENT_LANG.toLowerCase(), RESULT_LANG_NAME);

        facetConverter.put(INSTANCE_TYPE_NAME.toLowerCase(), PUBLICATION_TYPE);
        facetConverter.put(RESULT_DATE_OF_ACCEPTENCE.toLowerCase(), PUBLICATION_DATE);
        facetConverter.put(RESULT_RIGHTS.toLowerCase(), LICENCE);
        facetConverter.put(RESULT_LANG_NAME.toLowerCase(), DOCUMENT_LANG);
    }

    @Override
    public SearchResult search(Query query) {
        SearchResult searchResult = new SearchResult();
        final String FACET_FIELD_DOCUMENT_TYPE = "documentType";
        final String FACET_FIELD_COUNT_FIELD_DOCUMENT_TYPE = "fullText";

        try {
            Parser parser = new Parser();
            facetConversion(query);
            OpenAireSolrClient client = new OpenAireSolrClient();
            QueryResponse response = client.query(query);
            searchResult.setFrom((int) response.getResults().getStart());
            searchResult.setTo((int) response.getResults().getStart() + response.getResults().size());
            searchResult.setTotalHits((int) response.getResults().getNumFound());

            List<Facet> facets = new ArrayList<>();
            if (response.getFacetFields() != null) {
                for (FacetField facetField : response.getFacetFields()) {
                    Facet facet = buildFacet(facetField);
                    if (facet != null)
                        facets.add(facet);
                }
                // Facet Field documenttype does not exist in OpenAIRE, so we added it explicitly
                facets.add(buildFacet(FACET_FIELD_DOCUMENT_TYPE, FACET_FIELD_COUNT_FIELD_DOCUMENT_TYPE, searchResult.getTotalHits()));
            }

            searchResult.setFacets(facets);
            searchResult.setPublications(new ArrayList<>());

            for (SolrDocument document : response.getResults()) {
                // TODO: The getFieldName to get the result should be given as input
                // There may be more than one fields, yet we care only for the result
                // It would be nice, if this field is the only field needed, to be set once
                // from a properties file.

                // TODO: xml validation for the initial queries is needed and yet the oaf xsd has issues
                // leaving xml validation for as feature in future commit

                String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + document.getFieldValue("__result").toString().replaceAll("\\[|\\]", "");
//                if (!schemaAddress.isEmpty()) {
//                    Validator validator = createValidator(schemaAddress);
//                    Source source = new StreamSource(xml);
//                    try {
//                        if (validator != null) {
//                            validator.validate(source);
//                            log.info(source.getSystemId() + " is valid");
//                        }
//                    } catch (SAXException e) {
//                        log.error(source.getSystemId() + " is NOT valid");
//                        log.error("Reason: " + e.getLocalizedMessage());
//                        continue;
//                    }
//                }

                parser.parse(new InputSource(new StringReader(xml)));
                searchResult.getPublications().add(parser.getOMTDPublication());
            }
        } catch (Exception e) {
            log.error("OpenAireConnector.search", e);
        }
        return searchResult;
    }

    private void facetConversion(Query query) {
        List<String> facetsToAdd = new ArrayList<>();
        if (query.getFacets() != null && query.getFacets().size() > 0) {
            for (String facet : query.getFacets()) {
                if (facet != null && !facet.isEmpty()) {
                    if (facetConverter.containsKey(facet.toLowerCase())) {
                        facetsToAdd.add(facetConverter.get(facet.toLowerCase()));
                    }
                }
            }
            query.setFacets(facetsToAdd);
        }
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
        return null;
    }

    @Override
    public InputStream fetchMetadata(Query query) {
        facetConversion(query);
        OpenAireSolrClient client = new OpenAireSolrClient();
        PipedInputStream inputStream = new PipedInputStream();
        try {
            new Thread(()->
                    client.fetchMetadata(query)).start();

            client.getPipedOutputStream().connect(inputStream);

            client.getPipedOutputStream().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
            client.getPipedOutputStream().write("<OMTDPublications>\n".getBytes());
        } catch (IOException e) {
            log.error("OpenAireConnector.fetchMetadata", e);
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

    private Facet buildFacet(FacetField facetField) {
        Facet facet = null;
        if (facetConverter.containsKey(facetField.getName().toLowerCase())) {
            facet = new Facet();
            facet.setLabel(facetConverter.get(facetField.getName().toLowerCase()));
            facet.setField(facetConverter.get(facetField.getName().toLowerCase()));
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

    private Facet buildFacet(String name, String countName, int countValue) {
        Facet facet = new Facet();
        facet.setLabel(name);
        facet.setField(name);

        List<Value> values = new ArrayList<>();
        Value value = new Value();
        value.setValue(countName);
        value.setCount(countValue);
        values.add(value);

        facet.setValues(values);
        return facet;
    }
}
