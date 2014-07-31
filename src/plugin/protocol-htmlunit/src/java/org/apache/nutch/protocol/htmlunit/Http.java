package org.apache.nutch.protocol.htmlunit;

// JDK imports
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
// import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.protocol.http.api.HttpBase;
// import org.apache.nutch.net.protocols.Response;
import org.apache.nutch.protocol.ProtocolException;
import org.apache.nutch.protocol.htmlunit.HttpResponse;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.storage.WebPage.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Commons Logging imports
// Hadoop imports
// Nutch imports


public class Http extends HttpBase {

  public static final Logger LOG = LoggerFactory.getLogger(Http.class);

  private static final Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

  static {
    FIELDS.add(WebPage.Field.MODIFIED_TIME);
    FIELDS.add(WebPage.Field.HEADERS);
  }

  public Http() {
    super(LOG);
  }

  @Override
  public void setConf(Configuration conf) {
    super.setConf(conf);
//    Level logLevel = Level.WARNING;
//    if (conf.getBoolean("http.verbose", false)) {
//      logLevel = Level.FINE;
//    }
//    LOG.setLevel(logLevel);
  }

  public static void main(String[] args) throws Exception {
    Http http = new Http();
    http.setConf(NutchConfiguration.create());
    main(http, args);
  }

  protected Response getResponse(URL url, WebPage page, boolean redirect)
    throws ProtocolException, IOException {
    LOG.info("fetching this url " + url); 
    return new HttpResponse(this, url, page, getConf());
  }

  @Override
  public Collection<WebPage.Field> getFields() {
    return FIELDS;
  }
}
