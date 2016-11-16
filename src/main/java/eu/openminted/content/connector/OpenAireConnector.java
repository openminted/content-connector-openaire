package eu.openminted.content.connector;

import eu.openminted.registry.domain.DocumentMetadataRecord;
import org.hsqldb.lib.StringInputStream;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.List;

@Component
public class OpenAireConnector implements ContentConnector {
    @Override
    public SearchResult search(Query query) {

        /*
            example 1:
            http://api.openaire.eu/search/publications?openairePublicationID=od______2806::3596cc1b1e96409b1677a0efe085912d

            example 2:
            http://api.openaire.eu/search/publications?openairePublicationID=od______2806::3596cc1b1e96409b1677a0efe085912d,od______2806::36a266a2402a9214e8dda6dd9e68a3eb

            example 3:
            http://api.openaire.eu/search/publications?title=АНАЛИЗ ВКЛАДНЫХ ОПЕРАЦИЙ КОММЕРЧЕСКОГО БАНКА
         */
        String address = "http://api.openaire.eu/search/publications?";
        SearchResult searchResult = new SearchResult();

        if (query.getKeyword() != null){
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

        address = address.replaceAll("&$", "");
        searchResult.setPublications(Parser.getPublications(address));
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
            List<DocumentMetadataRecord> publications = Parser.getPublications(address);

            if (publications != null) {
                for (DocumentMetadataRecord documentMetadataRecord : publications) {
                    StringWriter sw = new StringWriter();
                    jaxbMarshaller.marshal(documentMetadataRecord, sw);
                    stringBuilder.append(sw.toString());
                }
                return new StringInputStream(stringBuilder.toString());
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getSourceName() {
        return "OpenAIRE";
    }
}
