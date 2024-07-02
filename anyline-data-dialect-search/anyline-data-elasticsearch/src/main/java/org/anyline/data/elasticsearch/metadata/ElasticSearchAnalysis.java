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

    public void setFilters(LinkedHashMap<String, ElasticSearchFilter> filters) {
        this.filters = filters;
    }

    public LinkedHashMap<String, ElasticSearchAnalyzer> getAnalyzers() {
        return analyzers;
    }

    public void setAnalyzers(LinkedHashMap<String, ElasticSearchAnalyzer> analyzers) {
        this.analyzers = analyzers;
    }
    public LinkedHashMap<String, Object> map(){
        LinkedHashMap<String, Object> map =new LinkedHashMap<>();
        if(null != filters && !filters.isEmpty()){
            for(String key:filters.keySet()){
                map.put(key, filters.get(key).map());
            }
        }
        if(null != analyzers && !analyzers.isEmpty()){
            for(String key:analyzers.keySet()){
                map.put(key, analyzers.get(key).map());
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