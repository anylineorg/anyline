/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.data.easysearch.metadata;

import java.util.LinkedHashMap;

/**
 * 对应index.setting[.index].analysis
 */
public class EasysearchAnalysis {
    private LinkedHashMap<String, EasysearchFilter> filters = new LinkedHashMap<>();
    private LinkedHashMap<String, EasysearchAnalyzer> analyzers = new LinkedHashMap<>();

    public LinkedHashMap<String, EasysearchFilter> getFilters() {
        return filters;
    }

    public EasysearchAnalysis setFilters(LinkedHashMap<String, EasysearchFilter> filters) {
        this.filters = filters;
        return this;
    }
    public EasysearchAnalysis addFilter(String key, EasysearchFilter filter) {
        this.filters.put(key, filter);
        return this;
    }

    public LinkedHashMap<String, EasysearchAnalyzer> getAnalyzers() {
        return analyzers;
    }

    public EasysearchAnalysis setAnalyzers(LinkedHashMap<String, EasysearchAnalyzer> analyzers) {
        this.analyzers = analyzers;
        return this;
    }
    public EasysearchAnalysis addAnalyzer(String key, EasysearchAnalyzer analyzer) {
        this.analyzers.put(key, analyzer);
        return this;
    }
    public LinkedHashMap<String, Object> map() {
        LinkedHashMap<String, Object> map =new LinkedHashMap<>();
        if(null != filters && !filters.isEmpty()) {
            LinkedHashMap<String, Object> filter = new LinkedHashMap<>();
            map.put("filter", filter);
            for(String key:filters.keySet()) {
                filter.put(key, filters.get(key).map());
            }
        }
        if(null != analyzers && !analyzers.isEmpty()) {
            LinkedHashMap<String, Object> analyzer = new LinkedHashMap<>();
            map.put("analyzer", analyzer);
            for(String key:analyzers.keySet()) {
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