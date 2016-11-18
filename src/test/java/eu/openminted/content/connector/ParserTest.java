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

    @Test
    public void justATest() {
        int from = 150;
        int to = 310;

        int size = to - from;
        if (to % size != 0) {
            System.out.println("oups");
        }
        int page = (to / size);
        System.out.println("from=" + from);
        System.out.println("to=" + to);


//        if (from < size) {
//            size = to;
//            page = 1;
//        } else {
//            System.out.println("ceiling[to/from]=" + Math.floor((double) to/from) + " else " + (double)to/from);
//            page = (int)Math.ceil((double) to/from);
//            size = to / page;
//
////            page = (to / size);
////            size = to - size;
//        }
        System.out.println("size=" + size);
        System.out.println("page=" + page);
    }
}
