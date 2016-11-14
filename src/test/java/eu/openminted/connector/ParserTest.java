package eu.openminted.connector;

import org.junit.Test;

public class ParserTest {
    @Test
    public void executeTest() {
        Parser parser = new Parser();
        String url = "http://api.openaire.eu/search/publications?size=5";
        int items = parser.execute(url);

        assert (items == 5);
    }
}
