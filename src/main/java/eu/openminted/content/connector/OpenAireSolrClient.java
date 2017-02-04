package eu.openminted.content.connector;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CursorMarkParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.net.ssl.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("WeakerAccess")
@Component
class OpenAireSolrClient {
    private static Logger log = Logger.getLogger(OpenAireConnector.class.getName());

    private int rows = 10;
    private int start = 0;

    @Value("${solr.default.collection}")
    private String defaultCollection;

    @Value("${services.openaire.getProfile}")
    private String getProfileUrl;

    @Value("${solr.hosts}")
    private String hosts;

    @Value("${solr.query.limit}")
    private String queryLimit;

    private final PipedOutputStream outputStream = new PipedOutputStream();

    public QueryResponse query(Query query) throws IOException, SolrServerException {
        SolrClient solrClient = new CloudSolrClient.Builder().withZkHost(hosts).build();
        SolrQuery solrQuery = queryBuilder(query);
        QueryResponse queryResponse = solrClient.query(getDefaultConnection(), solrQuery);
        solrClient.close();
        return queryResponse;
    }

    void fetchMetadata(Query query) {
        SolrClient solrClient = new CloudSolrClient.Builder().withZkHost(hosts).build();
        SolrQuery solrQuery = queryBuilder(query);
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;
        int limit = Integer.parseInt(queryLimit);

        try {
            outputStream.flush();
            int count = 0;
            while (!done) {
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
                QueryResponse rsp = solrClient.queryAndStreamResponse(getDefaultConnection(), solrQuery,
                        new OpenAireStreamingResponseCallback(outputStream, "__result"));

                count += this.rows;
                if (count >= limit) break;

                String nextCursorMark = rsp.getNextCursorMark();
                if (cursorMark.equals(nextCursorMark)) {
                    done = true;
                }
                cursorMark = nextCursorMark;
            }
            outputStream.write("</OMTDPublications>\n".getBytes());
            outputStream.flush();
        } catch (IOException | SolrServerException e) {
            log.error("OpenAireSolrClient.fetchMetadata", e);
        } finally {
            try {
                solrClient.close();
                outputStream.close();
            } catch (IOException e) {
                log.error("OpenAireSolrClient.fetchMetadata", e);
            }
        }
    }

    private SolrQuery queryBuilder(Query query) {
        String FILTER_QUERY_RESULT_TYPE_NAME = "resulttypename:publication";
        String FILTER_QUERY_DELETED_BY_INFERENCE = "deletedbyinference:false";

        if (query.getFrom() > 0) {
            this.start = query.getFrom();
        }

        if (query.getTo() > 0) {
            this.rows = query.getTo() - this.start;
        }

        SolrQuery solrQuery = (new SolrQuery()).setStart(this.start).setRows(this.rows);

        if (query.getFacets() != null) {
            solrQuery.setFacet(true);

            if (query.getFacets().size() > 0) {
                solrQuery.addFacetField(query.getFacets().toArray(new String[query.getFacets().size()]));
            }
        }

        if (query.getParams() != null) {

            for (String key : query.getParams().keySet()) {
                if (key.equalsIgnoreCase("sort")) {
                    for (String sortField : query.getParams().get("sort")) {
                        String[] sortingParameter = sortField.split(" ");
                        if (sortingParameter.length == 2) {
                            SolrQuery.ORDER order = SolrQuery.ORDER.valueOf(sortingParameter[1]);
                            solrQuery.setSort(sortingParameter[0], order);
                        } else if (sortingParameter.length == 1) {
                            solrQuery.setSort(sortingParameter[0], SolrQuery.ORDER.desc);
                        }
                    }
                } else if (key.equalsIgnoreCase("fl")) {
                    for (String field : query.getParams().get("fl")) {
                        solrQuery.addField(field);
                    }
                } else {
                    List<String> vals = query.getParams().get(key);

                    if (key.toLowerCase().contains("year") || key.toLowerCase().contains("date")) {
                        SimpleDateFormat yearFormat = new SimpleDateFormat("YYYY");
                        SimpleDateFormat queryDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                        TimeZone UTC = TimeZone.getTimeZone("UTC");
                        queryDateFormat.setTimeZone(UTC);
                        String datetimeFieldQuery = "";
                        for (String val : vals) {
                            Date date;
                            String queryDate;
                            try {

                                // try parse year with yearFormat "YYYY".
                                // If it is successful add to the year the
                                // rest datetime that is necessary for solr
                                // to parse and parse it with the proper
                                // queryDateFormat

                                yearFormat.parse(val);
                                val = val + "-01-01T00:00:00.000Z";

                                date = queryDateFormat.parse(val);
                                queryDate = queryDateFormat.format(date);
                                datetimeFieldQuery += key + ":[" + queryDate + " TO " + queryDate + "+1YEAR] OR ";
                            } catch (ParseException e) {
                                try {
                                    date = queryDateFormat.parse(val);
                                    queryDate = queryDateFormat.format(date);
                                    datetimeFieldQuery += key + ":[" + queryDate + " TO " + queryDate + "+1YEAR] OR ";
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                        datetimeFieldQuery = datetimeFieldQuery.replaceAll(" OR $", "");
                        solrQuery.addFilterQuery(datetimeFieldQuery);

                    } else {
                        String fieldQuery = "";
                        for (String val : vals) {
                            fieldQuery += key + ":" + val + " OR ";
                        }
                        fieldQuery = fieldQuery.replaceAll(" OR $", "");
                        solrQuery.addFilterQuery(fieldQuery);
                    }
                }
            }
        }

        solrQuery.addFilterQuery(FILTER_QUERY_RESULT_TYPE_NAME);
        solrQuery.addFilterQuery(FILTER_QUERY_DELETED_BY_INFERENCE);

        solrQuery.setQuery(query.getKeyword());

        log.info(solrQuery.toString());

        return solrQuery;
    }

    protected String getDefaultConnection() {
        InputStream inputStream;
        URLConnection con;

        try {
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

            if (value != null && !value.isEmpty())
                return value.toUpperCase() + "-index-openaire";

        } catch (IOException e) {

            log.error("Error applying SSLContext - IOException", e);
        } catch (NoSuchAlgorithmException e) {

            log.error("Error applying SSLContext - NoSuchAlgorithmException", e);
        } catch (KeyManagementException e) {

            log.error("Error applying SSLContext - KeyManagementException", e);
        } catch (SAXException e) {

            log.error("Error parsing value - SAXException", e);
        } catch (XPathExpressionException e) {

            log.error("Error parsing value - XPathExpressionException", e);
        } catch (ParserConfigurationException e) {

            log.error("Error parsing value - ParserConfigurationException", e);
        }
        return defaultCollection;
    }

    PipedOutputStream getPipedOutputStream() {
        return outputStream;
    }

    public String getDefaultCollection() {
        return defaultCollection;
    }

    public void setDefaultCollection(String defaultCollection) {
        this.defaultCollection = defaultCollection;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(String queryLimit) {
        this.queryLimit = queryLimit;
    }
}
