package org.anyline.data.elasticsearch.metadata;

import java.util.ArrayList;
import java.util.List;

public class ElasticSearchAnalyzer {
    private String tokenizer;
    private List<String> filters = new ArrayList<>();

    public String getTokenizer() {
        return tokenizer;
    }

    public ElasticSearchAnalyzer setTokenizer(String tokenizer) {
        this.tokenizer = tokenizer;
        return this;
    }

    public List<String> getFilters() {
        return filters;
    }

    public ElasticSearchAnalyzer setFilters(List<String> filters) {
        this.filters = filters;
        return this;
    }
    public ElasticSearchAnalyzer addFilter(String filter) {
        filters.add(filter);
        return this;
    }
}