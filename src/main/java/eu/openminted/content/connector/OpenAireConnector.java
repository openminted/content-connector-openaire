package eu.openminted.content.connector;

import eu.openminted.registry.domain.DocumentMetadataRecord;
import org.apache.log4j.Logger;
import org.hsqldb.lib.StringInputStream;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.List;

@Component
public class OpenAireConnector implements ContentConnector {
    private static Logger log = Logger.getLogger(OpenAireConnector.class.getName());

    @Override
    public SearchResult search(Query query) {

        /*
            example 1:
            http://api.openaire.eu/search/publications?openairePublicationID=od______2806::3596cc1b1e96409b1677a0efe085912d

            example 2:
            http://api.openaire.eu/search/publications?openairePublicationID=od______2806::3596cc1b1e96409b1677a0efe085912d,od______2806::36a266a2402a9214e8dda6dd9e68a3eb

            example 3:
            http://api.openaire.eu/search/publications?title=АНАЛИЗ ВКЛАДНЫХ ОПЕРАЦИЙ КОММЕРЧЕСКОГО БАНКА

            example 4:
            http://api.openaire.eu/search/publications?title=АНАЛИЗ%20ВКЛАДНЫХ%20ОПЕРАЦИЙ%20КОММЕРЧЕСКОГО%20БАНКА&openairePublicationID=od______2806::3596cc1b1e96409b1677a0efe085912d,od______2806::36a266a2402a9214e8dda6dd9e68a3eb

            example 5:
            http://scoobydoo.di.uoa.gr:8181/dnet-functionality-services-2.0.0-SNAPSHOT/rest/v2/api/publications
            &type=0001&tp=and&lang=eng&ln=and&refine=true&fields=relfunderid&fields=resultbestlicense&page=1&size=10

         */

        String address = "http://api.openaire.eu/search/publications?";
        SearchResult searchResult = new SearchResult();

        try {
            if (query.getKeyword() != null) {
                address += "title=" + query.getKeyword().replaceAll(" ", "%20") + "&";
            }

            if (query.getParams() != null) {
                for (String parameter : query.getParams().keySet()) {
                    String parameters = parameter + "=";
                    for (String value : query.getParams().get(parameter)) {
                        parameters += value + ",";
                    }
                    parameters = parameters.replaceAll(",$", "&");
                    address += parameters;
                }
            }

            int size = query.getTo() - query.getFrom();
            int page = 0;
            if (query.getTo() % size == 0) {
                page = query.getTo() / size;
                address += "page=" + page + "&size=" + size + "&";
            } else {
                size = query.getFrom();
                page = 2;
                throw new Exception("`from` and `to` keywords should be meaningful for creating a page " +
                        "with specific size for your data representation");
            }

            address = address.replaceAll("&$", "");

            Parser parser = Parser.initialize(address);
            searchResult.setPublications(parser.getOMTDPublications());
            searchResult.setTo(parser.getTo());
            searchResult.setFrom(parser.getFrom());
            searchResult.setTotalHits(parser.getTotalHits());
            return searchResult;
        }
        catch (Exception e) {
            log.error("OpenAireConnector.search", e);
        }
        Parser parser = Parser.initialize(address);
        searchResult.setPublications(parser.getOMTDPublications());
        searchResult.setTo(parser.getTo());
        searchResult.setFrom(parser.getFrom());
        searchResult.setTotalHits(parser.getTotalHits());

        return searchResult;
    }

    @Override
    public InputStream downloadFullText(String s) {
        String address = "http://api.openaire.eu/search/publications?openairePublicationID=" + s;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DocumentMetadataRecord.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringBuilder stringBuilder = new StringBuilder();

            Parser parser = Parser.initialize(address);
            List<String> publications = parser.getOMTDPublications();

            if (publications != null) {
                for (String documentMetadataRecord : publications) {
                    stringBuilder.append(documentMetadataRecord);
                }
                return new StringInputStream(stringBuilder.toString());
            }

        } catch (JAXBException e) {
            log.error("OpenAireConnector.downloadFullText", e);
        }

        return null;
    }

    @Override
    public InputStream fetchMetadata(Query query) {
        return null;
    }

    @Override
    public String getSourceName() {
        return "OpenAIRE";
    }
}
