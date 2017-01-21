package eu.openminted.content.connector;

import eu.openminted.content.ConnectorConfiguration;
import eu.openminted.registry.domain.Facet;
import eu.openminted.registry.domain.Value;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConnectorConfiguration.class})
public class OpenAireConnectorTest {

    @Autowired
    private OpenAireConnector openAireConnector;

    @Test
//    @Ignore
    public void search() throws Exception {
        // The way this test is implemented it supposes all of the following parameters enabled.
        // To alter the query by a parameter or field or facet
        // feel free to comment or add anything

        Query query = new Query();
        query.setFrom(0);
        query.setTo(10);
        query.setParams(new HashMap<>());
        query.getParams().put("fq", new ArrayList<>());
//        query.getParams().get("fq").add("__indexrecordidentifier:od_______165\\:\\:00000090f0a93f19f8fb17252976f1fb");
        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.getParams().put("licence", new ArrayList<>());
        query.getParams().get("licence").add("Open Access");
//        query.getParams().put("publicationYear", new ArrayList<>());
//        query.getParams().get("publicationYear").add("2010");
//        query.getParams().get("publicationYear").add("2011");
//        query.getParams().get("publicationYear").add("2012");
        query.setKeyword("digital");
        query.setFacets(new ArrayList<>());
        query.getFacets().add("Licence");
        query.getFacets().add("resulttypename");
        query.getFacets().add("publicationYear");
//        query.getFacets().add("DocumentLanguage");
//        query.getFacets().add("PublicationType");

        SearchResult searchResult = openAireConnector.search(query);

        if (searchResult.getPublications() != null) {
            for (String metadataRecord : searchResult.getPublications()) {
                System.out.println(metadataRecord);
            }

            for (Facet facet : searchResult.getFacets()) {
                System.out.println("facet:{" + facet.getLabel() + "[");
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

    @Test
    public void downloadFullText() throws Exception {
        InputStream inputStream = openAireConnector.downloadFullText("od_______165::00000090f0a93f19f8fb17252976f1fb");
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }
}