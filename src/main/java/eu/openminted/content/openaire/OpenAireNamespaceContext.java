package eu.openminted.content.openaire;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

public class OpenAireNamespaceContext implements NamespaceContext {
    @Override
    public String getNamespaceURI(String prefix) {

        if (prefix == null) {
            throw new NullPointerException("Null prefix");

        } else if ("xlink".equals(prefix)) {
            return "http://www.w3.org/1999/xlink";

        } else if ("gml".equals(prefix)) {
            return "http://www.opengis.net/gml/3.2";

        } else if ("gmd".equals(prefix)) {
            return "http://www.isotc211.org/2005/gmd";

        } else if ("xsi".equals(prefix)) {
            return "http://www.w3.org/2001/XMLSchema-instance";

        } else if ("xml".equals(prefix)) {
            return XMLConstants.XML_NS_URI;

        } else if ("xs".equals(prefix)) {
            return "http://www.w3.org/2001/XMLSchema";

        } else if ("dri".equals(prefix)) {
            return "http://www.driver-repository.eu/namespace/dri";
        }

        return XMLConstants.DEFAULT_NS_PREFIX;
    }

    // This method isn't necessary for XPath processing.
    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    // This method isn't necessary for XPath processing either.
    public Iterator<Object> getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }
}
