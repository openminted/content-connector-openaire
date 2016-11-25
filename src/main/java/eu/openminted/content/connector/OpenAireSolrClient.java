package eu.openminted.content.connector;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CursorMarkParams;

import java.io.IOException;

public class OpenAireSolrClient {
    private final String hosts = "index1.t.hadoop.research-infrastructures.eu:9983," +
            "index2.t.hadoop.research-infrastructures.eu:9983," +
            "index3.t.hadoop.research-infrastructures.eu:9983";
    private final String defaultCollection = "DMF-index-openaire";
    private SolrClient solrClient;

    private int rows = 10;
    private int start = 0;

    public OpenAireSolrClient() {
        this.solrClient = new CloudSolrClient.Builder().withZkHost(hosts).build();
    }

    public QueryResponse execute(Query query) throws IOException, SolrServerException {
        rows = query.getTo() - query.getFrom();
        start = query.getFrom();

        SolrQuery solrQuery = (new SolrQuery()).setRows(rows)
                .setStart(start)
                .setSort(SolrQuery.SortClause.asc("__indexrecordidentifier"));

        for (String facet : query.getFacets()) {
            solrQuery.setFields(facet);
        }

        solrQuery.setQuery(query.getKeyword());


        return solrClient.query(defaultCollection, solrQuery);
    }

    public void runWithCursor() throws IOException, SolrServerException {
        SolrClient solrClient = new CloudSolrClient.Builder().withZkHost(hosts).build();
        SolrQuery q = (new SolrQuery()).setRows(5).setSort(SolrQuery.SortClause.asc("__indexrecordidentifier")).setFields("__result").setQuery("*:*");
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;

        while (!done) {
            q.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            QueryResponse rsp = solrClient.query("DMF-index-openaire", q);
            String nextCursorMark = rsp.getNextCursorMark();

            for (SolrDocument document : rsp.getResults()) {
                System.out.println(document.getFieldValue("__result"));
            }

            if (cursorMark.equals(nextCursorMark)) {
                done = true;
            }
            cursorMark = nextCursorMark;
        }
    }
}
