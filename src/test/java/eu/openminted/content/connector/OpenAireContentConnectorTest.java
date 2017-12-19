package eu.openminted.content.connector;

import eu.openminted.content.ConnectorConfiguration;
import eu.openminted.registry.core.domain.Facet;
import eu.openminted.registry.core.domain.Value;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.net.ssl.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConnectorConfiguration.class})
public class OpenAireContentConnectorTest {
    private static Logger log = Logger.getLogger(OpenAireContentConnectorTest.class.getName());

    @Autowired
    private ContentConnector contentConnector;

    @org.springframework.beans.factory.annotation.Value("${services.openaire.getProfile}")
    private String getProfileUrl;

    @org.springframework.beans.factory.annotation.Value("${content.limit}")
    private String contentLimit;

    private Query query;

    @Before
    public void initialize() {
        // The way this test is implemented it supposes all of the following parameters enabled.
        // To alter the query by a parameter or field or facet
        // feel free to comment or add anything

        query = new Query();
        query.setFrom(0);
        query.setTo(1);
        query.setKeyword("");
        query.setParams(new HashMap<>());
        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.getParams().put("licence", new ArrayList<>());
        query.getParams().get("licence").add("Open Access");

    }

    @Test
    @Ignore
    public void searchFacets() {
        query.getFacets().add("rightsstmtname");
        query.getFacets().add("documentlanguage");
        query.getFacets().add("documenttype");
        query.getFacets().add("publicationtype");
        query.getFacets().add("publicationyear");

        SearchResult searchResult = contentConnector.search(query);

        // Assert that result is not null and also that the resulting facets are not null
        // and the resulting publication exists and it is equal to the required number
        assertNotEquals(null, searchResult);
        assertNotEquals(null, searchResult.getFacets());
        assertNotEquals(null, searchResult.getPublications());
        assertEquals(query.getTo(), searchResult.getPublications().size());

        // Assert that the number of facets returned is equal to the number of facets required
        assertEquals(query.getFacets().size(), searchResult.getFacets().size());

        int totalResults = 0;
        for (Facet facet : searchResult.getFacets()) {

            // For each facet the sum of the values returned should be equal to the total number of hits
            for (Value value : facet.getValues()) {
                totalResults += value.getCount();
            }

            System.out.println("For facet " + facet.getField() + " totalResults: " + totalResults + " of " + searchResult.getTotalHits());
            assertEquals(totalResults, searchResult.getTotalHits());

//            for (String pub : searchResult.getPublications())
//                System.out.println(pub);

            totalResults = 0;
        }
    }

    @Test
    @Ignore
    public void searchPublicationYear() {
        String[] years = {"2010", "2009", "2008"};
        query.getParams().put("publicationyear", new ArrayList<>());
        query.getFacets().add("publicationyear");

        for (String year : years)
            query.getParams().get("publicationyear").add(year);

        SearchResult searchResult = contentConnector.search(query);
        assertNotEquals(null, searchResult);
        assertNotEquals(null, searchResult.getFacets());
        assertNotEquals(null, searchResult.getPublications());
        assertEquals(searchResult.getFacets().size(), query.getFacets().size());
        assertTrue(searchResult.getFacets().get(0).getField().equalsIgnoreCase("publicationyear"));

        Facet facet = searchResult.getFacets().get(0);

        int totalResults = 0;
        for (int i = 0; i < years.length; i++) {
            assert facet.getValues().get(i) != null;
            Value value = facet.getValues().get(i);

            assert value.getLabel() != null && !value.getLabel().isEmpty();
            totalResults += facet.getValues().get(i).getCount();

            System.out.println(facet.getField() + ":{" + facet.getValues().get(i).getLabel() + ":" + facet.getValues().get(i).getCount() + "}");
        }

        assert searchResult.getTotalHits() == totalResults;

        System.out.println("Total hits: " + totalResults);
    }

