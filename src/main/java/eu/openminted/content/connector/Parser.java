package eu.openminted.content.connector;

import eu.openminted.content.openaire.PublicationResultHandler;
import eu.openminted.registry.domain.DocumentMetadataRecord;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Parser {
    private static Logger log = Logger.getLogger(Parser.class.getName());
    private Parser() {}

    private static Parser parser;
    private PublicationResultHandler handler;

    public static Parser initialize(String address) {
        parser = new Parser();
        try {
            URL url = new URL(address);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            parser.handler = new PublicationResultHandler();
            saxParser.parse(new InputSource(url.openStream()), parser.handler);
            return parser;
        } catch (ParserConfigurationException | SAXException | IOException | JAXBException e) {
            log.error(e);
        }
        return null;
    }

    public List<String> getOMTDPublications() {
        return handler.getOMTDPublications();
    }

    public int getFrom() {
        if (handler.getTotal() > handler.getSize()) {
            return handler.getSize() * handler.getPage() - handler.getSize();
        }
        else {
            return 0;
        }
    }

    public int getTo() {
        if (handler.getTotal() > handler.getSize()) {
            return handler.getSize() * handler.getPage();
        }
        else {
            return handler.getTotal();
        }
    }

    public int getTotalHits() {
        return handler.getTotal();
    }
}
