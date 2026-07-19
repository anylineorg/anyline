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


package org.anyline.data.opensearch.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 对应index.setting[.index].analysis.filter
 */
public class OpenSearchFilter {
    private String key;
    private String type;
    private String synonymsPath;   // 同义词文件path
    private String synonymsSet;
    private Boolean updateAble;
    private Boolean expand;
    private Boolean lenient;
    private List<String> stopWords;
    private List<String> synonyms = new ArrayList<>(); //同义词 ["pc => personal computer", "computer, pc, laptop"]
    private List<String> filters = new ArrayList<>(); //Multiplexer token filter

    public String getType() {
        return type;
    }

    public OpenSearchFilter setType(String type) {
        this.type = type;
        return this;
    }

    public String getSynonymsPath() {
        return synonymsPath;
    }

    public OpenSearchFilter setSynonymsPath(String synonymsPath) {
        this.synonymsPath = synonymsPath;
        return this;
    }

    public String getSynonymsSet() {
        return synonymsSet;
    }

    public OpenSearchFilter setSynonymsSet(String synonymsSet) {
        this.synonymsSet = synonymsSet;
        return this;
    }

    public Boolean getUpdateAble() {
        return updateAble;
    }

    public OpenSearchFilter setUpdateAble(Boolean updateAble) {
        this.updateAble = updateAble;
        return this;
    }

    public Boolean getExpand() {
        return expand;
    }

    public OpenSearchFilter setExpand(Boolean expand) {
        this.expand = expand;
        return this;
    }

    public Boolean getLenient() {
        return lenient;
    }

    public OpenSearchFilter setLenient(Boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    public List<String> getStopWords() {
        return stopWords;
    }

    public OpenSearchFilter setStopWords(List<String> stopWords) {
        this.stopWords = stopWords;
        return this;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public OpenSearchFilter setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
        return this;
    }

    public List<String> getFilters() {
        return filters;
    }

    public OpenSearchFilter setFilters(List<String> filters) {
        this.filters = filters;
        return this;
    }

    /**
     * 添加同义词
     * @param synonym 同义词  ["pc =&gt; personal computer", "computer, pc, laptop"]
     * @return this
     */
    public OpenSearchFilter addSynonym(String synonym) {
        synonyms.add(synonym);
        return this;
    }

    public String getKey() {
        return key;
    }

    public OpenSearchFilter setKey(String key) {
        this.key = key;
        return this;
    }

    public LinkedHashMap<String, Object> map() {
        LinkedHashMap<String, Object> map =new LinkedHashMap<>();
        if(null != type) {
            map.put("type", type);
        }
        if(null != synonymsPath) {
            map.put("synonyms_path", synonymsPath);
        }
        if(null != synonymsSet) {
            map.put("synonyms_set", synonymsSet);
        }
        if(null != updateAble) {
            map.put("updateable", updateAble);
        }
        if(null != synonyms && !synonyms.isEmpty()) {
            map.put("synonyms", synonyms);
        }
        if(null != lenient) {
            map.put("lenient", lenient);
        }
        if(null != expand) {
            map.put("expand", expand);
        }
        if(null != filters && !filters.isEmpty()) {
            map.put("filters", filters);
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