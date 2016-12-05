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
import java.util.List;

public class OpenAireSolrClient {
    private static Logger log = Logger.getLogger(OpenAireConnector.class.getName());

    private final String hosts = "index1.t.hadoop.research-infrastructures.eu:9983," +
            "index2.t.hadoop.research-infrastructures.eu:9983," +
            "index3.t.hadoop.research-infrastructures.eu:9983";
    private final String defaultCollection = "DMF-index-openaire";
    private SolrClient solrClient;
    private final PipedOutputStream outputStream = new PipedOutputStream();

    public OpenAireSolrClient() {
        this.solrClient = new CloudSolrClient.Builder().withZkHost(hosts).build();
    }

    public QueryResponse query(Query query) throws IOException, SolrServerException {
        SolrQuery solrQuery = queryBuilder(query, false);
        return solrClient.query(defaultCollection, solrQuery);
    }

    public void fetchMetadata(Query query) {
        SolrQuery solrQuery = queryBuilder(query, true);
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;

        try {
            outputStream.write("<OMTDPublications>\n".getBytes());
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

    private SolrQuery queryBuilder(Query query, boolean hasCursorMarkParams) {
        int rows = 10;
        int start = 0;

        if (query.getTo() > 0) {
            rows = query.getTo() - query.getFrom();
            start = query.getFrom();
        }

        SolrQuery solrQuery = (new SolrQuery()).setRows(rows);

        if (hasCursorMarkParams)
            solrQuery.setStart(start);

        if (query.getFacets() != null) {
            solrQuery.setFacet(true);
            for (String facet : query.getFacets()) {
                solrQuery.setFacetPrefix(facet);
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
                    solrQuery.set(key, vals.toArray(new String[vals.size()]));
                }
            }
        }

        solrQuery.setQuery(query.getKeyword());

        return solrQuery;
    }



    public PipedOutputStream getPipedOutputStream() {
        return outputStream;
    }
}
