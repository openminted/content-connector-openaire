package eu.openminted.content.connector;

import eu.openminted.registry.domain.DocumentMetadataRecord;
import org.junit.Test;

import java.util.List;

public class ParserTest {
    @Test
    public void getPublicationsTest() {
        Parser parser = new Parser();
        String url = "http://api.openaire.eu/search/publications?size=1";
        List<DocumentMetadataRecord> publications = parser.getPublications(url);
        assert (publications.size() == 1);
    }
}