    @Test
    @Ignore
    public void searchMultipleParameters() {
        query.getParams().put("publicationyear", new ArrayList<>());
        query.getParams().put("documentlanguage", new ArrayList<>());
        query.getParams().put("documenttype", new ArrayList<>());
        query.getParams().put("publicationtype", new ArrayList<>());

        query.getParams().get("publicationyear").add("2010");
        query.getParams().get("documentlanguage").add("ru");
        query.getParams().get("documenttype").add("fulltext");
        query.getParams().get("publicationtype").add("research");

        query.getFacets().add("rightsstmtname");
        query.getFacets().add("documentlanguage");
        query.getFacets().add("documenttype");
        query.getFacets().add("publicationtype");
        query.getFacets().add("publicationyear");

        query.setKeyword("*:*");
        SearchResult searchResult = contentConnector.search(query);

        if (searchResult.getPublications() != null) {
            for (String metadataRecord : searchResult.getPublications()) {
                System.out.println(metadataRecord);
            }

            for (Facet facet : searchResult.getFacets()) {
                int totalHits = 0;
                System.out.println("facet:{" + facet.getLabel() + "[");
                for (Value value : facet.getValues()) {
                    System.out.println("\t{" + value.getValue() + ":" + value.getCount() + "}");
                    totalHits += value.getCount();
                }
                System.out.println("]}");
                assert totalHits == searchResult.getTotalHits();
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
    public void searchSpecificIdentifier() {
        query.getParams().put("fq", new ArrayList<>());
        query.getParams().get("fq").add("__indexrecordidentifier:od_______165\\:\\:00000090f0a93f19f8fb17252976f1fb");

        SearchResult searchResult = contentConnector.search(query);
        assert searchResult != null;
        assert searchResult.getPublications() != null && searchResult.getPublications().size() == 1;

//        System.out.println(searchResult.getPublications());
    }

    @Test
    @Ignore
    public void fetchMetadata() throws Exception {
        query.getParams().put("documentlanguage", new ArrayList<>());
        query.getParams().put("documentyear", new ArrayList<>());
        query.getParams().get("documentlanguage").add("grc");
        query.getParams().get("documentyear").add("1532");

        query.setKeyword("*:*");
        query.setFrom(0);
        query.setTo(10);

        InputStream inputStream = contentConnector.fetchMetadata(query);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        int begin = 0;
        int end = 0;
        int head = 0;
        int tail = 0;
        while ((line = br.readLine()) != null) {
            if (line.contains("<documentMetadataRecords>")) head++;
            if (line.contains("</documentMetadataRecords>")) tail++;
            if (line.contains("<ns1:documentMetadataRecord")) begin++;
            if (line.contains("</ns1:documentMetadataRecord>")) end++;
            System.out.println(line);
        }
        br.close();

        assert head == tail;
        assert begin == end;
        assert head == 1;
        assert begin == query.getTo();
    }

    @Test
    @Ignore
    public void fetchAbstracts() throws Exception {
        Query query = new Query();
        query.setParams(new HashMap<>());

        query.getParams().put("licence", new ArrayList<>());
        query.getParams().get("licence").add("Open Access");
        query.getParams().put("__indexrecordidentifier", new ArrayList<>());
        query.getParams().get("__indexrecordidentifier").add("jairo_______::c18df4def4d30069e9557d686023675e");

        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.setKeyword("*:*");
        query.setFacets(new ArrayList<>());
        query.getFacets().add("Licence");
        query.getFacets().add("DocumentLanguage");
        query.getFacets().add("PublicationType");
        query.getFacets().add("publicationYear");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        XPath xpath = XPathFactory.newInstance().newXPath();

        Document currentDoc;
        NodeList nodes;
        currentDoc = dbf.newDocumentBuilder().newDocument();

        InputStream inputStream = contentConnector.fetchMetadata(query);
        Document doc = dbf.newDocumentBuilder().parse(inputStream);
        nodes = (NodeList) xpath.evaluate("//documentMetadataRecords/documentMetadataRecord", doc, XPathConstants.NODESET);
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node imported = currentDoc.importNode(nodes.item(i), true);
                XPathExpression identifierExpression = xpath.compile("metadataHeaderInfo/metadataRecordIdentifier/text()");
                String identifier = (String) identifierExpression.evaluate(imported, XPathConstants.STRING);

                System.out.println(identifier);

                // Find Abstracts from imported node
                XPathExpression abstractListExpression = xpath.compile("document/publication/abstracts/abstract");
                NodeList abstracts = (NodeList) abstractListExpression.evaluate(imported, XPathConstants.NODESET);

                for (int j = 0; j < abstracts.getLength(); j++) {
                    Node node = abstracts.item(j);
                    if (node != null)
                        System.out.println(node.getTextContent());
                }
            }
        }
    }

    @Test
    @Ignore
    public void fetchFullTexts() throws Exception {
        Query query = new Query();
        query.setParams(new HashMap<>());

        query.getParams().put("licence", new ArrayList<>());
        query.getParams().get("licence").add("Open Access");
        query.getParams().put("__indexrecordidentifier", new ArrayList<>());
        query.getParams().get("__indexrecordidentifier").add("od_______307::2ac66b99e43f23785fd1ae6011ead1f6");
//        query.getParams().get("__indexrecordidentifier").add("jairo_______::c18df4def4d30069e9557d686023675e");

        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.setKeyword("*:*");
        query.setFacets(new ArrayList<>());
        query.getFacets().add("Licence");
        query.getFacets().add("DocumentLanguage");
        query.getFacets().add("PublicationType");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        XPath xpath = XPathFactory.newInstance().newXPath();

        Document currentDoc;
        NodeList nodes;
        currentDoc = dbf.newDocumentBuilder().newDocument();

        InputStream inputStream = contentConnector.fetchMetadata(query);
        Document doc = dbf.newDocumentBuilder().parse(inputStream);
        nodes = (NodeList) xpath.evaluate("//documentMetadataRecords/documentMetadataRecord", doc, XPathConstants.NODESET);
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node imported = currentDoc.importNode(nodes.item(i), true);
                XPathExpression identifierExpression = xpath.compile("metadataHeaderInfo/metadataRecordIdentifier/text()");
                String identifier = (String) identifierExpression.evaluate(imported, XPathConstants.STRING);

                System.out.println(identifier);

                // Find Abstracts from imported node
                XPathExpression downloadUrlsListExpression = xpath.compile("document/publication/distributions/documentDistributionInfo/downloadURLs/downloadURL");
                NodeList downloadUrls = (NodeList) downloadUrlsListExpression.evaluate(imported, XPathConstants.NODESET);

                for (int j = 0; j < downloadUrls.getLength(); j++) {
                    Node downloadUrl = downloadUrls.item(j);
                    if (downloadUrl != null) {

                        URL url = new URL(downloadUrl.getTextContent());
                        URLConnection connection = url.openConnection();
                        connection.connect();
                        String contentType = connection.getContentType();
                        System.out.println(contentType);

                        if (contentType.toLowerCase().contains("html")) continue;
                        System.out.println(downloadUrl.getTextContent());
                    }
                }
            }
        }
    }

    @Test
    @Ignore
    public void fetchHashKeys() throws Exception {
        Query query = new Query();
        query.setParams(new HashMap<>());

        query.getParams().put("licence", new ArrayList<>());
        query.getParams().get("licence").add("Open Access");
//        query.getParams().put("__indexrecordidentifier", new ArrayList<>());
//        query.getParams().get("__indexrecordidentifier").add("jairo_______::c18df4def4d30069e9557d686023675e");

        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.setKeyword("hashkey");
        query.setFacets(new ArrayList<>());
        query.getFacets().add("Licence");
        query.getFacets().add("DocumentLanguage");
        query.getFacets().add("PublicationType");
        query.getFacets().add("publicationYear");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        XPath xpath = XPathFactory.newInstance().newXPath();

        Document currentDoc;
        NodeList nodes;
        currentDoc = dbf.newDocumentBuilder().newDocument();

        InputStream inputStream = contentConnector.fetchMetadata(query);
        Document doc = dbf.newDocumentBuilder().parse(inputStream);
        nodes = (NodeList) xpath.evaluate("//documentMetadataRecords/documentMetadataRecord", doc, XPathConstants.NODESET);
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node imported = currentDoc.importNode(nodes.item(i), true);
                XPathExpression identifierExpression = xpath.compile("metadataHeaderInfo/metadataRecordIdentifier/text()");
                String identifier = (String) identifierExpression.evaluate(imported, XPathConstants.STRING);

                System.out.println(identifier);

                // Find hashkeys from imported node
                XPathExpression distributionListExpression = xpath.compile("document/publication/distributions/documentDistributionInfo/hashkey");
                NodeList hashkeys = (NodeList) distributionListExpression.evaluate(imported, XPathConstants.NODESET);

                for (int j = 0; j < hashkeys.getLength(); j++) {
                    Node hashkey = hashkeys.item(j);
                    if (hashkey != null) {
                        System.out.println(hashkey.getTextContent());
                    }
                }
            }
        }
    }

    @Test
    @Ignore
    public void downloadFullText() throws Exception {

//        InputStream inputStream = contentConnector.downloadFullText("od_______307::2ac66b99e43f23785fd1ae6011ead1f6");
        InputStream inputStream = contentConnector.downloadFullText("core_ac_uk__::0114f25b46c4b6d69f6067e82c285d1a");
//        InputStream inputStream = contentConnector.downloadFullText("jairo_______::c18df4def4d30069e9557d686023675e");
//        InputStream inputStream = contentConnector.downloadFullText("od_______165::00000090f0a93f19f8fb17252976f1fb");

        assert inputStream != null;

        FileOutputStream fileOutputStream = new FileOutputStream(new File("downloaded.pdf"));
        IOUtils.copy(inputStream, fileOutputStream);
        fileOutputStream.close();
        inputStream.close();
    }

    @Test
    @Ignore
    public void indexConnectionTest() throws Exception {
        InputStream inputStream;
        URLConnection con;
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

        log.error(value.toUpperCase() + "-index-openaire");
    }
}