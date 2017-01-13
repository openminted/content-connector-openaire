package eu.openminted.content.connector;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CursorMarkParams;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

class OpenAireSolrClient {
    private static Logger log = Logger.getLogger(OpenAireConnector.class.getName());

    private final String defaultCollection = "TMF-index-openaire";
    private final PipedOutputStream outputStream = new PipedOutputStream();
    private final String hosts = "index1.t.hadoop.research-infrastructures.eu:9983," +
            "index2.t.hadoop.research-infrastructures.eu:9983," +
            "index3.t.hadoop.research-infrastructures.eu:9983";

// Production URL
// private final String hosts = "solr.openaire.eu:9983";

    QueryResponse query(Query query) throws IOException, SolrServerException {
        SolrClient solrClient = new CloudSolrClient.Builder().withZkHost(hosts).build();
        SolrQuery solrQuery = queryBuilder(query);
        QueryResponse queryResponse = solrClient.query(defaultCollection, solrQuery);
        solrClient.close();
        return queryResponse;
    }

    void fetchMetadata(Query query) {
        SolrClient solrClient = new CloudSolrClient.Builder().withZkHost(hosts).build();
        SolrQuery solrQuery = queryBuilder(query);
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;

        try {
            outputStream.flush();
            while (!done) {
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
                QueryResponse rsp = solrClient.queryAndStreamResponse(defaultCollection, solrQuery,
                        new OpenAireStreamingResponseCallback(outputStream, "__result"));
                String nextCursorMark = rsp.getNextCursorMark();
                if (cursorMark.equals(nextCursorMark)) {
                    done = true;
                }
                cursorMark = nextCursorMark;
            }
            outputStream.write("</OMTDPublications>\n".getBytes());
            outputStream.flush();
            solrClient.close();
        }
        catch (IOException | SolrServerException e) {
            log.error("OpenAireSolrClient.fetchMetadata", e);
        }
        finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                log.error("OpenAireSolrClient.fetchMetadata", e);
            }
        }
    }

    private SolrQuery queryBuilder(Query query) {
        String FILTER_QUERY_RESULT_TYPE_NAME = "resulttypename:publication";
        String FILTER_QUERY_DELETED_BY_INFERENCE = "deletedbyinference:false";

        int rows = 10;
        int start = 0;

        if (query.getFrom() > 0) {
            start = query.getFrom();
        }

        if (query.getTo() > 0) {
            rows = query.getTo() - start;
        }

        SolrQuery solrQuery = (new SolrQuery()).setStart(start).setRows(rows);

        if (query.getFacets() != null) {
            solrQuery.setFacet(true);

            if (query.getFacets().size() > 0) {
                solrQuery.addFacetField(query.getFacets().toArray(new String[query.getFacets().size()]));
//                for (String facet : query.getFacets()) {
//                    if (facet.toLowerCase().contains("year")
//                            ||facet.toLowerCase().contains("date")) {
//                        solrQuery.set("facet.range", facet);
//                        solrQuery.set("f." + facet + ".facet.range.start", "2010-01-01T00:00:00.000Z");
//                        solrQuery.set("f." + facet + ".facet.range.end", "NOW/YEAR");
//                        solrQuery.set("f." + facet + ".facet.range.gap", "+1YEAR");
//                    }
//                }
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
                                datetimeFieldQuery += key + ":[" + queryDate + " TO " + queryDate +"+1YEAR] OR ";
                            } catch (ParseException e) {
                                try {
                                    date = queryDateFormat.parse(val);
                                    queryDate = queryDateFormat.format(date);
                                    datetimeFieldQuery += key + ":[" + queryDate + " TO " + queryDate +"+1YEAR] OR ";
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                        datetimeFieldQuery = datetimeFieldQuery.replaceAll(" OR $", "");
                        solrQuery.addFilterQuery(datetimeFieldQuery);

                    }
                    else {
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

        System.out.println(solrQuery.toString());

        return solrQuery;
    }

    PipedOutputStream getPipedOutputStream() {
        return outputStream;
    }
}
