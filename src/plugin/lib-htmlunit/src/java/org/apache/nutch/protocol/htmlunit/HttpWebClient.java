package org.apache.nutch.protocol.htmlunit;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * Htmlunit WebClient Helper
 * Use one WebClient instance per thread by ThreadLocal to support multiple threads execution
 */
public class HttpWebClient {

    private static final Logger LOG = LoggerFactory.getLogger("org.apache.nutch.protocol");

    private static ThreadLocal<WebClient> threadWebClient = new ThreadLocal<WebClient>();

    public static HtmlPage getHtmlPage(String url, Configuration conf) {
        try {
            WebClient webClient = threadWebClient.get();
            if (webClient == null) {
                LOG.info("Initing web client for thread: {}", Thread.currentThread().getId());
                webClient = new WebClient(BrowserVersion.FIREFOX_24);
                webClient.getOptions().setCssEnabled(false);
                webClient.getOptions().setAppletEnabled(false);
                webClient.getOptions().setThrowExceptionOnScriptError(false);
                // AJAX support
                webClient.setAjaxController(new NicelyResynchronizingAjaxController());
                // Use extension version htmlunit cache process
                webClient.setCache(new ExtHtmlunitCache());
                // Enhanced WebConnection based on urlfilter
                webClient.setWebConnection(new RegexHttpWebConnection(webClient, conf));
                threadWebClient.set(webClient);
            }
            HtmlPage page = threadWebClient.get().getPage(url);
            //webClient.closeAllWindows();
            return page;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HtmlPage getHtmlPage(String url) {
        return getHtmlPage(url, null);
    }
}
