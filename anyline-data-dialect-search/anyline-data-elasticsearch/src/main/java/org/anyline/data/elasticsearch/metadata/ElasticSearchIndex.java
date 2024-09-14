/*
 * Copyright 2006-2023 www.anyline.org
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

package org.anyline.data.elasticsearch.metadata;

import org.anyline.metadata.Table;

import java.util.LinkedHashMap;

public class ElasticSearchIndex extends Table {
    protected String keyword = "index"            ;
    private Integer numberOfShards;
    private Integer numberOfReplicas;
    //index.store.type 后续版本会删除
    private ElasticSearchAnalysis analysis = null;

    public ElasticSearchIndex() {}
    public ElasticSearchIndex(String name) {
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

    public LinkedHashMap<String, Object> map() {
        LinkedHashMap<String, Object> map =new LinkedHashMap<>();
        if(null != analysis) {
            LinkedHashMap<String, Object> settings = new LinkedHashMap<>();
            map.put("settings", settings);
            settings.put("analysis", analysis.map());
            if(null != numberOfReplicas) {
                settings.put("number_of_replicas", numberOfReplicas);
            }
            if(null != numberOfShards) {
                settings.put("number_of_shards", numberOfShards);
            }
        }
        return map;
    }
    public String getKeyword() {
        return keyword;
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