package org.anyline.data.elasticsearch.metadata;

import java.util.LinkedHashMap;

/**
 * 对应index.setting[.index].analysis
 */
public class ElasticSearchAnalysis {
    private LinkedHashMap<String, ElasticSearchFilter> filters = new LinkedHashMap<>();
    private LinkedHashMap<String, ElasticSearchAnalyzer> analyzers = new LinkedHashMap<>();

    public LinkedHashMap<String, ElasticSearchFilter> getFilters() {
        return filters;
    }

    public ElasticSearchAnalysis setFilters(LinkedHashMap<String, ElasticSearchFilter> filters) {
        this.filters = filters;
        return this;
    }
    public ElasticSearchAnalysis addFilter(String key, ElasticSearchFilter filter){
        this.filters.put(key, filter);
        return this;
    }

    public LinkedHashMap<String, ElasticSearchAnalyzer> getAnalyzers() {
        return analyzers;
    }

    public ElasticSearchAnalysis setAnalyzers(LinkedHashMap<String, ElasticSearchAnalyzer> analyzers) {
        this.analyzers = analyzers;
        return this;
    }
    public ElasticSearchAnalysis addAnalyzer(String key, ElasticSearchAnalyzer analyzer) {
        this.analyzers.put(key, analyzer);
        return this;
    }
    public LinkedHashMap<String, Object> map(){
        LinkedHashMap<String, Object> map =new LinkedHashMap<>();
        if(null != filters && !filters.isEmpty()){
            LinkedHashMap<String, Object> filter = new LinkedHashMap<>();
            map.put("filter", filter);
            for(String key:filters.keySet()){
                filter.put(key, filters.get(key).map());
            }
        }
        if(null != analyzers && !analyzers.isEmpty()){
            LinkedHashMap<String, Object> analyzer = new LinkedHashMap<>();
            map.put("analyzer", analyzer);
            for(String key:analyzers.keySet()){
                analyzer.put(key, analyzers.get(key).map());
            }
        }
        return map;
    }
}
/*
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