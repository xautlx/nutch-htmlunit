package lab.s2jh.crawl.parse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.Text;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.protocol.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.collect.Lists;

public class S2jhHtmlParseFilter extends AbstractHtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(S2jhHtmlParseFilter.class);

    protected Pattern skuMatchPattern = Pattern.compile("http://detail.tmall.com/item.htm.*id=([^&]*)(&|$)");

    @Override
    public ParseResult filterInternal(Content content, ParseResult parseResult, HTMLMetaTags metaTags,
            DocumentFragment doc) {
        String url = content.getUrl();
        Parse parse = parseResult.get(new Text(url));
        //String text = parse.getText();
        ParseData parseData = parse.getData();

        //后续的index索引之用元数据
        Metadata parseMeta = parseData.getParseMeta();
        //数据库需要采集记录的数据信息集合
        //一个属性一行数据
        List<CrawlData> crawlDatas = Lists.newArrayList();

        String sku = null;
        Matcher matcher = this.skuMatchPattern.matcher(url);
        if (matcher.find()) {
            sku = matcher.group(1);
        }
        if (StringUtils.isBlank(sku)) {
            LOG.warn("SKU not parsed for url: " + url);
            return parseResult;
        }

        //数据库记录
        crawlDatas.add(new CrawlData(url, "sku").setTextValue(sku));
        //元数据数据
        parseMeta.add("sku", sku);

        String price = getXPathValue(doc, "//SPAN[@class='tm-price']");
        crawlDatas.add(new CrawlData(url, "price").setTextValue(price));
        parseMeta.add("price", price);

        LOG.info(" - SKU：{}, Parse Meta: {}", sku, parseMeta);

        NodeList nodes = selectNodeList(doc,
                "//DIV[@id='description']/DIV[@class='content ke-post']//IMG[@data-ks-lazyload]");
        LOG.info("Product description content image list: ");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String imgUrl = node.getAttributes().getNamedItem("data-ks-lazyload").getTextContent();
            LOG.info(" - {}", imgUrl);
        }

        saveCrawlData(url, crawlDatas);

        return parseResult;
    }

    @Override
    public String setupFilterRegex() {
        return "http://detail.tmall.com/item.htm.*id=.*";
    }

    @Override
    protected boolean isParseDataFetchLoaded(HtmlPage page) {
        HtmlDivision div = page.getFirstByXPath("//DIV[@id='description']/DIV[@class='content ke-post']");
        System.out.println("--------------------------------------" + div);
        if (div != null && div.getChildElementCount() > 0) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Product description content HTML: {}", asString(div));
            }
            return true;
        }
        return false;
    }
}
