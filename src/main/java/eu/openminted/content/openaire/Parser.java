package eu.openminted.content.openaire;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

/**
 * Default Parser class to parse OpenAire publication to OMTD publication
 */
class Parser {
    private PublicationResultHandler handler;

    Parser() throws JAXBException, ParserConfigurationException, SAXException, IOException {
        handler = new PublicationResultHandler();
    }

    void parse(InputSource inputSource) throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(inputSource, handler);
    }

    String getOMTDPublication() {
        return handler.getOMTDPublication();
    }

}
