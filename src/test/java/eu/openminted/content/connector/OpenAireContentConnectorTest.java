package eu.openminted.content.connector;

import eu.openminted.content.ConnectorConfiguration;
import eu.openminted.content.openaire.OpenAireContentConnector;
import eu.openminted.registry.domain.Facet;
import eu.openminted.registry.domain.Value;
import org.apache.log4j.Logger;
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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConnectorConfiguration.class})
public class OpenAireContentConnectorTest {
    private static Logger log = Logger.getLogger(OpenAireContentConnectorTest.class.getName());

    @Autowired
    private OpenAireContentConnector openAireContentConnector;

    @org.springframework.beans.factory.annotation.Value("${services.openaire.getProfile}")
    private String getProfileUrl;

    @Test
    @Ignore
    public void search() throws Exception {
        // The way this test is implemented it supposes all of the following parameters enabled.
        // To alter the query by a parameter or field or facet
        // feel free to comment or add anything

        Query query = new Query();
        query.setFrom(0);
        query.setTo(1);
        query.setParams(new HashMap<>());
        query.getParams().put("fq", new ArrayList<>());
//        query.getParams().get("fq").add("__indexrecordidentifier:od_______165\\:\\:00000090f0a93f19f8fb17252976f1fb");
        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.getParams().put("licence", new ArrayList<>());
        query.getParams().get("licence").add("Open Access");
        query.getParams().put("publicationYear", new ArrayList<>());
        query.getParams().get("publicationYear").add("2010");
//        query.getParams().get("publicationYear").add("2011");
//        query.getParams().get("publicationYear").add("2012");
        query.setKeyword("*:*");
        query.setFacets(new ArrayList<>());
//        query.getFacets().add("Licence");
//        query.getFacets().add("resulttypename");
        query.getFacets().add("PublicationYear");
//        query.getFacets().add("DocumentLanguage");
//        query.getFacets().add("PublicationType");

        while (openAireContentConnector.getDefaultCollection() == null || openAireContentConnector.getDefaultCollection().isEmpty())
            Thread.sleep(1000);

        SearchResult searchResult = openAireContentConnector.search(query);

        if (searchResult.getPublications() != null) {
//            for (String metadataRecord : searchResult.getPublications()) {
//                System.out.println(metadataRecord);
//            }

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

        Query query = new Query();
        query.setParams(new HashMap<>());

        query.getParams().put("licence", new ArrayList<>());
        query.getParams().get("licence").add("Open Access");

        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");

        query.getParams().put("__indexrecordidentifier", new ArrayList<>());
        query.getParams().get("__indexrecordidentifier").add("od________18::7ff13d895bc479a9143760e3ed68b6ed");


        query.setKeyword("*:*");
        query.setFacets(new ArrayList<>());
        query.getFacets().add("Licence");
        query.getFacets().add("DocumentLanguage");
        query.getFacets().add("PublicationType");

        while (openAireContentConnector.getDefaultCollection() == null || openAireContentConnector.getDefaultCollection().isEmpty())
            Thread.sleep(1000);

        InputStream inputStream = openAireContentConnector.fetchMetadata(query);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
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

        while (openAireContentConnector.getDefaultCollection() == null || openAireContentConnector.getDefaultCollection().isEmpty())
            Thread.sleep(1000);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        XPath xpath = XPathFactory.newInstance().newXPath();

        Document currentDoc;
        NodeList nodes;
        currentDoc = dbf.newDocumentBuilder().newDocument();

        InputStream inputStream = openAireContentConnector.fetchMetadata(query);
        Document doc = dbf.newDocumentBuilder().parse(inputStream);
        nodes = (NodeList) xpath.evaluate("//OMTDPublications/documentMetadataRecord", doc, XPathConstants.NODESET);
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
//        query.getParams().put("__indexrecordidentifier", new ArrayList<>());
//        query.getParams().get("__indexrecordidentifier").add("jairo_______::c18df4def4d30069e9557d686023675e");

        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.setKeyword("*:*");
        query.setFacets(new ArrayList<>());
        query.getFacets().add("Licence");
        query.getFacets().add("DocumentLanguage");
        query.getFacets().add("PublicationType");

        while (openAireContentConnector.getDefaultCollection() == null || openAireContentConnector.getDefaultCollection().isEmpty())
            Thread.sleep(1000);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        XPath xpath = XPathFactory.newInstance().newXPath();

        Document currentDoc;
        NodeList nodes;
        currentDoc = dbf.newDocumentBuilder().newDocument();

        InputStream inputStream = openAireContentConnector.fetchMetadata(query);
        Document doc = dbf.newDocumentBuilder().parse(inputStream);
        nodes = (NodeList) xpath.evaluate("//OMTDPublications/documentMetadataRecord", doc, XPathConstants.NODESET);
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

        while (openAireContentConnector.getDefaultCollection() == null || openAireContentConnector.getDefaultCollection().isEmpty())
            Thread.sleep(1000);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        XPath xpath = XPathFactory.newInstance().newXPath();

        Document currentDoc;
        NodeList nodes;
        currentDoc = dbf.newDocumentBuilder().newDocument();

        InputStream inputStream = openAireContentConnector.fetchMetadata(query);
        Document doc = dbf.newDocumentBuilder().parse(inputStream);
        nodes = (NodeList) xpath.evaluate("//OMTDPublications/documentMetadataRecord", doc, XPathConstants.NODESET);
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
        String line;
        InputStream inputStream = openAireContentConnector.downloadFullText("core_ac_uk__::0114f25b46c4b6d69f6067e82c285d1a");
//        InputStream inputStream = openAireContentConnector.downloadFullText("jairo_______::c18df4def4d30069e9557d686023675e");
//        InputStream inputStream = openAireContentConnector.downloadFullText("od_______165::00000090f0a93f19f8fb17252976f1fb");
        if (inputStream != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
        } else {
            System.out.println("Publication doesn't contain a valid document");
        }
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