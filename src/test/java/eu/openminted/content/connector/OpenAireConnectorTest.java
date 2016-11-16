package eu.openminted.content.connector;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.util.*;


public class OpenAireConnectorTest {
    @Test
    public void search() throws Exception {
        Query query = new Query();
        Map<String, List<String>> parameters = new HashMap<>();
        parameters.put("openairePublicationID", new ArrayList<>());
        parameters.get("openairePublicationID").add("od______2806::3596cc1b1e96409b1677a0efe085912d");
        parameters.get("openairePublicationID").add("od______2806::36a266a2402a9214e8dda6dd9e68a3eb");

//        query.setParams(parameters);
//        query.setFrom(1); //fromDateAccepted
//        query.setTo(2); //toDateAccepted
//        query.setKeyword("АНАЛИЗ ВКЛАДНЫХ ОПЕРАЦИЙ КОММЕРЧЕСКОГО БАНКА");//title
        query.setKeyword("");
        OpenAireConnector openAireConnector = new OpenAireConnector();
        SearchResult searchResult = openAireConnector.search(query);
        System.out.println(searchResult.getPublications().size());
        assert searchResult.getPublications().size() == 1;

    }

    @Test
    public void downloadFullText() throws Exception {
        OpenAireConnector openAireConnector = new OpenAireConnector();
        String output = IOUtils.toString(openAireConnector.downloadFullText("od______2806::3596cc1b1e96409b1677a0efe085912d,od______2806::36a266a2402a9214e8dda6dd9e68a3eb"));
        System.out.println(output);
    }

}