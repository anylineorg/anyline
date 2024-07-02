package org.anyline.data.elasticsearch.metadata;

import org.anyline.metadata.Table;

import java.util.LinkedHashMap;

public class ElasticSearchIndex extends Table {
    private Integer numberOfShards;
    private Integer numberOfReplicas;
    //index.store.type 后续版本会删除
    private ElasticSearchAnalysis analysis = null;

    public Integer getNumberOfShards() {
        return numberOfShards;
    }

    public void setNumberOfShards(Integer numberOfShards) {
        this.numberOfShards = numberOfShards;
    }

    public Integer getNumberOfReplicas() {
        return numberOfReplicas;
    }

    public void setNumberOfReplicas(Integer numberOfReplicas) {
        this.numberOfReplicas = numberOfReplicas;
    }

    public ElasticSearchAnalysis getAnalyzers() {
        return analysis;
    }

    public void setAnalyzers(ElasticSearchAnalysis analyzers) {
        this.analysis = analyzers;
    }
    public LinkedHashMap<String, Object> map(){
        LinkedHashMap<String, Object> map =new LinkedHashMap<>();
        if(null != analysis){
            LinkedHashMap<String, Object> setting = new LinkedHashMap<>();
            map.put("setting", setting);
            setting.put("analysis", analysis.map());
            if(null != numberOfReplicas){
                setting.put("number_of_replicas", numberOfReplicas);
            }
            if(null != numberOfShards){
                setting.put("number_of_shards", numberOfShards);
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