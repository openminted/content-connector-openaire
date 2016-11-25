package eu.openminted.content.connector;

import eu.openminted.registry.domain.DocumentMetadataRecord;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.*;


public class OpenAireConnectorTest {
    @Test
    @Ignore
    public void search() throws Exception {
        Query query = new Query();
        Map<String, List<String>> parameters = new HashMap<>();
        parameters.put("openairePublicationID", new ArrayList<>());
        parameters.get("openairePublicationID").add("od______2806::3596cc1b1e96409b1677a0efe085912d");
        parameters.get("openairePublicationID").add("od______2806::3695906b0423d41e074bb46a9bdf9cb9");
        parameters.get("openairePublicationID").add("od______2806::36a266a2402a9214e8dda6dd9e68a3eb");

//        query.setParams(parameters);
        query.setFrom(100);
        query.setTo(200);
//        query.setKeyword("АНАЛИЗ ВКЛАДНЫХ ОПЕРАЦИЙ КОММЕРЧЕСКОГО БАНКА");

        OpenAireConnector openAireConnector = new OpenAireConnector();
        SearchResult searchResult = openAireConnector.search(query);
        System.out.println("reading " + searchResult.getPublications().size() +
                " publications from " + searchResult.getFrom() +
                " to " + searchResult.getTo() +
                " out of " + searchResult.getTotalHits() + " total hits.");
    }

    @Test
    @Ignore
    public void downloadFullText() throws Exception {
        OpenAireConnector openAireConnector = new OpenAireConnector();
        String output = IOUtils.toString(openAireConnector.downloadFullText("od______2806::3596cc1b1e96409b1677a0efe085912d,od______2806::36a266a2402a9214e8dda6dd9e68a3eb"));
        System.out.println(output);
    }

    @Test
    @Ignore
    public void print() throws Exception {
        OpenAireConnector openAireConnector = new OpenAireConnector();
        Query query = new Query();
        query.setFrom(100);
        query.setTo(200);
        SearchResult searchResult = openAireConnector.search(query);

        JAXBContext jaxbContext = JAXBContext.newInstance(DocumentMetadataRecord.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        for (String metadataRecord : searchResult.getPublications()) {
            System.out.println(metadataRecord);
        }
    }

    @Test
    @Ignore
    public void solr() throws Exception {
        Query query = new Query();
        query.setFrom(10);
        query.setTo(15);
        List<String> facets = new ArrayList<>();
        facets.add("__result");
        query.setFacets(facets);
        query.setKeyword("*:*");
        OpenAireConnector openAireConnector = new OpenAireConnector();
        SearchResult searchResult = openAireConnector.search(query);

        for (String metadataRecord : searchResult.getPublications()) {
            System.out.println(metadataRecord);
        }

        System.out.println(searchResult.getFacets());
    }
}