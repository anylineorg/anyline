package org.anyline.data.elasticsearch.metadata;

import org.anyline.metadata.Table;

import java.util.LinkedHashMap;

public class ElasticSearchIndex extends Table {
    private LinkedHashMap<String, ElasticSearchAnalyzer> analyzers = new LinkedHashMap<>();

    public LinkedHashMap<String, ElasticSearchAnalyzer> getAnalyzers() {
        return analyzers;
    }

    public ElasticSearchIndex setAnalyzers(LinkedHashMap<String, ElasticSearchAnalyzer> analyzers) {
        this.analyzers = analyzers;
        return this;
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