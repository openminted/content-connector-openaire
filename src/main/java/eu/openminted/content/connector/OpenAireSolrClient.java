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
import java.util.ArrayList;
import java.util.List;

class OpenAireSolrClient {
    private static Logger log = Logger.getLogger(OpenAireConnector.class.getName());

    private final String defaultCollection = "TMF-index-openaire";
    private final PipedOutputStream outputStream = new PipedOutputStream();
    private final String hosts = "index1.t.hadoop.research-infrastructures.eu:9983," +
            "index2.t.hadoop.research-infrastructures.eu:9983," +
            "index3.t.hadoop.research-infrastructures.eu:9983";

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
                    for (String val : vals) {
                        solrQuery.addFilterQuery(key + ":" + val);
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
