package org.apache.nutch.protocol.htmlunit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Default htmlunit will request all parsed HTTP requests (html, js, css, etc.)
 * But only a few requests used for parse specified information,
 * So introduce urlfilter support configurable htmlunit HTTP request list.
 */
public class RegexHttpWebConnection extends HttpWebConnection {

    public static final Logger LOG = LoggerFactory.getLogger(RegexHttpWebConnection.class);

    public static final String URLFILTER_REGEX_FILE = "urlfilter.regex.file";
    public static final String URLFILTER_REGEX_RULES = "urlfilter.regex.rules";

    /** An array of applicable rules */
    private List<Rule> rules;

    public RegexHttpWebConnection(WebClient webClient) {
        super(webClient);
    }

    public RegexHttpWebConnection(WebClient webClient, Configuration conf) {
        super(webClient);
        try {
            rules = readRules(getRulesReader(conf));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WebResponse getResponse(final WebRequest request) throws IOException {
        final URL url = request.getUrl();
        if (StringUtils.isBlank(filter(url.toString()))) {
            LOG.info("Thread: {}, - Http Excluding URL: {}", Thread.currentThread().getId(), url);
            return new StringWebResponse("", url);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Thread: {}, + Http Fetching URL: {}", Thread.currentThread().getId(), url);
        }
        return super.getResponse(request);
    }

    public String filter(String url) {
        for (Rule rule : rules) {
            if (rule.match(url)) {
                return rule.accept() ? url : null;
            }
        }
        ;
        return null;
    }

    /**
     * Rules specified as a config property will override rules specified
     * as a config file.
     */
    protected Reader getRulesReader(Configuration conf) throws IOException {
        String stringRules = conf.get(URLFILTER_REGEX_RULES);
        if (stringRules != null) {
            return new StringReader(stringRules);
        }
        String fileRules = conf.get(URLFILTER_REGEX_FILE);
        return conf.getConfResourceAsReader(fileRules);
    }

    protected Rule createRule(boolean sign, String regex) {
        return new Rule(sign, regex);
    }

    /**
     * Read the specified file of rules.
     * @param reader is a reader of regular expressions rules.
     * @return the corresponding {@RegexRule rules}.
     */
    private List<Rule> readRules(Reader reader) throws IOException, IllegalArgumentException {

        BufferedReader in = new BufferedReader(reader);
        List<Rule> rules = new ArrayList<Rule>();
        String line;

        while ((line = in.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            char first = line.charAt(0);
            boolean sign = false;
            switch (first) {
            case '+':
                sign = true;
                break;
            case '-':
                sign = false;
                break;
            case ' ':
            case '\n':
            case '#': // skip blank & comment lines
                continue;
            default:
                throw new IOException("Invalid first character: " + line);
            }

            String regex = line.substring(1);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Adding rule [" + regex + "]");
            }
            Rule rule = createRule(sign, regex);
            rules.add(rule);
        }
        return rules;
    }

    public class Rule {

        private final boolean sign;

        private Pattern pattern;

        Rule(boolean sign, String regex) {
            this.sign = sign;
            pattern = Pattern.compile(regex);
        }

        protected boolean accept() {
            return sign;
        }

        public boolean match(String url) {
            return pattern.matcher(url).find();
        }
    }
}
