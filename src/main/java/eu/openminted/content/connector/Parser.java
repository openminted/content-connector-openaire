package eu.openminted.content.connector;

import eu.openminted.content.openaire.PublicationResultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

public class Parser {
    private PublicationResultHandler handler;

    public Parser() throws JAXBException, ParserConfigurationException, SAXException, IOException {
        handler = new PublicationResultHandler();
    }

    public void parse(InputSource inputSource) throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(inputSource, handler);
    }

    public String getOMTDPublication() {
        return handler.getOMTDPublication();
    }

}
