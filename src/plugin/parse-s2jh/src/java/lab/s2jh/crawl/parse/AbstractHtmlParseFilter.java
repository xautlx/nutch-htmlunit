package lab.s2jh.crawl.parse;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.HtmlParseFilter;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.StringUtil;
import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public abstract class AbstractHtmlParseFilter implements HtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractHtmlParseFilter.class);

    private static final long start = System.currentTimeMillis(); // start time of fetcher run

    private AtomicInteger pages = new AtomicInteger(0); // total pages fetched

    private Pattern filterPattern;

    protected Transformer transformer;

    protected static NodeList selectNodeList(Node node, String xpath) {
        try {
            return XPathAPI.selectNodeList(node, xpath);
        } catch (TransformerException e) {
            LOG.warn("Bad 'xpath' expression [{}]", xpath);
        }
        return null;
    }

    protected Node selectSingleNode(Node contextNode, String xpath) {
        try {
            return XPathAPI.selectSingleNode(contextNode, xpath);
        } catch (TransformerException e) {
            LOG.warn("Bad 'xpath' expression [{}]", xpath);
        }
        return null;
    }

    protected String getXPathValue(Node contextNode, String xpath) {
        return getXPathValue(contextNode, xpath, null);
    }

    protected String getXPathValue(Node contextNode, String xpath, String defaultVal) {
        Node node = selectSingleNode(contextNode, xpath);
        if (node == null) {
            return defaultVal;
        } else {
            String txt = null;
            if (node instanceof Text) {
                txt = node.getNodeValue();
            } else {
                txt = node.getTextContent();
            }
            return cleanInvisibleChar(txt);
        }
    }

    /**
     * 处理不同src图片属性格式，返回统一格式的http格式的图片URL
     * @param url
     * @param imgSrc
     * @return
     */
    protected String parseImgSrc(String url, String imgSrc) {
        if (StringUtils.isBlank(imgSrc)) {
            return null;
        }
        imgSrc = imgSrc.trim();
        //去掉链接最后的#号
        imgSrc = StringUtils.substringBefore(imgSrc, "#");
        if (imgSrc.startsWith("http")) {
            return imgSrc;
        } else if (imgSrc.startsWith("/")) {
            if (url.indexOf(".com") > -1) {
                return StringUtils.substringBefore(url, ".com/") + ".com" + imgSrc;
            } else if (url.indexOf(".net") > -1) {
                return StringUtils.substringBefore(url, ".net/") + ".net" + imgSrc;
            } else {
                throw new RuntimeException("Undefined site domain suffix");
            }
        } else {
            return StringUtils.substringBeforeLast(url, "/") + "/" + imgSrc;
        }
    }

    /**
     * 清除无关的不可见空白字符
     * @param str
     * @return
     */
    protected String cleanInvisibleChar(String str) {
        if (str != null) {
            str = StringUtils.remove(str, (char) 160);
            //str = StringUtils.remove(str, " ");
            str = StringUtils.remove(str, "\r");
            str = StringUtils.remove(str, "\n");
            str = StringUtils.remove(str, "\t");
            str = StringUtils.remove(str, "\\s*");
            str = StringUtil.cleanField(str);
            str = str.trim();
        }
        return str;
    }

    /**
     * 清除无关的Node节点元素
     * @param str
     * @return
     */
    protected void cleanUnusedNodes(Node doc) {
        cleanUnusedNodes(doc, "//STYLE");
        cleanUnusedNodes(doc, "//MAP");
        cleanUnusedNodes(doc, "//SCRIPT");
        cleanUnusedNodes(doc, "//script");
    }

    /**
     * 清除无关的Node节点元素
     * @param str
     * @return
     */
    protected void cleanUnusedNodes(Node node, String xpath) {
        try {
            NodeList nodes = XPathAPI.selectNodeList(node, xpath);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                element.getParentNode().removeChild(element);
            }
        } catch (DOMException e) {
            throw new IllegalStateException(e);
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    private Configuration conf;

    public void setConf(Configuration conf) {
        this.conf = conf;
        this.filterPattern = Pattern.compile(setupFilterRegex());
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Configuration getConf() {
        return this.conf;
    }

    @Override
    public ParseResult filter(Content content, ParseResult parseResult, HTMLMetaTags metaTags, DocumentFragment doc) {
        String url = content.getUrl();
        LOG.debug("Invoking parse  {} for url: {}", this.getClass().getName(), url);
        try {
            //URL匹配
            if (!filterPattern.matcher(url).find()) {
                LOG.debug("Skipped {} as not match regex [{}]", this.getClass().getName(), setupFilterRegex());
                return parseResult;
            }

            if (content.getContent() == null) {
                LOG.warn("Empty content for url: {}", url);
                return parseResult;
            }

            //清除无关的Node节点元素
            cleanUnusedNodes(doc);

            LOG.debug("--------HTML--------: {}", asString(doc));

            pages.incrementAndGet();
            parseResult = filterInternal(content, parseResult, metaTags, doc);

            if (LOG.isInfoEnabled()) {
                long elapsed = (System.currentTimeMillis() - start) / 1000;
                float avgPagesSec = (float) pages.get() / elapsed;
                LOG.info(" - Custom prased total " + pages.get() + " pages, " + elapsed + " seconds, avg "
                        + avgPagesSec + " pages/s");
            }
            return parseResult;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    protected String asString(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("null 'node' arg in method call.");
        }
        try {
            StringWriter writer = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            String xml = writer.toString();
            return xml;
        } catch (Exception e) {
            throw new IllegalArgumentException("error for parse node to string.", e);
        }
    }

    private static final String selectSQL = "SELECT count(*) from crawl_data where url=?";
    private static final String deleteSQL = "DELETE from crawl_data where url=?";
    private static final String insertSQL = "INSERT INTO crawl_data(url, code, name, category, order_index, fetch_time, text_value, html_value,date_value, num_value) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?)";

    protected void saveCrawlData(String url, List<CrawlData> crawlDatas) {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                PreparedStatement selectPS = conn.prepareStatement(selectSQL);
                selectPS.setString(1, url);
                ResultSet rs = selectPS.executeQuery();
                if (rs.next()) {
                    int cnt = rs.getInt(1);
                    rs.close();
                    selectPS.close();
                    if (cnt > 0) {
                        LOG.debug("Cleaning exists properties for url: {}", url);
                        PreparedStatement deletePS = conn.prepareStatement(deleteSQL);
                        deletePS.setString(1, url);
                        deletePS.execute();
                        deletePS.close();
                    }
                    LOG.debug("Saving properties for url: {}", url);
                    PreparedStatement insertPS = conn.prepareStatement(insertSQL);
                    int idx = 10;
                    for (CrawlData crawlData : crawlDatas) {
                        if (!crawlData.getUrl().equals(url)) {
                            LOG.error("Invalid crawlData not match url: {}", url);
                            continue;
                        }
                        LOG.debug(" - {} : {}", crawlData.getCode(), crawlData.getDisplayValue());
                        insertPS.setString(1, crawlData.getUrl());
                        insertPS.setString(2, crawlData.getCode());
                        insertPS.setString(3, crawlData.getName());
                        insertPS.setString(4, crawlData.getCategory());
                        if (crawlData.getOrderIndex() != null) {
                            insertPS.setInt(5, crawlData.getOrderIndex());
                        } else {
                            insertPS.setInt(5, idx);
                        }
                        insertPS.setDate(6, new java.sql.Date(new Date().getTime()));
                        insertPS.setString(7, crawlData.getTextValue());
                        insertPS.setString(8, crawlData.getHtmlValue());
                        if (crawlData.getDateValue() != null) {
                            insertPS.setDate(9, new java.sql.Date(crawlData.getDateValue().getTime()));
                        } else {
                            insertPS.setDate(9, null);
                        }
                        if (crawlData.getNumValue() != null) {
                            insertPS.setBigDecimal(10, crawlData.getNumValue());
                        } else {
                            insertPS.setBigDecimal(10, null);
                        }
                        insertPS.addBatch();
                        idx += 10;
                    }
                    insertPS.executeBatch();
                    insertPS.close();
                }
            } catch (Exception e) {
                LOG.error("Error to get jdbc operation", e);
            } finally {
                try {
                    conn.close();
                } catch (Exception e) {
                    LOG.error("Error to close jdbc connection", e);
                }
            }
        } else {
            LOG.warn("Database save ignored as NO jdbc connection");
        }
    }

    private static int sqlConnectionCounter = 0;

    private static Connection getConnection() {
        if (sqlConnectionCounter > 5) {
            return null;
        }
        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/nutch", "nutch", "nutch");
            sqlConnectionCounter = 0;
        } catch (Exception e) {
            sqlConnectionCounter++;
            if (LOG.isDebugEnabled()) {
                LOG.error("Error to get jdbc connection", e);
            }
        }
        return con;
    }

    /**
     * 设置当前解析过滤器匹配的URL正则表达式
     * 只有匹配的url才调用当前解析处理逻辑
     * @return
     */
    public abstract String setupFilterRegex();

    /**
     * 子类实现具体的页面数据解析逻辑
     * @param content
     * @param parseResult
     * @param metaTags
     * @param doc
     * @return
     */
    public abstract ParseResult filterInternal(Content content, ParseResult parseResult, HTMLMetaTags metaTags,
            DocumentFragment doc);

    /**
     * 检测url获取页面内容是否已加载完毕，主要用于支持一些AJAX页面延迟等待加载
     * 返回false则表示告知Fetcher处理程序继续AJAX执行短暂等待后再回调此方法直到返回true标识内容已加载完毕
     * @param fetchUrl
     * @param page
     * @return 默认返回true，子类根据需要定制判断逻辑
     */
    public boolean isParseDataFetchLoaded(String fetchUrl, HtmlPage page) {
        //首先判断url是否匹配当前过滤器，如果是则继续调用内容判断逻辑
        if (filterPattern.matcher(fetchUrl).find()) {
            return isParseDataFetchLoaded(page);
        }
        return true;
    }

    /**
     * 检测url获取页面内容是否已加载完毕，主要用于支持一些AJAX页面延迟等待加载
     * 返回false则表示告知Fetcher处理程序继续AJAX执行短暂等待后再回调此方法直到返回true标识内容已加载完毕
     * @param page
     * @return 默认返回true，子类根据需要定制判断逻辑
     */
    protected boolean isParseDataFetchLoaded(HtmlPage page) {
        return true;
    }
}
