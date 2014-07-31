package lab.s2jh.crawl.indexer;

import java.util.regex.Pattern;

import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.parse.Parse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S2jhDiscardIndexingFilter extends AbstractIndexingFilter {

    public static final Logger LOG = LoggerFactory.getLogger(S2jhDiscardIndexingFilter.class);

    //http://detail.tmall.com/item.htm?spm=a220o.1000855.w5003-5270320300.15.NhnkIC&id=36641396665&mt&scene=taobao_shop
    private Pattern keepIndexPattern = Pattern.compile("^http://detail.tmall.com/item.htm.*id=.*");

    @Override
    public NutchDocument filterInternal(NutchDocument doc, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks) {

        if (!keepIndexPattern.matcher(url.toString()).find()) {
            LOG.debug("Cancel index for {} as not match regex [{}]", url, keepIndexPattern);
            doc = null;
            return null;
        }

        return doc;
    }
}
