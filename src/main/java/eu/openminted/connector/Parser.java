package eu.openminted.connector;

import eu.openminted.openaire.PublicationResultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.net.URL;
import java.util.List;

class Parser {
    /***
     * Reads the URL address to parse the publications retrieved from the OpenAire search API
     * @param address the URL to the OpenAire search API
     * @return the number of documents processed
     */
    List<String> getPublications(String address) {
        try {
            URL url = new URL(address);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            PublicationResultHandler handler = new PublicationResultHandler();
            saxParser.parse(new InputSource(url.openStream()), handler);

            return handler.getOMTDPublications();
        } catch (ParserConfigurationException | SAXException | IOException | JAXBException e) {
            e.printStackTrace();
        }

        return null;
    }
}
