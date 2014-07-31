/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nutch.protocol.htmlunit;

// JDK imports
import java.io.IOException;
import java.net.URL;

// Hadoop imports
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
// Nutch imports
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.net.protocols.Response;
import org.apache.nutch.protocol.ProtocolException;
import org.apache.nutch.protocol.RobotRulesParser;
import org.apache.nutch.protocol.http.api.HttpBase;
import org.apache.nutch.util.DeflateUtils;
import org.apache.nutch.util.GZIPUtils;
import org.apache.nutch.util.NutchConfiguration;
// Commons Logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.robots.BaseRobotRules;

public class Http extends HttpBase {

    public static final Logger LOG = LoggerFactory.getLogger(Http.class);

    /**
     * Public default constructor.
     */
    public Http() {
        super(LOG);
    }

    /**
     * Set the {@link org.apache.hadoop.conf.Configuration}
     * object.
     * @param conf
     */
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

    protected Response getResponse(URL url, CrawlDatum datum, boolean redirect) throws ProtocolException, IOException {
        return new HttpResponse(this, url, datum);
    }

    @Override
    public BaseRobotRules getRobotRules(Text url, CrawlDatum datum) {
        //不好意思，耍流氓了。如果你是正当用途请删除此方法。
        return RobotRulesParser.EMPTY_RULES;
    }
}
