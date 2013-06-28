package org.apache.nutch.protocol.htmlunit;

// JDK imports
import java.io.IOException;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.net.protocols.Response;
import org.apache.nutch.protocol.ProtocolException;
import org.apache.nutch.protocol.htmlunit.HttpResponse;
import org.apache.nutch.protocol.http.api.HttpBase;
import org.apache.nutch.util.NutchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Commons Logging imports
// Hadoop imports
// Nutch imports


public class Http extends HttpBase {

  public static final Logger LOG = LoggerFactory.getLogger(Http.class);


  public Http() {
    super(LOG);
  }

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

  protected Response getResponse(URL url, CrawlDatum datum, boolean redirect)
    throws ProtocolException, IOException {
    return new HttpResponse(this, url, datum,getConf());
  }

}
