package lab.s2jh.crawl.indexer;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S2jhIndexingFilter extends AbstractIndexingFilter {

    public static final Logger LOG = LoggerFactory.getLogger(S2jhIndexingFilter.class);

    @Override
    public NutchDocument filterInternal(NutchDocument doc, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks) {
        ParseData parseData = parse.getData();

        String sku = parseData.getMeta("sku");
        if (StringUtils.isBlank(sku)) {
            return null;
        }

        doc.add("sku", sku);
        doc.add("price", parseData.getMeta("price"));

        return doc;
    }
}
