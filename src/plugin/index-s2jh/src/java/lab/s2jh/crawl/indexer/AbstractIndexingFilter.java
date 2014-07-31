package lab.s2jh.crawl.indexer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.parse.Parse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractIndexingFilter implements IndexingFilter {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractIndexingFilter.class);

    private Configuration conf;

    @Override
    public Configuration getConf() {
        return conf;
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public NutchDocument filter(NutchDocument doc, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks)
            throws IndexingException {
        LOG.debug("Invoking  indexer {} for url: {}", this.getClass().getName(), url);

        if (doc == null) {
            LOG.debug("Skipped as NutchDocument doc is null");
            return doc;
        }

        return filterInternal(doc, parse, url, datum, inlinks);
    }

    protected String cancelIndexRegex() {
        return null;
    }

    public abstract NutchDocument filterInternal(NutchDocument doc, Parse parse, Text url, CrawlDatum datum,
            Inlinks inlinks);
}
