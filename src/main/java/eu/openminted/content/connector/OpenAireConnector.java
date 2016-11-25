package eu.openminted.content.connector;

import eu.openminted.registry.domain.DocumentMetadataRecord;
import eu.openminted.registry.domain.Facet;
import eu.openminted.registry.domain.Value;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.hsqldb.lib.StringInputStream;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenAireConnector implements ContentConnector {
    private static Logger log = Logger.getLogger(OpenAireConnector.class.getName());

    @Override
    public SearchResult search(Query query) {
        SearchResult searchResult = new SearchResult();

        try {
            Parser parser = new Parser();
            OpenAireSolrClient client = new OpenAireSolrClient();
            QueryResponse response = client.execute(query);
            searchResult.setFrom((int) response.getResults().getStart());
            searchResult.setTo((int) response.getResults().getStart() + response.getResults().size());
            searchResult.setTotalHits((int) response.getResults().getNumFound());

            List<Facet> facets = new ArrayList<>();
            if (response.getFacetFields() != null) {
                for (FacetField facetField : response.getFacetFields()) {
                    Facet facet = new Facet();
                    facet.setLabel(facetField.getName());
                    facet.setField(facetField.getName());
                    List<Value> values = new ArrayList<>();
                    for (FacetField.Count count : facetField.getValues()) {
                        Value value = new Value();
                        value.setValue(count.getName());
                        value.setCount((int) count.getCount());
                        values.add(value);
                    }
                    facet.setValues(values);
                    facets.add(facet);
                }
            }

            searchResult.setFacets(facets);

            for (SolrDocument document : response.getResults()) {
                String xml = document.getFieldValue("__result").toString().replaceAll("\\[|\\]", "");

                parser.parse(new InputSource(new StringReader(xml)));
                searchResult.setPublications(new ArrayList<>());
                searchResult.getPublications().addAll(parser.getOMTDPublications());
            }
        } catch (Exception e) {
            log.error("OpenAireConnector.search", e);
        }
        return searchResult;
    }

    @Override
    public InputStream downloadFullText(String s) {
        return null;
    }

    @Override
    public InputStream fetchMetadata(Query query) {
        return null;
    }

    @Override
    public String getSourceName() {
        return "OpenAIRE";
    }
}
