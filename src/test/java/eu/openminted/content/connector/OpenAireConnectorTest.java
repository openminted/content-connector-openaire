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
        query.setFrom(0);
        query.setTo(1);
        query.setParams(new HashMap<>());
        query.getParams().put("fl", new ArrayList<>());
        query.getParams().get("fl").add("__result");
        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");


        query.setFacets(new ArrayList<>());
        query.setKeyword("*:*");
        OpenAireConnector openAireConnector = new OpenAireConnector();
//        openAireConnector.setSchemaAddress("https://www.openaire.eu/schema/0.3/oaf-0.3.xsd");
        SearchResult searchResult = openAireConnector.search(query);

        if (searchResult.getPublications() != null) {
            for (String metadataRecord : searchResult.getPublications()) {
                System.out.println(metadataRecord);
            }


            System.out.println(searchResult.getFacets());
            System.out.println("reading " + searchResult.getPublications().size() +
                    " publications from " + searchResult.getFrom() +
                    " to " + searchResult.getTo() +
                    " out of " + searchResult.getTotalHits() + " total hits.");
        } else {
            System.out.println("Could not find any result with these parameters or keyword");
        }
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
}