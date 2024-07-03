package org.anyline.data.elasticsearch.metadata;

import org.anyline.metadata.Table;

import java.util.LinkedHashMap;

public class ElasticSearchIndex extends Table {
    private Integer numberOfShards;
    private Integer numberOfReplicas;
    //index.store.type 后续版本会删除
    private ElasticSearchAnalysis analysis = null;

    public ElasticSearchIndex(){}
    public ElasticSearchIndex(String name){
        super(name);
    }
    public Integer getNumberOfShards() {
        return numberOfShards;
    }

    public ElasticSearchIndex setNumberOfShards(Integer numberOfShards) {
        this.numberOfShards = numberOfShards;
        return this;
    }

    public Integer getNumberOfReplicas() {
        return numberOfReplicas;
    }

    public ElasticSearchIndex setNumberOfReplicas(Integer numberOfReplicas) {
        this.numberOfReplicas = numberOfReplicas;
        return this;
    }

    public ElasticSearchAnalysis getAnalysis() {
        return analysis;
    }

    public ElasticSearchIndex setAnalysis(ElasticSearchAnalysis analysis) {
        this.analysis = analysis;
        return this;
    }

    public LinkedHashMap<String, Object> map(){
        LinkedHashMap<String, Object> map =new LinkedHashMap<>();
        if(null != analysis){
            LinkedHashMap<String, Object> settings = new LinkedHashMap<>();
            map.put("settings", settings);
            settings.put("analysis", analysis.map());
            if(null != numberOfReplicas){
                settings.put("number_of_replicas", numberOfReplicas);
            }
            if(null != numberOfShards){
                settings.put("number_of_shards", numberOfShards);
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