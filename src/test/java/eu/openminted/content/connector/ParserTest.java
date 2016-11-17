package eu.openminted.content.connector;

import eu.openminted.registry.domain.DocumentMetadataRecord;
import org.junit.Test;

import java.util.List;

public class ParserTest {
    @Test
    public void getPublicationsTest() {
        String url = "http://api.openaire.eu/search/publications?size=1";
        Parser parser = Parser.initialize(url);
        List<DocumentMetadataRecord> publications = parser.getOMTDPublications();
        assert (publications.size() == 1);
    }
}
