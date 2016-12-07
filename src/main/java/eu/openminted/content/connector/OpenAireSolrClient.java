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

class OpenAireSolrClient {
    private static Logger log = Logger.getLogger(OpenAireConnector.class.getName());
    private final String RESULT_TYPE_ID_FACET_FIELD = "resulttypeid";
    private final String RESULT_TYPE_ID_FACET_QUERY = "resulttypeid:publication";
    private final String DELETED_BY_INFERENCE_FACET_FIELD = "deletedbyinference";
    private final String DELETED_BY_INFERENCE_FACET_QUERY = "deletedbyinference:false";

    private final String hosts = "index1.t.hadoop.research-infrastructures.eu:9983," +
            "index2.t.hadoop.research-infrastructures.eu:9983," +
            "index3.t.hadoop.research-infrastructures.eu:9983";
    private final String defaultCollection = "DMF-index-openaire";
    private SolrClient solrClient;
    private final PipedOutputStream outputStream = new PipedOutputStream();

    OpenAireSolrClient() {
        this.solrClient = new CloudSolrClient.Builder().withZkHost(hosts).build();
    }

    QueryResponse query(Query query) throws IOException, SolrServerException {
        SolrQuery solrQuery = queryBuilder(query);
        return solrClient.query(defaultCollection, solrQuery);
    }

    void fetchMetadata(Query query) {
        SolrQuery solrQuery = queryBuilder(query);
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

    private SolrQuery queryBuilder(Query query) {
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

            /*
                Facets resulttypeid and deletedbyinference should be inserted by default.
             */
            if (!query.getFacets().contains(RESULT_TYPE_ID_FACET_FIELD)) {
                query.getFacets().add(RESULT_TYPE_ID_FACET_FIELD);
                solrQuery.addFacetField(RESULT_TYPE_ID_FACET_FIELD);
            }

            if (!query.getFacets().contains(DELETED_BY_INFERENCE_FACET_FIELD)) {
                query.getFacets().add(DELETED_BY_INFERENCE_FACET_FIELD);
                solrQuery.addFacetField(DELETED_BY_INFERENCE_FACET_FIELD);
            }

            if (query.getFacets().size() > 0) {
                solrQuery.addFacetField(query.getFacets().toArray(new String[query.getFacets().size()]));

                for (String facet : query.getFacets()) {
                    if (facet.equalsIgnoreCase(RESULT_TYPE_ID_FACET_FIELD))
                        solrQuery.addFacetQuery(RESULT_TYPE_ID_FACET_QUERY);
                    else if (facet.equalsIgnoreCase(DELETED_BY_INFERENCE_FACET_FIELD))
                        solrQuery.addFacetQuery(DELETED_BY_INFERENCE_FACET_QUERY);
                    else
                        solrQuery.addFacetQuery(facet);
                }
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
        System.out.println(solrQuery);

        return solrQuery;
    }

    PipedOutputStream getPipedOutputStream() {
        return outputStream;
    }
}
