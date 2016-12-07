package eu.openminted.content.connector;

import eu.openminted.registry.domain.DocumentMetadataRecord;
import eu.openminted.registry.domain.Facet;
import eu.openminted.registry.domain.Value;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.*;


public class OpenAireConnectorTest {
    @Test
    @Ignore
    public void search() throws Exception {
        Query query = new Query();
        query.setFrom(0);
        query.setTo(10);
        query.setParams(new HashMap<>());
        query.getParams().put("fl", new ArrayList<>());
        query.getParams().get("fl").add("__result");
//        query.getParams().put("fq", new ArrayList<>());
//        query.getParams().get("fq").add("__indexrecordidentifier:*00680ab21c76269e780f5e9e7e636619");
        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.setKeyword("*:*");
//        query.setFacets(new ArrayList<>());
//        query.getFacets().add("instancetypename");

        OpenAireConnector openAireConnector = new OpenAireConnector();
        SearchResult searchResult = openAireConnector.search(query);

        if (searchResult.getPublications() != null) {
            for (String metadataRecord : searchResult.getPublications()) {
                System.out.println(metadataRecord);
            }

            for (Facet facet : searchResult.getFacets()) {
                System.out.println("facet:{" + facet.getField() + "[");
                for (Value value : facet.getValues()) {
                    System.out.println("\t{" + value.getValue() + ":" + value.getCount() + "}");
                }
                System.out.println("]}");
            }
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
    public void fetchMetadata() throws Exception {
        OpenAireConnector openAireConnector = new OpenAireConnector();
        Query query = new Query();
        query.setParams(new HashMap<>());
        query.getParams().put("fl", new ArrayList<>());
        query.getParams().get("fl").add("__result");
        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.setKeyword("*:*");
        query.setFacets(new ArrayList<>());
        query.getFacets().add("instancetypename");

        InputStream inputStream = openAireConnector.fetchMetadata(query);
        String line;
        BufferedReader br  = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }

    @Test
    @Ignore
    public void downloadFullText() throws Exception {
        OpenAireConnector openAireConnector = new OpenAireConnector();
        String output = IOUtils.toString(openAireConnector.downloadFullText("od______2806::3596cc1b1e96409b1677a0efe085912d,od______2806::36a266a2402a9214e8dda6dd9e68a3eb"), "UTF-8");
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