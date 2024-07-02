package org.anyline.data.elasticsearch.metadata;

import org.anyline.metadata.Table;

import java.util.LinkedHashMap;

public class ElasticSearchIndex extends Table {
    private Settings settings;

    public Settings getSettings() {
        return settings;
    }

    public ElasticSearchIndex setSettings(Settings settings) {
        this.settings = settings;
        return this;
    }

    public class Settings{
        private LinkedHashMap<String, ElasticSearchFilter> filters = new LinkedHashMap<>();
        private LinkedHashMap<String, ElasticSearchAnalyzer> analyzers = new LinkedHashMap<>();

        public LinkedHashMap<String, ElasticSearchFilter> getFilters() {
            return filters;
        }

        public Settings setFilters(LinkedHashMap<String, ElasticSearchFilter> filters) {
            this.filters = filters;
            return this;
        }
        public Settings addFilter(String key, ElasticSearchFilter filter) {
            filters.put(key, filter);
            return this;
        }

        public LinkedHashMap<String, ElasticSearchAnalyzer> getAnalyzers() {
            return analyzers;
        }

        public Settings setAnalyzers(LinkedHashMap<String, ElasticSearchAnalyzer> analyzers) {
            this.analyzers = analyzers;
            return this;
        }
        public Settings addAnalyzer(String key, ElasticSearchAnalyzer analyzer) {
            this.analyzers.put(key, analyzer);
            return this;
        }
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