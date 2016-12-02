package eu.openminted.content.connector;

import eu.openminted.content.openaire.PublicationResultHandler;
import eu.openminted.registry.domain.DocumentMetadataRecord;
import org.apache.log4j.Logger;
import org.hsqldb.lib.StringInputStream;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class Parser {
    private static Logger log = Logger.getLogger(Parser.class.getName());
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
