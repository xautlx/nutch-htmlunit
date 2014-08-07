Nutch Htmlunit Plugin
==============

### 项目简介

基于Apache Nutch 1.8和Htmlunit组件，实现对于AJAX加载类型页面的完整页面内容抓取解析。

### 主要特性

* **常规的HTML页面**: 对于常规的例如新闻类没有AJAX特性的页面可以直接用Nutch自带的protocol-http插件抓取

* **常规的AJAX页面**: 对于绝大部分诸如jQuery ajax加载的页面，可以直接用protocol-htmlunit插件抓取



```

<property>
  <name>plugin.includes</name>
  <value>protocol-htmlunit|urlfilter-regex|parse-...</value>
  <description>Regular expression naming plugin directory names to
  include.  Any plugin not matching this expression is excluded.
  In any case you need at least include the nutch-extensionpoints plugin. By
  default Nutch includes crawling just HTML and plain text via HTTP,
  and basic indexing and search plugins. In order to use HTTPS please enable 
  protocol-httpclient, but be aware of possible intermittent problems with the 
  underlying commons-httpclient library.
  </description>
</property>

```

* Optionally, you can config apache-nutch-2.1/conf/regex-urlfilter.txt to control htmlunit only fetch specified urls including internal AJAX request. 
See detail: https://github.com/xautlx/nutch-htmlunit/blob/master/src/plugin/lib-htmlunit/src/java/org/apache/nutch/protocol/htmlunit/RegexHttpWebConnection.java

* That's all. Now you can execute: apache-nutch-2.1/bin/nutch crawl urls, and see page contents parsed by htmlunit.

### Contact Author

* E-Mail: xautlx@hotmail.com