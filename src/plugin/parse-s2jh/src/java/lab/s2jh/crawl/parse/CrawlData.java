package lab.s2jh.crawl.parse;

import java.math.BigDecimal;
import java.util.Date;

public class CrawlData {
    private String url;
    private String code;
    private String name;
    private String category;
    private Integer orderIndex;
    private String textValue;
    private String htmlValue;
    private BigDecimal numValue;
    private Date dateValue;

    public CrawlData(String url, String code) {
        super();
        this.url = url;
        this.code = code;
    }

    public CrawlData(String url, String code, String name) {
        super();
        this.url = url;
        this.code = code;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public String getCode() {
        return code;
    }

    public CrawlData setCode(String code) {
        this.code = code;
        return this;
    }

    public String getName() {
        return name;
    }

    public CrawlData setName(String name) {
        this.name = name;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public CrawlData setCategory(String category) {
        this.category = category;
        return this;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public CrawlData setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
        return this;
    }

    public String getTextValue() {
        return textValue;
    }

    public CrawlData setTextValue(String textValue) {
        this.textValue = textValue;
        return this;
    }

    public String getHtmlValue() {
        return htmlValue;
    }

    public CrawlData setHtmlValue(String htmlValue) {
        this.htmlValue = htmlValue;
        return this;
    }

    public BigDecimal getNumValue() {
        return numValue;
    }

    public CrawlData setNumValue(BigDecimal numValue) {
        this.numValue = numValue;
        return this;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public CrawlData setDateValue(Date dateValue) {
        this.dateValue = dateValue;
        return this;
    }

    public String getDisplayValue() {
        if (textValue != null) {
            return textValue;
        }
        if (htmlValue != null) {
            if (htmlValue.length() > 30) {
                return htmlValue.substring(0, 30);
            } else {
                return htmlValue;
            }
        }
        if (numValue != null) {
            return numValue.toString();
        }
        if (dateValue != null) {
            return dateValue.toString();
        }
        return "Undefined";
    }
}
