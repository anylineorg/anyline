package org.anyline.data.elasticsearch.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * 对应index.setting[.index].analysis.analyzer
 */
public class ElasticSearchAnalyzer {
    private String key;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public LinkedHashMap<String, Object> map(){
        LinkedHashMap<String, Object> map =new LinkedHashMap<>();
        if(null != tokenizer){
            map.put("tokenizer", tokenizer);
        }
        if(null != filters && !filters.isEmpty()){
            map.put("filter", filters);
        }
        return map;
    }
}/*
"settings": {
    "analysis": {
        "filter": {
            "us_synonym_filter": {
              "type": "synonym_graph",
              "synonyms_path": "analysis/us_synonym.txt"
            }
        },
          "analyzer": {
            "us_max_word": {
              "tokenizer": "ik_max_word",
              "filter": [
                "us_synonym_filter"
              ]
            },
            "us_smart": {
              "tokenizer": "ik_smart",
              "filter": [
                "us_synonym_filter"
              ]
            }
        }
    }
  }*/