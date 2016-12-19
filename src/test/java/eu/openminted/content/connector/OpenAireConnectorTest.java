package eu.openminted.content.connector;

import eu.openminted.registry.domain.Facet;
import eu.openminted.registry.domain.Value;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.*;


public class OpenAireConnectorTest {
    @Test
    @Ignore
    public void search() throws Exception {
        // The way this test is implemented it supposes all of the following parameters enabled.
        // To alter the query by a parameter or field or facet
        // feel free to comment or add anything

        Query query = new Query();
        query.setFrom(0);
        query.setTo(10);
        query.setParams(new HashMap<>());
//        query.getParams().put("fq", new ArrayList<>());
//        query.getParams().get("fq").add("__indexrecordidentifier:*00680ab21c76269e780f5e9e7e636619");
        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.setKeyword("*:*");
        query.setFacets(new ArrayList<>());
        query.getFacets().add("Licence");
        query.getFacets().add("DocumentLanguage");
        query.getFacets().add("PublicationType");

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
        // The way this test is implemented it supposes all of the following parameters enabled.
        // To alter the query by a parameter or field or facet
        // feel free to comment or add anything

        OpenAireConnector openAireConnector = new OpenAireConnector();
        Query query = new Query();
        query.setParams(new HashMap<>());

        query.getParams().put("fq", new ArrayList<>());
        query.getParams().get("fq").add("__indexrecordidentifier:*00680ab21c76269e780f5e9e7e636619");

        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.setKeyword("*:*");
        query.setFacets(new ArrayList<>());
        query.getFacets().add("Licence");
        query.getFacets().add("DocumentLanguage");
        query.getFacets().add("PublicationType");

        InputStream inputStream = openAireConnector.fetchMetadata(query);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }
}