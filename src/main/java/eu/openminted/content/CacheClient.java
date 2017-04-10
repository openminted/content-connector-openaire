package eu.openminted.content;

import eu.openminted.omtdcache.CacheDataID;
import eu.openminted.omtdcache.CacheDataIDMD5;
import eu.openminted.omtdcache.core.Cache;
import eu.openminted.omtdcache.core.CacheFactory;
import eu.openminted.omtdcache.core.CacheProperties;
import eu.openminted.omtdcache.core.Data;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class CacheClient {
    private static Logger log = Logger.getLogger(CacheClient.class.toString());

    private Cache myCache;
    private CacheDataID cacheDataIDProvider;

    public CacheClient(CacheProperties cacheProperties) {
        myCache = CacheFactory.getCache(cacheProperties);
        cacheDataIDProvider = new CacheDataIDMD5();
    }

    private boolean insertDocumentIntoCache(String omtdId) {
        try {
            String documentId = cacheDataIDProvider.getID(omtdId.getBytes());
            InputStream inputStream = new URL("http://adonis.athenarc.gr/pdfs/" + omtdId + ".pdf").openStream();

            if (inputStream != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                IOUtils.copy(inputStream, outputStream);

                Data data = new Data(outputStream.toByteArray());
                return myCache.putData(documentId, data);
            }

        } catch (IOException e) {
            log.error("Error inserting document", e);
        }
        return false;
    }

    public InputStream getDocument(String omtdId) {
        InputStream inputStream = null;
        try {
            String documentId = cacheDataIDProvider.getID(omtdId.getBytes());
            boolean existsInCache = myCache.contains(documentId);
            if (!existsInCache) {
                log.debug("document not found... Inserting document now!");
                existsInCache = insertDocumentIntoCache(omtdId);
            }

            if (existsInCache) {
                log.debug("document found... Retrieving document now!");
                Data retrievedData = myCache.getData(documentId);
                if (retrievedData != null) {
                    inputStream = new ByteArrayInputStream(retrievedData.getBytes());
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving document", e);
        }
        return inputStream;
    }
}
