Nutch Htmlunit Plugin
==============

### 项目简介

基于Apache Nutch 1.8和Htmlunit组件，实现对于AJAX加载类型页面的完整页面内容抓取解析。

According to the implementation of Apache Nutch 1.8, we can't get dynamic HTML information from fetch pages including AJAX requests as it will ignore all AJAX requests.

This plugin will use Htmlunit to fetch whole page content with necessary dynamic AJAX requests. It developed and tested with Apache Nutch 1.8, you can try it on other Nutch version or refactor the source codes as your design.

### 主要特性

* **常规的HTML页面抓取**: 对于常规的例如新闻类没有AJAX特性的页面可以直接用Nutch自带的protocol-http插件抓取。

* **常规的AJAX页面抓取**: 对于绝大部分诸如jQuery ajax加载的页面，可以直接用protocol-htmlunit插件抓取。

* **特殊的AJAX请求页面抓取**: 诸如淘宝/天猫的页面采用了独特的Kissy Javascript组件，
导致htmlunit无法直接感知到需要等待Kissy发起的请求完成，通过等待页面加载解析内容判断处理实现此类页面数据抓取。

* **基于页面滚动的AJAX请求页面抓取**: 诸如淘宝/天猫的商品详情页面会基于页面滚动发起商品描述信息的加载，
通过protocol-htmlunit扩展处理可以实现此类页面数据抓取。

### 运行体验

由于Nutch运行是基于Unix/Linux环境的，请自行准备Unix/Linux系统或Cygwin运行环境。

git clone整个工程代码后，进行本地git下载目录：

cd nutch-htmlunit/runtime/local

bin/crawl urls crawl false 1  

//urls参数为爬虫入库url文件目录; crawl为爬虫输出目录; false本应为solr索引url参数，此处设置为false不做solr索引处理; 1为爬虫执行回数

运行结束后可以看到天猫商品页面的价格/描述/滚动加载的图片等所有信息都已经完整获取到。

运行日志输入示例参考：http://git.oschina.net/xautlx/nutch-htmlunit/wikis/Log

### 扩展插件说明

* **protocol-htmlunit**: 基于Htmlunit实现的AJAX页面Fetcher插件

* **parse-s2jh**: 基于XPath解析页面元素内容; 基于数据库模式输出解析到结构化数据; 对于个别复杂类型AJAX页面定制判断页面加载完成的回调判断逻辑

* **index-s2jh**: 追加设置需要额外传递给solr索引的属性数据; 设定不需要索引的页面规则;

### 源码工程说明

整个工程基于Apache Nutch 1.8源码工程扩展插件实现，插件的定义和配置与官方插件处理模式一致，具体可参考Apache Nutch 1.8官方文档资料。
具体实现原理和代码，请自行导入Eclipse工程查看即可。

### 开源许可说明

* 开源协议

本项目所有代码完整开源，在保留标识本项目来源信息以及保证不对本项目进行非授权的销售行为的前提下，可以以任意方式自由使用：开源、非开源、商业及非商业。

* 收费服务

如果你希望提供基于Apache Nutch/Solr/Lucene等系列技术的定制的扩展实现/技术咨询服务/毕业设计指导/二次开发项目指导等任何有兴趣的合作形式，可以联系 E-Mail: xautlx@hotmail.com 或 QQ: 2414521719 (加Q请注明：nutch/solr/lucene) 协商收费服务。[上述联系方式恕不直接提供免费的技术咨询类询问，若对项目有任何技术问题或Issue反馈，请直接提交到项目站点提问或Git平台的Issue]

### Reference

欢迎关注作者其他项目：

* [S2JH](https://github.com/xautlx/s2jh) -  基于SSH的企业Web应用开发框架

* [12306 Hunter](https://github.com/xautlx/12306-hunter) - （功能已失效不可用，不过还可以当作Swing开发样列参考只用）Java Swing C/S版本12306订票助手，用处你懂的